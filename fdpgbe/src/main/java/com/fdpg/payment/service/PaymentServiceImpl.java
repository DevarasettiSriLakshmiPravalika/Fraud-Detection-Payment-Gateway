package com.fdpg.payment.service;

import com.fdpg.account.entity.Account;
import com.fdpg.account.entity.AccountStatus;
import com.fdpg.account.repository.AccountRepository;
import com.fdpg.auth.entity.User;
import com.fdpg.auth.repository.UserRepository;
import com.fdpg.common.response.ApiResponse;
import com.fdpg.payment.dto.PaymentRequest;
import com.fdpg.payment.dto.PaymentResponse;
import com.fdpg.payment.dto.TransactionDetail;
import com.fdpg.payment.dto.TransactionSummary;
import com.fdpg.payment.entity.Transaction;
import com.fdpg.payment.entity.TransactionStatus;
import com.fdpg.payment.repository.TransactionRepository;
import com.fdpg.risk.entity.FraudLog;
import com.fdpg.risk.model.AssessmentResult;
import com.fdpg.risk.model.TransactionContext;
import com.fdpg.risk.repository.FraudLogRepository;
import com.fdpg.risk.service.KnownDeviceService;
import com.fdpg.risk.service.KnownIpService;
import com.fdpg.risk.service.RiskAssessmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final TransactionRepository  transactionRepository;
    private final UserRepository         userRepository;
    private final AccountRepository      accountRepository;
    private final RiskAssessmentService  riskAssessmentService;
    private final KnownDeviceService     knownDeviceService;
    private final KnownIpService         knownIpService;
    private final FraudLogRepository     fraudLogRepository;

    @Override
    @Transactional
    public ApiResponse<PaymentResponse> createPayment(String username, PaymentRequest request, String ipAddress) {

        // 1. Resolve sender from JWT — never trust the frontend for identity
        User sender = userRepository.findByUsername(username).orElse(null);
        if (sender == null) {
            return ApiResponse.error("User not found", List.of("Authenticated user does not exist"));
        }

        // 2. Lock sender account for update (pessimistic lock prevents race conditions)
        Account senderAccount = accountRepository.findByUserForUpdate(sender).orElse(null);
        if (senderAccount == null) {
            return ApiResponse.error("Account not found", List.of("Sender does not have a virtual account"));
        }
        if (senderAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            return ApiResponse.error("Account inactive", List.of("Your account is not active"));
        }

        // 3. Resolve receiver account
        String receiverAccNum = request.getReceiverAccountNumber().trim();
        Account receiverAccount = accountRepository.findByAccountNumberForUpdate(receiverAccNum).orElse(null);
        if (receiverAccount == null) {
            return ApiResponse.error("Receiver not found",
                    List.of("No account found with number: " + receiverAccNum));
        }
        if (receiverAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            return ApiResponse.error("Receiver account inactive",
                    List.of("The receiver's account is not active"));
        }

        // 4. Self-transfer check
        if (senderAccount.getAccountNumber().equals(receiverAccount.getAccountNumber())) {
            return ApiResponse.error("Invalid payment",
                    List.of("You cannot send money to your own account"));
        }

        // 5. Balance check
        BigDecimal amount = request.getAmount();
        if (senderAccount.getBalance().compareTo(amount) < 0) {
            return ApiResponse.error("Insufficient balance",
                    List.of("Your balance (" + senderAccount.getCurrency() + " " +
                            senderAccount.getBalance().toPlainString() + ") is insufficient for this transaction"));
        }

        // 6. Idempotency check
        String idempotencyKey = resolveIdempotencyKey(request.getIdempotencyKey());
        Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            List<String> existingReasons = fraudLogRepository
                    .findByTransactionId(existing.get().getTransactionId())
                    .map(FraudLog::getReasons)
                    .orElse(List.of());
            return ApiResponse.success("Payment already processed (idempotent)",
                    toPaymentResponse(existing.get(), existingReasons));
        }

        // 7. Debit sender, credit receiver
        senderAccount.setBalance(senderAccount.getBalance().subtract(amount));
        receiverAccount.setBalance(receiverAccount.getBalance().add(amount));
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        // 8. Record transaction with PENDING status
        Transaction transaction = Transaction.builder()
                .sender(sender)
                .senderAccountNumber(senderAccount.getAccountNumber())
                .receiverAccountNumber(receiverAccNum)
                .receiverName(receiverAccount.getUser().getUsername())
                .amount(amount)
                .currency(senderAccount.getCurrency())
                .description(request.getDescription())
                .status(TransactionStatus.PENDING)
                .idempotencyKey(idempotencyKey)
                .build();

        Transaction saved = transactionRepository.save(transaction);

        // 9. Build TransactionContext for the risk engine
        TransactionContext riskContext = TransactionContext.builder()
                .userId(sender.getId())
                .transactionId(saved.getTransactionId())
                .senderAccountNumber(senderAccount.getAccountNumber())
                .amount(amount)
                .ipAddress(ipAddress)
                .deviceId(request.getDeviceId())
                .country(request.getCountry())
                .transactionTime(saved.getCreatedAt() != null ? saved.getCreatedAt() : LocalDateTime.now())
                .build();

        // 10. Run risk assessment — logs FraudLog internally
        AssessmentResult assessment = riskAssessmentService.assess(riskContext);

        // 11. Update transaction with risk result and final status
        saved.setRiskScore((double) assessment.getRiskScore());
        saved.setDecision(assessment.getDecision());
        saved.setStatus(TransactionStatus.valueOf(assessment.getDecision())); // APPROVED / REVIEW / FLAGGED
        transactionRepository.save(saved);

        // 12. Register device and IP as known so subsequent transactions aren't penalised
        knownDeviceService.registerIfNew(sender.getId(), request.getDeviceId());
        knownIpService.registerIfNew(sender.getId(), ipAddress);

        return ApiResponse.success("Payment created successfully",
                toPaymentResponse(saved, assessment.getReasons()));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TransactionSummary>> getUserPayments(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found", List.of("Authenticated user does not exist"));
        }

        // Get user's own account number for received-transaction lookup
        String myAccountNumber = accountRepository.findByUser(user)
                .map(account -> account.getAccountNumber())
                .orElse(null);

        // Sent transactions
        List<TransactionSummary> sent = transactionRepository
                .findBySenderOrderByCreatedAtDesc(user)
                .stream()
                .map(t -> toSummary(t, "SENT"))
                .collect(Collectors.toList());

        // Received transactions (only if user has an account)
        List<TransactionSummary> received = new java.util.ArrayList<>();
        if (myAccountNumber != null) {
            received = transactionRepository
                    .findReceivedByAccountNumber(myAccountNumber)
                    .stream()
                    .filter(t -> !t.getSender().getId().equals(user.getId()))
                    .map(t -> toSummary(t, "RECEIVED"))
                    .collect(Collectors.toList());
        }

        // Merge and sort by createdAt descending
        List<TransactionSummary> all = new java.util.ArrayList<>();
        all.addAll(sent);
        all.addAll(received);
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return ApiResponse.success("Payments retrieved successfully", all);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<TransactionDetail> getPaymentDetail(String username, String transactionId) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found", List.of("Authenticated user does not exist"));
        }

        Transaction transaction = transactionRepository.findByTransactionId(transactionId).orElse(null);
        if (transaction == null) {
            return ApiResponse.error("Transaction not found",
                    List.of("No transaction found with id: " + transactionId));
        }

        // Security: users can only view their own sent transactions
        if (!transaction.getSender().getId().equals(user.getId())) {
            return ApiResponse.error("Access denied",
                    List.of("You do not have permission to view this transaction"));
        }

        return ApiResponse.success("Transaction retrieved successfully", toTransactionDetail(transaction));
    }

    // ---- Private helpers ----

    private String resolveIdempotencyKey(String provided) {
        if (provided != null && !provided.isBlank()) {
            return provided.trim();
        }
        return UUID.randomUUID().toString();
    }

    private PaymentResponse toPaymentResponse(Transaction t, List<String> reasons) {
        return PaymentResponse.builder()
                .id(t.getId())
                .transactionId(t.getTransactionId())
                .senderUsername(t.getSender().getUsername())
                .senderEmail(t.getSender().getEmail())
                .senderAccountNumber(t.getSenderAccountNumber())
                .receiverName(t.getReceiverName())
                .receiverAccountNumber(t.getReceiverAccountNumber())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .description(t.getDescription())
                .status(t.getStatus())
                .idempotencyKey(t.getIdempotencyKey())
                .riskScore(t.getRiskScore())
                .decision(t.getDecision())
                .riskReasons(reasons)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }

    private TransactionSummary toSummary(Transaction t, String type) {
        String counterpartyName;
        String counterpartyAccountNumber;
        if ("SENT".equals(type)) {
            counterpartyName = t.getReceiverName();
            counterpartyAccountNumber = t.getReceiverAccountNumber() != null
                    ? t.getReceiverAccountNumber() : t.getReceiverAccount();
        } else {
            counterpartyName = t.getSender().getUsername();
            counterpartyAccountNumber = t.getSenderAccountNumber();
        }
        return TransactionSummary.builder()
                .transactionId(t.getTransactionId())
                .transactionType(type)
                .counterpartyName(counterpartyName)
                .counterpartyAccountNumber(counterpartyAccountNumber)
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .status(t.getStatus())
                .riskScore(t.getRiskScore())
                .decision(t.getDecision())
                .createdAt(t.getCreatedAt())
                .build();
    }

    private TransactionDetail toTransactionDetail(Transaction t) {
        List<String> riskReasons = fraudLogRepository
                .findByTransactionId(t.getTransactionId())
                .map(FraudLog::getReasons)
                .orElse(List.of());

        return TransactionDetail.builder()
                .id(t.getId())
                .transactionId(t.getTransactionId())
                .senderUsername(t.getSender().getUsername())
                .senderEmail(t.getSender().getEmail())
                .senderAccountNumber(t.getSenderAccountNumber())
                .receiverName(t.getReceiverName())
                .receiverAccountNumber(t.getReceiverAccountNumber())
                .amount(t.getAmount())
                .currency(t.getCurrency())
                .description(t.getDescription())
                .status(t.getStatus())
                .idempotencyKey(t.getIdempotencyKey())
                .riskScore(t.getRiskScore())
                .decision(t.getDecision())
                .riskReasons(riskReasons)
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}

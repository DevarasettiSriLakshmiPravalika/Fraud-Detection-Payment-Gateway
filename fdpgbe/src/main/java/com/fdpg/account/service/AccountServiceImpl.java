package com.fdpg.account.service;

import com.fdpg.account.dto.AccountResponse;
import com.fdpg.account.entity.Account;
import com.fdpg.account.entity.AccountStatus;
import com.fdpg.account.repository.AccountRepository;
import com.fdpg.auth.entity.User;
import com.fdpg.auth.repository.UserRepository;
import com.fdpg.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    private static final BigDecimal INITIAL_BALANCE = new BigDecimal("5000.0000");
    private static final String ACCOUNT_PREFIX = "FDPG";
    private static final long ACCOUNT_NUMBER_START = 100001L;

    @Override
    @Transactional
    public AccountResponse createAccountForUser(User user) {
        // Idempotent: return existing account if already created
        return accountRepository.findByUser(user)
                .map(this::toAccountResponse)
                .orElseGet(() -> {
                    Account account = Account.builder()
                            .accountNumber(generateAccountNumber())
                            .user(user)
                            .balance(INITIAL_BALANCE)
                            .currency("INR")
                            .accountStatus(AccountStatus.ACTIVE)
                            .build();
                    Account saved = accountRepository.save(account);
                    log.info("Created account {} for user {}", saved.getAccountNumber(), user.getUsername());
                    return toAccountResponse(saved);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<AccountResponse> getMyAccount(String username) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ApiResponse.error("User not found", List.of("Authenticated user does not exist"));
        }

        Account account = accountRepository.findByUser(user).orElse(null);
        if (account == null) {
            return ApiResponse.error("Account not found", List.of("No account found for user"));
        }

        return ApiResponse.success("Account retrieved successfully", toAccountResponse(account));
    }

    @Override
    @Transactional
    public void ensureAccountsForAllUsers() {
        List<User> allUsers = userRepository.findAll();
        int created = 0;
        for (User user : allUsers) {
            if (!accountRepository.existsByUser(user)) {
                createAccountForUser(user);
                created++;
            }
        }
        if (created > 0) {
            log.info("Startup migration: created {} account(s) for existing users", created);
        }
    }

    // ---- Private helpers ----

    private String generateAccountNumber() {
        // Find the current max account number sequence and increment
        // Use count-based approach: FDPG + (100001 + total accounts)
        long count = accountRepository.count();
        long seq = ACCOUNT_NUMBER_START + count;
        String candidate = ACCOUNT_PREFIX + seq;

        // Ensure uniqueness (handles edge cases on concurrent inserts)
        while (accountRepository.existsByAccountNumber(candidate)) {
            seq++;
            candidate = ACCOUNT_PREFIX + seq;
        }
        return candidate;
    }

    public AccountResponse toAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .username(account.getUser().getUsername())
                .email(account.getUser().getEmail())
                .balance(account.getBalance())
                .currency(account.getCurrency())
                .accountStatus(account.getAccountStatus())
                .createdAt(account.getCreatedAt())
                .build();
    }
}

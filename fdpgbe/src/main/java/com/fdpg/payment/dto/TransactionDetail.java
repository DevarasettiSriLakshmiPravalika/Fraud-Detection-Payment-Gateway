package com.fdpg.payment.dto;

import com.fdpg.payment.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetail {

    private Long id;
    private String transactionId;
    private String senderUsername;
    private String senderEmail;
    private String senderAccountNumber;
    private String receiverName;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
    private TransactionStatus status;
    private String idempotencyKey;

    // Phase 4: Risk Assessment fields
    private Double riskScore;
    private String decision;
    private List<String> riskReasons;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

package com.fdpg.payment.dto;

import com.fdpg.payment.entity.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummary {

    private String transactionId;

    /** SENT or RECEIVED — from the perspective of the calling user */
    private String transactionType;

    /** The other party: sender's username when RECEIVED, receiver's username when SENT */
    private String counterpartyName;

    /** The other party's account number */
    private String counterpartyAccountNumber;

    private BigDecimal amount;
    private String currency;
    private TransactionStatus status;

    // Phase 4: Risk Assessment fields
    private Double riskScore;
    private String decision;

    private LocalDateTime createdAt;
}

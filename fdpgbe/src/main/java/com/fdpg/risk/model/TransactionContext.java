package com.fdpg.risk.model;

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
public class TransactionContext {

    private Long userId;
    private String transactionId;
    private String senderAccountNumber;
    private BigDecimal amount;
    private String ipAddress;
    private String deviceId;
    private String country;
    private LocalDateTime transactionTime;
}

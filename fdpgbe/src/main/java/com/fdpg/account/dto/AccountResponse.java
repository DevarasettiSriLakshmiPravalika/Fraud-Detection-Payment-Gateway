package com.fdpg.account.dto;

import com.fdpg.account.entity.AccountStatus;
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
public class AccountResponse {

    private Long id;
    private String accountNumber;
    private String username;
    private String email;
    private BigDecimal balance;
    private String currency;
    private AccountStatus accountStatus;
    private LocalDateTime createdAt;
}

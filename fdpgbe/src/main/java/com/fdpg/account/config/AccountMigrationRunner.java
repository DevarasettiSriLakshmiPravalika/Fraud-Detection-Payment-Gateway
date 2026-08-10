package com.fdpg.account.config;

import com.fdpg.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountMigrationRunner implements ApplicationRunner {

    private final AccountService accountService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Running account migration: ensuring all users have an account...");
        accountService.ensureAccountsForAllUsers();
        log.info("Account migration complete.");
    }
}

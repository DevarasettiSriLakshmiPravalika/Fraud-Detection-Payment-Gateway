package com.fdpg.risk.config;

import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.repository.FraudRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the 6 default fraud rules into the database on application startup.
 * Idempotent — existing rules are not overwritten.
 * Order(2) runs after AccountMigrationRunner (Order 1).
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class FraudRuleDataSeeder implements ApplicationRunner {

    private final FraudRuleRepository fraudRuleRepository;

    @Override
    public void run(ApplicationArguments args) {
        List<FraudRule> defaults = List.of(
            rule("HIGH_AMOUNT",       "High Transaction Amount",
                 "Transaction amount exceeds the high-value threshold (₹50,000).", 30, true),
            rule("UNKNOWN_DEVICE",    "Unknown Device",
                 "Transaction originated from an unrecognized device.", 20, true),
            rule("UNKNOWN_IP",        "Unknown IP Address",
                 "Transaction originated from an unfamiliar IP address.", 25, true),
            rule("FOREIGN_COUNTRY",   "Foreign Country",
                 "Transaction originated from a different country.", 20, true),
            rule("HIGH_VELOCITY",     "High Transaction Velocity",
                 "More than 3 transactions within 2 minutes.", 25, true),
            rule("NIGHT_TRANSACTION", "Night Time Transaction",
                 "Transaction occurred between 01:00 and 05:00 server time.", 10, true)
        );

        for (FraudRule r : defaults) {
            if (fraudRuleRepository.findByRuleCode(r.getRuleCode()).isEmpty()) {
                fraudRuleRepository.save(r);
                log.info("Seeded FraudRule: {} (weight={})", r.getRuleCode(), r.getWeight());
            }
        }
    }

    private FraudRule rule(String code, String name, String desc, int weight, boolean enabled) {
        return FraudRule.builder()
                .ruleCode(code)
                .name(name)
                .description(desc)
                .weight(weight)
                .enabled(enabled)
                .build();
    }
}

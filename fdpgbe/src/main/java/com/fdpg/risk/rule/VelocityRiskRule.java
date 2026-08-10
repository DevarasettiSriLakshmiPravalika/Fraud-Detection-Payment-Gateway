package com.fdpg.risk.rule;

import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.model.RuleResult;
import com.fdpg.risk.model.TransactionContext;
import com.fdpg.payment.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Rule: HIGH_VELOCITY
 * Triggers when the sender has made 3 or more transactions within the last 2 minutes.
 * Uses PostgreSQL via JPA — no Redis required in Phase 4.
 */
@Component
@RequiredArgsConstructor
public class VelocityRiskRule implements RiskRule {

    private static final int VELOCITY_LIMIT          = 3;
    private static final int VELOCITY_WINDOW_MINUTES = 2;

    private final TransactionRepository transactionRepository;

    @Override
    public String getRuleCode() {
        return "HIGH_VELOCITY";
    }

    @Override
    public RuleResult evaluate(TransactionContext context, FraudRule config) {
        LocalDateTime windowStart = context.getTransactionTime()
                .minusMinutes(VELOCITY_WINDOW_MINUTES);
        long count = transactionRepository.countBySenderIdAndCreatedAtAfter(
                context.getUserId(), windowStart);
        if (count >= VELOCITY_LIMIT) {
            return RuleResult.triggered(config.getWeight(),
                    "Multiple transactions detected within a short period.");
        }
        return RuleResult.notTriggered();
    }
}

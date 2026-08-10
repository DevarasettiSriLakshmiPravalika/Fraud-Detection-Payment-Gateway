package com.fdpg.risk.rule;

import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.model.RuleResult;
import com.fdpg.risk.model.TransactionContext;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Rule: NIGHT_TRANSACTION
 * Triggers when the transaction occurs between 01:00 and 05:00 server time.
 * The transaction time is always taken from the server, never the client.
 */
@Component
public class NightTransactionRiskRule implements RiskRule {

    private static final LocalTime NIGHT_START = LocalTime.of(1, 0);
    private static final LocalTime NIGHT_END   = LocalTime.of(5, 0);

    @Override
    public String getRuleCode() {
        return "NIGHT_TRANSACTION";
    }

    @Override
    public RuleResult evaluate(TransactionContext context, FraudRule config) {
        LocalTime txTime = context.getTransactionTime().toLocalTime();
        // Triggers for [01:00, 05:00)
        if (!txTime.isBefore(NIGHT_START) && txTime.isBefore(NIGHT_END)) {
            return RuleResult.triggered(config.getWeight(),
                    "Transaction occurred during a high-risk time window.");
        }
        return RuleResult.notTriggered();
    }
}

package com.fdpg.risk.rule;

import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.model.RuleResult;
import com.fdpg.risk.model.TransactionContext;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Rule: HIGH_AMOUNT
 * Triggers when the transaction amount exceeds ₹50,000.
 */
@Component
public class AmountRiskRule implements RiskRule {

    private static final BigDecimal HIGH_AMOUNT_THRESHOLD = new BigDecimal("50000");

    @Override
    public String getRuleCode() {
        return "HIGH_AMOUNT";
    }

    @Override
    public RuleResult evaluate(TransactionContext context, FraudRule config) {
        if (context.getAmount().compareTo(HIGH_AMOUNT_THRESHOLD) > 0) {
            return RuleResult.triggered(config.getWeight(),
                    "Transaction amount exceeds the high-value threshold.");
        }
        return RuleResult.notTriggered();
    }
}

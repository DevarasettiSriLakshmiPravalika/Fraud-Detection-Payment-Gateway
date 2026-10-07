package com.fdpg.risk.rule;

import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.model.RuleResult;
import com.fdpg.risk.model.TransactionContext;
import org.springframework.stereotype.Component;

/**
 * Rule: FOREIGN_COUNTRY
 * Triggers when the transaction country differs from the FDPG home country (India = "IN").
 * No external geo-IP service is used; the country is supplied by the frontend as an ISO code.
 * If no country is provided, the rule does not trigger (benefit of the doubt).
 */
@Component
public class CountryRiskRule implements RiskRule {

    private static final String HOME_COUNTRY = "IN";

    @Override
    public String getRuleCode() {
        return "FOREIGN_COUNTRY";
    }

    @Override
    public RuleResult evaluate(TransactionContext context, FraudRule config) {
        String country = context.getCountry();
        if (country == null || country.isBlank()) {
            // Missing country — do not penalise
            return RuleResult.notTriggered();
        }
        if (!HOME_COUNTRY.equalsIgnoreCase(country.trim())) {
            return RuleResult.triggered(config.getWeight(),
                    "Transaction originated from a different country.");
        }
        return RuleResult.notTriggered();
    }
}

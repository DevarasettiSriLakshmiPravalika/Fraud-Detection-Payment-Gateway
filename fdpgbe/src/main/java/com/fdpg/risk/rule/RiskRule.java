package com.fdpg.risk.rule;

import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.model.RuleResult;
import com.fdpg.risk.model.TransactionContext;

/**
 * Strategy interface for risk rules.
 * Each implementation handles exactly one rule identified by its ruleCode.
 */
public interface RiskRule {

    /** Must match FraudRule.ruleCode in the database. */
    String getRuleCode();

    /**
     * Evaluate this rule against the transaction context.
     * The FraudRule provides the DB-configured weight.
     */
    RuleResult evaluate(TransactionContext context, FraudRule config);
}

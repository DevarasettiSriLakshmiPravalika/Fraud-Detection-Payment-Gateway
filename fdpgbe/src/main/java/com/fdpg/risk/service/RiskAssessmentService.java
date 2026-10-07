package com.fdpg.risk.service;

import com.fdpg.risk.model.AssessmentResult;
import com.fdpg.risk.model.TransactionContext;

public interface RiskAssessmentService {

    /**
     * Evaluate the given transaction context against all enabled rules,
     * persist a FraudLog, and return the assessment result.
     */
    AssessmentResult assess(TransactionContext context);
}

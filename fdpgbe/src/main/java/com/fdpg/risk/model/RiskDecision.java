package com.fdpg.risk.model;

/**
 * Central source of truth for risk decision thresholds.
 * Change the band values here to affect the entire engine.
 */
public final class RiskDecision {

    public static final String APPROVED = "APPROVED";
    public static final String REVIEW   = "REVIEW";
    public static final String FLAGGED  = "FLAGGED";

    /** 0 – APPROVED_MAX  → APPROVED */
    public static final int APPROVED_MAX = 30;
    /** APPROVED_MAX+1 – REVIEW_MAX → REVIEW */
    public static final int REVIEW_MAX   = 60;
    /** REVIEW_MAX+1 – 100 → FLAGGED */

    private RiskDecision() {}

    public static String decide(int score) {
        if (score <= APPROVED_MAX) return APPROVED;
        if (score <= REVIEW_MAX)   return REVIEW;
        return FLAGGED;
    }
}

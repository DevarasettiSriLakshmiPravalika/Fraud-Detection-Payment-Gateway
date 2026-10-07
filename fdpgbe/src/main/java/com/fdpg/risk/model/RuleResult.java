package com.fdpg.risk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResult {

    private boolean triggered;
    private int score;
    private String reason;

    public static RuleResult notTriggered() {
        return RuleResult.builder().triggered(false).score(0).reason(null).build();
    }

    public static RuleResult triggered(int score, String reason) {
        return RuleResult.builder().triggered(true).score(score).reason(reason).build();
    }
}

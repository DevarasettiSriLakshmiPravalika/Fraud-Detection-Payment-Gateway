package com.fdpg.risk.rule;

import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.model.RuleResult;
import com.fdpg.risk.model.TransactionContext;
import com.fdpg.risk.repository.KnownIpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Rule: UNKNOWN_IP
 * Triggers when the client IP address has never been seen before for this user.
 * The IP is extracted server-side from HttpServletRequest (never trusted from the frontend).
 */
@Component
@RequiredArgsConstructor
public class IpRiskRule implements RiskRule {

    private final KnownIpRepository knownIpRepository;

    @Override
    public String getRuleCode() {
        return "UNKNOWN_IP";
    }

    @Override
    public RuleResult evaluate(TransactionContext context, FraudRule config) {
        String ip = context.getIpAddress();
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            return RuleResult.triggered(config.getWeight(),
                    "Transaction originated from an unfamiliar IP address.");
        }
        boolean known = knownIpRepository.existsByUserIdAndIpAddress(context.getUserId(), ip);
        if (!known) {
            return RuleResult.triggered(config.getWeight(),
                    "Transaction originated from an unfamiliar IP address.");
        }
        return RuleResult.notTriggered();
    }
}

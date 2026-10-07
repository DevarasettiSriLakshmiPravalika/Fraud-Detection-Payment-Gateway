package com.fdpg.risk.rule;

import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.model.RuleResult;
import com.fdpg.risk.model.TransactionContext;
import com.fdpg.risk.repository.KnownDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Rule: UNKNOWN_DEVICE
 * Triggers when the device ID is absent or has never been seen before for this user.
 * After the payment succeeds, the device is registered as known.
 */
@Component
@RequiredArgsConstructor
public class DeviceRiskRule implements RiskRule {

    private final KnownDeviceRepository knownDeviceRepository;

    @Override
    public String getRuleCode() {
        return "UNKNOWN_DEVICE";
    }

    @Override
    public RuleResult evaluate(TransactionContext context, FraudRule config) {
        String deviceId = context.getDeviceId();
        if (deviceId == null || deviceId.isBlank()) {
            return RuleResult.triggered(config.getWeight(),
                    "Transaction originated from an unrecognized device.");
        }
        boolean known = knownDeviceRepository.existsByUserIdAndDeviceId(context.getUserId(), deviceId);
        if (!known) {
            return RuleResult.triggered(config.getWeight(),
                    "Transaction originated from an unrecognized device.");
        }
        return RuleResult.notTriggered();
    }
}

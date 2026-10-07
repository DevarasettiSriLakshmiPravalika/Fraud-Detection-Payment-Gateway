package com.fdpg.risk.service;

import com.fdpg.risk.entity.FraudLog;
import com.fdpg.risk.entity.FraudRule;
import com.fdpg.risk.model.AssessmentResult;
import com.fdpg.risk.model.RiskDecision;
import com.fdpg.risk.model.RuleResult;
import com.fdpg.risk.model.TransactionContext;
import com.fdpg.risk.repository.FraudLogRepository;
import com.fdpg.risk.repository.FraudRuleRepository;
import com.fdpg.risk.rule.RiskRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskAssessmentServiceImpl implements RiskAssessmentService {

    private final FraudRuleRepository fraudRuleRepository;
    private final FraudLogRepository  fraudLogRepository;
    /** Spring collects all @Component RiskRule beans into this list automatically. */
    private final List<RiskRule>       riskRules;

    @Override
    @Transactional
    public AssessmentResult assess(TransactionContext context) {

        // 1. Load enabled rules from DB, keyed by ruleCode for O(1) lookup
        Map<String, FraudRule> enabledRules = fraudRuleRepository.findByEnabledTrue()
                .stream()
                .collect(Collectors.toMap(FraudRule::getRuleCode, Function.identity()));

        // 2. Evaluate each rule that has both an implementation and a DB config entry
        List<String> reasons = new ArrayList<>();
        int totalScore = 0;

        for (RiskRule rule : riskRules) {
            FraudRule config = enabledRules.get(rule.getRuleCode());
            if (config == null) {
                // Rule disabled or not configured in DB — skip silently
                continue;
            }
            RuleResult result = rule.evaluate(context, config);
            if (result.isTriggered()) {
                totalScore += result.getScore();
                reasons.add(result.getReason());
                log.debug("Rule [{}] triggered: +{} — {}",
                        rule.getRuleCode(), result.getScore(), result.getReason());
            }
        }

        // 3. Cap score at 100
        totalScore = Math.min(totalScore, 100);

        // 4. Determine decision band
        String decision = RiskDecision.decide(totalScore);

        log.info("Risk assessment complete: txId={} score={} decision={} reasons={}",
                context.getTransactionId(), totalScore, decision, reasons);

        // 5. Persist FraudLog
        FraudLog fraudLog = FraudLog.builder()
                .transactionId(context.getTransactionId())
                .riskScore(totalScore)
                .decision(decision)
                .reasons(reasons)
                .build();
        fraudLogRepository.save(fraudLog);

        return AssessmentResult.builder()
                .riskScore(totalScore)
                .decision(decision)
                .reasons(reasons)
                .build();
    }
}

package com.fdpg.risk.repository;

import com.fdpg.risk.entity.FraudRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, Long> {

    List<FraudRule> findByEnabledTrue();

    Optional<FraudRule> findByRuleCode(String ruleCode);
}

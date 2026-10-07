package com.fdpg.risk.repository;

import com.fdpg.risk.entity.FraudLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FraudLogRepository extends JpaRepository<FraudLog, Long> {

    Optional<FraudLog> findByTransactionId(String transactionId);
}

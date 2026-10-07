package com.fdpg.payment.repository;

import com.fdpg.auth.entity.User;
import com.fdpg.payment.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findBySenderOrderByCreatedAtDesc(User sender);

    /**
     * Finds transactions where the user is the receiver.
     * Checks both the new receiver_account_number column (Phase 3.5+)
     * and the legacy receiver_account column (Phase 3 transactions).
     */
    @Query("SELECT t FROM Transaction t JOIN FETCH t.sender WHERE t.receiverAccountNumber = :accountNumber " +
           "OR t.receiverAccount = :accountNumber " +
           "ORDER BY t.createdAt DESC")
    List<Transaction> findReceivedByAccountNumber(@Param("accountNumber") String accountNumber);

    Optional<Transaction> findByTransactionId(String transactionId);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);

    /**
     * Counts transactions sent by this user within the velocity time window.
     * Used by VelocityRiskRule (HIGH_VELOCITY).
     */
    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.sender.id = :senderId AND t.createdAt >= :since")
    long countBySenderIdAndCreatedAtAfter(@Param("senderId") Long senderId,
                                          @Param("since") LocalDateTime since);
}

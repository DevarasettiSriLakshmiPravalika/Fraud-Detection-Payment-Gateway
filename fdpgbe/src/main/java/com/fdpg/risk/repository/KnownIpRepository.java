package com.fdpg.risk.repository;

import com.fdpg.risk.entity.KnownIp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnownIpRepository extends JpaRepository<KnownIp, Long> {

    boolean existsByUserIdAndIpAddress(Long userId, String ipAddress);
}

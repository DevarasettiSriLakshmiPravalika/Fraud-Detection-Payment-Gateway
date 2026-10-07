package com.fdpg.risk.repository;

import com.fdpg.risk.entity.KnownDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KnownDeviceRepository extends JpaRepository<KnownDevice, Long> {

    boolean existsByUserIdAndDeviceId(Long userId, String deviceId);
}

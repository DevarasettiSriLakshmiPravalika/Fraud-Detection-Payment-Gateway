package com.fdpg.risk.service;

import com.fdpg.risk.entity.KnownDevice;
import com.fdpg.risk.repository.KnownDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnownDeviceService {

    private final KnownDeviceRepository knownDeviceRepository;

    /**
     * Register the device as known for this user after a successful payment.
     * Idempotent — safe to call multiple times.
     */
    @Transactional
    public void registerIfNew(Long userId, String deviceId) {
        if (deviceId == null || deviceId.isBlank()) return;
        if (!knownDeviceRepository.existsByUserIdAndDeviceId(userId, deviceId)) {
            knownDeviceRepository.save(
                KnownDevice.builder().userId(userId).deviceId(deviceId).build()
            );
        }
    }
}

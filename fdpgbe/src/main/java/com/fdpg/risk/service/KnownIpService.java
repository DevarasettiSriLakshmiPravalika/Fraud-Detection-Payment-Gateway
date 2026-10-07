package com.fdpg.risk.service;

import com.fdpg.risk.entity.KnownIp;
import com.fdpg.risk.repository.KnownIpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KnownIpService {

    private final KnownIpRepository knownIpRepository;

    /**
     * Register the IP address as known for this user after a successful payment.
     * Idempotent — safe to call multiple times.
     */
    @Transactional
    public void registerIfNew(Long userId, String ipAddress) {
        if (ipAddress == null || ipAddress.isBlank()) return;
        if (!knownIpRepository.existsByUserIdAndIpAddress(userId, ipAddress)) {
            knownIpRepository.save(
                KnownIp.builder().userId(userId).ipAddress(ipAddress).build()
            );
        }
    }
}

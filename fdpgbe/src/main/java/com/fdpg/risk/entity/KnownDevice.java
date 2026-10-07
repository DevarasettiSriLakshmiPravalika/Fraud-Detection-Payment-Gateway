package com.fdpg.risk.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "known_devices",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "device_id"}))
public class KnownDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @CreationTimestamp
    @Column(name = "first_seen", updatable = false)
    private LocalDateTime firstSeen;
}

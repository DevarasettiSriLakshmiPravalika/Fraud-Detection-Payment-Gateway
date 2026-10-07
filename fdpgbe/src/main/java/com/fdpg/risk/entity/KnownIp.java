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
@Table(name = "known_ips",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "ip_address"}))
public class KnownIp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "first_seen", updatable = false)
    private LocalDateTime firstSeen;
}

package com.notegraph.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "activity_logs")
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    @Column(nullable = false)
    private String action; // e.g., "edited", "created a new folder"

    @NotBlank
    @Column(name = "target_type", nullable = false)
    private String targetType; // e.g., "NOTE", "FOLDER", "WORKSPACE"

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "target_name", nullable = false)
    private String targetName; // Cached name for fast UI retrieval

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;
}

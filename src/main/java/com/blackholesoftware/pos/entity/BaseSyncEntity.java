package com.blackholesoftware.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter @Setter
public abstract class BaseSyncEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;

    @Column(name = "is_synced", nullable = false)
    private Boolean isSynced = false;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    @PreUpdate
    public void onUpdate() {
        if (this.isSynced == null) {
            this.isSynced = false;
        }
        this.updatedAt = LocalDateTime.now();
    }
}
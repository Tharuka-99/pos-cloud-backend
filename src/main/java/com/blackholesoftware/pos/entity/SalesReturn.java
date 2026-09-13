package com.blackholesoftware.pos.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_returns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesReturn extends BaseSyncEntity {

    @Column(nullable = false, unique = true)
    private String returnNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnoreProperties({"returns", "items", "hibernateLazyInitializer", "handler"})
    private Sale originalSale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "hibernateLazyInitializer", "handler"})
    private User processedBy;

    private Double totalRefundAmount;
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, columnDefinition = "VARCHAR(255) DEFAULT 'UNSETTLED'")
    @Builder.Default
    private ReturnStatus status = ReturnStatus.UNSETTLED;

    @Builder.Default
    private LocalDateTime returnedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "salesReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<SalesReturnItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.status == null) {
            this.status = ReturnStatus.UNSETTLED;
        }
        if (this.returnedAt == null) {
            this.returnedAt = LocalDateTime.now();
        }
    }

    public enum ReturnStatus {
        UNSETTLED,
        SETTLED,
        CANCELLED
    }
}
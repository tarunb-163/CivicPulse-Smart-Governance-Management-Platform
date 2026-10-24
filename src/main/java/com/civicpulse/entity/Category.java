package com.civicpulse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "categories", indexes = {
        @Index(name = "idx_categories_department", columnList = "department_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "severity_weight", nullable = false)
    @Builder.Default
    private int severityWeight = 3; // 1 to 5

    @Column(name = "default_sla_hours", nullable = false)
    @Builder.Default
    private int defaultSlaHours = 72;

    @Column(length = 50)
    private String icon;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}

package com.civicpulse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "keyword_rules", indexes = {
        @Index(name = "idx_keyword_rules_keyword", columnList = "keyword"),
        @Index(name = "idx_keyword_rules_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String keyword;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false)
    @Builder.Default
    private int weight = 1;
}

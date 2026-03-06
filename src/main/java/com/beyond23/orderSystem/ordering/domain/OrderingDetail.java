package com.beyond23.orderSystem.ordering.domain;

import com.beyond23.orderSystem.product.domains.Product;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderingId", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Ordering ordering;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productId", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;

    @CreationTimestamp
    private LocalDateTime createdTime;
}

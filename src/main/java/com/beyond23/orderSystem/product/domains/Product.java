package com.beyond23.orderSystem.product.domains;

import com.beyond23.orderSystem.common.domain.BaseTimeEntity;
import com.beyond23.orderSystem.member.domain.Member;
import com.beyond23.orderSystem.product.dtos.ProductUpdateDto;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.security.PrivateKey;
import java.time.LocalDateTime;

@Entity
@Getter @ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Product extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String category;
    private int price;
    @Column(nullable = false)
    private int stockQuantity;
    private String imagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT), nullable = false)
    private Member member;

    public void updateProfileImageUrl(String url){
        this.imagePath = url;
    }

    @CreationTimestamp
    private LocalDateTime createdTime;

//    @OneToMany(mappedBy = "member_id", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = )
//    private Member member;

//    주문시 기존수량-주문수량 으로 재고 변경
    public void updateStockQuantity(int orderQuantity){
        this.stockQuantity = this.stockQuantity-orderQuantity;
    }

    public void updateProduct(ProductUpdateDto dto){
        this.name = dto.getName();
        this.price = dto.getPrice();
        this.category = dto.getCategory();
        this.stockQuantity = dto.getStockQuantity();
    }

}

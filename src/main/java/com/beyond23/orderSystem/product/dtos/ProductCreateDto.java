package com.beyond23.orderSystem.product.dtos;

import com.beyond23.orderSystem.member.domain.Member;
import com.beyond23.orderSystem.product.domains.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreateDto {

    private String name;
    private int price;
    private String category;
    private int stockQuantity;
    private MultipartFile productImage;

    public Product toEntity(Member member){
        return Product.builder()
                .name(this.name)
                .price(this.price)
                .category(this.category)
                .stockQuantity(this.stockQuantity)
                .member(member)
                .build();
    }
}

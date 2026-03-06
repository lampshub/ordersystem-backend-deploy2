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
public class ProductUpdateDto {

    private String name;
    private int price;
    private String category;
    private int stockQuantity;
//  Redis 이미지는 일반적으로 별도의 api로 처리한다
    private MultipartFile productImage;

}

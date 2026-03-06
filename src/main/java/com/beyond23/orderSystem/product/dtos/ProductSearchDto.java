package com.beyond23.orderSystem.product.dtos;

import com.beyond23.orderSystem.product.domains.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSearchDto {
    private String productName;
    private String category;

    public ProductSearchDto fromEntity(Product product){
        return ProductSearchDto.builder()
                .productName(product.getName())
//                .productCount() //구매한 개수
                .build();


//        private Long id;
//        private String name;
//        private String category;
//        private int price;
//        private int stockQuantity;
//        private String imagePath;
//
//        public static ProductListDto fromEntity(Product product){
//            return ProductListDto.builder()
//                    .id(product.getId())
//                    .name(product.getName())
//                    .category(product.getCategory())
//                    .price(product.getPrice())
//                    .stockQuantity(product.getStockQuantity())
//                    .build();

        }
}

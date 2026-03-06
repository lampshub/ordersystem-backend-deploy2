package com.beyond23.orderSystem.ordering.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderingCreateDto {
    private Long productId;
    private int productCount;

}

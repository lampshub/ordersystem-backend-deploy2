package com.beyond23.orderSystem.ordering.dtos;


import com.beyond23.orderSystem.ordering.domain.OrderingDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderingDetailDto {
    private Long detailId;
    private String productName;
    private int productCount;

    public static OrderingDetailDto fromEntity(OrderingDetail orderingDetail){
        return OrderingDetailDto.builder()
                .detailId(orderingDetail.getId())
                .productName(orderingDetail.getProduct().getName())
                .productCount(orderingDetail.getQuantity())
                .build();
    }

}

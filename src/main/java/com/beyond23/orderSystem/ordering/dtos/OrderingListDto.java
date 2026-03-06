package com.beyond23.orderSystem.ordering.dtos;

import com.beyond23.orderSystem.ordering.domain.Ordering;
import com.beyond23.orderSystem.ordering.domain.OrderStatus;
import com.beyond23.orderSystem.ordering.domain.OrderingDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderingListDto {

    private Long id;
    private String memberEmail;
    private OrderStatus orderStatus;
    private List<OrderingDetailDto> orderDetails;

    public static OrderingListDto fromEntity(Ordering ordering){

        List<OrderingDetailDto> orderingDetailDtos = new ArrayList<>();
        for(OrderingDetail orderingDetail : ordering.getOrderingDetailList()){
//            orderingDetail -> orderingDetailDtos 변환
            orderingDetailDtos.add(OrderingDetailDto.fromEntity(orderingDetail));
        }

        OrderingListDto orderingListDto = OrderingListDto.builder()
                                        .id(ordering.getId())
                                        .orderStatus(ordering.getOrderStatus())
                                        .memberEmail(ordering.getMember().getEmail())
                                        .orderDetails(orderingDetailDtos)
                                        .build();
        return orderingListDto;
    }
}

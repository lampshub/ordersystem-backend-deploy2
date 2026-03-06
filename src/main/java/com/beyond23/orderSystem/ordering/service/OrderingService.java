package com.beyond23.orderSystem.ordering.service;

import com.beyond23.orderSystem.common.service.SseAlarmService;
import com.beyond23.orderSystem.member.domain.Member;
import com.beyond23.orderSystem.member.repository.MemberRepository;
import com.beyond23.orderSystem.ordering.domain.Ordering;
import com.beyond23.orderSystem.ordering.domain.OrderingDetail;
import com.beyond23.orderSystem.ordering.dtos.OrderingCreateDto;
import com.beyond23.orderSystem.ordering.dtos.OrderingListDto;
import com.beyond23.orderSystem.ordering.repository.OrderDetailRepository;
import com.beyond23.orderSystem.ordering.repository.OrderingRepository;
import com.beyond23.orderSystem.product.domains.Product;
import com.beyond23.orderSystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderingService {

    private final OrderingRepository orderingRepository;
    private final MemberRepository memberRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ProductRepository productRepository;
    private final SseAlarmService sseAlarmService;
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public OrderingService(OrderingRepository orderingRepository, MemberRepository memberRepository, OrderDetailRepository orderDetailRepository, ProductRepository productRepository, SseAlarmService sseAlarmService, @Qualifier("stockInventory") RedisTemplate<String, String> redisTemplate) {
        this.orderingRepository = orderingRepository;
        this.memberRepository = memberRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.productRepository = productRepository;
        this.sseAlarmService = sseAlarmService;
        this.redisTemplate = redisTemplate;
    }

    public Long create(List<OrderingCreateDto> dtoList){
//        토큰-api gateway에서 처리해서 가져올수 없음
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//        member도 다른 서버에 있음
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        Ordering ordering = Ordering.builder()
                .member(member)
                .build();
        orderingRepository.save(ordering);
//
        for(OrderingCreateDto dto : dtoList){
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(()-> new EntityNotFoundException("entity is not found"));
//            재고관리
            if(product.getStockQuantity() < dto.getProductCount()){
                throw new IllegalArgumentException("재고가 부족합니다");
            }
            product.updateStockQuantity(dto.getProductCount());
            OrderingDetail orderingDetail = OrderingDetail.builder()
                    .ordering(ordering)
                    .product(product)
                    .quantity(dto.getProductCount())
                    .build();
            orderDetailRepository.save(orderingDetail);
        }

        return ordering.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderingListDto> findAll(){
        return orderingRepository.findAll().stream().map(o->OrderingListDto.fromEntity(o)).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderingListDto> myOrders(){
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("member is not found"));
        return orderingRepository.findAllByMember(member).stream().map(o->OrderingListDto.fromEntity(o)).collect(Collectors.toList());
    }
}

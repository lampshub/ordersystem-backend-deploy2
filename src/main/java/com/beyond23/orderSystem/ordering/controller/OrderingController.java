package com.beyond23.orderSystem.ordering.controller;

import com.beyond23.orderSystem.ordering.domain.Ordering;
import com.beyond23.orderSystem.ordering.dtos.OrderingCreateDto;
import com.beyond23.orderSystem.ordering.dtos.OrderingListDto;
import com.beyond23.orderSystem.ordering.service.OrderingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ordering")
public class OrderingController {
    private final OrderingService orderingService;
    @Autowired
    public OrderingController(OrderingService orderingService) {
        this.orderingService = orderingService;
    }

    //    create, list, myorders
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody List<OrderingCreateDto> dtoList) {
        Long id = orderingService.create(dtoList);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findAll(){
        List<OrderingListDto> orderingListDtoList = orderingService.findAll();
        return ResponseEntity.status(HttpStatus.OK).body(orderingListDtoList);
    }

    @GetMapping("/myorders")
    public ResponseEntity<?> myOrders(){
        List<OrderingListDto> orderingListDtoList = orderingService.myOrders();
        return ResponseEntity.status(HttpStatus.OK).body(orderingListDtoList);
    }


}

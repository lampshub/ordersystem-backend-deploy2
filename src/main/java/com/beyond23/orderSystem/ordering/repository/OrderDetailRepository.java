package com.beyond23.orderSystem.ordering.repository;

import com.beyond23.orderSystem.ordering.domain.OrderingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

//cascading을 쓰면 불필요함
@Repository
public interface OrderDetailRepository extends JpaRepository<OrderingDetail, Long> {
}

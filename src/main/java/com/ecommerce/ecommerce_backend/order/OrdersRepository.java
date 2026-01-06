package com.ecommerce.ecommerce_backend.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {

	Page<Orders> findByUser_Id(long userId, Pageable pageable);
	
	Optional <Orders> findByIdAndUser_Id( long orderId, long userId);
}

package com.ecommerce.ecommerce_backend.order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders,Long>{

	Optional<List<Orders>> findByUserId(long id);
}

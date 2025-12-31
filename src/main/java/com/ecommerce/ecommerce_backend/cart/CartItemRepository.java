package com.ecommerce.ecommerce_backend.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

	Optional<CartItem> findByProductId(Long productId);
}

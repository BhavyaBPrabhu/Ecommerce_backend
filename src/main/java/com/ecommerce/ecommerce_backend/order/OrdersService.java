package com.ecommerce.ecommerce_backend.order;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_backend.cart.Cart;
import com.ecommerce.ecommerce_backend.cart.CartItem;
import com.ecommerce.ecommerce_backend.cart.CartRepository;
import com.ecommerce.ecommerce_backend.exceptions.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.user.AuthUserPrincipal;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class OrdersService {

	private final OrdersRepository ordersRepository;
	private final CartRepository cartRepository;
	private final OrdersResponseMapper ordersResponseMapper;

	private Long getLoggedInUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof AuthUserPrincipal principal)) {
			throw new ResourceNotFoundException("User not authenticated");
		}
		return principal.id();
	}

	@Transactional
	public OrdersResponseDTO checkout() {
		Long userId = getLoggedInUserId();

		Cart cart = cartRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));

		Orders order = new Orders();
		order.setUser(cart.getUser());
		order.setOrdersStatus(OrdersStatus.CREATED);
		order.setCreatedAt(LocalDateTime.now());

		double totalAmount = 0;

		for (CartItem cartItem : cart.getItems()) {
			OrdersItem orderItem = new OrdersItem();
			orderItem.setOrder(order);
			orderItem.setProductId(cartItem.getProduct().getId());
			orderItem.setProductName(cartItem.getProduct().getName());
			orderItem.setProductPrice(cartItem.getProduct().getPrice());
			orderItem.setProductQuantity(cartItem.getQuantity());

			order.getItems().add(orderItem);
			totalAmount += orderItem.getProductPrice() * orderItem.getProductQuantity();

		}

		order.setTotalAmount(totalAmount);
		Orders savedOrder = ordersRepository.save(order);
		// To clear cart
		cart.getItems().clear();
		cartRepository.save(cart);
		return ordersResponseMapper.toDTO(savedOrder);
	}

	public Page<OrdersResponseDTO> myOrders(Pageable pageable) {
		Long userId = getLoggedInUserId();

		Page<Orders> ordersPage = ordersRepository.findByUser_Id(userId,pageable);
		if(ordersPage.isEmpty())
				throw new ResourceNotFoundException("No orders found");


		return ordersPage.map(ordersResponseMapper::toDTO);

	}

	public OrdersResponseDTO cancelOrder(Long orderId) {
		Long userId = getLoggedInUserId();
		Orders order = ordersRepository.findByIdAndUser_Id(orderId,userId)
				.orElseThrow(() -> new ResourceNotFoundException("No order found with id = " + orderId));

		order.setOrdersStatus(OrdersStatus.CANCELLED);
		Orders savedOrder = ordersRepository.save(order);
		return ordersResponseMapper.toDTO(savedOrder);

	}
	
	
}

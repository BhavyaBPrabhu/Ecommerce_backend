package com.ecommerce.ecommerce_backend.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_backend.cart.Cart;
import com.ecommerce.ecommerce_backend.cart.CartItem;
import com.ecommerce.ecommerce_backend.cart.CartRepository;
import com.ecommerce.ecommerce_backend.exceptions.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.user.AuthUserPrincipal;

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
	
	public OrdersResponseDTO checkout() {
		Long userId = getLoggedInUserId();
		
		 Cart cart = cartRepository.findByUserId(userId)
		            .orElseThrow(() -> new ResourceNotFoundException("Cart is empty"));
		 
		 Orders order = new Orders();
		 order.setUser(cart.getUser());
		 order.setOrdersStatus(OrdersStatus.CREATED);
		 order.setCreatedAt(LocalDateTime.now());
		 
		 double totalAmount=0;
		 
		 for(CartItem cartItem : cart.getItems()) {
			 OrdersItem orderItem = new OrdersItem();
			 orderItem.setOrder(order);
			 orderItem.setProductId(cartItem.getProduct().getId());
			 orderItem.setProductName(cartItem.getProduct().getName());
			 orderItem.setProductPrice(cartItem.getProduct().getPrice());
			 orderItem.setProductQuantity(cartItem.getQuantity());
			 
			 order.getItems().add(orderItem);
			 totalAmount+=orderItem.getProductPrice()*orderItem.getProductQuantity();
			 
		 }
		 
		 order.setTotalAmount(totalAmount);
		 Orders savedOrder = ordersRepository.save(order);
		 //To clear cart
		 cart.getItems().clear();
		 cartRepository.save(cart);
		 return ordersResponseMapper.toDTO(savedOrder);
	}
	
	public List<OrdersResponseDTO> myOrders(){
		Long userId = getLoggedInUserId();
		
		List<Orders> orderList = ordersRepository.findByUserId(userId)
									.orElseThrow(() ->  new ResourceNotFoundException("No orders found"));
		
		return orderList.stream().map(order -> ordersResponseMapper.toDTO(order)).toList();
		
		
	}

	public OrdersResponseDTO cancelOrder(Long orderId) {
		Long userId = getLoggedInUserId();
		List<Orders> orders =ordersRepository.findByUserId(userId)
				.orElseThrow(() ->  new ResourceNotFoundException("No orders found"));
		
		 Orders order = orders.stream()
		            .filter(o -> o.getId().equals(orderId))
		            .findFirst()
		            .orElseThrow(() ->
		                    new ResourceNotFoundException("No order found with id = " + orderId));
		
			order.setOrdersStatus(OrdersStatus.CANCELLED);
			Orders savedOrder =ordersRepository.save(order);
			return ordersResponseMapper.toDTO(savedOrder);
			
		}

	
}

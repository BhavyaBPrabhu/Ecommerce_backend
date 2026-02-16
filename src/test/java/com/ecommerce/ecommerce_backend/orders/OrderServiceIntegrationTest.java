package com.ecommerce.ecommerce_backend.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ecommerce.ecommerce_backend.cart.Cart;
import com.ecommerce.ecommerce_backend.cart.CartItem;
import com.ecommerce.ecommerce_backend.cart.CartRepository;
import com.ecommerce.ecommerce_backend.category.Category;
import com.ecommerce.ecommerce_backend.order.Orders;
import com.ecommerce.ecommerce_backend.order.OrdersRepository;
import com.ecommerce.ecommerce_backend.order.OrdersResponseDTO;
import com.ecommerce.ecommerce_backend.order.OrdersService;
import com.ecommerce.ecommerce_backend.order.OrdersStatus;
import com.ecommerce.ecommerce_backend.product.Product;
import com.ecommerce.ecommerce_backend.product.ProductRepository;
import com.ecommerce.ecommerce_backend.user.AuthUserPrincipal;
import com.ecommerce.ecommerce_backend.user.UserRepository;
import com.ecommerce.ecommerce_backend.user.Users;

import jakarta.transaction.Transactional;

@SpringBootTest
@Transactional
public class OrderServiceIntegrationTest {

	@Autowired
	private OrdersRepository ordersRepository;
	@Autowired
	private CartRepository cartRepository;
	@Autowired
	private  OrdersService ordersService;
	@Autowired
	private UserRepository usersRepository;

	@Autowired
	private ProductRepository productRepository;

	
	private Users user;
	private Cart cart;
	private Category category;
	private Product product;
	private CartItem item;
	private Orders order1;
	
	private final Long USER_ID =1L;
	
	@BeforeEach
	void setup() {
		AuthUserPrincipal auth = new AuthUserPrincipal(USER_ID, "test_user");
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(auth, null, List.of());
		SecurityContextHolder.getContext().setAuthentication(token);
		
		user = new Users();
		user.setId(USER_ID);
		user = usersRepository.save(user);
		
		product = new Product();
		product.setId(1L);
		product.setName("Air Fryer");
		product.setPrice(10000.00);
		product.setQuantity(10);
		product = productRepository.save(product);
		
		item = new CartItem();
		item.setId(1L);
		item.setProduct(product);
		item.setQuantity(1);
		
		cart = new Cart();
		cart.setId(1L);
		cart.setUser(user);
		cart.setItems(List.of(item));
		cartRepository.save(cart);
		
		item.setCart(cart);
		cart.getItems().add(item);
		
		order1 = new Orders();
		order1.setId(1L);
		order1.setUser(user);
		ordersRepository.save(order1);
		
	}
	
	@Test
	void testToCreateOrder_And_ClearCart() {
		
		
		OrdersResponseDTO response = ordersService.checkout();
		
		assertNotNull(response);
		assertEquals(response.getStatus(), OrdersStatus.CREATED);
		assertEquals(1,ordersRepository.count());
		Optional<Cart> presentCart = cartRepository.findByUser_Id(user.getId());
		assertTrue(presentCart.get().getItems().isEmpty());
	}
	
	@Test
	void testForMyOrder_PaginatedOrders() {
		order1.setOrdersStatus(OrdersStatus.CREATED);
		order1.setCreatedAt(LocalDateTime.now());
		order1.setTotalAmount(10000.0);
		
		ordersRepository.save(order1);
		
		Pageable pageable = PageRequest.of(0, 10);
		Page<OrdersResponseDTO> pageOrder = ordersService.myOrders(pageable);
		
		assertNotNull(pageOrder);
		assertEquals(1,pageOrder.getTotalElements());
		
	}
	
}

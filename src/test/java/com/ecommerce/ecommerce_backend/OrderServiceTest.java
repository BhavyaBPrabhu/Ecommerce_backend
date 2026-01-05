package com.ecommerce.ecommerce_backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ecommerce.ecommerce_backend.cart.Cart;
import com.ecommerce.ecommerce_backend.cart.CartItem;
import com.ecommerce.ecommerce_backend.cart.CartRepository;
import com.ecommerce.ecommerce_backend.category.Category;
import com.ecommerce.ecommerce_backend.exceptions.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.order.Orders;
import com.ecommerce.ecommerce_backend.order.OrdersRepository;
import com.ecommerce.ecommerce_backend.order.OrdersResponseDTO;
import com.ecommerce.ecommerce_backend.order.OrdersResponseMapper;
import com.ecommerce.ecommerce_backend.order.OrdersService;
import com.ecommerce.ecommerce_backend.order.OrdersStatus;
import com.ecommerce.ecommerce_backend.product.Product;
import com.ecommerce.ecommerce_backend.user.AuthUserPrincipal;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

	@Mock
	private OrdersRepository ordersRepository;
	@Mock
	private CartRepository cartRepository;
	@Mock
	private OrdersResponseMapper ordersResponseMapper;

	@InjectMocks
	private OrdersService ordersService;

	private final Long USER_ID = 1L;

	private Category category;
	private Product product;
	private CartItem item;
	private Cart cart;
	private Orders order1;
	private Orders order2;
	private Orders order3;

	@BeforeEach
	void securitySetup() {
		AuthUserPrincipal auth = new AuthUserPrincipal(USER_ID, "test_user");
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(auth, null, List.of());
		SecurityContextHolder.getContext().setAuthentication(token);

		// Test data
		category = new Category();
		category.setName("Appliance");
		category.setId(1L);

		product = new Product();
		product.setId(1L);
		product.setName("Air Fryer");
		product.setPrice(10000.0);
		product.setQuantity(50);
		product.setSku("ARF-123");
		product.setCategory(category);

		item = new CartItem();
		item.setProduct(product);
		item.setId(3L);
		item.setQuantity(1);

		cart = new Cart();
		cart.setId(1L);
		cart.setItems(new ArrayList<>(List.of(item)));

		order1 = new Orders();
		order1.setId(1L);

		order2 = new Orders();
		order2.setId(2L);
		order3 = new Orders();
		order3.setId(3L);
	}

	@Test
	void testForCheckOutOrderCreated() {

		when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

		when(ordersRepository.save(any(Orders.class))).thenAnswer(invocation -> invocation.getArgument(0));

		OrdersResponseDTO expectedDto = new OrdersResponseDTO();
		expectedDto.setStatus(OrdersStatus.CREATED);
		expectedDto.setTotalAmount(10000.0);

		when(ordersResponseMapper.toDTO(any(Orders.class))).thenReturn(expectedDto);

		OrdersResponseDTO response = ordersService.checkout();

		assertNotNull(response);
		assertEquals(OrdersStatus.CREATED, response.getStatus());

		verify(ordersRepository).save(any(Orders.class));
		verify(cartRepository).save(cart);

	}

	@Test
	void testForCheckOutOrderFailure() {
		when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> ordersService.checkout());

		verify(ordersRepository, never()).save(any());
		verify(cartRepository, never()).save(any());
	}

	@Test
	void testForGetMyOrdersSuccess() {
		Orders order1 = new Orders();
		order1.setId(1L);

		OrdersResponseDTO dto1 = new OrdersResponseDTO();
		OrdersResponseDTO dto2 = new OrdersResponseDTO();
		OrdersResponseDTO dto3 = new OrdersResponseDTO();
		when(ordersRepository.findByUserId(USER_ID)).thenReturn(Optional.of(List.of(order1, order2, order3)));
		when(ordersResponseMapper.toDTO(order1)).thenReturn(dto1);
		when(ordersResponseMapper.toDTO(order2)).thenReturn(dto2);
		when(ordersResponseMapper.toDTO(order3)).thenReturn(dto3);

		List<OrdersResponseDTO> response = ordersService.myOrders();

		assertNotNull(response);
		assertEquals(dto1, response.get(0));
		assertEquals(dto2, response.get(1));
		assertEquals(dto3, response.get(2));

		verify(ordersRepository).findByUserId(USER_ID);
		verify(ordersResponseMapper).toDTO(order3);
		verify(ordersResponseMapper).toDTO(order2);
		verify(ordersResponseMapper).toDTO(order1);

	}

	@Test
	void testForGetMyOrdersFailure() {
		when(ordersRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> ordersService.myOrders());

		verify(ordersResponseMapper, never()).toDTO(any(Orders.class));

	}

	@Test
	void testForCancelOrderSuccess() {

		order1.setOrdersStatus(OrdersStatus.CREATED);
		when(ordersRepository.findByUserId(USER_ID)).thenReturn(Optional.of(List.of(order1)));
		when(ordersRepository.save(order1)).thenReturn(order1);
		OrdersResponseDTO dto = new OrdersResponseDTO();
		dto.setStatus(OrdersStatus.CANCELLED);
		when(ordersResponseMapper.toDTO(order1)).thenReturn(dto);

		OrdersResponseDTO response = ordersService.cancelOrder(USER_ID);

		assertEquals(response.getStatus(), OrdersStatus.CANCELLED);
		verify(ordersRepository).save(order1);

	}

	@Test
	void testForCancelOrderFailEmptyOrders() {
		when(ordersRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> ordersService.cancelOrder(1L));
		verify(ordersRepository, never()).save(any());
	}

	@Test
	void testForCancelOrderFailNoOrderFound() {

		when(ordersRepository.findByUserId(USER_ID)).thenReturn(Optional.of(List.of(order1)));

		assertThrows(ResourceNotFoundException.class, () -> ordersService.cancelOrder(2L));
		verify(ordersRepository, never()).save(any());
	}
}

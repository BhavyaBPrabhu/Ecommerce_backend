package com.ecommerce.ecommerce_backend.cart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.ecommerce.ecommerce_backend.category.Category;
import com.ecommerce.ecommerce_backend.exceptions.BadRequestException;
import com.ecommerce.ecommerce_backend.exceptions.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.product.Product;
import com.ecommerce.ecommerce_backend.product.ProductRepository;
import com.ecommerce.ecommerce_backend.user.AuthUserPrincipal;
import com.ecommerce.ecommerce_backend.user.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CartServiceUnitTest {

	@Mock
	private  CartRepository cartRepository;
	@Mock
	private CartItemRepository cartItemRepository;
	@Mock
	private UserRepository userRepository;
	@Mock
	private  ProductRepository productRepository;
	@Mock
	private CartResponseDTOMapper cartResponseDTOMapper;
	
	@InjectMocks
	private CartService cartService;
	
	private final Long USER_ID = 1L;
	private Category category;
	private Product product;
	private CartItem cartItem;
	private Cart cart;
	
	@BeforeEach
	void securitySetup(){
		
		AuthUserPrincipal auth = new AuthUserPrincipal(USER_ID,"test_user");
		UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(auth, null, List.of());
		SecurityContextHolder.getContext().setAuthentication(token);
		//Test data
		
		category = new Category();
		category.setName("Appliance");
		category.setId(1L);
		
		product = new Product();
		product.setId(1L);
		product.setCategory(category);
		product.setName("Air Conditioner");
		product.setPrice(40000.00);
		product.setQuantity(25);
		product.setSku("ARC-897");
		
		cartItem = new CartItem();
		cartItem.setId(1L);
		cartItem.setProduct(product);
		cartItem.setQuantity(2); //
		
		cart= new Cart();
		cart.setId(1L);
		cart.setItems(new ArrayList<>());
	}
	
	@Test
	void testForAddToCart_Success() {
		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setId(1L);
		cartItemDTO.setProduct_id(1L);
		cartItemDTO.setQuantity(2);
		CartResponseDTO responseDTO = new CartResponseDTO();
		responseDTO.setTotal(product.getPrice()*cartItemDTO.getQuantity());
		when( cartRepository.findByUser_Id(USER_ID)).thenReturn(Optional.of(cart));
		when(productRepository.findById(1L)).thenReturn(Optional.of(product));
		when( cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
		when(cartResponseDTOMapper.toDTO(any(Cart.class))).thenReturn(responseDTO);
		CartResponseDTO cartResponseDTO = cartService.addToCart(cartItemDTO);
		
		assertNotNull(cartResponseDTO);
		assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(cartItemDTO.getQuantity());
		assertThat(cart.getTotalPrice()).isEqualTo(responseDTO.getTotal());
		
		verify(cartRepository).save(any(Cart.class));
		verify(cartItemRepository).save(any(CartItem.class));
		
	}
	
	@Test
	void testForAddToCart_ProductNotFound() {
		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setId(1L);
		cartItemDTO.setProduct_id(12L);
		cartItemDTO.setQuantity(2);
		
		when( cartRepository.findByUser_Id(USER_ID)).thenReturn(Optional.of(cart));
		when(productRepository.findById(12L)).thenReturn(Optional.empty());
		
		assertThrows(ResourceNotFoundException.class, ()->cartService.addToCart(cartItemDTO));
		
		verify(cartItemRepository,never()).save(any(CartItem.class));
		verify(cartRepository,never()).save(any(Cart.class));
	}
	
	@Test
	void testForAddToCart_ZeroQuantity() {
		CartItemDTO cartItemDTO = new CartItemDTO();
		cartItemDTO.setId(1L);
		cartItemDTO.setProduct_id(12L);
		cartItemDTO.setQuantity(0);
		when( cartRepository.findByUser_Id(USER_ID)).thenReturn(Optional.of(cart));
		when(productRepository.findById(12L)).thenReturn(Optional.of(product));
		when( cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
		
		assertThrows(BadRequestException.class,()->cartService.addToCart(cartItemDTO));
		
		verify(cartItemRepository,never()).save(any(CartItem.class));
		verify(cartRepository,never()).save(any(Cart.class));
	}
	
		@Test
		void testFor_GetMyCart_Success() {
			
			
			cart.setItems(List.of(cartItem));
			cart.setTotalPrice(product.getPrice()*cartItem.getQuantity());
			CartItemResponseDTO cartItemResponseDTO = new CartItemResponseDTO();
			cartItemResponseDTO.setProduct(null);
			cartItemResponseDTO.setQuantity(2);
			CartResponseDTO cartResponseDTO = new CartResponseDTO();
			cartResponseDTO.setTotal(cart.getTotalPrice());
			cartResponseDTO.setItems(List.of(cartItemResponseDTO));
			when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
			when(cartResponseDTOMapper.toDTO(cart)).thenReturn(cartResponseDTO);
			CartResponseDTO response = cartService.getMyCart();
			assertEquals(response.getItems().get(0).getQuantity(),cartItem.getQuantity());
			assertEquals(response.getTotal(), cartResponseDTO.getTotal());
			
			verify(cartResponseDTOMapper).toDTO(cart);
			
		}
		
		@Test
		void testFor_GetMyCart_Failure() {
			
			when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
			assertThrows(ResourceNotFoundException.class,()-> cartService.getMyCart());
			
		}
		
		@Test
		void testFor_removeItemByProductId_Success() {
			cart.getItems().add(cartItem);
			cart.setTotalPrice(product.getPrice()*cartItem.getQuantity());
			
			CartResponseDTO cartResponseDTO = new CartResponseDTO();
			cartResponseDTO.setTotal(0.0);
			cartResponseDTO.setCartId(1L);
			cartResponseDTO.setItems(List.of());
			
			when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
			when(cartResponseDTOMapper.toDTO(cart)).thenReturn(cartResponseDTO);
			
			CartResponseDTO responseDTO = cartService.removeItemByProductId(1L);
			
			assertEquals(cartResponseDTO.getCartId(),responseDTO.getCartId());
			assertTrue(responseDTO.getItems().isEmpty());
			assertEquals(responseDTO.getTotal(),0.0);
			
			verify(cartItemRepository).delete(cartItem);
			verify(cartRepository).save(cart);
			verify(cartResponseDTOMapper).toDTO(cart);
			
		}
		
		@Test
		void testFor_removeItemByProductId_Failure() {
			cart.getItems().add(cartItem);
			cart.setTotalPrice(product.getPrice()*cartItem.getQuantity());
			
			when(cartRepository.findByUser_Id(1L)).thenReturn(Optional.of(cart));
			assertThrows(ResourceNotFoundException.class,()-> cartService.removeItemByProductId(12L));
			
			verify(cartItemRepository,never()).delete(any());
			verify(cartRepository,never()).save(any());
			verify(cartResponseDTOMapper,never()).toDTO(any());
		}
}

package com.ecommerce.ecommerce_backend.orders;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.ecommerce_backend.cart.Cart;
import com.ecommerce.ecommerce_backend.cart.CartItem;
import com.ecommerce.ecommerce_backend.cart.CartRepository;
import com.ecommerce.ecommerce_backend.order.Orders;
import com.ecommerce.ecommerce_backend.order.OrdersItem;
import com.ecommerce.ecommerce_backend.order.OrdersRepository;
import com.ecommerce.ecommerce_backend.order.OrdersStatus;
import com.ecommerce.ecommerce_backend.product.Product;
import com.ecommerce.ecommerce_backend.product.ProductRepository;
import com.ecommerce.ecommerce_backend.user.UserRepository;
import com.ecommerce.ecommerce_backend.user.Users;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class OrdersControllerIntegrationTest {

	@Autowired
	private MockMvc mockmvc;
	
	@Autowired
	private CartRepository cartRepository;
	
	@Autowired
	private OrdersRepository ordersRepository;
	
	@Autowired
	private UserRepository usersRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	private Users user;
	private Product product1;
	private Product product2;
	private CartItem item1;
	private CartItem item2;
	private Cart cart;
	@Value("${JWT_SECRET}")
	private String jwtSecret;
	private String jwtToken;
	
	@BeforeEach
	void setup() {
		user =new Users();
		//user.setId(1L); ->Not needed since DB does this 
		user.setUsername("Test_User"+ System.currentTimeMillis());
		user.setPassword("Test_User@123");
		usersRepository.save(user);
	
		
		product1 = new Product();
		product1.setName("LG Microwave Oven");
		product1.setPrice(20000.00);
		product1.setQuantity(10);
		product1.setSku("MWEN-LG-"+System.currentTimeMillis());
		productRepository.save(product1);
		
		product2 = new Product();
		product2.setName("Philips AirFryer");
		product2.setPrice(10000.00);
		product2.setQuantity(20);
		product2.setSku("ARFRY-PL-"+System.currentTimeMillis());
		productRepository.save(product2);
		
		item1 = new CartItem();
		item1.setProduct(product1);
		item1.setQuantity(1);
		

		item2 = new CartItem();
		item2.setProduct(product2);
		item2.setQuantity(1);
		
		cart = new Cart();
		cart.setUser(user);
		cart.setItems(new ArrayList<>());
		cart.getItems().add(item1);
		cart.getItems().add(item2);
		
		item1.setCart(cart);
		item2.setCart(cart);
		user.setCart(cart);
		
		
		cartRepository.save(cart);
		jwtToken = generateJwt(user);
	    
	}
	
	private String generateJwt(Users user) {
		SecretKey key =Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
		return Jwts.builder()
					.claim("username",user.getUsername())
					.claim("userId",user.getId())
					.claim("authorities","ROLE_USER")
					.setIssuedAt(new Date())
					.setExpiration(new Date(System.currentTimeMillis() + 3600000))
					 .signWith(key, SignatureAlgorithm.HS256)
		            .compact();
	}
	
	@Test
	void checkout_Success() throws Exception {
		 
		double totalAmount = cart.getItems().stream().mapToDouble(x -> x.getProduct().getPrice()* x.getQuantity())
									.sum();
		int expectedItemCount = cart.getItems().size();
		mockmvc.perform(
				post("/orders")
				 .header("Authorization", "Bearer " + jwtToken)
                
				)
		.andExpect(status().isOk())
		.andExpect(jsonPath("$.status").value("CREATED"))
		.andExpect(jsonPath("$.totalAmount").value(totalAmount))
		.andExpect(jsonPath("$.items.length()").value(expectedItemCount))
		//First item 
		.andExpect(jsonPath("$.items[0].productQuantity").value(item1.getQuantity()))
		.andExpect(jsonPath("$.items[0].productId").value(item1.getProduct().getId()))
		.andExpect(jsonPath("$.items[0].productName").value(item1.getProduct().getName()))
		.andExpect(jsonPath("$.items[0].productPrice").value(item1.getProduct().getPrice()))
		//Second item 
		.andExpect(jsonPath("$.items[1].productQuantity").value(item2.getQuantity()))
		.andExpect(jsonPath("$.items[1].productId").value(item2.getProduct().getId()))
		.andExpect(jsonPath("$.items[1].productName").value(item2.getProduct().getName()))
		.andExpect(jsonPath("$.items[1].productPrice").value(item2.getProduct().getPrice()));
		
		Optional<Cart> presentCart = cartRepository.findByUser_Id(user.getId());
		assertThat(presentCart.get().getItems()).isEmpty();
		
		Pageable pageable = PageRequest.of(0, 10);
		Page<Orders> ordersPage =  ordersRepository.findByUser_Id(user.getId(), pageable);
		Orders savedOrder = ordersPage.getContent().get(0);
		OrdersItem savedItem1 = savedOrder.getItems().get(0);
		OrdersItem savedItem2 = savedOrder.getItems().get(1);
		
		assertNotNull(ordersPage);
		assertThat(savedOrder.getItems().size()).isEqualTo(expectedItemCount);
		assertThat(savedOrder.getUser().getId()).isEqualTo(user.getId());
		assertThat(savedOrder.getUser().getUsername()).isEqualTo(user.getUsername());
		assertThat(savedOrder.getOrdersStatus()).isEqualTo(OrdersStatus.CREATED);
		assertThat(savedOrder.getTotalAmount()).isEqualTo(totalAmount);
		
		
		
		assertThat(savedItem1.getProductId()).isEqualTo(product1.getId());
		assertThat(savedItem1.getProductName()).isEqualTo(product1.getName());
		assertThat(savedItem1.getProductPrice()).isEqualTo(product1.getPrice());
		assertThat(savedItem1.getProductQuantity()).isEqualTo(item1.getQuantity());
		
		assertThat(savedItem2.getProductId()).isEqualTo(product2.getId());
		assertThat(savedItem2.getProductName()).isEqualTo(product2.getName());
		assertThat(savedItem2.getProductPrice()).isEqualTo(product2.getPrice());
		assertThat(savedItem2.getProductQuantity()).isEqualTo(item2.getQuantity());
	}
	
	@Test
	void checkout_Failure_EmptyCart() throws Exception {
		cart.getItems().clear();
		cartRepository.save(cart);
		mockmvc.perform(
				post("/orders")
				 .header("Authorization", "Bearer " + jwtToken)
                
				)
		.andExpect(status().isNotFound())
		.andExpect(jsonPath("$.message").value("Cart is empty"));
		//Cart still empty
		Optional <Cart> presentCart = cartRepository.findByUser_Id(user.getId());
		assertThat(presentCart.get().getItems()).isEmpty();
		//
		Pageable pageable = PageRequest.of(0, 10);
		Page<Orders> ordersPage =  ordersRepository.findByUser_Id(user.getId(), pageable);
		assertThat(ordersPage.getTotalElements()).isEqualTo(0);
	
}
}
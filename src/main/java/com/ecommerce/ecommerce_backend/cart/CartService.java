package com.ecommerce.ecommerce_backend.cart;

import java.util.Optional;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_backend.exceptions.BadRequestException;
import com.ecommerce.ecommerce_backend.exceptions.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.product.Product;
import com.ecommerce.ecommerce_backend.product.ProductRepository;
import com.ecommerce.ecommerce_backend.user.AuthUserPrincipal;
import com.ecommerce.ecommerce_backend.user.UserRepository;
import com.ecommerce.ecommerce_backend.user.Users;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final UserRepository userRepository;
	private final ProductRepository productRepository;
	private final CartResponseDTOMapper cartResponseMapper;

	private Long getLoggedInUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || !(auth.getPrincipal() instanceof AuthUserPrincipal principal)) {
			throw new ResourceNotFoundException("User not authenticated");
		}
		return principal.id();
	}

	public CartResponseDTO getMyCart() {
		Long userId = getLoggedInUserId();

		Cart cart = cartRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

		return cartResponseMapper.toDTO(cart);

	}

	public CartResponseDTO addToCart(CartItemDTO cartItemDTO)  {

		Long userId = getLoggedInUserId();

		// Get user's cart or create a new one for the user
		Cart cart = cartRepository.findByUser_Id(userId).orElseGet(() -> {
			Cart newCart = new Cart();
			Users user = userRepository.getReferenceById(userId);
			newCart.setUser(user);
			return cartRepository.save(newCart);
		});

		// Fetch product
		Product product = productRepository.findById(cartItemDTO.getProduct_id())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		;

		// Check whether the same product exists in the cart
		Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId());

		//Check whether the cart does not contain the item and User entered negative quantity
		if(existingItem.isEmpty() && cartItemDTO.getQuantity()<=0)
			throw new BadRequestException("Quantity must be greater than zero");
	 
		// Increase quantity
		if (existingItem.isPresent()) {
			CartItem item = existingItem.get();
			int newQuantity = item.getQuantity() + cartItemDTO.getQuantity();
			if (newQuantity <= 0) {
				cartItemRepository.delete(item);
				cart.getItems().remove(item);
			} else {
				item.setQuantity(newQuantity);
				cartItemRepository.save(item);
			}
		} else {
			if (cartItemDTO.getQuantity() > 0) {
				CartItem item = new CartItem();
				item.setProduct(product);
				item.setCart(cart);
				item.setQuantity(cartItemDTO.getQuantity());

				cartItemRepository.save(item);
				cart.getItems().add(item);
			}
		}

		double totalPrice = cart.getItems().stream().mapToDouble(x -> x.getProduct().getPrice() * x.getQuantity())
				.sum();

		cart.setTotalPrice(totalPrice);
		cartRepository.save(cart);
		return cartResponseMapper.toDTO(cart);
	}

	public CartResponseDTO removeItemByProductId(Long id) {
		Long userId = getLoggedInUserId();
//		// Get logged-in username
//		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//		String username = authentication.getName();

		// Fetch User
		// Users user = userRepository.findByUsername(username).orElseThrow(()-> new
		// ResourceNotFoundException("User not found"));

		// Get cart
		Cart cart = cartRepository.findByUser_Id(userId)
				.orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

		CartItem cartItem = cart.getItems().stream().filter(item -> item.getProduct().getId().equals(id)).findFirst()
				.orElseThrow(() -> new ResourceNotFoundException("Product not in cart"));

		// remove
		cartItemRepository.delete(cartItem);
		cart.getItems().remove(cartItem);

		double totalPrice = cart.getItems().stream()
				.mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
		cart.setTotalPrice(totalPrice);
		cartRepository.save(cart);
		return cartResponseMapper.toDTO(cart);
	}

}

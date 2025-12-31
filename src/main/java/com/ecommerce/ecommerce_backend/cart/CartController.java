package com.ecommerce.ecommerce_backend.cart;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "APIs to manage shopping cart")
public class CartController {

	private final CartService cartService;

	@GetMapping("/myCart")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Get user's cart", description = "Returns the current logged-in user's cart with items and total")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cart retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Cart not found") })
	public ResponseEntity<CartResponseDTO> getMyCart() {
		CartResponseDTO cartResponseDTO = cartService.getMyCart();
		return ResponseEntity.ok(cartResponseDTO);
	}

	@PostMapping
	@Transactional
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Add item to cart", description = "Adds a product with specified quantity to the user's cart")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Item added successfully"),
			@ApiResponse(responseCode = "404", description = "Product not found") })
	public ResponseEntity<CartResponseDTO> addToCart(@RequestBody CartItemDTO cartItemDTO) {

		CartResponseDTO addedCartDTO = cartService.addToCart(cartItemDTO);
		return ResponseEntity.ok(addedCartDTO);
	}

	@DeleteMapping("/productId/{productId}")
	@Transactional
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Remove item from cart", description = "Removes a product from the cart by product ID")
	public ResponseEntity<CartResponseDTO> removeItemById(@PathVariable Long productId) {
		CartResponseDTO cartDTO = cartService.removeItemByProductId(productId);
		return ResponseEntity.ok(cartDTO);
	}

}

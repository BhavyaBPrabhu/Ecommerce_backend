package com.ecommerce.ecommerce_backend.cart;

import com.ecommerce.ecommerce_backend.product.ProductViewDTO;

import lombok.Data;

@Data
public class CartItemResponseDTO {
	private Integer quantity;
	private ProductViewDTO product;
}

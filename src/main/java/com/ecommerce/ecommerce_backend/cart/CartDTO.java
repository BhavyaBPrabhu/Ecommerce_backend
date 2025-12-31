package com.ecommerce.ecommerce_backend.cart;

import java.util.List;

import lombok.Data;

@Data
public class CartDTO {

	private Long id;

    private Double totalPrice;

    private List<CartItemDTO> items;
}

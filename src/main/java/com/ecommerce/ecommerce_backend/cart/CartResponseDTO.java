package com.ecommerce.ecommerce_backend.cart;

import java.util.List;

import lombok.Data;

@Data
public class CartResponseDTO {
    private Long cartId;
    private double total;
    private List<CartItemResponseDTO> items;
}
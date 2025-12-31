package com.ecommerce.ecommerce_backend.cart;

import lombok.Data;

@Data
public class CartItemDTO {

	private Long id;

    private Integer quantity;

    private Long product_id;

    private Long cart_id;
}

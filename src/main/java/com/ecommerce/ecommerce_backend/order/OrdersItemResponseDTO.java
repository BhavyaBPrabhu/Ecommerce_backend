package com.ecommerce.ecommerce_backend.order;

import lombok.Data;

@Data
public class OrdersItemResponseDTO {
	private Long productId;
	private String productName;
	private Double productPrice;
	private Integer productQuantity;

}

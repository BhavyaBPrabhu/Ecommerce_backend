package com.ecommerce.ecommerce_backend.product;

import lombok.Data;

@Data
public class ProductViewDTO {
    private Long id;
    private String name;
    private Double price;
}
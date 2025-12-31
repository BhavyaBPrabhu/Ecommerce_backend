package com.ecommerce.ecommerce_backend.product;


import java.util.Set;

import com.ecommerce.ecommerce_backend.user.Authority;
import com.ecommerce.ecommerce_backend.user.UserDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ProductDTO {

	private Long id;
	
	@NotBlank(message = "Product name cannot be blank")
    @Size(min = 4, max = 50, message = " Product name should be between 4 and 50 characters")
	private String name;
	

	@NotNull
	@Positive
	private Double price;
	

	@NotNull()
	private Long categoryId;
	
	
	private String sku;
	
	@NotNull(message = "Quantity is required")
	private Integer quantity;
	
}

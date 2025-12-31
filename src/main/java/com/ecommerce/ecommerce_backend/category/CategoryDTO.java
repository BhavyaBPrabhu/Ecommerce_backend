package com.ecommerce.ecommerce_backend.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryDTO {

	private Long id;
	@NotBlank
	@Size(max = 50)
	private String name;
}

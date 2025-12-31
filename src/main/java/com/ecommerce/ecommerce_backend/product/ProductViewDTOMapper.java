package com.ecommerce.ecommerce_backend.product;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductViewDTOMapper {

	ProductViewDTO toDTO(Product product);
}

package com.ecommerce.ecommerce_backend.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.ecommerce.ecommerce_backend.product.ProductViewDTOMapper;

@Mapper(componentModel = "spring", uses = ProductViewDTOMapper.class)
public interface CartItemResponseDTOMapper {
	// @Mapping(source = "product", target = "product")
	CartItemResponseDTO toDTO(CartItem cartItem);
}

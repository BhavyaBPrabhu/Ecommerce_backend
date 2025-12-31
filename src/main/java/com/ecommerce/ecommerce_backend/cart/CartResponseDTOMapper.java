package com.ecommerce.ecommerce_backend.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = CartItemResponseDTOMapper.class)
public interface CartResponseDTOMapper {

	@Mapping(source = "id", target = "cartId")
	@Mapping(source = "totalPrice", target = "total")
	CartResponseDTO toDTO(Cart cart);
}
package com.ecommerce.ecommerce_backend.cart;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

	CartDTO toDTO(Cart cart);

	Cart toEntity(CartDTO dto);
}

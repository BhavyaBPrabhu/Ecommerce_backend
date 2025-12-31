package com.ecommerce.ecommerce_backend.cart;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

	@Mapping(source = "product.id", target = "product_id")
	@Mapping(source = "cart.id", target = "cart_id")
	CartItemDTO toDTO(CartItem cartItem);

}

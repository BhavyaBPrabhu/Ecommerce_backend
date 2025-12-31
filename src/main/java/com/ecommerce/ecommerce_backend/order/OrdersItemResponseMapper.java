package com.ecommerce.ecommerce_backend.order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrdersItemResponseMapper {

	
	OrdersItemResponseDTO toDTO(OrdersItem orderItem);
}

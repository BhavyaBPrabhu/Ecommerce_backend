package com.ecommerce.ecommerce_backend.order;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
	    componentModel = "spring",
	    uses = OrdersItemResponseMapper.class
	)
public interface OrdersResponseMapper {

	@Mapping(source = "id", target = "orderId")
	@Mapping(source = "ordersStatus", target = "status")
	OrdersResponseDTO toDTO(Orders order);
}

package com.ecommerce.ecommerce_backend.user;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

	 UserDTO toDTO(Users user);
	 Users toEntity(UserDTO dto);
}

package com.ecommerce.ecommerce_backend.category;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

	 CategoryDTO toDTO(Category category);
	    Category toEntity(CategoryDTO dto);
}

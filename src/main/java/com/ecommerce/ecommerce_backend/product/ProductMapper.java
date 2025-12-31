package com.ecommerce.ecommerce_backend.product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring")
public interface ProductMapper {

	//“When converting a Product entity to ProductD take product.getCategory().getId() and put it into productDTO.setCategoryId().”
	 	@Mapping(source = "category.id", target = "categoryId")
	    ProductDTO toDTO(Product product);

	 	//When converting a ProductDTO to a Product entity, take dto.getCategoryId() and set it into product.getCategory().setId().”
	    @Mapping( target = "category", ignore=true)
	    Product toEntity(ProductDTO dto);
}

package com.ecommerce.ecommerce_backend.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_backend.exceptions.ResourceAlreadyExistsException;
import com.ecommerce.ecommerce_backend.exceptions.ResourceNotFoundException;
import com.ecommerce.ecommerce_backend.product.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

	  private final CategoryRepository categoryRepository;
	  private final ProductRepository productRepository;
	    private final CategoryMapper mapper;

	    public CategoryDTO create(CategoryDTO dto) {
	        if (categoryRepository.existsByNameIgnoreCase(dto.getName())) {
	            throw new ResourceAlreadyExistsException("Category already exists");
	        }

	        Category category = mapper.toEntity(dto);
	        categoryRepository.save(category);
	        return mapper.toDTO(category);
	    }

	    public Page<CategoryDTO> findAll(Pageable pageable) {
	        return categoryRepository.findAll(pageable)
	                .map(mapper::toDTO);
	                
	    }

	    public void delete(Long id) {
	    	if (productRepository.existsByCategoryId(id)) {
	    	    throw new IllegalStateException("Cannot delete category with products");
	    	}
	        if (!categoryRepository.existsById(id)) {
	            throw new ResourceNotFoundException("Category not found");
	        }
	        categoryRepository.deleteById(id);
	    }
}

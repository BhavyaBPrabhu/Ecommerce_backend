package com.ecommerce.ecommerce_backend.product;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.ecommerce.ecommerce_backend.category.Category;
import com.ecommerce.ecommerce_backend.category.CategoryRepository;
import com.ecommerce.ecommerce_backend.exceptions.EmptyListException;
import com.ecommerce.ecommerce_backend.exceptions.ResourceAlreadyExistsException;
import com.ecommerce.ecommerce_backend.exceptions.ResourceNotFoundException;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final ProductMapper productMapper;
	private final CategoryRepository categoryRepository;

	public Page<ProductDTO> getAllProducts(Pageable pageable) {
		// TODO Auto-generated method stub
		Page<Product> productPage = productRepository.findAllWithCategory(pageable);
		if (productPage.isEmpty())
			throw new EmptyListException("No products found");
		else {
			return productPage.map(productMapper::toDTO);
		}

	}

	public ProductDTO getProductById(Long id) {
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));
		return productMapper.toDTO(product);
	}

	public ProductDTO createProduct(ProductDTO productDTO) {
		boolean exists = productRepository.findByNameAndCategoryId(productDTO.getName(), productDTO.getCategoryId())
				.isPresent();

		if (exists) {
			throw new ResourceAlreadyExistsException("Product already exists in this category");
		}
		Product product = productMapper.toEntity(productDTO);
		Category category = categoryRepository.findById(productDTO.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));
		product.setCategory(category);
		product.setSku(generateUniqueSku(productDTO));
		Product newProduct = productRepository.save(product);
		return productMapper.toDTO(newProduct);
	}

	public ProductDTO updateProduct(Long id, @Valid ProductDTO productDTO) {
		// TODO Auto-generated method stub
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product does not exist with id: " + id));

		// Prevent duplicate (ignore same product)
		Optional<Product> optional = productRepository.findByNameAndCategoryId(productDTO.getName(),
				productDTO.getCategoryId());

		if (optional.isPresent()) {
			Product existing = optional.get();

			if (!existing.getId().equals(id)) {
				throw new ResourceAlreadyExistsException("Product already exists in this category");
			}
		}
		product.setName(productDTO.getName());
		product.setPrice(productDTO.getPrice());
		product.setQuantity(productDTO.getQuantity());
		Category category = categoryRepository.findById(productDTO.getCategoryId())
				.orElseThrow(() -> new ResourceNotFoundException("Category not found"));
		product.setCategory(category);

		return productMapper.toDTO(productRepository.save(product));

	}

	public void deleteProduct(Long id) {
		// TODO Auto-generated method stub
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

		productRepository.delete(product);

	}

	private String generateUniqueSku(ProductDTO dto) {

		String sku;

		do {
			sku = generateSku(dto);
		} while (productRepository.findBySku(sku).isPresent());

		return sku;
	}

	private String generateSku(ProductDTO dto) {

		String namePart = dto.getName().substring(0, Math.min(3, dto.getName().length())).toUpperCase();
		Category category = categoryRepository.findById(dto.getCategoryId()).orElseThrow();
		String categoryPart = category.getName().substring(0, Math.min(3, category.getName().length())).toUpperCase();

		int random = (int) (Math.random() * 9000) + 1000; // 4-digit number

		return categoryPart + "-" + namePart + "-" + random;
	}

	/**
	 * Category: Electronics, Name: Laptop → ELE-LAP-7384
	 * 
	 * Category: Books, Name: Novel → BOO-NOV-1829
	 **/

	public Page<ProductDTO> searchProducts(String name, String categoryName, Double minPrice, Double maxPrice,
			Pageable pageable) {

		Specification<Product> spec = Specification.where(ProductSpecification.nameContains(name))
				.and(ProductSpecification.categoryEquals(categoryName)).and(ProductSpecification.minPrice(minPrice))
				.and(ProductSpecification.maxPrice(maxPrice));

		Page<Product> page = productRepository.findAll(spec, pageable);

		return page.map(productMapper::toDTO);
	}
	// Builds a composable Specification dynamically
	// Page<Product> allows pagination and total counts.
	// Maps entities to DTOs for clean API response.
}
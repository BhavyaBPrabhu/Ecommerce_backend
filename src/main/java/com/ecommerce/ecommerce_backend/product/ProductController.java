package com.ecommerce.ecommerce_backend.product;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
@Tag(name = "Products", description = "APIs for managing and browsing products")
public class ProductController {

	private final ProductService productService;

	@GetMapping("/health")
	public String health() {
	    return "Running";
	}
	
	@Operation(summary = "Get all products", description = "Fetches paginated list of products as per the search criteria")
	@GetMapping("/search")
	public ResponseEntity<Page<ProductDTO>> searchProducts(@RequestParam(required = false) String name,
			@RequestParam(required = false) String categoryName, @RequestParam(required = false) Double minPrice,
			@RequestParam(required = false) Double maxPrice, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "id") String sortBy,
			@RequestParam(defaultValue = "asc") String direction) {

		Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

		Pageable pageable = PageRequest.of(page, size, sort);

		Page<ProductDTO> result = productService.searchProducts(name, categoryName, minPrice, maxPrice, pageable);

		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Get all products", description = "Fetches paginated list of products")
	@GetMapping
	public ResponseEntity<Page<ProductDTO>> getAllProducts(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		Pageable pageable = PageRequest.of(page, size);
		return ResponseEntity.ok(productService.getAllProducts(pageable));
	}

	@Operation(summary = "Get all products", description = "Fetches the product based on the product id")
	@GetMapping("/{id}")
	public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
		ProductDTO productDTO = productService.getProductById(id);
		return ResponseEntity.ok(productDTO);
	}

	@Operation(summary = "Create a new product", description = "Creates a product under a category. Admin access only.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Product created successfully"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
		ProductDTO savedProduct = productService.createProduct(productDTO);

		URI location = ServletUriComponentsBuilder.fromCurrentRequest() // gets current URL
				.path("/{id}") // appends /{id} to it.
				.buildAndExpand(savedProduct.getId()).toUri();
		return ResponseEntity.created(location).body(savedProduct);
	}

	@Operation(summary = "Update product", description = "Updates an existing product by ID. Admin access only.", security = @SecurityRequirement(name = "bearerAuth"))
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Product updated successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid input data"),
			@ApiResponse(responseCode = "404", description = "Product not found"),
			@ApiResponse(responseCode = "403", description = "Access denied") })
	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDTO productDTO) {
		ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
		return ResponseEntity.ok(updatedProduct);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	@Operation(summary = "Delete product", description = "Deletes a product by ID. Admin only.", security = @SecurityRequirement(name = "bearerAuth"))
	public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
		productService.deleteProduct(id);
		return ResponseEntity.ok("Product deleted successfully");
	}
}

package com.ecommerce.ecommerce_backend.category;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(
	    name = "Category",
	    description = "APIs to manage product categories (Admin & Public access)"
	)
public class CategoryController {

	 private final CategoryService service;

	 	@Operation(
		        summary = "Create a new category",
		        description = "Creates a new product category. Only ADMIN users are allowed."
		    )
		    @ApiResponse(
		        responseCode = "200",
		        description = "Category created successfully"
		       
		    )
		    @ApiResponse(
		        responseCode = "400",
		        description = "Category already exists or validation failed"
		    )
		    @ApiResponse(
		        responseCode = "403",
		        description = "Access denied (ADMIN only)"
		    )
	    @PostMapping
	    @PreAuthorize("hasRole('ADMIN')")
	    public ResponseEntity<CategoryDTO> create(@Valid @RequestBody CategoryDTO dto) {
	        return ResponseEntity.ok(service.create(dto));
	    }

	 	  @Operation(
	 		        summary = "Get all categories",
	 		        description = "Fetches all categories with pagination. Public API."
	 		    )
	 		    @ApiResponse(
	 		        responseCode = "200",
	 		        description = "Categories fetched successfully"
	 		    )
	    @GetMapping
	    public ResponseEntity<Page<CategoryDTO>> getAll( @RequestParam(defaultValue = "0") int page,
	            @RequestParam(defaultValue = "10") int size) {
	    	Pageable pageable = PageRequest.of(page, size);
	        return ResponseEntity.ok(service.findAll(pageable));
	    }

	 	
	 	 @Operation(
	 	        summary = "Delete a category",
	 	        description = "Deletes a category by ID. Category must not contain products. ADMIN only."
	 	    )
	 	    @ApiResponse(
	 	        responseCode = "204",
	 	        description = "Category deleted successfully"
	 	    )
	 	    @ApiResponse(
	 	        responseCode = "400",
	 	        description = "Category contains products and cannot be deleted"
	 	    )
	 	    @ApiResponse(
	 	        responseCode = "404",
	 	        description = "Category not found"
	 	    )
	 	    @ApiResponse(
	 	        responseCode = "403",
	 	        description = "Access denied (ADMIN only)"
	 	    )
	    @PreAuthorize("hasRole('ADMIN')")
	    @DeleteMapping("/{id}")
	    public ResponseEntity<Void> delete(@PathVariable Long id) {
	        service.delete(id);
	        return ResponseEntity.noContent().build();
	    }
	}


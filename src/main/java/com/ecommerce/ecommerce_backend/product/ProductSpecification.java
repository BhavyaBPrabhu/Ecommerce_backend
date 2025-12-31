package com.ecommerce.ecommerce_backend.product;

import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {

	 public static Specification<Product> nameContains(String name) {
	        return (root, query, cb) ->
	                name == null ? null : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
	    }

	    public static Specification<Product> categoryEquals(String categoryName) {
	        return (root, query, cb) ->
	        categoryName == null ? null : cb.equal(cb.lower(root.get("category").get("name")), categoryName.toLowerCase());
	    }

	    public static Specification<Product> minPrice(Double minPrice) {
	        return (root, query, cb) ->
	                minPrice == null ? null : cb.greaterThanOrEqualTo(root.get("price"), minPrice);
	    }

	    public static Specification<Product> maxPrice(Double maxPrice) {
	        return (root, query, cb) ->
	                maxPrice == null ? null : cb.lessThanOrEqualTo(root.get("price"), maxPrice);
	    }

		
}

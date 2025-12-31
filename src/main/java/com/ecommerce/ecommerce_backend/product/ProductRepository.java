package com.ecommerce.ecommerce_backend.product;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

	//With JpaSpecificationExecutor productRepository.findAll(specification, pageable) works for dynamic queries with pagination and sorting.
	
    Optional<Product> findBySku(String sku);
    Optional<Product> findByNameAndCategoryId(String name, Long categoryId);
    
    @Query(value ="SELECT p FROM Product p JOIN FETCH p.category",countQuery = "SELECT COUNT(p) FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);
    
	boolean existsByCategoryId(Long id);
    

}


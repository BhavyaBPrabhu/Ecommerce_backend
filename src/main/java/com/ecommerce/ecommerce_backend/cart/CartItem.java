package com.ecommerce.ecommerce_backend.cart;

import com.ecommerce.ecommerce_backend.product.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

	 	@Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private Integer quantity;

	    @ManyToOne
	    @JoinColumn(name = "product_id")
	    private Product product;

	    @ManyToOne
	    @JoinColumn(name = "cart_id")
	    private Cart cart;
}

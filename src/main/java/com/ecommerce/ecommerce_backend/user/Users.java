package com.ecommerce.ecommerce_backend.user;

import java.util.HashSet;
import java.util.Set;

import com.ecommerce.ecommerce_backend.cart.Cart;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = { "authorities", "cart" })
public class Users {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id")
	private long id;

	@Column(nullable = false, unique = true) // ensures DB never stores null
	private String username;

	@Column(nullable = false)
	private String password;

	@OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true) // mappedBy
																											// = "user"
																											// matches
																											// the field
																											// private
																											// User
																											// user; in
																											// Authority.
	@JsonManagedReference
	private Set<Authority> authorities = new HashSet<>();

	@OneToOne(mappedBy = "user")
	private Cart cart;

}

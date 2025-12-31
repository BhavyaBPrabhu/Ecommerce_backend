package com.ecommerce.ecommerce_backend.order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.ecommerce.ecommerce_backend.user.Users;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Orders {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch= FetchType.LAZY)
	private Users user;
	
	@Enumerated(EnumType.STRING)
	private OrdersStatus ordersStatus;
	
	@OneToMany(mappedBy = "order", cascade= CascadeType.ALL,  orphanRemoval = true)
	private List<OrdersItem> items = new ArrayList<>();
	private LocalDateTime createdAt;
	
	private Double totalAmount;
}

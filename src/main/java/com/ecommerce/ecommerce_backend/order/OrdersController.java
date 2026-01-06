package com.ecommerce.ecommerce_backend.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Transactional
@Tag(name = "Orders", description = "Order checkout, retrieval, and cancellation APIs")
public class OrdersController {

	private final OrdersService ordersService;

	@Operation(summary = "Checkout cart and create order", description = """
			Converts the logged-in user's cart into an order.
			Clears the cart after successful order creation.
			Initial order status is CREATED.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Order created successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "Cart is empty") })
	@PostMapping
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<OrdersResponseDTO> checkout() {
		return ResponseEntity.ok(ordersService.checkout());
	}

	@Operation(summary = "Get my orders", description = "Fetches paginated list of orders placed by the currently logged-in user")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Orders fetched successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "No orders found") })
	@GetMapping("/myOrders")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Page<OrdersResponseDTO>> myOrders(@RequestParam(defaultValue="0")int page, @RequestParam(defaultValue="10")int size) {
		Pageable pageable= PageRequest.of(page, size);
		return ResponseEntity.ok(ordersService.myOrders(pageable));
	}

	@Operation(summary = "Cancel an order", description = """
			Cancels an order belonging to the logged-in user.
			Only orders in CREATED state can be cancelled.
			""")
	@ApiResponses({ @ApiResponse(responseCode = "200", description = "Order cancelled successfully"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "404", description = "Order not found") })
	@PatchMapping("/cancel/{orderId}")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<OrdersResponseDTO> cancelOrder(@PathVariable Long orderId) {
		return ResponseEntity.ok(ordersService.cancelOrder(orderId));
	}

}

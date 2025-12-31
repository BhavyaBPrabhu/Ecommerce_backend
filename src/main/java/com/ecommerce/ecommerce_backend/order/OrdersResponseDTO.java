package com.ecommerce.ecommerce_backend.order;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class OrdersResponseDTO {
    private Long orderId;
    private Double totalAmount;
    private OrdersStatus status;
    private LocalDateTime createdAt;
    private List<OrdersItemResponseDTO> items;
    
}

package edu.hei.school.restaurant.endpoint.rest;

import edu.hei.school.restaurant.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderRest {
    private String reference;
    private LocalDateTime creationDateTime;
    private OrderStatus status;
    private Double totalAmount;
    private List<OrderDishRest> dishes;
} 
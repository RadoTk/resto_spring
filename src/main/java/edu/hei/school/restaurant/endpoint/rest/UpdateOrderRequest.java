package edu.hei.school.restaurant.endpoint.rest;

import edu.hei.school.restaurant.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateOrderRequest {
    private OrderStatus status;
    private List<OrderDishRequest> dishes;
} 
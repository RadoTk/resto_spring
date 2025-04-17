package edu.hei.school.restaurant.endpoint.rest;

import edu.hei.school.restaurant.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrderDishesRequest {
    private List<OrderDishRequest> dishes;
    private OrderStatus status;
}


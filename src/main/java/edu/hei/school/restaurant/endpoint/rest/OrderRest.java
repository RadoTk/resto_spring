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
    private Long id;
    private Double totalAmount;
    private String actualStatus;
    private List<OrderDishRest> dishes;
} 
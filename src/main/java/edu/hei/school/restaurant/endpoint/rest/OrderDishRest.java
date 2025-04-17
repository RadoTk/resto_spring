package edu.hei.school.restaurant.endpoint.rest;

import edu.hei.school.restaurant.model.DishOrderStatus;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class OrderDishRest {
        private String dishName; 
        private Double currentPrice;
        private Integer quantity;
        private DishOrderStatus status;
    }

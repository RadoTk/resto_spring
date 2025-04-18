package edu.hei.school.restaurant.endpoint.rest;

import edu.hei.school.restaurant.model.DishOrderStatus;
import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class OrderDishRest {
        private Long id;
        private String name; 
        private Integer quantity;
        private String actualOrderStatus;
    }

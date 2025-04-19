package edu.hei.school.restaurant.endpoint.rest;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateOrderRequest {
    private String reference;
    private List<DishQuantity> dishes = new ArrayList<>(); ;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
    public static class DishQuantity {
        private Long dishId;
        private Integer quantity;
    }
}
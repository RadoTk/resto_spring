package edu.hei.school.restaurant.endpoint.rest;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishOrderRequest {
    private Long dishId;
    private Integer quantity;
}
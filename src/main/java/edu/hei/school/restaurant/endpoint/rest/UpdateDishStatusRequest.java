package edu.hei.school.restaurant.endpoint.rest;

import edu.hei.school.restaurant.model.DishOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateDishStatusRequest {
    private DishOrderStatus newStatus;
} 
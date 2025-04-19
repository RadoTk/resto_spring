package edu.hei.school.restaurant.endpoint.rest;


import edu.hei.school.restaurant.model.DishOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateDishOrderStatus {
    private DishOrderStatus newStatus;
}
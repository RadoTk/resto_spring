package edu.hei.school.restaurant.endpoint.rest;

import edu.hei.school.restaurant.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDishIngredients {
    private Long ingredientId;
    private Double requiredQuantity;
    private Unit unit;
}
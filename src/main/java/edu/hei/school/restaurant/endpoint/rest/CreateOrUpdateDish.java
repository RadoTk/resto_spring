package edu.hei.school.restaurant.endpoint.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class CreateOrUpdateDish {
    private Long id;
    private String name;
    private Double price; 
    private List<DishIngredientRest> ingredients; 
}

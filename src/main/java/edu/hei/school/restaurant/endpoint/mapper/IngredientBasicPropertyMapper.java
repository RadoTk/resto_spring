package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.endpoint.rest.IngredientBasicProperty;
import edu.hei.school.restaurant.model.Ingredient;
import org.springframework.stereotype.Component;

@Component
public class IngredientBasicPropertyMapper {

    public IngredientBasicProperty toBasicProperty(Ingredient ingredient) {
        if (ingredient == null) {
            return null; 
        }
        return new IngredientBasicProperty(
                ingredient.getId(),
                ingredient.getName() 
        );
    }
}

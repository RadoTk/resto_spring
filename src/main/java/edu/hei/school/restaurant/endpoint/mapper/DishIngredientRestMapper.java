package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.endpoint.rest.DishIngredientRest;
import edu.hei.school.restaurant.model.DishIngredient;
import edu.hei.school.restaurant.model.Ingredient;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DishIngredientRestMapper {

    @Autowired
    private IngredientBasicPropertyMapper ingredientBasicPropertyMapper;

    public DishIngredientRest toRest(DishIngredient dishIngredient) {
        return new DishIngredientRest(
                ingredientBasicPropertyMapper.toBasicProperty(dishIngredient.getIngredient()), // Utilisation du mapper
                dishIngredient.getRequiredQuantity(),
                dishIngredient.getUnit()
        );
    }

    
    public DishIngredient toModel(DishIngredientRest dishIngredientRest) {
        Ingredient ingredient = new Ingredient(); // Récupérer l'ingrédient par ID ou autre méthode
        ingredient.setId(dishIngredientRest.getIngredient().getId());
        ingredient.setName(dishIngredientRest.getIngredient().getName());

        return new DishIngredient(
                null, // ID peut être null lors de la création
                ingredient,
                dishIngredientRest.getRequiredQuantity(),
                dishIngredientRest.getUnit()
        );
    }
}

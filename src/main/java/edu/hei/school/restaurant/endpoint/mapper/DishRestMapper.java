package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.endpoint.rest.CreateOrUpdateDish;
import edu.hei.school.restaurant.endpoint.rest.DishIngredientRest;
import edu.hei.school.restaurant.endpoint.rest.DishRest;
import edu.hei.school.restaurant.model.Dish;
import edu.hei.school.restaurant.model.DishIngredient;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DishRestMapper {

    private final DishIngredientRestMapper dishIngredientRestMapper;

    public DishRestMapper(DishIngredientRestMapper dishIngredientRestMapper) {
        this.dishIngredientRestMapper = dishIngredientRestMapper;
    }

    public DishRest toRest(Dish dish) {
        List<DishIngredientRest> ingredientRests = dish.getDishIngredients().stream()
                .map(dishIngredientRestMapper::toRest) // Utiliser le mapper pour les ingrédients
                .collect(Collectors.toList());

        return new DishRest(
                dish.getId(),
                dish.getName(),
                dish.getAvailableQuantity().intValue(), // Convertir en Integer
                dish.getPrice(),
                ingredientRests
        );
    }

    public Dish toModel(CreateOrUpdateDish newDish) {
        Dish dish = new Dish();
        dish.setId(newDish.getId());
        dish.setName(newDish.getName());
        dish.setPrice(newDish.getPrice());

        // Convertir la liste des ingrédients
        List<DishIngredient> dishIngredients = newDish.getIngredients().stream()
                .map(dishIngredientRestMapper::toModel) // Utiliser le mapper pour les ingrédients
                .collect(Collectors.toList());
        dish.setDishIngredients(dishIngredients);

        return dish;
    }

    public Object toRestSummary(Dish dish) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'toRestSummary'");
    }
}

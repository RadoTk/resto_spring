package edu.hei.school.restaurant.dao.mapper;

import edu.hei.school.restaurant.model.DishIngredient;
import edu.hei.school.restaurant.model.Ingredient;
import edu.hei.school.restaurant.model.Unit; // Assurez-vous d'importer l'énumération Unit
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@RequiredArgsConstructor
public class DishIngredientMapper {

    @SneakyThrows
    public DishIngredient apply(ResultSet resultSet, Ingredient ingredient) {
    try {
        DishIngredient dishIngredient = new DishIngredient();
        dishIngredient.setId(resultSet.getLong("dish_ingredient_id"));
        dishIngredient.setRequiredQuantity(resultSet.getDouble("required_quantity"));
        
        String unitValue = resultSet.getString("unit");
        try {
            dishIngredient.setUnit(Unit.valueOf(unitValue.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid unit value: " + unitValue, e);
        }
        
        dishIngredient.setIngredient(ingredient);
        return dishIngredient;
    } catch (SQLException e) {
        throw new RuntimeException("Error mapping DishIngredient", e);
    }
}
}

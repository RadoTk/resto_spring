package edu.hei.school.restaurant.dao.mapper;

import edu.hei.school.restaurant.model.Dish;
import edu.hei.school.restaurant.model.DishIngredient;
import edu.hei.school.restaurant.dao.operations.IngredientCrudOperations;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class DishMapper implements Function<ResultSet, Dish> {

    private final IngredientCrudOperations ingredientCrudOperations; // Injection de dépendance

    @SneakyThrows
    @Override
    public Dish apply(ResultSet resultSet) {
        Long idDish = resultSet.getLong("id");
        String name = resultSet.getString("name");
        Double price = resultSet.getDouble("price");

        Dish dish = new Dish();
        dish.setId(idDish);
        dish.setName(name);
        dish.setPrice(price);

        // Récupérer les ingrédients associés au plat
        List<DishIngredient> dishIngredients = ingredientCrudOperations.findByDishId(idDish);
        dish.setDishIngredients(dishIngredients); // Assurez-vous que la méthode setDishIngredients existe dans Dish

        return dish;
    }
}

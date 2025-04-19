package edu.hei.school.restaurant.service;

import edu.hei.school.restaurant.dao.operations.DishCrudOperations;
import edu.hei.school.restaurant.model.Dish;
import edu.hei.school.restaurant.model.DishIngredient;
import edu.hei.school.restaurant.service.exception.ClientException;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DishService {
    private final DishCrudOperations dishCrudOperations;

    public List<Dish> getDishes(Integer page, Integer size) {
        if (page != null && page < 0) {
            throw new ClientException("Page number " + page + " is negative");
        }
        if (size != null && size <= 0) {
            throw new ClientException("Page size " + size + " must be greater than zero");
        }

        return dishCrudOperations.getAll(page != null ? page : 0, size != null ? size : 10);
    }

    public Dish getById(Long id) {
        Dish dish = dishCrudOperations.findById(id);
        if (dish == null) {
            throw new NotFoundException("Dish with id " + id + " not found");
        }
        return dish;
    }

    public List<Dish> saveAll(List<Dish> dishes) {
        return dishCrudOperations.saveAll(dishes);  
    }


    public Dish updateDishIngredients(Long dishId, List<DishIngredient> dishIngredients) {
        Dish dish = getById(dishId);
        
        // Validation des ingrédients
        dishIngredients.forEach(dishIngredient -> {
            if (dishIngredient.getRequiredQuantity() <= 0) {
                throw new ClientException("Required quantity must be positive");
            }
            if (dishIngredient.getIngredient() == null) {
                throw new ClientException("Ingredient cannot be null");
            }
        });

        // Mise à jour des ingrédients du plat
        dish.setDishIngredients(dishIngredients);
        
        // Sauvegarde des modifications
        return dishCrudOperations.save(dish);
    }

    public Dish save(Dish dish) {
        return dishCrudOperations.save(dish);
    }
}

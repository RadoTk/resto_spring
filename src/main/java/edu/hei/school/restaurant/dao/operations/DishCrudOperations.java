package edu.hei.school.restaurant.dao.operations;

import edu.hei.school.restaurant.dao.DataSource;
import edu.hei.school.restaurant.model.Dish;
import edu.hei.school.restaurant.model.DishIngredient;
import edu.hei.school.restaurant.dao.mapper.DishMapper; 
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DishCrudOperations implements CrudOperations<Dish> {
    private final DataSource dataSource;
    private final DishMapper dishMapper; // Mapper pour convertir ResultSet en Dish
    private final IngredientCrudOperations ingredientCrudOperations;

    @Override
    public List<Dish> getAll(int page, int size) {
        List<Dish> dishes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT d.id, d.name, d.price FROM dish d ORDER BY d.id ASC LIMIT ? OFFSET ?")) {
            // Validation des paramètres
            if (page < 0) {
                throw new IllegalArgumentException("Page number must be >= 0");
            }
            if (size <= 0) {
                throw new IllegalArgumentException("Page size must be > 0");
            }
    
            statement.setInt(1, size);
            statement.setInt(2, page * size);  // Modification clé ici
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Dish dish = dishMapper.apply(resultSet);
                    dishes.add(dish);
                }
                return dishes;
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    
@Override
public Dish findById(Long id) {
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement("SELECT d.id, d.name, d.price FROM dish d WHERE id = ?")) {
        statement.setLong(1, id);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                Dish dish = dishMapper.apply(resultSet); // Mapper les propriétés de base
                List<DishIngredient> dishIngredients = ingredientCrudOperations.findByDishId(id); // Récupérer les ingrédients
                dish.setDishIngredients(dishIngredients); // Remplir les ingrédients
                return dish;
            }
        }
        throw new RuntimeException("Dish.id=" + id + " not found");
    } catch (SQLException e) {
        throw new RuntimeException("Error while finding dish by id: " + id, e);
    }
}

    @SneakyThrows
    @Override
    public List<Dish> saveAll(List<Dish> entities) {
        List<Dish> savedDishes = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                for (Dish dish : entities) {
                    Dish savedDish = save(dish);
                    savedDishes.add(savedDish);
                }
                connection.commit();
                return savedDishes;
            } catch (SQLException e) {
                connection.rollback();
                throw new ServerException(e);
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    public Dish save(Dish dish) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Sauvegarde du plat
                String dishSql = "INSERT INTO dish (id, name, price) VALUES (?, ?, ?) " +
                               "ON CONFLICT (id) DO UPDATE SET name = excluded.name, price = excluded.price " +
                               "RETURNING id, name, price";
                
                Dish savedDish;
                try (PreparedStatement dishStatement = connection.prepareStatement(dishSql)) {
                    dishStatement.setLong(1, dish.getId());
                    dishStatement.setString(2, dish.getName());
                    dishStatement.setDouble(3, dish.getPrice());
                    
                    try (ResultSet resultSet = dishStatement.executeQuery()) {
                        if (resultSet.next()) {
                            savedDish = dishMapper.apply(resultSet);
                        } else {
                            throw new ServerException("Failed to save dish");
                        }
                    }
                }
                
                // Suppression des anciens ingrédients
                String deleteSql = "DELETE FROM dish_ingredient WHERE id_dish = ?";
                try (PreparedStatement deleteStatement = connection.prepareStatement(deleteSql)) {
                    deleteStatement.setLong(1, dish.getId());
                    deleteStatement.executeUpdate();
                }
                
                // Ajout des nouveaux ingrédients
                if (dish.getDishIngredients() != null && !dish.getDishIngredients().isEmpty()) {
                    String ingredientSql = "INSERT INTO dish_ingredient (id_dish, id_ingredient, required_quantity, unit) " +
                                         "VALUES (?, ?, ?, ?::unit)";
                    try (PreparedStatement ingredientStatement = connection.prepareStatement(ingredientSql)) {
                        for (DishIngredient dishIngredient : dish.getDishIngredients()) {
                            ingredientStatement.setLong(1, dish.getId());
                            ingredientStatement.setLong(2, dishIngredient.getIngredient().getId());
                            ingredientStatement.setDouble(3, dishIngredient.getRequiredQuantity());
                            ingredientStatement.setString(4, dishIngredient.getUnit().name());
                            ingredientStatement.addBatch();
                        }
                        ingredientStatement.executeBatch();
                    }
                }
                
                connection.commit();
                
                // Récupération du plat avec ses ingrédients mis à jour
                return findById(dish.getId());
            } catch (SQLException e) {
                connection.rollback();
                throw new ServerException(e);
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }
}

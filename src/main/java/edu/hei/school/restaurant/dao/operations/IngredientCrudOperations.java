package edu.hei.school.restaurant.dao.operations;

import edu.hei.school.restaurant.dao.DataSource;
import edu.hei.school.restaurant.model.DishIngredient;
import edu.hei.school.restaurant.model.Ingredient;
import edu.hei.school.restaurant.dao.mapper.DishIngredientMapper;
import edu.hei.school.restaurant.dao.mapper.IngredientMapper;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class IngredientCrudOperations implements CrudOperations<Ingredient> {
    private final DataSource dataSource;
    private final IngredientMapper ingredientMapper;

    // TODO : default values for page and size
    @Override
    public List<Ingredient> getAll(int page, int size) {
        List<Ingredient> ingredients = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "select i.id, i.name from ingredient i order by i.id asc limit ? offset ?")) {
            statement.setInt(1, size);
            statement.setInt(2, size * (page - 1));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Ingredient ingredient = ingredientMapper.apply(resultSet);
                    ingredients.add(ingredient);
                }
                return ingredients;
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }


    @Override
    public Ingredient findById(Long id) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "select i.id, i.name from ingredient i where i.id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return ingredientMapper.apply(resultSet);
                }
                throw new NotFoundException("Ingredient.id=" + id + " not found");
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    @SneakyThrows
    @Override
    public List<Ingredient> saveAll(List<Ingredient> entities) {
        List<Ingredient> savedIngredients = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String insertSql = "INSERT INTO ingredient (name) VALUES (?) RETURNING id, name";
                String updateSql = "UPDATE ingredient SET name = ? WHERE id = ? RETURNING id, name";
                
                for (Ingredient entity : entities) {
                    Ingredient savedIngredient;
                    if (entity.getId() == null) {
                        // INSERT pour les nouveaux (sans ID)
                        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                            stmt.setString(1, entity.getName());
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    savedIngredient = ingredientMapper.apply(rs);
                                } else {
                                    throw new ServerException("Failed to insert ingredient");
                                }
                            }
                        }
                    } else {
                        // UPDATE pour les existants (avec ID)
                        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                            stmt.setString(1, entity.getName());
                            stmt.setLong(2, entity.getId());
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    savedIngredient = ingredientMapper.apply(rs);
                                } else {
                                    throw new ServerException("Ingredient not found for update");
                                }
                            }
                        }
                    }
                    savedIngredients.add(savedIngredient);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
        return savedIngredients;
    }

    
    public Ingredient save(Ingredient entity) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String sql = "UPDATE ingredient SET name = ? WHERE id = ? RETURNING id, name";
                
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, entity.getName());
                    stmt.setLong(2, entity.getId());
                    
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            Ingredient updated = ingredientMapper.apply(rs);
                            connection.commit();
                            return updated;
                        }
                        throw new NotFoundException("Ingredient not found for update");
                    }
                }
            } catch (Exception e) {
                connection.rollback();
                throw new ServerException("Failed to update ingredient: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new ServerException("Database connection error: " + e.getMessage());
        }
    }


    @Autowired
    private DishIngredientMapper dishIngredientMapper;
    
    public List<DishIngredient> findByDishId(Long dishId) {
        List<DishIngredient> dishIngredients = new ArrayList<>();
    
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT i.id, i.name, di.id AS dish_ingredient_id, di.required_quantity, di.unit FROM ingredient i"
                     + " JOIN dish_ingredient di ON i.id = di.id_ingredient"
                     + " WHERE di.id_dish = ?")) {
            statement.setLong(1, dishId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Ingredient ingredient = ingredientMapper.apply(resultSet);
                    DishIngredient dishIngredient = dishIngredientMapper.apply(resultSet, ingredient);
                    dishIngredients.add(dishIngredient);
                }
                return dishIngredients;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while finding dish ingredients by dish id: " + dishId, e);
        }
    }
    
}

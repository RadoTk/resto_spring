package edu.hei.school.restaurant.dao.mapper;

import edu.hei.school.restaurant.model.Dish;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.Order;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

@Component
public class DishOrderMapper implements Function<ResultSet, DishOrder> {
    @Override
    public DishOrder apply(ResultSet resultSet) {
        try {
            Dish dish = Dish.builder()
                    .id(resultSet.getLong("dish_id"))
                    .name(resultSet.getString("name")) // Nom du plat
                    .price(resultSet.getDouble("price")) // Prix du plat
                    .build();

            return DishOrder.builder()
                    .id(resultSet.getLong("id"))
                    .order(new Order(resultSet.getLong("order_id")))
                    .dish(dish)
                    .quantity(resultSet.getInt("quantity"))
                    .build();
        } catch (SQLException e) {
            throw new RuntimeException("Error mapping DishOrder", e);
        }
    }
}
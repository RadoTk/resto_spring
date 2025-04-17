package edu.hei.school.restaurant.dao.mapper;

import edu.hei.school.restaurant.model.Order;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.function.Function;

@Component
public class OrderMapper implements Function<ResultSet, Order> {
    @Override
    public Order apply(ResultSet resultSet) {
        try {
            return Order.builder()
                .id(resultSet.getLong("id"))
                .reference(resultSet.getString("reference"))
                .creationDateTime(resultSet.getTimestamp("creation_datetime").toLocalDateTime())
                .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
package edu.hei.school.restaurant.dao.mapper;

import edu.hei.school.restaurant.model.DishOrderStatus;
import edu.hei.school.restaurant.model.DishOrderStatusHistory;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.function.Function;

@Component
public class DishOrderStatusHistoryMapper implements Function<ResultSet, DishOrderStatusHistory> {
    @Override
    public DishOrderStatusHistory apply(ResultSet resultSet) {
        try {
            return DishOrderStatusHistory.builder()
                .id(resultSet.getLong("id"))
                .status(DishOrderStatus.valueOf(resultSet.getString("status")))
                .statusDateTime(resultSet.getTimestamp("status_datetime").toLocalDateTime())
                .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
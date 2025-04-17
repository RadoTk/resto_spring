package edu.hei.school.restaurant.dao.mapper;

import edu.hei.school.restaurant.model.OrderStatus;
import edu.hei.school.restaurant.model.OrderStatusHistory;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.function.Function;

@Component
public class OrderStatusHistoryMapper implements Function<ResultSet, OrderStatusHistory> {
    @Override
    public OrderStatusHistory apply(ResultSet resultSet) {
        try {
            return OrderStatusHistory.builder()
                .id(resultSet.getLong("id"))
                .status(OrderStatus.valueOf(resultSet.getString("status")))
                .statusDateTime(resultSet.getTimestamp("status_datetime").toLocalDateTime())
                .build();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
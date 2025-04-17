package edu.hei.school.restaurant.dao.operations;

import edu.hei.school.restaurant.dao.DataSource;
import edu.hei.school.restaurant.dao.mapper.DishOrderMapper;
import edu.hei.school.restaurant.dao.mapper.DishOrderStatusHistoryMapper;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.DishOrderStatus;
import edu.hei.school.restaurant.model.DishOrderStatusHistory;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class DishOrderCrudOperations implements CrudOperations<DishOrder> {
    private final DataSource dataSource;
    private final DishOrderMapper dishOrderMapper;
    private final DishOrderStatusHistoryMapper statusHistoryMapper;

    @Override
    public List<DishOrder> getAll(int page, int size) {
        List<DishOrder> dishOrders = new ArrayList<>();
        String sql = """
    SELECT od.id, od.order_id, od.dish_id, od.quantity 
    FROM \"order_dish\" od
    ORDER BY od.id ASC
    LIMIT ? OFFSET ?
    """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, size);
            statement.setInt(2, size * (page - 1));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    DishOrder dishOrder = dishOrderMapper.apply(resultSet);
                    dishOrder.setStatusHistory(getStatusHistory(dishOrder.getId()));
                    dishOrders.add(dishOrder);
                }
                return dishOrders;
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    public List<DishOrder> findByOrderId(Long orderId) {
        List<DishOrder> dishOrders = new ArrayList<>();
        String sql = """
    SELECT od.id, od.order_id, od.dish_id, od.quantity,
           d.name, d.price 
    FROM \"order_dish\" od
    JOIN \"dish\" d ON od.dish_id = d.id
    WHERE od.order_id = ?
    ORDER BY od.id ASC
    """;

        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, orderId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    DishOrder dishOrder = dishOrderMapper.apply(resultSet);
                    dishOrder.setStatusHistory(getStatusHistory(dishOrder.getId()));
                    dishOrders.add(dishOrder);
                }
                return dishOrders;
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    @Override
    public DishOrder findById(Long id) {
        String sql = """
            SELECT od.id, od.order_id, od.dish_id, od.quantity,
                   d.name, d.price
            FROM \"order_dish\" od
            JOIN \"dish\" d ON od.dish_id = d.id
            WHERE od.id = ?
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    DishOrder dishOrder = dishOrderMapper.apply(resultSet);
                    dishOrder.setStatusHistory(getStatusHistory(dishOrder.getId()));
                    return dishOrder;
                }
                throw new NotFoundException("DishOrder.id=" + id + " not found");
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    @SneakyThrows
    @Override
    public List<DishOrder> saveAll(List<DishOrder> entities) {
        List<DishOrder> savedDishOrders = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String insertSql = """
    INSERT INTO \"order_dish\" (order_id, dish_id, quantity) 
    VALUES (?, ?, ?) 
    RETURNING id, order_id, dish_id, quantity
    """;
                
    String updateSql = """
        UPDATE \"order_dish\" 
        SET order_id = ?, dish_id = ?, quantity = ? 
        WHERE id = ? 
        RETURNING id, order_id, dish_id, quantity
        """;
                for (DishOrder entity : entities) {
                    DishOrder savedDishOrder;
                    if (entity.getId() == null) {
                        // INSERT
                        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                            stmt.setLong(1, entity.getOrder().getId());
                            stmt.setLong(2, entity.getDish().getId());
                            stmt.setInt(3, entity.getQuantity());
                            
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    savedDishOrder = dishOrderMapper.apply(rs);
                                    saveStatusHistory(savedDishOrder.getId(), entity.getStatusHistory());
                                } else {
                                    throw new ServerException("Failed to insert dish order");
                                }
                            }
                        }
                    } else {
                        // UPDATE
                        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                            stmt.setLong(1, entity.getOrder().getId());
                            stmt.setLong(2, entity.getDish().getId());
                            stmt.setInt(3, entity.getQuantity());
                            stmt.setLong(4, entity.getId());
                            
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    savedDishOrder = dishOrderMapper.apply(rs);
                                    updateStatusHistory(savedDishOrder.getId(), entity.getStatusHistory());
                                } else {
                                    throw new ServerException("Dish order not found for update");
                                }
                            }
                        }
                    }
                    savedDishOrders.add(savedDishOrder);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
        return savedDishOrders;
    }

    public void updateStatus(Long dishOrderId, DishOrderStatus newStatus) {
        String sql = """
            INSERT INTO \"order_dish_status\" (order_dish_id, status, status_datetime)
            VALUES (?, ?, ?)
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, dishOrderId);
            statement.setString(2, newStatus.name());
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    private List<DishOrderStatusHistory> getStatusHistory(Long dishOrderId) {
        List<DishOrderStatusHistory> history = new ArrayList<>();
        String sql = """
    SELECT id, status, status_datetime 
    FROM \"order_dish_status\" 
    WHERE order_dish_id = ? 
    ORDER BY status_datetime
    """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, dishOrderId);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    history.add(statusHistoryMapper.apply(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return history;
    }

    private void saveStatusHistory(Long dishOrderId, List<DishOrderStatusHistory> history) {
        String sql = """
    INSERT INTO \"order_dish_status\" (order_dish_id, status, status_datetime) 
    VALUES (?, ?, ?)
    """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (DishOrderStatusHistory entry : history) {
                statement.setLong(1, dishOrderId);
                statement.setString(2, entry.getStatus().name());
                statement.setTimestamp(3, Timestamp.valueOf(entry.getStatusDateTime()));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    private void updateStatusHistory(Long dishOrderId, List<DishOrderStatusHistory> history) {
        String deleteSql = "DELETE FROM \"order_dish_status\" WHERE order_dish_id = ?";
String insertSql = """
    INSERT INTO \"order_dish_status\" (order_dish_id, status, status_datetime) 
    VALUES (?, ?, ?)
    """;
        
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
                 PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                
                deleteStmt.setLong(1, dishOrderId);
                deleteStmt.executeUpdate();
                
                for (DishOrderStatusHistory entry : history) {
                    insertStmt.setLong(1, dishOrderId);
                    insertStmt.setString(2, entry.getStatus().name());
                    insertStmt.setTimestamp(3, Timestamp.valueOf(entry.getStatusDateTime()));
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new ServerException(e);
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }
}
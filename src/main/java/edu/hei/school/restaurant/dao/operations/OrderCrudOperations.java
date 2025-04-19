package edu.hei.school.restaurant.dao.operations;

import edu.hei.school.restaurant.dao.DataSource;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.Order;
import edu.hei.school.restaurant.model.OrderStatus;
import edu.hei.school.restaurant.model.OrderStatusHistory;
import edu.hei.school.restaurant.dao.mapper.OrderMapper;
import edu.hei.school.restaurant.dao.mapper.OrderStatusHistoryMapper;
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
public class OrderCrudOperations implements CrudOperations<Order> {
    private final DataSource dataSource;
    private final OrderMapper orderMapper;
    private final OrderStatusHistoryMapper statusHistoryMapper;
    private final DishOrderCrudOperations dishOrderCrudOperations;

    @Override
    public List<Order> getAll(int page, int size) {
        List<Order> orders = new ArrayList<>();
        String sql = """
            SELECT o.id, o.reference, o.creation_datetime 
            FROM "order" o 
            ORDER BY o.creation_datetime DESC 
            LIMIT ? OFFSET ?
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, size);
            statement.setInt(2, size * (page - 1));
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Order order = orderMapper.apply(resultSet);
                    order.setStatusHistory(getStatusHistory(order.getId()));
                    order.setDishOrders(dishOrderCrudOperations.findByOrderId(order.getId()));
                    orders.add(order);
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return orders;
    }

    @Override
    public Order findById(Long id) {
        String sql = "SELECT o.id, o.reference, o.creation_datetime FROM \"order\" o WHERE o.id = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, id);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Order order = orderMapper.apply(resultSet);
                    order.setStatusHistory(getStatusHistory(order.getId()));
                    order.setDishOrders(dishOrderCrudOperations.findByOrderId(order.getId()));
                    return order;
                }
                throw new NotFoundException("Order.id=" + id + " not found");
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    public Order findByReference(String reference) {
        String sql = "SELECT o.id, o.reference, o.creation_datetime FROM \"order\" o WHERE o.reference = ?";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, reference);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Order order = orderMapper.apply(resultSet);
                    order.setStatusHistory(getStatusHistory(order.getId()));
                    order.setDishOrders(dishOrderCrudOperations.findByOrderId(order.getId()));
                    return order;
                }
                throw new NotFoundException("Order.reference=" + reference + " not found");
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }


@SneakyThrows
    @Override
    public List<Order> saveAll(List<Order> entities) {
        List<Order> savedOrders = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                String insertSql = """
                    INSERT INTO "order" (reference, creation_datetime) 
                    VALUES (?, ?) 
                    RETURNING id, reference, creation_datetime
                    """;
                
                String updateSql = """
                    UPDATE "order" 
                    SET reference = ?, creation_datetime = ? 
                    WHERE id = ? 
                    RETURNING id, reference, creation_datetime
                    """;
                
                for (Order entity : entities) {
                    Order savedOrder;
                    if (entity.getId() == null) {
                        // INSERT
                        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                            stmt.setString(1, entity.getReference());
                            stmt.setTimestamp(2, Timestamp.valueOf(entity.getCreationDateTime()));
                            
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    savedOrder = orderMapper.apply(rs);
                                    saveStatusHistory(connection, savedOrder.getId(), entity.getStatusHistory());
                                    saveDishOrders(connection, savedOrder.getId(), entity.getDishOrders());
                                } else {
                                    throw new ServerException("Failed to insert order");
                                }
                            }
                        }
                    } else {
                        // UPDATE
                        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
                            stmt.setString(1, entity.getReference());
                            stmt.setTimestamp(2, Timestamp.valueOf(entity.getCreationDateTime()));
                            stmt.setLong(3, entity.getId());
                            
                            try (ResultSet rs = stmt.executeQuery()) {
                                if (rs.next()) {
                                    savedOrder = orderMapper.apply(rs);
                                    updateStatusHistory(connection, savedOrder.getId(), entity.getStatusHistory());
                                    updateDishOrders(connection, savedOrder.getId(), entity.getDishOrders());
                                } else {
                                    throw new ServerException("Order not found for update");
                                }
                            }
                        }
                    }
                    savedOrders.add(savedOrder);
                }
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        }
        return savedOrders;
    }



    private List<OrderStatusHistory> getStatusHistory(Long orderId) {
        List<OrderStatusHistory> history = new ArrayList<>();
        String sql = "SELECT id, status, status_datetime FROM \"order_status\" WHERE order_id = ? ORDER BY status_datetime";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, orderId);
            
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

    private void saveStatusHistory(Connection connection, Long orderId, List<OrderStatusHistory> history) throws SQLException {
        String sql = "INSERT INTO \"order_status\" (order_id, status, status_datetime) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (OrderStatusHistory entry : history) {
                statement.setLong(1, orderId);
                statement.setString(2, entry.getStatus().name());
                statement.setTimestamp(3, Timestamp.valueOf(entry.getStatusDateTime()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void updateStatusHistory(Connection connection, Long orderId, List<OrderStatusHistory> history) throws SQLException {
        String deleteSql = "DELETE FROM \"order_status\" WHERE order_id = ?";
        String insertSql = "INSERT INTO \"order_status\" (order_id, status, status_datetime) VALUES (?, ?, ?)";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql);
             PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            
            deleteStmt.setLong(1, orderId);
            deleteStmt.executeUpdate();
            
            for (OrderStatusHistory entry : history) {
                insertStmt.setLong(1, orderId);
                insertStmt.setString(2, entry.getStatus().name());
                insertStmt.setTimestamp(3, Timestamp.valueOf(entry.getStatusDateTime()));
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }

    private void saveDishOrders(Connection connection, Long orderId, List<DishOrder> dishOrders) throws SQLException {
    // 1. Insérer dans order_dish
    String dishOrderSql = "INSERT INTO order_dish (order_id, dish_id, quantity) VALUES (?, ?, ?) RETURNING id";
    String statusSql = "INSERT INTO order_dish_status (order_dish_id, status, status_datetime) VALUES (?, ?, ?)";
    
    try (PreparedStatement dishOrderStmt = connection.prepareStatement(dishOrderSql, Statement.RETURN_GENERATED_KEYS);
         PreparedStatement statusStmt = connection.prepareStatement(statusSql)) {
        
        for (DishOrder dishOrder : dishOrders) {
            // Insérer dans order_dish
            dishOrderStmt.setLong(1, orderId);
            dishOrderStmt.setLong(2, dishOrder.getDish().getId());
            dishOrderStmt.setInt(3, dishOrder.getQuantity());
            dishOrderStmt.executeUpdate();
            
            // Récupérer l'ID généré
            try (ResultSet generatedKeys = dishOrderStmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long dishOrderId = generatedKeys.getLong(1);
                    
                    // Insérer le statut initial dans order_dish_status
                    statusStmt.setLong(1, dishOrderId);
                    statusStmt.setString(2, dishOrder.getStatus().name());
                    statusStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    statusStmt.addBatch();
                }
            }
        }
        statusStmt.executeBatch();
    }
}

    private void updateDishOrders(Connection connection, Long orderId, List<DishOrder> dishOrders) throws SQLException {
        // Supprimer les anciens plats
        String deleteSql = "DELETE FROM order_dish WHERE order_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(deleteSql)) {
            stmt.setLong(1, orderId);
            stmt.executeUpdate();
        }
        
        // Ajouter les nouveaux plats
        if (dishOrders != null && !dishOrders.isEmpty()) {
            saveDishOrders(connection, orderId, dishOrders);
        }
    }

    public Order save(Order order) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Order savedOrder;
                if (order.getId() == null) {
                    savedOrder = createNewOrder(connection, order);
                } else {
                    savedOrder = updateExistingOrder(connection, order);
                }
                connection.commit();
                return savedOrder;
            } catch (Exception e) {
                connection.rollback();
                throw new ServerException(e);
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    private Order createNewOrder(Connection connection, Order order) throws SQLException {
        String orderSql = """
            INSERT INTO "order" (reference, creation_datetime) 
            VALUES (?, ?) 
            RETURNING id, reference, creation_datetime
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(orderSql)) {
            stmt.setString(1, order.getReference());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getCreationDateTime()));
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order savedOrder = orderMapper.apply(rs);
                    saveStatusHistory(connection, savedOrder.getId(), order.getStatusHistory());
                    if (order.getDishOrders() != null && !order.getDishOrders().isEmpty()) {
                        saveDishOrders(connection, savedOrder.getId(), order.getDishOrders());
                    }
                    return savedOrder;
                }
                throw new ServerException("Failed to create order");
            }
        }
    }

    private Order updateExistingOrder(Connection connection, Order order) throws SQLException {
        String updateSql = """
            UPDATE "order" 
            SET reference = ?, creation_datetime = ? 
            WHERE id = ? 
            RETURNING id, reference, creation_datetime
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, order.getReference());
            stmt.setTimestamp(2, Timestamp.valueOf(order.getCreationDateTime()));
            stmt.setLong(3, order.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Order savedOrder = orderMapper.apply(rs);
                    updateStatusHistory(connection, savedOrder.getId(), order.getStatusHistory());
                    updateDishOrders(connection, savedOrder.getId(), order.getDishOrders());
                    return savedOrder;
                }
                throw new ServerException("Order not found for update");
            }
        }
    }


    public boolean existsByReference(String reference) {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "SELECT EXISTS(SELECT 1 FROM \"order\" WHERE reference = ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, reference);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }


    public List<Order> findByStatus(OrderStatus status) {
    List<Order> orders = new ArrayList<>();
    String sql = """
        SELECT o.id, o.reference, o.creation_datetime 
        FROM "order" o 
        JOIN (
            SELECT order_id, MAX(status_datetime) as latest_status
            FROM "order_status"
            GROUP BY order_id
        ) latest ON o.id = latest.order_id
        JOIN "order_status" os ON os.order_id = latest.order_id 
                            AND os.status_datetime = latest.latest_status
        WHERE os.status = ?
        ORDER BY o.creation_datetime DESC
        """;
    
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        
        statement.setString(1, status.name());
        
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Order order = orderMapper.apply(resultSet);
                order.setStatusHistory(getStatusHistory(order.getId()));
                order.setDishOrders(dishOrderCrudOperations.findByOrderId(order.getId()));
                orders.add(order);
            }
        }
    } catch (SQLException e) {
        throw new ServerException(e);
    }
    return orders;
}
}
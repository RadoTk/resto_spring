package edu.hei.school.restaurant.dao.operations;

import edu.hei.school.restaurant.dao.DataSource;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.Order;
import edu.hei.school.restaurant.model.OrderStatusHistory;
import edu.hei.school.restaurant.dao.mapper.OrderMapper;
import edu.hei.school.restaurant.dao.mapper.OrderStatusHistoryMapper;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Repository;

import java.sql.*;
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
                                    saveStatusHistory(savedOrder.getId(), entity.getStatusHistory());
                                    saveDishOrders(savedOrder.getId(), entity.getDishOrders());
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
                                    updateStatusHistory(savedOrder.getId(), entity.getStatusHistory());
                                    updateDishOrders(savedOrder.getId(), entity.getDishOrders());
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

    private void saveStatusHistory(Long orderId, List<OrderStatusHistory> history) {
        String sql = "INSERT INTO \"order_status\" (order_id, status, status_datetime) VALUES (?, ?, ?)";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (OrderStatusHistory entry : history) {
                statement.setLong(1, orderId);
                statement.setString(2, entry.getStatus().name());
                statement.setTimestamp(3, Timestamp.valueOf(entry.getStatusDateTime()));
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    private void updateStatusHistory(Long orderId, List<OrderStatusHistory> history) {
        // Delete existing and insert new
        String deleteSql = "DELETE FROM \"order_status\" WHERE order_id = ?";
        String insertSql = "INSERT INTO \"order_status\" (order_id, status, status_datetime) VALUES (?, ?, ?)";
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
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
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new ServerException(e);
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    private void saveDishOrders(Long orderId, List<DishOrder> dishOrders) {
        dishOrderCrudOperations.saveAll(dishOrders.stream()
            .peek(d -> d.setOrder(new Order(orderId)))
            .toList());
    }

    private void updateDishOrders(Long orderId, List<DishOrder> dishOrders) {
        // Supprimer les plats existants
        dishOrderCrudOperations.deleteByOrderReference(orderId);
        
        // Ajouter les nouveaux plats
        dishOrderCrudOperations.saveAll(dishOrders);
    }


    /*     public Order save(Order order) {
        // Supprimer les plats existants (sans lever d'exception si vide)
        dishOrderCrudOperations.deleteByOrderReference(order.getId());
        
        // Sauvegarder la commande elle-même
        List<Order> savedOrders = saveAll(List.of(order));
        Order savedOrder = savedOrders.get(0);
        
        // Sauvegarder les nouveaux plats
        if (order.getDishOrders() != null && !order.getDishOrders().isEmpty()) {
            List<DishOrder> dishOrders = order.getDishOrders();
            List<DishOrder> savedDishOrders = dishOrderCrudOperations.saveAll(dishOrders);
            savedOrder.setDishOrders(savedDishOrders);
        }
        
        return savedOrder;
    }
        */

        public Order save(Order order) {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                
                if (order.getId() == null) {
                    // CAS CRÉATION - Pas de suppression des plats
                    return createNewOrder(connection, order);
                } else {
                    // CAS MISE À JOUR - Logique existante
                    return updateExistingOrder(connection, order);
                }
            } catch (SQLException e) {
                throw new ServerException(e);
            }
        }
        
        private Order createNewOrder(Connection connection, Order order) throws SQLException {
            // 1. Insertion de la commande
            String orderSql = """
                INSERT INTO "order" (reference, creation_datetime, status) 
                VALUES (?, ?, ?) 
                RETURNING id
                """;
            
            try (PreparedStatement orderStmt = connection.prepareStatement(orderSql)) {
                orderStmt.setString(1, order.getReference());
                orderStmt.setTimestamp(2, Timestamp.valueOf(order.getCreationDateTime()));
                orderStmt.setString(3, order.getStatus().name());
                
                try (ResultSet rs = orderStmt.executeQuery()) {
                    if (rs.next()) {
                        order.setId(rs.getLong("id"));
                        
                        // 2. Insertion de l'historique de statut
                        saveStatusHistory(connection, order.getId(), order.getStatusHistory());
                        
                        // 3. Insertion des plats (si existants)
                        if (order.getDishOrders() != null && !order.getDishOrders().isEmpty()) {
                            saveDishOrders(connection, order.getId(), order.getDishOrders());
                        }
                        
                        connection.commit();
                        return order;
                    }
                    throw new ServerException("Failed to create order");
                }
            }
        }
        
        // Ajoutez cette version de saveStatusHistory pour utiliser une connexion existante
        private void saveStatusHistory(Connection connection, Long orderId, List<OrderStatusHistory> history) throws SQLException {
            String sql = "INSERT INTO order_status (order_id, status, status_datetime) VALUES (?, ?, ?)";
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

}
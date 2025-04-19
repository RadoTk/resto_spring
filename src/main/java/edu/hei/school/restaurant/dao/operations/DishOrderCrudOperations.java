package edu.hei.school.restaurant.dao.operations;

import edu.hei.school.restaurant.dao.DataSource;
import edu.hei.school.restaurant.dao.mapper.DishOrderMapper;
import edu.hei.school.restaurant.dao.mapper.DishOrderStatusHistoryMapper;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.DishOrderStatus;
import edu.hei.school.restaurant.model.DishOrderStatusHistory;
import edu.hei.school.restaurant.model.DishOrderWithTimestamps;
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
                   d.name AS name, d.price AS price,
                   (
                       SELECT ods.status 
                       FROM order_dish_status ods 
                       WHERE ods.order_dish_id = od.id 
                       ORDER BY ods.status_datetime DESC 
                       LIMIT 1
                   ) AS current_status
            FROM order_dish od
            JOIN dish d ON od.dish_id = d.id
            WHERE od.order_id = ?
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
               d.name AS name, d.price AS price
        FROM order_dish od
        JOIN dish d ON od.dish_id = d.id
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
            // Requête INSERT modifiée pour joindre `dish` et retourner name/price
            String insertSql = """
                WITH inserted AS (
                    INSERT INTO "order_dish" (order_id, dish_id, quantity) 
                    VALUES (?, ?, ?) 
                    RETURNING id, order_id, dish_id, quantity
                )
                SELECT i.id, i.order_id, i.dish_id, i.quantity, 
                       d.name, d.price  -- Ajout des colonnes manquantes
                FROM inserted i
                JOIN dish d ON i.dish_id = d.id
                """;
            
            // Requête UPDATE modifiée pour joindre `dish` et retourner name/price
            String updateSql = """
                WITH updated AS (
                    UPDATE "order_dish" 
                    SET order_id = ?, dish_id = ?, quantity = ? 
                    WHERE id = ? 
                    RETURNING id, order_id, dish_id, quantity
                )
                SELECT u.id, u.order_id, u.dish_id, u.quantity, 
                       d.name, d.price  -- Ajout des colonnes manquantes
                FROM updated u
                JOIN dish d ON u.dish_id = d.id
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
                                savedDishOrder = dishOrderMapper.apply(rs);  // Maintenant, rs contient name/price
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
                                savedDishOrder = dishOrderMapper.apply(rs);  // Maintenant, rs contient name/price
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
    // Sauvegarde dans order_dish_status (statut courant)
    String statusSql = """
        INSERT INTO \"order_dish_status\" (order_dish_id, status, status_datetime)
        VALUES (?, ?, ?)
        """;
    
    // Sauvegarde dans dish_order_status_history (historique)
    String historySql = """
        INSERT INTO \"dish_order_status_history\" (dish_order_id, status, status_date_time)
        VALUES (?, ?, ?)
        """;
    
    try (Connection connection = dataSource.getConnection()) {
        connection.setAutoCommit(false);
        try (
            PreparedStatement statusStmt = connection.prepareStatement(statusSql);
            PreparedStatement historyStmt = connection.prepareStatement(historySql)
        ) {
            LocalDateTime now = LocalDateTime.now();
            
            // Insert dans order_dish_status
            statusStmt.setLong(1, dishOrderId);
            statusStmt.setString(2, newStatus.name());
            statusStmt.setTimestamp(3, Timestamp.valueOf(now));
            statusStmt.executeUpdate();
            
            // Insert dans dish_order_status_history
            historyStmt.setLong(1, dishOrderId);
            historyStmt.setString(2, newStatus.name());
            historyStmt.setTimestamp(3, Timestamp.valueOf(now));
            historyStmt.executeUpdate();
            
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw new ServerException(e);
        }
    } catch (SQLException e) {
        throw new ServerException(e);
    }
}

private List<DishOrderStatusHistory> getStatusHistory(Long dishOrderId) {
    List<DishOrderStatusHistory> history = new ArrayList<>();
    String sql = """
        SELECT id, status, status_date_time 
        FROM \"dish_order_status_history\" 
        WHERE dish_order_id = ? 
        ORDER BY status_date_time
        """;
    
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        
        statement.setLong(1, dishOrderId);
        
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                history.add(DishOrderStatusHistory.builder()
                    .id(resultSet.getLong("id"))
                    .status(DishOrderStatus.valueOf(resultSet.getString("status")))
                    .statusDateTime(resultSet.getTimestamp("status_date_time").toLocalDateTime())
                    .build());
            }
        }
    } catch (SQLException e) {
        throw new ServerException(e);
    }
    return history;
}

    public void saveStatusHistory(Long dishOrderId, List<DishOrderStatusHistory> history) {
        String sql = """
            INSERT INTO \"dish_order_status_history\" 
            (dish_order_id, status, status_date_time) 
            VALUES (?, ?, ?)
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            for (DishOrderStatusHistory entry : history) {
                statement.setLong(1, dishOrderId);
                statement.setString(2, entry.getStatus().name());
                statement.setTimestamp(3, Timestamp.valueOf(
                    entry.getStatusDateTime() != null ? 
                    entry.getStatusDateTime() : 
                    LocalDateTime.now()
                ));
                statement.addBatch();
            }
            
            statement.executeBatch();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    public boolean orderDishExists(Long dishOrderId) {
        if (dishOrderId == null) return false;
        
        String sql = "SELECT EXISTS(SELECT 1 FROM order_dish WHERE id = ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, dishOrderId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
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

    public void deleteByOrderReference(Long orderId) {
        String deleteStatusSql = """
            DELETE FROM "order_dish_status"
            WHERE order_dish_id IN (
                SELECT id FROM "order_dish" WHERE order_id = ?
            )
        """;
    
        String deleteDishOrderSql = """
            DELETE FROM "order_dish" WHERE order_id = ?
        """;
    
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (
                PreparedStatement deleteStatusStmt = connection.prepareStatement(deleteStatusSql);
                PreparedStatement deleteDishOrderStmt = connection.prepareStatement(deleteDishOrderSql)
            ) {
                // Supprimer les historiques de statuts
                deleteStatusStmt.setLong(1, orderId);
                deleteStatusStmt.executeUpdate();
    
                // Supprimer les dish orders
                deleteDishOrderStmt.setLong(1, orderId);
                deleteDishOrderStmt.executeUpdate();
                // SUPPRIMEZ LA VÉRIFICATION D'AFFECTED ROWS ICI
                // Ne pas lever d'exception si aucune ligne n'est affectée
    
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new ServerException(e);
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }
    

    public DishOrder save(DishOrder dishOrder) {
        String sql = """
            WITH inserted AS (
                INSERT INTO "order_dish" (order_id, dish_id, quantity) 
                VALUES (?, ?, ?) 
                RETURNING id, order_id, dish_id, quantity
            )
            SELECT i.id, i.order_id, i.dish_id, i.quantity,
                   d.name, d.price
            FROM inserted i
            JOIN dish d ON i.dish_id = d.id
            """;
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, dishOrder.getOrder().getId());
            statement.setLong(2, dishOrder.getDish().getId());
            statement.setInt(3, dishOrder.getQuantity());
            
            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    DishOrder saved = dishOrderMapper.apply(rs);
                    if (dishOrder.getStatusHistory() != null) {
                        saveStatusHistory(saved.getId(), dishOrder.getStatusHistory());
                    }
                    return saved;
                }
                throw new ServerException("Failed to save dish order");
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

    public void saveStatusHistory(Long dishOrderId, DishOrderStatusHistory history) {
        String sql = "INSERT INTO dish_order_status_history " +
                     "(dish_order_id, status, status_date_time) " +
                     "VALUES (?, ?, ?)";
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, dishOrderId);
            statement.setString(2, history.getStatus().name());
            statement.setObject(3, history.getStatusDateTime());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }

public List<DishOrderWithTimestamps> findAllWithTimestamps(int page, int size) {
    String sql = """
        SELECT 
            d.id AS dish_id,
            d.name AS dish_name,
            o.reference AS order_reference,
            od.quantity AS quantity_ordered,
            (SELECT MIN(h.status_date_time) 
             FROM dish_order_status_history h 
             WHERE h.dish_order_id = od.id AND h.status = 'EN_PREPARATION') AS in_preparation_date,
            (SELECT MIN(h.status_date_time) 
             FROM dish_order_status_history h 
             WHERE h.dish_order_id = od.id AND h.status = 'TERMINE') AS finished_date
        FROM order_dish od
        JOIN dish d ON od.dish_id = d.id
        JOIN "order" o ON od.order_id = o.id
        ORDER BY od.id
        LIMIT ? OFFSET ?
        """;
    
    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        
        statement.setInt(1, size);
        statement.setInt(2, (page - 1) * size);
        
        List<DishOrderWithTimestamps> result = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                result.add(DishOrderWithTimestamps.builder()
                    .dishId(resultSet.getLong("dish_id"))
                    .dishName(resultSet.getString("dish_name"))
                    .orderReference(resultSet.getString("order_reference"))
                    .quantityOrdered(resultSet.getInt("quantity_ordered"))
                    .inPreparationDate(resultSet.getTimestamp("in_preparation_date") != null ? 
                        resultSet.getTimestamp("in_preparation_date").toLocalDateTime() : null)
                    .finishedDate(resultSet.getTimestamp("finished_date") != null ? 
                        resultSet.getTimestamp("finished_date").toLocalDateTime() : null)
                    .build());
            }
        }
        return result;
    } catch (SQLException e) {
        throw new ServerException(e);
    }
}
}

package edu.hei.school.restaurant.dao.operations;

import edu.hei.school.restaurant.dao.DataSource;
import edu.hei.school.restaurant.dao.mapper.StockMovementMapper;
import edu.hei.school.restaurant.model.StockMovement;
import edu.hei.school.restaurant.service.exception.ServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.time.Instant.now;

@Repository
public class StockMovementCrudOperations implements CrudOperations<StockMovement> {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private StockMovementMapper stockMovementMapper;

    @Override
    public List<StockMovement> getAll(int page, int size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public StockMovement findById(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


    @Override
public List<StockMovement> saveAll(List<StockMovement> entities) {
    List<StockMovement> stockMovements = new ArrayList<>();
    String sql = """
            insert into stock_movement (quantity, unit, movement_type, creation_datetime, id_ingredient)
            values (?, ?, ?, ?, ?)
            returning id, quantity, unit, movement_type, creation_datetime, id_ingredient""";

    try (Connection connection = dataSource.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {
        
        for (StockMovement entityToSave : entities) {
            try {
                // Vérifiez que les champs obligatoires sont présents
                if (entityToSave.getQuantity() == null || entityToSave.getUnit() == null || 
                    entityToSave.getMovementType() == null || entityToSave.getIngredient() == null) {
                    throw new ServerException("Missing required stock movement fields");
                }

                statement.setDouble(1, entityToSave.getQuantity());
                statement.setObject(2, entityToSave.getUnit().name(), java.sql.Types.OTHER);
                statement.setObject(3, entityToSave.getMovementType().name(), java.sql.Types.OTHER);
                statement.setTimestamp(4, Timestamp.from(entityToSave.getCreationDatetime()));
                statement.setLong(5, entityToSave.getIngredient().getId());
                statement.addBatch(); // Ajoutez le mouvement au batch
            } catch (SQLException e) {
                // Log l'erreur et continuez avec le prochain mouvement
                System.err.println("Erreur lors de l'insertion du mouvement : " + e.getMessage());
            }
        }

        // Exécutez le batch et récupérez les résultats
        int[] results = statement.executeBatch(); // Exécutez le batch
        for (int result : results) {
            if (result >= 0) {
                // Si l'insertion a réussi, vous pouvez récupérer les résultats ici
                // (si vous avez besoin de faire quelque chose avec les IDs retournés)
            }
        }

        return stockMovements; // Retournez les mouvements enregistrés
    } catch (SQLException e) {
        throw new ServerException(e);
    }
}

    
    public List<StockMovement> findByIdIngredient(Long idIngredient) {
        List<StockMovement> stockMovements = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "select s.id, s.quantity, s.unit, s.movement_type, s.creation_datetime from stock_movement s"
                             + " join ingredient i on s.id_ingredient = i.id"
                             + " where s.id_ingredient = ?")) {
            statement.setLong(1, idIngredient);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    stockMovements.add(stockMovementMapper.apply(resultSet));
                }
                return stockMovements;
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }
}

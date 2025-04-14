package edu.hei.school.restaurant.dao.operations;

import edu.hei.school.restaurant.dao.DataSource;
import edu.hei.school.restaurant.dao.mapper.PriceMapper;
import edu.hei.school.restaurant.model.Price;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class PriceCrudOperations implements CrudOperations<Price> {
    @Autowired
    private DataSource dataSource;
    @Autowired
    private PriceMapper priceMapper;

    @Override
    public List<Price> getAll(int page, int size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Price findById(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Price> saveAll(List<Price> prices) {
        if (prices == null || prices.isEmpty()) {
            return List.of(); // Retourne une liste vide si pas de prix
        }
        
        try (Connection connection = dataSource.getConnection()) {
            String sql = "INSERT INTO price (amount, date_value, ingredient_id) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (Price price : prices) {
                    // Vérification des paramètres obligatoires
                    if (price.getAmount() == null || price.getDateValue() == null || price.getIngredient() == null) {
                        throw new ServerException("Missing required price fields");
                    }
                    stmt.setDouble(1, price.getAmount());
                    stmt.setDate(2, Date.valueOf(price.getDateValue()));
                    stmt.setLong(3, price.getIngredient().getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
        return prices;
    }
    public List<Price> findByIdIngredient(Long idIngredient) {
        List<Price> prices = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("select p.id, p.amount, p.date_value from price p"
                     + " join ingredient i on p.id_ingredient = i.id"
                     + " where p.id_ingredient = ?")) {
            statement.setLong(1, idIngredient);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Price price = priceMapper.apply(resultSet);
                    prices.add(price);
                }
                return prices;
            }
        } catch (SQLException e) {
            throw new ServerException(e);
        }
    }
}

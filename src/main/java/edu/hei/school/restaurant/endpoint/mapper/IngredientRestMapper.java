package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.dao.operations.IngredientCrudOperations;
import edu.hei.school.restaurant.endpoint.rest.CreateOrUpdateIngredient;
import edu.hei.school.restaurant.endpoint.rest.IngredientRest;
import edu.hei.school.restaurant.endpoint.rest.PriceRest;
import edu.hei.school.restaurant.endpoint.rest.StockMovementRest;
import edu.hei.school.restaurant.model.Ingredient;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class IngredientRestMapper {
    @Autowired private PriceRestMapper priceRestMapper;
    @Autowired private StockMovementRestMapper stockMovementRestMapper;
    @Autowired private IngredientCrudOperations ingredientCrudOperations;

    public IngredientRest toRest(Ingredient ingredient) {
        // Conversion des prix
        List<PriceRest> priceRests = ingredient.getPrices() != null ?
            ingredient.getPrices().stream()
                .map(price -> priceRestMapper.apply(price))
                .toList() :
            Collections.emptyList();
    
        // Conversion des mouvements de stock
        List<StockMovementRest> stockMovementRests = ingredient.getStockMovements() != null ?
            ingredient.getStockMovements().stream()
                .map(stock -> stockMovementRestMapper.apply(stock))
                .toList() :
            Collections.emptyList();
    
        return new IngredientRest(
            ingredient.getId(),
            ingredient.getName(),
            priceRests,
            stockMovementRests,
            ingredient.getAvailableQuantity(),  
            ingredient.getActualPrice()        
        );
    }


    public Ingredient toModel(CreateOrUpdateIngredient newIngredient) {
        Ingredient ingredient = new Ingredient();
        ingredient.setName(newIngredient.getName());
        
        // Initialiser les listes à vides
        ingredient.setPrices(new ArrayList<>());
        ingredient.setStockMovements(new ArrayList<>());
        
        // Si c'est une mise à jour (id non null), on récupère les données existantes
        if (newIngredient.getId() != null) {
            try {
                Ingredient existingIngredient = ingredientCrudOperations.findById(newIngredient.getId());
                if (existingIngredient != null) {
                    ingredient.setId(newIngredient.getId());
                    ingredient.addPrices(existingIngredient.getPrices());
                    ingredient.addStockMovements(existingIngredient.getStockMovements());
                }
            } catch (NotFoundException e) {
                // Les listes sont déjà initialisées à vides
            }
        }
        
        return ingredient;
    }
    
}

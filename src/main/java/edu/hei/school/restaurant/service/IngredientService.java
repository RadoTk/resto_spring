package edu.hei.school.restaurant.service;

import edu.hei.school.restaurant.dao.operations.IngredientCrudOperations;
import edu.hei.school.restaurant.dao.operations.PriceCrudOperations;
import edu.hei.school.restaurant.dao.operations.StockMovementCrudOperations;
import edu.hei.school.restaurant.model.Ingredient;
import edu.hei.school.restaurant.model.Price;
import edu.hei.school.restaurant.model.StockMovement;
import edu.hei.school.restaurant.service.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientCrudOperations ingredientCrudOperations;
    private final PriceCrudOperations priceCrudOperations;
    private final StockMovementCrudOperations stockMovementCrudOperations;
    

    public List<Ingredient> getIngredientsByPrices(Double priceMinFilter, Double priceMaxFilter) {
        if (priceMinFilter != null && priceMinFilter < 0) {
            throw new ClientException("PriceMinFilter " + priceMinFilter + " is negative");
        }
        if (priceMaxFilter != null && priceMaxFilter < 0) {
            throw new ClientException("PriceMaxFilter " + priceMaxFilter + " is negative");
        }
        if (priceMinFilter != null && priceMaxFilter != null) {
            if (priceMinFilter > priceMaxFilter) {
                throw new ClientException("PriceMinFilter " + priceMinFilter + " is greater than PriceMaxFilter " + priceMaxFilter);
            }
        }
        // TODO : paginate from restController OR filter from repository directly
        List<Ingredient> ingredients = ingredientCrudOperations.getAll(1, 500);

        return ingredients.stream()
                .filter(ingredient -> {
                    if (priceMinFilter == null && priceMaxFilter == null) {
                        return true;
                    }
                    Double unitPrice = ingredient.getActualPrice();
                    if (priceMinFilter != null && priceMaxFilter == null) {
                        return unitPrice >= priceMinFilter;
                    }
                    if (priceMinFilter == null) {
                        return unitPrice <= priceMaxFilter;
                    }
                    return unitPrice >= priceMinFilter && unitPrice <= priceMaxFilter;
                })
                .toList();
    }

    
    public List<Ingredient> getAll(Integer page, Integer size) {
        return ingredientCrudOperations.getAll(page, size);
    }

    public Ingredient getById(Long id) {
        return ingredientCrudOperations.findById(id);
    }

    public List<Ingredient> saveAll(List<Ingredient> ingredients) {
        return ingredientCrudOperations.saveAll(ingredients);
    }


    public Ingredient addPrices(Long ingredientId, List<Price> pricesToAdd) {
        Ingredient ingredient = ingredientCrudOperations.findById(ingredientId);
        
        // Charge les prix existants depuis la base de données
        List<Price> existingPrices = priceCrudOperations.findByIdIngredient(ingredientId);
        ingredient.setPrices(existingPrices);
        
        // Ajoute les nouveaux prix
        ingredient.addPrices(pricesToAdd);
        
        // Sauvegarde les nouveaux prix
        priceCrudOperations.saveAll(pricesToAdd);
        
        return ingredientCrudOperations.save(ingredient);
    }



    public Ingredient addStockMovements(Long ingredientId, List<StockMovement> stockMovementsToAdd) {
        // Récupérer l'ingrédient par son ID
        Ingredient ingredient = ingredientCrudOperations.findById(ingredientId);
        
        // Charger les mouvements de stock existants depuis la base de données
        List<StockMovement> existingMovements = stockMovementCrudOperations.findByIdIngredient(ingredientId);
        ingredient.setStockMovements(existingMovements);
        
        // Ajouter les nouveaux mouvements de stock
        ingredient.addStockMovements(stockMovementsToAdd);
        
        // Sauvegarder les nouveaux mouvements de stock
        stockMovementCrudOperations.saveAll(stockMovementsToAdd);
        
        // Sauvegarder l'ingrédient mis à jour
        return ingredientCrudOperations.save(ingredient);
    }
    


    public Ingredient update(Ingredient ingredient) {
        // Vérifie que l'ingrédient existe (utilise votre findById existant)
        Ingredient existing = ingredientCrudOperations.findById(ingredient.getId());
        
        // Met à jour les champs modifiables
        existing.setName(ingredient.getName());
        
        // Sauvegarde la mise à jour
        return ingredientCrudOperations.save(existing);
    }
}

package edu.hei.school.restaurant.service;

import edu.hei.school.restaurant.dao.operations.IngredientCrudOperations;
import edu.hei.school.restaurant.dao.operations.PriceCrudOperations;
import edu.hei.school.restaurant.model.Ingredient;
import edu.hei.school.restaurant.model.Price;
import edu.hei.school.restaurant.model.StockMovement;
import edu.hei.school.restaurant.service.exception.ClientException;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngredientService {
    private final IngredientCrudOperations ingredientCrudOperations;
    private final PriceCrudOperations priceCrudOperations;
    

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



    public Ingredient addStockMovements(Long ingredientId, List<StockMovement> stockMovements) {
        Ingredient ingredient = getById(ingredientId);
        if (ingredient == null) {
            throw new NotFoundException("Ingredient with id " + ingredientId + " not found");
        }

        // Add the movements to the ingredient using the entity method
        ingredient.addStockMovements(stockMovements);

        // Save the updated ingredient
        return ingredientCrudOperations.saveAll(List.of(ingredient)).get(0);
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

package edu.hei.school.restaurant.endpoint;

import edu.hei.school.restaurant.endpoint.mapper.IngredientRestMapper;
import edu.hei.school.restaurant.endpoint.rest.CreateIngredientPrice;
import edu.hei.school.restaurant.endpoint.rest.CreateOrUpdateIngredient;
import edu.hei.school.restaurant.endpoint.rest.IngredientRest;
import edu.hei.school.restaurant.model.Ingredient;
import edu.hei.school.restaurant.model.Price;
import edu.hei.school.restaurant.service.IngredientService;
import edu.hei.school.restaurant.service.exception.ClientException;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
public class IngredientRestController {
    private final IngredientService ingredientService;
    private final IngredientRestMapper ingredientRestMapper;

    @GetMapping("/ingredients")
    public ResponseEntity<Object> getIngredients(@RequestParam(name = "priceMinFilter", required = false) Double priceMinFilter,
                                                 @RequestParam(name = "priceMaxFilter", required = false) Double priceMaxFilter) {
        try {
            List<Ingredient> ingredientsByPrices = ingredientService.getIngredientsByPrices(priceMinFilter, priceMaxFilter);
            List<IngredientRest> ingredientRests = ingredientsByPrices.stream()
                    .map(ingredient -> ingredientRestMapper.toRest(ingredient))
                    .toList();
            return ResponseEntity.ok().body(ingredientRests);
        } catch (ClientException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/ingredients")
    public ResponseEntity<Object> addIngredients(@RequestBody List<CreateOrUpdateIngredient> ingredientsToCreate) {
    try {
        // Ajoutez une validation
        ingredientsToCreate.forEach(ingredient -> {
            if (ingredient.getName() == null || ingredient.getName().isBlank()) {
                throw new ServerException("Ingredient name cannot be empty");
            }
        });

        List<Ingredient> ingredients = ingredientsToCreate.stream()
                .map(ingredientRestMapper::toModel)
                .toList();
                
        List<IngredientRest> ingredientsRest = ingredientService.saveAll(ingredients).stream()
                .map(ingredientRestMapper::toRest)
                .toList();
                
        return ResponseEntity.ok().body(ingredientsRest);
    } catch (ServerException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}

    @PutMapping("/ingredients")
    public ResponseEntity<Object> updateIngredients(@RequestBody List<CreateOrUpdateIngredient> ingredientsToCreateOrUpdate) {
        try {
            List<Ingredient> ingredients = ingredientsToCreateOrUpdate.stream()
                    .map(ingredient -> ingredientRestMapper.toModel(ingredient))
                    .toList();
            List<IngredientRest> ingredientsRest = ingredientService.saveAll(ingredients).stream()
                    .map(ingredient -> ingredientRestMapper.toRest(ingredient))
                    .toList();
            return ResponseEntity.ok().body(ingredientsRest);
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/ingredients/{ingredientId}")
    public ResponseEntity<Object> updateIngredient(
        @PathVariable Long ingredientId,
        @RequestBody CreateOrUpdateIngredient ingredientToUpdate) {
    try {
        // Validation
        if (ingredientToUpdate.getName() == null || ingredientToUpdate.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Ingredient name cannot be empty");
        }

        // Conversion et assignation de l'ID
        Ingredient ingredientModel = ingredientRestMapper.toModel(ingredientToUpdate);
        ingredientModel.setId(ingredientId);

        // Mise à jour
        Ingredient updatedIngredient = ingredientService.update(ingredientModel);
        
        return ResponseEntity.ok(ingredientRestMapper.toRest(updatedIngredient));
        
    } catch (NotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
    } catch (ClientException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ServerException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}



    @PutMapping("/ingredients/{ingredientId}/prices")
    public ResponseEntity<Object> updateIngredientPrices(@PathVariable Long ingredientId, @RequestBody List<CreateIngredientPrice> ingredientPrices) {
        List<Price> prices = ingredientPrices.stream()
                .map(ingredientPrice ->
                        new Price(ingredientPrice.getAmount(), ingredientPrice.getDateValue()))
                .toList();
        Ingredient ingredient = ingredientService.addPrices(ingredientId, prices);
        IngredientRest ingredientRest = ingredientRestMapper.toRest(ingredient);
        return ResponseEntity.ok().body(ingredientRest);
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<Object> getIngredient(@PathVariable(name = "id") Long id) {
        try {
            return ResponseEntity.ok().body(ingredientRestMapper.toRest(ingredientService.getById(id)));
        } catch (ClientException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

}


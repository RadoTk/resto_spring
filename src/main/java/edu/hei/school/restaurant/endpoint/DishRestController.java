package edu.hei.school.restaurant.endpoint;

import edu.hei.school.restaurant.endpoint.mapper.DishRestMapper;
import edu.hei.school.restaurant.endpoint.rest.CreateOrUpdateDish;
import edu.hei.school.restaurant.endpoint.rest.DishRest;
import edu.hei.school.restaurant.endpoint.rest.UpdateDishIngredients;
import edu.hei.school.restaurant.model.Dish;
import edu.hei.school.restaurant.model.DishIngredient;
import edu.hei.school.restaurant.model.Ingredient;
import edu.hei.school.restaurant.service.DishService;
import edu.hei.school.restaurant.service.exception.ClientException;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
public class DishRestController {
    private final DishService dishService;
    private final DishRestMapper dishRestMapper;

    @GetMapping("/dishes")
    public ResponseEntity<Object> getDishes(
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size) {
        try {
            List<Dish> dishes = dishService.getDishes(page, size);
            List<DishRest> dishRests = dishes.stream()
                    .map(dishRestMapper::toRest)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dishRests);
        } catch (ClientException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/dishes")
public ResponseEntity<Object> createDish(@RequestBody CreateOrUpdateDish dishToCreate) {
    try {
        // Conversion du DTO en modèle
        Dish dishModel = dishRestMapper.toModel(dishToCreate);
        
        // Validation du prix
        if (dishModel.getPrice() == null || dishModel.getPrice() <= 0) {
            throw new ClientException("Le prix doit être positif");
        }
        
        // Sauvegarde du plat
        Dish createdDish = dishService.save(dishModel);
        
        // Conversion du modèle en DTO pour la réponse
        DishRest dishRest = dishRestMapper.toRest(createdDish);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(dishRest);
    } catch (ClientException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ServerException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}


@PostMapping("/dishes/batch")  // Nouveau chemin spécifique
public ResponseEntity<Object> createDishesBatch(
    @RequestBody List<CreateOrUpdateDish> dishesToCreate) {
    try {
        // 1. Conversion des DTOs en modèles
        List<Dish> dishModels = dishesToCreate.stream()
            .map(dishRestMapper::toModel)
            .collect(Collectors.toList());

        // 2. Validation des prix (exemple)
        for (Dish dish : dishModels) {
            if (dish.getPrice() == null || dish.getPrice() <= 0) {
                throw new ClientException("Prix invalide pour le plat: " + dish.getName());
            }
        }

        // 3. Appel au service pour sauvegarde en batch
        List<Dish> createdDishes = dishService.saveAll(dishModels);

        // 4. Conversion des modèles en DTOs pour la réponse
        List<DishRest> dishRests = createdDishes.stream()
            .map(dishRestMapper::toRest)
            .collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.CREATED).body(dishRests);
    } catch (ClientException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    } catch (ServerException e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}

    @PutMapping("/dishes/{id}/ingredients")
    public ResponseEntity<Object> updateDishIngredients(
            @PathVariable(name = "id") Long dishId,
            @RequestBody List<UpdateDishIngredients> ingredientsToUpdate) {
        try {
            // Conversion des DTOs en modèles
            List<DishIngredient> dishIngredients = ingredientsToUpdate.stream()
                    .map(ingredient -> {
                        DishIngredient dishIngredient = new DishIngredient();
                        dishIngredient.setRequiredQuantity(ingredient.getRequiredQuantity());
                        dishIngredient.setUnit(ingredient.getUnit());
                        
                        Ingredient ingredientModel = new Ingredient();
                        ingredientModel.setId(ingredient.getIngredientId());
                        dishIngredient.setIngredient(ingredientModel);
                        
                        return dishIngredient;
                    })
                    .collect(Collectors.toList());

            // Mise à jour des ingrédients
            Dish updatedDish = dishService.updateDishIngredients(dishId, dishIngredients);
            
            return ResponseEntity.ok(dishRestMapper.toRest(updatedDish));
        } catch (ClientException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /*
    @PostMapping
    public ResponseEntity<Object> createDishes(@RequestBody List<DishRest> dishesToCreate) {
        try {
            // Validation basique
            dishesToCreate.forEach(dish -> {
                if (dish.getName() == null || dish.getName().isBlank()) {
                    throw new ClientException("Dish name cannot be empty");
                }
                if (dish.getActualPrice() == null || dish.getActualPrice() <= 0) {
                    throw new ClientException("Dish price must be positive");
                }
            });

            List<Dish> dishes = dishesToCreate.stream()
                    .map(dishRestMapper::toModel) // Remplacez toDomain par toModel
                    .toList();

            List<DishRest> createdDishes = dishService.saveAll(dishes).stream()
                    .map(dishRestMapper::toRest)
                    .toList();

            return ResponseEntity.ok(createdDishes);
        } catch (ClientException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping
    public ResponseEntity<Object> updateDishes(@RequestBody List<DishRest> dishesToUpdate) {
        try {
            List<Dish> dishes = dishesToUpdate.stream()
                    .map(dishRestMapper::toModel) // Remplacez toDomain par toModel
                    .toList();

            List<DishRest> updatedDishes = dishService.saveAll(dishes).stream()
                    .map(dishRestMapper::toRest)
                    .toList();

            return ResponseEntity.ok(updatedDishes);
        } catch (ClientException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getDishById(@PathVariable Long id) {
        try {
            Dish dish = dishService.getById(id);
            return ResponseEntity.ok(dishRestMapper.toRest(dish)); // Correction ici
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/{dishId}")
    public ResponseEntity<Object> updateDish(
            @PathVariable Long dishId,
            @RequestBody DishRest dishToUpdate) {
        try {
            // Validation
            if (dishToUpdate.getName() == null || dishToUpdate.getName().isBlank()) {
                throw new ClientException("Dish name cannot be empty");
            }
            if (dishToUpdate.getActualPrice() <= 0) {
                throw new ClientException("Dish price must be positive");
            }

            // Conversion et assignation de l'ID
            Dish dish = dishRestMapper.toModel(dishToUpdate); // Remplacez toDomain par toModel
            dish.setId(dishId);

            // Mise à jour
            Dish updatedDish = dishService.update(dish);
            return ResponseEntity.ok(dishRestMapper.toRest(updatedDish));
            
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (ClientException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
         */
}


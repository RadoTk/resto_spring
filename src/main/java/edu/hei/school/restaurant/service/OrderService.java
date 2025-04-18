package edu.hei.school.restaurant.service;

import edu.hei.school.restaurant.dao.operations.DishCrudOperations;
import edu.hei.school.restaurant.dao.operations.DishOrderCrudOperations;
import edu.hei.school.restaurant.dao.operations.OrderCrudOperations;
import edu.hei.school.restaurant.endpoint.rest.OrderDishRequest;
import edu.hei.school.restaurant.endpoint.rest.UpdateOrderDishesRequest;
import edu.hei.school.restaurant.endpoint.rest.UpdateOrderRequest;
import edu.hei.school.restaurant.model.*;
import edu.hei.school.restaurant.service.exception.ClientException;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderCrudOperations orderCrudOperations;
    private final DishCrudOperations dishCrudOperations;
    private final DishOrderCrudOperations dishOrderCrudOperations;

    public Order getByReference(String reference) {
        Order order = orderCrudOperations.findByReference(reference);
        
        // Initialisation des statuts si absents
        if (order.getStatusHistory() == null || order.getStatusHistory().isEmpty()) {
            OrderStatusHistory initialStatus = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.CREE)
                .statusDateTime(order.getCreationDateTime() != null ? 
                    order.getCreationDateTime() : 
                    LocalDateTime.now())
                .build();
            order.setStatusHistory(List.of(initialStatus));
        }
        
        if (order.getDishOrders() != null) {
            order.getDishOrders().forEach(dishOrder -> {
                if (dishOrder.getStatusHistory() == null || dishOrder.getStatusHistory().isEmpty()) {
                    DishOrderStatusHistory initialStatus = DishOrderStatusHistory.builder()
                        .dishOrder(dishOrder)
                        .status(DishOrderStatus.CREE)
                        .statusDateTime(order.getCreationDateTime() != null ? 
                            order.getCreationDateTime() : 
                            LocalDateTime.now())
                        .build();
                    dishOrder.setStatusHistory(List.of(initialStatus));
                }
            });
        }
        
        return order;
    }

    public Order updateDishes(String reference, UpdateOrderRequest request) {
        Order order = getByReference(reference);
        
        List<DishOrder> dishOrders = request.getDishes().stream()
                .map(dishRequest -> toDishOrder(order, dishRequest))
                .toList();
        
        order.setDishOrders(dishOrders);
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
            if (OrderStatus.CONFIRME.equals(request.getStatus())) {
                order.confirm();
            }
        }
        
        List<Order> savedOrders = orderCrudOperations.saveAll(List.of(order));
        return savedOrders.getFirst();
    }

    public Order updateDishStatus(String reference, Long dishId, DishOrderStatus newStatus) {
        Order order = getByReference(reference);
        
        DishOrder dishOrder = order.getDishOrders().stream()
                .filter(d -> d.getDish().getId().equals(dishId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Dish not found in order: " + dishId));
        
        dishOrder.updateStatus(newStatus);
        
        List<Order> savedOrders = orderCrudOperations.saveAll(List.of(order));
        return savedOrders.getFirst();
    }

    private DishOrder toDishOrder(Order order, OrderDishRequest dishRequest) {
        Dish dish = dishCrudOperations.findById(dishRequest.getDishId());
        if (dish == null) {
            throw new NotFoundException("Dish not found with id: " + dishRequest.getDishId());
        }
        
        return DishOrder.builder()
                .order(order)
                .dish(dish)
                .quantity(dishRequest.getQuantity())
                .status(DishOrderStatus.CREE)
                .build();
    }


    public Order updateOrderDishes(String reference, OrderStatus newStatus, List<DishOrder> dishes) {
        // Récupération de la commande
        Order order = findOrderByReference(reference);

        // Validation
        validateOrderStatus(order);
        validateDishes(dishes);

        // Mise à jour
        updateOrderStatus(order, newStatus, dishes);

        // Sauvegarde
        return saveOrder(order, dishes);
    }

    private Order findOrderByReference(String reference) {
        Order order = orderCrudOperations.findByReference(reference);
        if (order == null) {
            throw new NotFoundException("Order not found with reference: " + reference);
        }
        return order;
    }

    private void validateOrderStatus(Order order) {
        if (order.getActualStatus() != OrderStatus.CREE) {
            throw new IllegalStateException("Order cannot be modified in current status");
        }
    }

    private void validateDishes(List<DishOrder> dishes) {
        for (DishOrder dishOrder : dishes) {
            if (dishOrder.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (dishOrder.getDish() == null || dishCrudOperations.findById(dishOrder.getDish().getId()) == null) {
                throw new NotFoundException("Dish not found");
            }
        }
    }

    private void updateOrderStatus(Order order, OrderStatus newStatus, List<DishOrder> dishes) {
        if (newStatus == OrderStatus.CONFIRME) {
            order.setStatus(OrderStatus.CONFIRME);
            dishes.forEach(dish -> dish.setStatus(DishOrderStatus.CONFIRME));
        } else {
            order.setStatus(newStatus);
        }
    }

    private Order saveOrder(Order order, List<DishOrder> dishes) {
        // 1. Sauvegardez d'abord l'ordre pour obtenir son ID
        Order savedOrder = orderCrudOperations.save(order);
        
        // 2. Initialisez et sauvegardez chaque DishOrder
        List<DishOrder> savedDishes = new ArrayList<>();
        for (DishOrder dish : dishes) {
            // Établissez la relation
            dish.setOrder(savedOrder);
            
            // Initialisez les valeurs par défaut
            if (dish.getStatus() == null) {
                dish.setStatus(DishOrderStatus.CREE);
            }
            
            // Initialisez l'historique si null
            if (dish.getStatusHistory() == null) {
                dish.setStatusHistory(new ArrayList<>());
            }
            
            // Ajoutez le statut initial si l'historique est vide
            if (dish.getStatusHistory().isEmpty()) {
                DishOrderStatusHistory initialStatus = DishOrderStatusHistory.builder()
                    .status(dish.getStatus())
                    .statusDateTime(LocalDateTime.now())
                    .build();
                dish.getStatusHistory().add(initialStatus);
            }
            
            // Sauvegarde séquentielle
            DishOrder savedDish = dishOrderCrudOperations.save(dish);
            savedDishes.add(savedDish);
        }
        
        // 3. Mettez à jour l'ordre avec les DishOrder sauvegardés
        savedOrder.setDishOrders(savedDishes);
        return savedOrder;
    }

    public boolean orderExists(String reference) {
        return orderCrudOperations.existsByReference(reference);
    }
    
    public Order createEmptyOrder(String reference) {
        // Vérifie si la référence existe déjà
        if (existsByReference(reference)) {
            throw new ClientException("Order reference already exists");
        }
    
        // Crée la commande avec statut initial
        Order newOrder = Order.builder()
                .reference(reference)
                .creationDateTime(LocalDateTime.now())
                .status(OrderStatus.CREE)
                .dishOrders(new ArrayList<>()) // Liste vide
                .build();
    
        // Initialise l'historique
        OrderStatusHistory initialStatus = OrderStatusHistory.builder()
                .order(newOrder)
                .status(OrderStatus.CREE)
                .statusDateTime(LocalDateTime.now())
                .build();
        newOrder.setStatusHistory(List.of(initialStatus));
    
        return orderCrudOperations.save(newOrder);
    }

    
    public boolean existsByReference(String reference) {
        return orderCrudOperations.existsByReference(reference);
    }
    
    public Order createOrder(Order order) {
        // Initialise l'historique de statut
        OrderStatusHistory initialStatus = OrderStatusHistory.builder()
                .order(order)
                .status(OrderStatus.CREE)
                .statusDateTime(LocalDateTime.now())
                .build();
    
        order.setStatusHistory(List.of(initialStatus));
        return orderCrudOperations.save(order);
    }
}


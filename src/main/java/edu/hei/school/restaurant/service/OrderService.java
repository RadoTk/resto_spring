package edu.hei.school.restaurant.service;

import edu.hei.school.restaurant.dao.operations.DishCrudOperations;
import edu.hei.school.restaurant.dao.operations.OrderCrudOperations;
import edu.hei.school.restaurant.endpoint.rest.OrderDishRequest;
import edu.hei.school.restaurant.endpoint.rest.UpdateOrderDishesRequest;
import edu.hei.school.restaurant.endpoint.rest.UpdateOrderRequest;
import edu.hei.school.restaurant.model.*;
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
    // Initialisation des historiques manquants
    dishes.forEach(dish -> {
        if (dish.getStatusHistory() == null) {
            dish.setStatusHistory(new ArrayList<>());
            // Ajout d'un statut par défaut si nécessaire
            if (dish.getStatus() != null) {
                dish.getStatusHistory().add(
                    DishOrderStatusHistory.builder()
                        .status(dish.getStatus())
                        .statusDateTime(LocalDateTime.now())
                        .build()
                );
            }
        }
    });
    
    order.setDishOrders(dishes);
    List<Order> savedOrders = orderCrudOperations.saveAll(List.of(order));
    return savedOrders.get(0);
}
}


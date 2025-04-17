package edu.hei.school.restaurant.service;

import edu.hei.school.restaurant.dao.operations.DishCrudOperations;
import edu.hei.school.restaurant.dao.operations.OrderCrudOperations;
import edu.hei.school.restaurant.endpoint.rest.OrderDishRequest;
import edu.hei.school.restaurant.endpoint.rest.UpdateOrderRequest;
import edu.hei.school.restaurant.model.*;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderCrudOperations orderCrudOperations;
    private final DishCrudOperations dishCrudOperations;

    public Order getByReference(String reference) {
        Order order = orderCrudOperations.findByReference(reference);
        if (order == null) {
            throw new NotFoundException("Order not found with reference: " + reference);
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
} 
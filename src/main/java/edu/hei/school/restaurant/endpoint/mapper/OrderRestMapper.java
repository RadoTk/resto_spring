package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.endpoint.rest.OrderDishRest;
import edu.hei.school.restaurant.endpoint.rest.OrderRest;
import edu.hei.school.restaurant.model.Dish;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; 

@Component
public class OrderRestMapper {

    public OrderRest toRest(Order order) {
        return OrderRest.builder()
                .reference(order.getReference())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .dishes(mapDishOrdersToRest(order.getDishOrders()))
                .build();
    }

    private List<OrderDishRest> mapDishOrdersToRest(List<DishOrder> dishOrders) {
        return dishOrders.stream()
                .map(this::mapToOrderDishRest)
                .collect(Collectors.toList());
    }

    private OrderDishRest mapToOrderDishRest(DishOrder dishOrder) {
    Dish dish = dishOrder.getDish();
    if (dish == null) {
        throw new IllegalStateException("Dish is null for DishOrder with ID: " + dishOrder.getId());
    }

    return OrderDishRest.builder()
            .dishName(dish.getName() != null ? dish.getName() : "Unknown Dish")
            .currentPrice(dish.getPrice() != null ? dish.getPrice() : 0.0)
            .quantity(dishOrder.getQuantity())
            .status(dishOrder.getStatus())
            .build();
}
}
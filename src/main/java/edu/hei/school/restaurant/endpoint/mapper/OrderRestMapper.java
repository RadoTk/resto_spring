/*
package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.endpoint.rest.OrderDishRest;
import edu.hei.school.restaurant.endpoint.rest.OrderRest;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderRestMapper {
    private final DishRestMapper dishRestMapper;

    public OrderRest toRest(Order order) {
        return OrderRest.builder()
                .reference(order.getReference())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .dishes(toRestDishes(order.getDishes()))
                .build();
    }

    private List<OrderDishRest> toRestDishes(List<DishOrder> dishes) {
        return dishes.stream()
                .map(this::toRestDish)
                .toList();
    }

    private OrderDishRest toRestDish(DishOrder dishOrder) {
        return OrderDishRest.builder()
                .dishRest(dishRestMapper.toRestSummary(dishOrder.getDish()))
                .quantity(dishOrder.getQuantity())
                .status(dishOrder.getStatus())
                .build();
    }
} 

 */
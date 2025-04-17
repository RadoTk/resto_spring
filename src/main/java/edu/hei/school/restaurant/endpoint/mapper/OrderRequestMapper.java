package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.endpoint.rest.OrderDishRequest;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.Order;
import edu.hei.school.restaurant.service.DishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderRequestMapper {
    private final DishService dishService;

    public DishOrder apply(OrderDishRequest orderDishRequest, Order order) {
        return DishOrder.builder()
                .order(order)
                .dish(dishService.getById(orderDishRequest.getDishId()))
                .quantity(orderDishRequest.getQuantity())
                .build();
    }
}
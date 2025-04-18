package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.endpoint.rest.OrderDishRest;
import edu.hei.school.restaurant.endpoint.rest.OrderRest;
import edu.hei.school.restaurant.model.Dish;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.DishOrderStatus;
import edu.hei.school.restaurant.model.DishOrderStatusHistory;
import edu.hei.school.restaurant.model.Order;
import edu.hei.school.restaurant.model.OrderStatus;
import edu.hei.school.restaurant.model.OrderStatusHistory;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; 

@Component
@RequiredArgsConstructor
public class OrderRestMapper {
    private static final OrderStatus DEFAULT_ORDER_STATUS = OrderStatus.CREE;
    private static final DishOrderStatus DEFAULT_DISH_STATUS = DishOrderStatus.CREE;

    public OrderRest toRest(Order order) {
        return OrderRest.builder()
                .id(order.getId())
                .totalAmount(order.getTotalAmount())
                .actualStatus(getCurrentOrderStatus(order).name()) // .name() convertit l'enum en String
                .dishes(mapDishOrdersToRest(order.getDishOrders()))
                .build();
    }

    private OrderStatus getCurrentOrderStatus(Order order) {
        if (order.getStatusHistory() != null && !order.getStatusHistory().isEmpty()) {
            return order.getStatusHistory().get(order.getStatusHistory().size() - 1).getStatus();
        }
        return DEFAULT_ORDER_STATUS;
    }


    private List<OrderDishRest> mapDishOrdersToRest(List<DishOrder> dishOrders) {
        if (dishOrders == null) {
            return List.of();
        }
        
        return dishOrders.stream()
                .map(this::mapToOrderDishRest)
                .collect(Collectors.toList());
    }

    private OrderDishRest mapToOrderDishRest(DishOrder dishOrder) {
        return OrderDishRest.builder()
                .id(dishOrder.getId())
                .name(dishOrder.getDish().getName())
                .quantity(dishOrder.getQuantity())
                .actualOrderStatus(getCurrentDishStatus(dishOrder).name()) // .name() convertit l'enum en String
                .build();
    }

    
    private DishOrderStatus getCurrentDishStatus(DishOrder dishOrder) {
        if (dishOrder.getStatusHistory() != null && !dishOrder.getStatusHistory().isEmpty()) {
            return dishOrder.getStatusHistory().get(dishOrder.getStatusHistory().size() - 1).getStatus();
        }
        return DEFAULT_DISH_STATUS;
    }

    private OrderStatusHistory createInitialOrderStatus(Order order) {
    return OrderStatusHistory.builder()
            .order(order)
            .status(OrderStatus.CREE)
            .statusDateTime(order.getCreationDateTime() != null ? 
                order.getCreationDateTime() : 
                LocalDateTime.now())
            .build();
}

private DishOrderStatusHistory createInitialDishStatus(DishOrder dishOrder, Order order) {
    return DishOrderStatusHistory.builder()
            .dishOrder(dishOrder)
            .status(DishOrderStatus.CREE)
            .statusDateTime(order.getCreationDateTime() != null ? 
                order.getCreationDateTime() : 
                LocalDateTime.now())
            .build();
}
}
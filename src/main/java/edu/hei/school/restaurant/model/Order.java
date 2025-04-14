package edu.hei.school.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Order {
    private String reference;
    private OrderStatus status;
    private List<DishOrder> dishes;

    public Double getTotalAmount() {
        return dishes.stream()
                .mapToDouble(dishOrder -> 
                    dishOrder.getDish().getTotalPrice() * dishOrder.getQuantity())
                .sum();
    }

    public void confirm() {
        if (OrderStatus.CREATED.equals(status)) {
            status = OrderStatus.CONFIRMED;
            dishes.forEach(dishOrder -> {
                if (DishOrderStatus.CREATED.equals(dishOrder.getStatus())) {
                    dishOrder.setStatus(DishOrderStatus.CONFIRMED);
                }
            });
        }
    }
} 
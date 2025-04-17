package edu.hei.school.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Order {
    private Long id;
    private String reference;
    private LocalDateTime creationDateTime;
    private OrderStatus status;
    private List<OrderStatusHistory> statusHistory;
    private List<DishOrder> dishOrders;

    public Order(Long id) {
        this.id = id;
    }

    public Double getTotalAmount() {
        if (dishOrders == null) {
            return 0.0;
        }
    
        return dishOrders.stream()
                .mapToDouble(dishOrder -> {
                    Dish dish = dishOrder.getDish();
                    if (dish == null || dish.getPrice() == null) {
                        throw new IllegalStateException(
                            "Invalid dish data in order: " + this.reference + 
                            ", DishOrder ID: " + dishOrder.getId());
                    }
                    return dish.getPrice() * dishOrder.getQuantity();
                })
                .sum();
    }
    
    public void confirm() {
        if (OrderStatus.CREE.equals(status)) {
            updateStatus(OrderStatus.CONFIRME);
            dishOrders.forEach(dishOrder -> {
                if (DishOrderStatus.CREE.equals(dishOrder.getStatus())) {
                    dishOrder.updateStatus(DishOrderStatus.CONFIRME);
                }
            });
        }
    }

    public void updateStatus(OrderStatus newStatus) {
        if (canTransitionTo(newStatus)) {
            OrderStatusHistory statusHistoryEntry = new OrderStatusHistory();
            statusHistoryEntry.setStatus(newStatus);
            statusHistoryEntry.setStatusDateTime(LocalDateTime.now());
            this.statusHistory.add(statusHistoryEntry);
            this.status = newStatus;
        } else {
            throw new IllegalStateException(
                "Transition interdite de " + this.status + " vers " + newStatus);
        }
    }

    private boolean canTransitionTo(OrderStatus newStatus) {
        if (this.status == null) return newStatus == OrderStatus.CREE;
        
        return switch (this.status) {
            case CREE -> newStatus == OrderStatus.CONFIRME;
            case CONFIRME -> {
                boolean allDishesConfirmed = dishOrders.stream()
                    .allMatch(d -> d.getStatus() == DishOrderStatus.CONFIRME);
                yield allDishesConfirmed && newStatus == OrderStatus.EN_PREPARATION;
            }
            case EN_PREPARATION -> {
                boolean allDishesFinished = dishOrders.stream()
                    .allMatch(d -> d.getStatus() == DishOrderStatus.TERMINE);
                yield allDishesFinished && newStatus == OrderStatus.TERMINE;
            }
            case TERMINE -> {
                boolean allDishesServed = dishOrders.stream()
                    .allMatch(d -> d.getStatus() == DishOrderStatus.SERVI);
                yield allDishesServed && newStatus == OrderStatus.SERVI;
            }
            case SERVI -> false;
        };
    }

    public OrderStatus getActualStatus() {
        return statusHistory.isEmpty() ? null : 
               statusHistory.get(statusHistory.size() - 1).getStatus();
    }
}
package edu.hei.school.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class DishOrder {
    private Long id;
    private Order order;
    private Dish dish;
    private Integer quantity;
    private DishOrderStatus status;

    public void updateStatus(DishOrderStatus newStatus) {
        if (canTransitionTo(newStatus)) {
            this.status = newStatus;
        } else {
            throw new IllegalStateException("Cannot transition from " + this.status + " to " + newStatus);
        }
    }

    private boolean canTransitionTo(DishOrderStatus newStatus) {
        if (this.status == null) return true;
        
        return switch (this.status) {
            case CREATED -> newStatus == DishOrderStatus.CONFIRMED;
            case CONFIRMED -> newStatus == DishOrderStatus.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == DishOrderStatus.FINISHED;
            case FINISHED -> newStatus == DishOrderStatus.DELIVERED;
            case DELIVERED -> false;
        };
    }
} 
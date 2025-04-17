package edu.hei.school.restaurant.model;

import java.time.LocalDateTime;
import java.util.List;

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
    private List<DishOrderStatusHistory> statusHistory;

    public void updateStatus(DishOrderStatus newStatus) {
        if (canTransitionTo(newStatus)) {
            DishOrderStatusHistory statusHistoryEntry = new DishOrderStatusHistory();
            statusHistoryEntry.setStatus(newStatus);
            statusHistoryEntry.setStatusDateTime(LocalDateTime.now());
            this.statusHistory.add(statusHistoryEntry);
            this.status = newStatus;
        } else {
            throw new IllegalStateException(
                "Transition interdite de " + this.status + " vers " + newStatus);
        }
    }

    private boolean canTransitionTo(DishOrderStatus newStatus) {
        if (this.status == null) return newStatus == DishOrderStatus.CREE;
        
        return switch (this.status) {
            case CREE -> newStatus == DishOrderStatus.CONFIRME;
            case CONFIRME -> newStatus == DishOrderStatus.EN_PREPARATION;
            case EN_PREPARATION -> newStatus == DishOrderStatus.TERMINE;
            case TERMINE -> newStatus == DishOrderStatus.SERVI;
            case SERVI -> false;
        };
    }

    public DishOrderStatus getActualStatus() {
        return statusHistory.isEmpty() ? null : 
               statusHistory.get(statusHistory.size() - 1).getStatus();
    }
}
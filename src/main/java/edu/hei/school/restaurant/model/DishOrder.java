package edu.hei.school.restaurant.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    // Initialisation si null
    if (this.status == null) {
        this.status = DishOrderStatus.CREE;
    }
    
    if (!canTransitionTo(newStatus)) {
        throw new IllegalStateException(
            "Transition interdite de " + this.status + " vers " + newStatus);
    }
    
    DishOrderStatusHistory historyEntry = new DishOrderStatusHistory();
    historyEntry.setStatus(newStatus);
    historyEntry.setStatusDateTime(LocalDateTime.now());
    
    if (this.statusHistory == null) {
        this.statusHistory = new ArrayList<>();
    }
    this.statusHistory.add(historyEntry);
    this.status = newStatus;
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
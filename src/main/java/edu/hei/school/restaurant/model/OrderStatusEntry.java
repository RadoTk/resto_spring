package edu.hei.school.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusEntry {
    private Long id;
    private Long orderId;
    private OrderStatus status;
    private Instant statusDatetime;

    public OrderStatusEntry(Long orderId, OrderStatus status) {
        this.orderId = orderId;
        this.status = status;
        this.statusDatetime = Instant.now();
    }

    @Override
    public String toString() {
        return "OrderStatusEntry{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", status=" + status +
                ", statusDatetime=" + statusDatetime +
                '}';
    }
} 
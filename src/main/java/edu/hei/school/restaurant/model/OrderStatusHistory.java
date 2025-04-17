package edu.hei.school.restaurant.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class OrderStatusHistory {
    private Long id;
    private Order order;
    private OrderStatus status;
    private LocalDateTime statusDateTime;
}
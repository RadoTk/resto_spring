package edu.hei.school.restaurant.model;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishOrderStatusHistory {
    private Long id;
    private DishOrder dishOrder;
    private DishOrderStatus status;
    private LocalDateTime statusDateTime;
}
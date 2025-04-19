
package edu.hei.school.restaurant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishOrderWithTimestamps {
    private Long dishId;
    private String dishName;
    private String orderReference;
    private Integer quantityOrdered;
    private LocalDateTime inPreparationDate;
    private LocalDateTime finishedDate;
}
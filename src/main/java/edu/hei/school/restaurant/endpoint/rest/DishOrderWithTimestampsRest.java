package edu.hei.school.restaurant.endpoint.rest;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishOrderWithTimestampsRest {
    private Long dishId;
    private String dishName;
    private String orderReference;
    private Integer quantityOrdered;
    private LocalDateTime inPreparationDate;
    private LocalDateTime finishedDate;
}
package edu.hei.school.restaurant.endpoint.rest;

import edu.hei.school.restaurant.model.StockMovementType;
import edu.hei.school.restaurant.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStockMovementRequest {
    private Double quantity;
    private Unit unit;
    private StockMovementType type;
    private LocalDateTime creationDatetime;
} 
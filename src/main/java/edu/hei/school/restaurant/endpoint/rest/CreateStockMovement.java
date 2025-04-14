package edu.hei.school.restaurant.endpoint.rest;

import java.time.Instant;
import java.time.LocalDateTime;

import edu.hei.school.restaurant.model.StockMovementType;
import edu.hei.school.restaurant.model.Unit;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CreateStockMovement {
    private Double quantity;
    private Unit unit;
    private StockMovementType movementType;
    private LocalDateTime creationDateTime;
} 
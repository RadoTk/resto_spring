package edu.hei.school.restaurant.endpoint.rest;

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
} 
// DishOrderService.java
package edu.hei.school.restaurant.service;

import edu.hei.school.restaurant.dao.operations.DishOrderCrudOperations;
import edu.hei.school.restaurant.model.DishOrderWithTimestamps;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class DishOrderService {
    private final DishOrderCrudOperations dishOrderCrudOperations;

    public List<DishOrderWithTimestamps> findAllWithTimestamps(int page, int size) {
        return dishOrderCrudOperations.findAllWithTimestamps(page, size);
    }
}
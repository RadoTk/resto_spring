// DishOrderWithTimestampsMapper.java
package edu.hei.school.restaurant.endpoint.mapper;

import edu.hei.school.restaurant.endpoint.rest.DishOrderWithTimestampsRest;
import edu.hei.school.restaurant.model.DishOrderWithTimestamps;
import org.springframework.stereotype.Component;

@Component
public class DishOrderWithTimestampsMapper {
    public DishOrderWithTimestampsRest toRest(DishOrderWithTimestamps domain) {
        return DishOrderWithTimestampsRest.builder()
                .dishId(domain.getDishId())
                .dishName(domain.getDishName())
                .orderReference(domain.getOrderReference())
                .quantityOrdered(domain.getQuantityOrdered())
                .inPreparationDate(domain.getInPreparationDate())
                .finishedDate(domain.getFinishedDate())
                .build();
    }
}
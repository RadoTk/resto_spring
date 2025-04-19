// DishOrderRestController.java
package edu.hei.school.restaurant.endpoint;

import edu.hei.school.restaurant.endpoint.mapper.DishOrderWithTimestampsMapper;
import edu.hei.school.restaurant.endpoint.rest.DishOrderWithTimestampsRest;
import edu.hei.school.restaurant.service.DishOrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dishOrders")
@AllArgsConstructor
public class DishOrderRestController {
    private final DishOrderService dishOrderService;
    private final DishOrderWithTimestampsMapper mapper;

    @GetMapping
    public List<DishOrderWithTimestampsRest> getAllWithTimestamps(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return dishOrderService.findAllWithTimestamps(page, size).stream()
                .map(mapper::toRest)
                .collect(Collectors.toList());
    }
}
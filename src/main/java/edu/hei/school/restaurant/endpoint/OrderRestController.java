
/*
package edu.hei.school.restaurant.endpoint;

import edu.hei.school.restaurant.endpoint.mapper.OrderRestMapper;
import edu.hei.school.restaurant.endpoint.rest.OrderRest;
import edu.hei.school.restaurant.endpoint.rest.UpdateDishStatusRequest;
import edu.hei.school.restaurant.endpoint.rest.UpdateOrderRequest;
import edu.hei.school.restaurant.service.OrderService;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
public class OrderRestController {
    private final OrderService orderService;
    private final OrderRestMapper orderRestMapper;

    @GetMapping("/orders/{reference}")
    public ResponseEntity<Object> getOrder(@PathVariable String reference) {
        try {
            OrderRest orderRest = orderRestMapper.toRest(
                    orderService.getByReference(reference));
            return ResponseEntity.ok().body(orderRest);
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/orders/{reference}/dishes")
    public ResponseEntity<Object> updateOrderDishes(
            @PathVariable String reference,
            @RequestBody UpdateOrderRequest request) {
        try {
            OrderRest orderRest = orderRestMapper.toRest(
                    orderService.updateDishes(reference, request));
            return ResponseEntity.ok().body(orderRest);
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PutMapping("/orders/{reference}/dishes/{dishId}")
    public ResponseEntity<Object> updateDishStatus(
            @PathVariable String reference,
            @PathVariable Long dishId,
            @RequestBody UpdateDishStatusRequest request) {
        try {
            OrderRest orderRest = orderRestMapper.toRest(
                    orderService.updateDishStatus(reference, dishId, request.getStatus()));
            return ResponseEntity.ok().body(orderRest);
        } catch (NotFoundException e) {
            return ResponseEntity.status(NOT_FOUND).body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
} 

 */
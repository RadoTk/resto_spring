

package edu.hei.school.restaurant.endpoint;

import edu.hei.school.restaurant.dao.mapper.DishOrderMapper;
import edu.hei.school.restaurant.endpoint.mapper.OrderRequestMapper;
import edu.hei.school.restaurant.endpoint.mapper.OrderRestMapper;
import edu.hei.school.restaurant.endpoint.rest.OrderRest;
import edu.hei.school.restaurant.endpoint.rest.UpdateDishStatusRequest;
import edu.hei.school.restaurant.endpoint.rest.UpdateOrderDishesRequest;
import edu.hei.school.restaurant.endpoint.rest.UpdateOrderRequest;
import edu.hei.school.restaurant.model.DishOrder;
import edu.hei.school.restaurant.model.Order;
import edu.hei.school.restaurant.model.OrderStatus;
import edu.hei.school.restaurant.service.OrderService;
import edu.hei.school.restaurant.service.exception.ClientException;
import edu.hei.school.restaurant.service.exception.NotFoundException;
import edu.hei.school.restaurant.service.exception.ServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class OrderRestController {
    private final OrderService orderService;
    private final OrderRestMapper orderRestMapper;
    private final OrderRequestMapper orderRequestMapper;


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
public ResponseEntity<OrderRest> updateOrderDishes(
    @PathVariable String reference,
    @RequestBody UpdateOrderDishesRequest request) {

// Conversion DTO → Modèle
Order order = orderService.getByReference(reference);
List<DishOrder> dishOrders = request.getDishes().stream()
        .map(orderDishRequest -> orderRequestMapper.apply(orderDishRequest, order))
        .collect(Collectors.toList());

// Appel service
Order updatedOrder = orderService.updateOrderDishes(
    reference,
    request.getStatus(),
    dishOrders
);

// Conversion Modèle → DTO
return ResponseEntity.ok(orderRestMapper.toRest(updatedOrder));
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

    @PostMapping("/orders/{reference}")
    public ResponseEntity<?> createEmptyOrder(@PathVariable String reference) {
        try {
            Order createdOrder = orderService.createEmptyOrder(reference);
            return ResponseEntity.ok(orderRestMapper.toRest(createdOrder));
        } catch (ClientException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ServerException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
} 

 
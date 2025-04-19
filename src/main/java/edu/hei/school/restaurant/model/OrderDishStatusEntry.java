package edu.hei.school.restaurant.model;


import java.time.Instant;

public class OrderDishStatusEntry {
    private Long id;
    private Long orderDishId;
    private OrderDishStatus status;
    private Instant statusDatetime;

    public OrderDishStatusEntry() {
    }

    public OrderDishStatusEntry(Long id, Long orderDishId, OrderDishStatus status, Instant statusDatetime) {
        this.id = id;
        this.orderDishId = orderDishId;
        this.status = status;
        this.statusDatetime = statusDatetime;
    }

    public OrderDishStatusEntry(Long orderDishId, OrderDishStatus status) {
        this.orderDishId = orderDishId;
        this.status = status;
        this.statusDatetime = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrderDishId() {
        return orderDishId;
    }

    public void setOrderDishId(Long orderDishId) {
        this.orderDishId = orderDishId;
    }

    public OrderDishStatus getStatus() {
        return status;
    }

    public void setStatus(OrderDishStatus status) {
        this.status = status;
    }

    public Instant getStatusDatetime() {
        return statusDatetime;
    }

    public void setStatusDatetime(Instant statusDatetime) {
        this.statusDatetime = statusDatetime;
    }

    @Override
    public String toString() {
        return "OrderDishStatusEntry{" +
                "id=" + id +
                ", orderDishId=" + orderDishId +
                ", status=" + status +
                ", statusDatetime=" + statusDatetime +
                '}';
    }
} 
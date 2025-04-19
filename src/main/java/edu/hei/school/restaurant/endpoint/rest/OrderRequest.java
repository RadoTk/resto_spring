package edu.hei.school.restaurant.endpoint.rest;

import java.util.List;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private String reference;
    private List<DishOrderRequest> dishOrderRequests;
}
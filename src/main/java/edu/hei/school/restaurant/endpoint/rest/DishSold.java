package edu.hei.school.restaurant.endpoint.rest;



import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DishSold {
    private Long dishIdentifier;
    private String dishName;
    private int quantitySold;

    // Constructors

}
package com.example.Surisuri_Masuri.orders.model.dto.response;

import com.example.Surisuri_Masuri.product.model.Product;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Builder
@Getter
public class OrdersListRes {
    ProductDtoRes productDtoRes;
    Date createdDate;
    Long totalPrice;
    String payMethod;
    String deliveryStatus;
    String merchantUid;
}

package com.example.Surisuri_Masuri.cart.model.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CartListRes {
    Long cartIdx;
    String productName;
    Integer price;
    Integer productQuantity;
}

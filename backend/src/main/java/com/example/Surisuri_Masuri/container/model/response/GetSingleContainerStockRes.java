package com.example.Surisuri_Masuri.container.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSingleContainerStockRes {

    private String containerName;
    private String productName;
    private Long productQuantity;
    private LocalDate expiredAt;


}

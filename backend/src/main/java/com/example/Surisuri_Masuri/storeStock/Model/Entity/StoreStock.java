package com.example.Surisuri_Masuri.storeStock.Model.Entity;

import com.example.Surisuri_Masuri.product.model.Product;
import com.example.Surisuri_Masuri.store.Model.Entity.Store;
import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoreStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false)
    private Long stockQuantitiy;

    @Column(nullable = false)
    private LocalDate expiredAt;

    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Date updatedAt;

    @Column(nullable = false)
    private Boolean isDiscarded; // 폐기 여부

    private LocalDate discardedAt; // 폐기 일자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storeIdx")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productIdx")
    private Product product;
}

package com.example.Surisuri_Masuri.product.service;

import com.example.Surisuri_Masuri.common.BaseResponse;
import com.example.Surisuri_Masuri.exception.EntityException.ManagerException;
import com.example.Surisuri_Masuri.exception.EntityException.ProductException;
import com.example.Surisuri_Masuri.exception.ErrorCode;
import com.example.Surisuri_Masuri.product.model.Product;
import com.example.Surisuri_Masuri.product.model.dto.request.ProductCreateReq;
import com.example.Surisuri_Masuri.product.model.dto.request.ProductUpdateReq;
import com.example.Surisuri_Masuri.product.model.dto.response.ProductListRes;
import com.example.Surisuri_Masuri.product.model.dto.response.ProductReadRes;
import com.example.Surisuri_Masuri.product.model.dto.response.ProductSearchRes;
import com.example.Surisuri_Masuri.product.repository.ProductRepository;
import com.example.Surisuri_Masuri.store.Model.Entity.Store;
import com.example.Surisuri_Masuri.store.Model.ResDtos.StoreReadRes;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public BaseResponse create(ProductCreateReq req) {

        Product product = productRepository.save(Product.builder()
                .productName(req.getProductName())
                .isItFood(req.getIsItFood())
                .expiredAt(req.getExpiredAt())
                .price(req.getPrice())
                .build());

        return BaseResponse.successResponse("요청 성공", null);
    }

    public BaseResponse search(String productName) {
        Optional<Product> result = productRepository.findByProductName(productName);

        if(result.isPresent()) {
            Product product = result.get();
            ProductSearchRes res = ProductSearchRes.builder()
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .createdAt(product.getCreatedAt())
                    .updatedAt(product.getUpdatedAt())
                    .build();
            return BaseResponse.successResponse("상품 검색 성공", res);
        }

        else
            throw new ProductException(ErrorCode.ProductSearch_002,
                    String.format("존재하지 않는 상품 정보"));
    }

    public BaseResponse list(Integer page, Integer size) {

        Pageable pageable = PageRequest.of(page-1,size);

        Page<Product> result = productRepository.findList(pageable);

        List<ProductReadRes> productReadResList = new ArrayList<>();

        for (Product product : result.getContent()) {

            ProductReadRes productReadRes = ProductReadRes
                    .builder()
                    .productIdx(product.getIdx())
                    .productName(product.getProductName())
                    .price(product.getPrice())
                    .build();

            productReadResList.add(productReadRes);
        }

        return BaseResponse.successResponse("상품 리스트 검색 성공", productReadResList);

    }

    public BaseResponse update(ProductUpdateReq req) {
        Optional<Product> result = productRepository.findById(req.getIdx());

        if (result.isPresent()) {
            Product product = result.get();
            if (req.getProductName() != null) {
                product.setProductName(req.getProductName());
            }
            if (req.getPrice() != null) {
                product.setPrice(req.getPrice());
            }
            productRepository.save(product);

            return BaseResponse.successResponse("상품 수정 성공", null);
        }

        else throw new ProductException(ErrorCode.ProductUpdate_002, String.format("존재하지 않는 상품 정보"));

    }

    public BaseResponse delete(Long idx) {
        Optional<Product> result = productRepository.findById(idx);

        if(result.isPresent()) {
            Product product = result.get();

            productRepository.delete(product);

            return BaseResponse.successResponse("상품 삭제 성공", null);
        }

        else throw new ProductException(ErrorCode.ProductDelete_002, String.format("존재하지 않는 상품 정보"));
    }
}

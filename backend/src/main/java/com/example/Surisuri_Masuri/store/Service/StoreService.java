package com.example.Surisuri_Masuri.store.Service;

import com.example.Surisuri_Masuri.common.BaseResponse;
import com.example.Surisuri_Masuri.exception.EntityException.ContainerException;
import com.example.Surisuri_Masuri.exception.EntityException.StoreException;
import com.example.Surisuri_Masuri.exception.EntityException.UserException;
import com.example.Surisuri_Masuri.exception.ErrorCode;
import com.example.Surisuri_Masuri.jwt.JwtUtils;
import com.example.Surisuri_Masuri.member.Model.Entity.Manager;
import com.example.Surisuri_Masuri.member.Repository.ManagerRepository;
import com.example.Surisuri_Masuri.store.Model.Entity.Store;
import com.example.Surisuri_Masuri.store.Model.ReqDtos.StoreCreateReq;
import com.example.Surisuri_Masuri.store.Model.ReqDtos.StoreSearchReq;
import com.example.Surisuri_Masuri.store.Model.ResDtos.StoreCreateRes;
import com.example.Surisuri_Masuri.store.Model.ResDtos.StoreReadRes;
import com.example.Surisuri_Masuri.store.Repository.StoreRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    private final ManagerRepository managerRepository;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private int expiredTimeMs;

    StoreReadRes storeReadRes;
    StoreCreateRes storeCreateRes;



    // 가맹점 등록
    public BaseResponse<StoreCreateRes> StoreCreate(String token, StoreCreateReq storeCreateReq) {

        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        Date create = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
        Date update = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());

        token = JwtUtils.replaceToken(token);

        String managerId = JwtUtils.getManagerInfo(token, secretKey);

        Optional<Manager> manager = managerRepository.findByManagerId(managerId);

        if (manager.isPresent()) {

            storeRepository.save(Store.builder()
                    .storeUuid(storeCreateReq.getStoreUuid())
                    .storeName(storeCreateReq.getStoreName())
                    .createdAt(create)
                    .updatedAt(update)
                    .build());

            storeCreateRes = StoreCreateRes
                    .builder()
                    .storeUuid(storeCreateReq.getStoreUuid())
                    .storeName(storeCreateReq.getStoreName())
                    .build();

            return BaseResponse.successResponse("요청 성공했습니다.", storeCreateRes);
        }
        else {
            throw new UserException(ErrorCode.UNREGISTERD_USER_VALUE,
                    String.format("가입되지 않은 본사 관리자입니다."));
        }
    }

    // 가맹점 리스트 조회
    public BaseResponse<List<StoreReadRes>> StoreList(String token, Integer page, Integer size) {

        token = JwtUtils.replaceToken(token);

        Claims managerInfo = JwtUtils.getManagerInfo2(token, secretKey);

        String managerId = managerInfo.get("managerId", String.class);

        Optional<Manager> manager = managerRepository.findByManagerId(managerId);
        if (manager.isPresent()){

        Pageable pageable = PageRequest.of(page-1,size);

        Page<Store> result = storeRepository.findList(pageable);

        List<StoreReadRes> storeSearchResList = new ArrayList<>();

        for (Store store : result.getContent()) {

            storeReadRes = StoreReadRes
                    .builder()
                    .storeName(store.getStoreName())
                    .storePhoneNo(store.getStorePhoneNo())
                    .userPhoneNo(store.getUser().getUserPhoneNo())
                    .storeAddr(store.getStoreAddr())
                    .storeUuid(store.getStoreUuid())
                    .build();

            storeSearchResList.add(storeReadRes);
        }
        // DtoToRes
        return BaseResponse.successResponse("요청 성공했습니다.", storeSearchResList);
        }
        else {
            throw new UserException(ErrorCode.UNREGISTERD_USER_VALUE,
                    String.format("가입되지 않은 본사 관리자입니다."));
        }
    }

    // 가맹점 단일 조회
    public BaseResponse<StoreReadRes> StoreSearch(String token, StoreSearchReq storeSearchReq) {

        token = JwtUtils.replaceToken(token);

        Claims managerInfo = JwtUtils.getManagerInfo2(token, secretKey);

        String managerId = managerInfo.get("managerId", String.class);

        Optional<Manager> manager = managerRepository.findByManagerId(managerId);

        if (manager.isPresent()) {
            Optional<Store> store = storeRepository.findByStoreName(storeSearchReq.getStoreName());
            if (store.isPresent()) {
                Store store2 = store.get();
                if (store2.getStorePhoneNo() == null) {
                    throw new StoreException(ErrorCode.StoreSearch_004,
                            String.format("가맹점 정보가 등록되었으나 가맹점 관리자가 가입이 되지 않았습니다."));
                }
                storeReadRes = StoreReadRes
                        .builder()
                        .storeName(store2.getStoreName())
                        .storePhoneNo(store2.getStorePhoneNo())
                        .userPhoneNo(store2.getUser().getUserPhoneNo())
                        .storeAddr(store2.getStoreAddr())
                        .storeUuid(store2.getStoreUuid())
                        .build();
            } else
                throw new StoreException(ErrorCode.StoreSearch_003,
                        String.format("가맹점 검색 결과가 존재하지 않습니다"));

            return BaseResponse.successResponse("요청 성공했습니다.", storeReadRes);
        }  else {
            throw new UserException(ErrorCode.UNREGISTERD_USER_VALUE,
                    String.format("가입되지 않은 본사 관리자입니다."));
        }
    }
}

package com.example.Surisuri_Masuri.member.Service;

import com.example.Surisuri_Masuri.common.BaseResponse;
import com.example.Surisuri_Masuri.email.Model.SendEmailReq;
import com.example.Surisuri_Masuri.email.Service.EmailService;
import com.example.Surisuri_Masuri.jwt.JwtUtils;
import com.example.Surisuri_Masuri.member.Model.Entity.User;
import com.example.Surisuri_Masuri.member.Model.ReqDtos.*;
import com.example.Surisuri_Masuri.member.Model.ResDtos.*;
import com.example.Surisuri_Masuri.member.Repository.UserRepository;
import com.example.Surisuri_Masuri.store.Model.Entity.Store;
import com.example.Surisuri_Masuri.store.Repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final StoreRepository storeRepository;

    private final EmailService emailService;

    User compare1;
    User compare2;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.token.expired-time-ms}")
    private int expiredTimeMs;

    LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    Date create = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
    Date update = Date.from(localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());

    UserSignUpRes userSignUpRes;

    // 회원가입 기능
    public BaseResponse<UserSignUpRes> UserSignUp(UserSignUpReq userSignUpReq) {

        // 1. 이메일을 통해 이미 존재하는 회원인지 확인
        if (userRepository.findByUserEmail(userSignUpReq.getUserEmail()).isPresent()) {
            return BaseResponse.failResponse(7000, "중복된 회원이 있습니다.");
        }

        // 2. storeUuid를 통해 이미 본사와 계약이 체결되어 uuid가 발급된 회원인지 확인
        else if (storeRepository.findByStoreUuid(userSignUpReq.getStoreUuid()).isPresent()) {

            // 3. storeUuid를 통해 Entity를 찾고, Store 및 User 엔티티의 정보를 수정해서 저장 // 가입되는 과정
            Optional<Store> store = storeRepository.findByStoreUuid(userSignUpReq.getStoreUuid());
            if (store.isPresent()) {

                User user = User.builder()
                        .userName(userSignUpReq.getUserName())
                        .userEmail(userSignUpReq.getUserEmail())
                        .userPassword(passwordEncoder.encode(userSignUpReq.getUserPassword()))
                        .userPhoneNo(userSignUpReq.getUserPhoneNo())
                        .userAuthority("User")
                        .status(false)
                        .createdAt(create)
                        .updatedAt(update)
                        .build();

                userRepository.save(user);

                Store store2 = store.get();

                store2.setStoreAddr(userSignUpReq.getStoreAddr());
                store2.setStorePhoneNo(userSignUpReq.getUserPhoneNo());
                store2.setCreatedAt(create);
                store2.setUpdatedAt(update);
                store2.setUser(user);

                storeRepository.save(store2);

                // 3. AccessToken을 생성하여
                String accessToken = JwtUtils.generateAccessToken(user, secretKey, expiredTimeMs);

                // 4. 이메일에 포함시켜 사용자에게 전달하여 이메일 인증을 요청
                SendEmailReq sendEmailReq = SendEmailReq.builder()
                        .email(user.getUserEmail())
                        .authority(user.getUserAuthority())
                        .accessToken(accessToken)
                        .build();

                // 5. 이메일 전송
                emailService.sendEmail(sendEmailReq);

                // 6. 응답 Dto 생성을 위한 과정
                Optional<User> result = userRepository.findByUserEmail(user.getUserEmail());

                if (result.isPresent()) {
                    user = result.get();
                }

                userSignUpRes = UserSignUpRes.builder()
                        .userName(user.getUsername())
                        .userEmail(user.getUserEmail())
                        .userPhoneNo(user.getUserPhoneNo())
                        .storeUuid(store2.getStoreUuid())
                        .storeAddr(store2.getStoreAddr())
                        .storePhoneNo(store2.getStorePhoneNo())
                        .status(false)
                        .build();

            }
        }
        return BaseResponse.successResponse("이메일 인증 대기중...", userSignUpRes);
    }

    // 로그인 기능
    public BaseResponse<LoginRes> UserLogin(LoginReq userLoginReq) {
        LoginRes loginRes = null;
        Optional<User> user = userRepository.findByUserEmail(userLoginReq.getId());
        if (user.isEmpty()) {
            return BaseResponse.failResponse(7000, "가입되지 않은 회원입니다.");
        } else if (user.isPresent() && passwordEncoder.matches(userLoginReq.getPassword(), user.get().getPassword()))
            ;
        {
            loginRes = LoginRes.builder()
                    .jwtToken(JwtUtils.generateAccessToken(user.get(), secretKey, expiredTimeMs))
                    .build();
        }
        return BaseResponse.successResponse("정상적으로 로그인 되었습니다.", loginRes);
    }

    // 이메일 찾기 기능
    public BaseResponse<UserFindEmailRes> findEmail(UserFindEmailReq userFindEmailReq) {
        compare1 = userRepository.findByUserName(userFindEmailReq.getUserName()).get();
        compare2 = userRepository.findByUserPhoneNo(userFindEmailReq.getUserPhoneNo()).get();

        if (compare1.equals(compare2)) {
            UserFindEmailRes userFindEmailRes = UserFindEmailRes
                    .builder()
                    .userEmail(compare1.getUserEmail())
                    .build();

            return BaseResponse.successResponse("요청하신 회원 정보입니다.", userFindEmailRes);
        } else
            return BaseResponse.failResponse(7000, "잘못된 정보를 입력하셨습니다.");
    }

    // 회원정보 수정 기능
    public BaseResponse<UserUpdateRes> userUpdate(String token, UserUpdateReq userUpdateReq) {
        token = JwtUtils.replaceToken(token);
        String email = JwtUtils.getUserEmail(token, secretKey);
        Optional<User> user = userRepository.findByUserEmail(email);

        User user3 = user.get();
        Long idx = user3.getIdx(); // store에서 사용하는 키값

        if (user.isPresent()) {
            User user2 = user.get();
            user2.setUserPassword(passwordEncoder.encode(userUpdateReq.getUserPassword()));
            user2.setUserPhoneNo(userUpdateReq.getUserPhoneNo());
            userRepository.save(user2);

            Optional<Store> store = storeRepository.findById(idx);
            Store store2 = store.get();

            store2.setStoreAddr(userUpdateReq.getStoreAddr());
            store2.setStorePhoneNo(userUpdateReq.getUserPhoneNo());
            store2.setCreatedAt(create);
            store2.setUpdatedAt(update);
            storeRepository.save(store2);


            UserUpdateRes userUpdateRes = UserUpdateRes
                    .builder()
                    .userPassword(userUpdateReq.getUserPassword())
                    .storeAddr(userUpdateReq.getStoreAddr())
                    .userPhoneNo(userUpdateReq.getUserPhoneNo())
                    .storePhoneNo(userUpdateReq.getStorePhoneNo())
                    .build();
            BaseResponse baseResponse = BaseResponse.successResponse("수정된 회원정보입니다.", userUpdateRes);


            return baseResponse;
        } else {
            return BaseResponse.failResponse(7000, "요청실패");
        }
    }

    // 회원 비밀번호 찾기 기능
    public BaseResponse<FindUserPasswordRes> findPassword(FindUserPasswordReq findUserPasswordReq) {

        Optional<User> user = userRepository.findByUserEmail(findUserPasswordReq.getUserEmail());
        if (user.isPresent()) {
            User user2 = user.get();
            Long idx = user2.getIdx();
            String userEmail = user2.getUserEmail();
            SendEmailReq sendEmailReq = SendEmailReq.builder()
                    .idx(idx)
                    .email(userEmail)
                    .build();

            // 5. 이메일 전송
            emailService.sendEmail2(sendEmailReq);

            FindUserPasswordRes findUserPasswordRes = FindUserPasswordRes.builder()
                    .idx(idx)
                    .build();

            BaseResponse baseResponse = BaseResponse.successResponse("비밀번호 초기화 이메일 발송이 완료되었습니다.", findUserPasswordRes);

            return baseResponse;

        }

        {
            return BaseResponse.failResponse(7000, "요청실패");
        }
    }

    // 비밀번호 재설정 기능
    public BaseResponse<ResetPasswordRes> resetPassword(Long idx, ResetPasswordReq resetPasswordReq) {
        Optional<User> user = userRepository.findById(idx);
        if (user.isPresent()) {

            User user2= user.get();
            user2.setUserPassword(passwordEncoder.encode(resetPasswordReq.getUserPassword()));
            userRepository.save(user2);

            ResetPasswordRes resetPasswordRes = ResetPasswordRes
                    .builder()
                    .password(resetPasswordReq.getUserPassword())
                    .build();

            BaseResponse baseResponse = BaseResponse.successResponse("비밀번호가 재설정 되었습니다.", resetPasswordRes);

            return baseResponse;
        }

        {
            return BaseResponse.failResponse(7000, "요청실패");
        }
    }

    public User getUserByUserEmail (String email){
        Optional<User> user = userRepository.findByUserEmail(email);

        if (user.isPresent()) {
            return user.get();
        }
        return null;
    }
}


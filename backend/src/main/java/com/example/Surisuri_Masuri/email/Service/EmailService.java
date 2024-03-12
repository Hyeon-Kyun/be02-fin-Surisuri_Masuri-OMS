package com.example.Surisuri_Masuri.email.Service;

import com.example.Surisuri_Masuri.email.Model.EmailConfirmReq;
import com.example.Surisuri_Masuri.email.Model.EmailVerify;
import com.example.Surisuri_Masuri.email.Model.SendEmailReq;
import com.example.Surisuri_Masuri.email.Repository.EmailVerifyRepository;
import com.example.Surisuri_Masuri.member.Model.Entity.User;
import com.example.Surisuri_Masuri.member.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    private final EmailVerifyRepository emailVerifyRepository;

    private final UserRepository userRepository;

    // 이메일 전송 메소드
    public void sendEmail(SendEmailReq sendEmailReq) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sendEmailReq.getEmail());
        message.setSubject("[혼자 만들어보는 백엔드] 이메일 인증");

        // UUID도 생성하여 추가적으로 메일 전송
        String uuid = UUID.randomUUID().toString();
        message.setText("http://localhost:8080/user/confirm?email="
                + sendEmailReq.getEmail()
                + "&authority=" + sendEmailReq.getAuthority()
                + "&token=" + uuid
                + "&jwt=" + sendEmailReq.getAccessToken()
        );
        emailSender.send(message);
        create(sendEmailReq.getEmail(),uuid,sendEmailReq.getAccessToken());
    }

    // 이메일 전송 메소드 - 비밀번호 재설정용
    public void sendEmail2(SendEmailReq sendEmailReq) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(sendEmailReq.getEmail());
        message.setSubject("비밀번호 초기화용 이메일입니다.");

        message.setText("http://localhost:8080/user/resetPassword/"
                + sendEmailReq.getIdx()
        );
        emailSender.send(message);
    }

    // 이메일 전송 후 인증 여부를 저장하기 위한 메소드
    public void create(String email,String uuid, String AccessToken)
    {
        EmailVerify emailVerify = EmailVerify.builder()
                .email(email)
                .uuid(uuid)
                .jwt(AccessToken)
                .build();
        emailVerifyRepository.save(emailVerify);
    }

    // 이메일로 전송된 링크를 검증하기 위한 메소드
    public RedirectView verify(EmailConfirmReq emailConfirmReq) {
        Optional<EmailVerify> result = emailVerifyRepository.findByEmail(emailConfirmReq.getEmail());
        if(result.isPresent()){
            EmailVerify emailVerify = result.get();
            if(emailVerify.getJwt().equals(emailConfirmReq.getJwt()) && emailVerify.getUuid().equals(emailConfirmReq.getToken())) {
                update(emailConfirmReq.getEmail(), emailConfirmReq.getAuthority());
                return new RedirectView("http://www.naver.com");
            }
        }
        return new RedirectView("http://localhost:3000/emailCertError");
    }


    // 검증된 사용자의 status를 변경하기 위한 메소드
    public void update(String email, String authority) {
        if (authority.equals("User")){
            Optional<User> result = userRepository.findByUserEmail(email);
            if(result.isPresent()) {
                User user = result.get();
                user.changeStatus(true);
                userRepository.save(user);
            }
        }
    }
}
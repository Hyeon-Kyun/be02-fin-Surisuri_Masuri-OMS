package com.example.Surisuri_Masuri.member.Controller;

import com.example.Surisuri_Masuri.email.Model.EmailConfirmReq;
import com.example.Surisuri_Masuri.email.Service.EmailService;
import com.example.Surisuri_Masuri.member.Model.ReqDtos.LoginReq;
import com.example.Surisuri_Masuri.member.Model.ReqDtos.ManagerLoginReq;
import com.example.Surisuri_Masuri.member.Model.ReqDtos.ManagerSignUpReq;
import com.example.Surisuri_Masuri.member.Model.ReqDtos.UserSignUpReq;
import com.example.Surisuri_Masuri.member.Service.ManagerService;
import com.example.Surisuri_Masuri.member.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import javax.validation.Valid;

@RestController
@CrossOrigin("*")
@RequiredArgsConstructor
public class ManagerController {

    private final ManagerService managerService;

    // 계정 생성 - Create
    @PostMapping("/manager/register")
    public ResponseEntity ManagerSignUp(@Valid @RequestBody ManagerSignUpReq managerSignUpReq)
    {
        return ResponseEntity.ok().body(managerService.ManagerSignUp(managerSignUpReq));
    }

    // 로그인 기능
    @PostMapping("/manager/login")
    public ResponseEntity ManagerLogin(@RequestBody @Valid ManagerLoginReq managerLoginReq)
    {
        return ResponseEntity.ok().body(managerService.ManagerLogin(managerLoginReq));
    }

}

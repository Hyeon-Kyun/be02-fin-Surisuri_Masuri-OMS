package com.example.Surisuri_Masuri.member.Model.ReqDtos;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginReq {
    String id;
    String password;
}
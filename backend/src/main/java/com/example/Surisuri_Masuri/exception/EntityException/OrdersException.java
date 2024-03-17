package com.example.Surisuri_Masuri.exception.EntityException;

import com.example.Surisuri_Masuri.exception.BusinessException;
import com.example.Surisuri_Masuri.exception.ErrorCode;
import lombok.Getter;

@Getter
public class OrdersException extends BusinessException {

    private ErrorCode errorCode;

    private String message;

    public OrdersException(ErrorCode errorCode, String message) {
        super(errorCode, message);
        this.errorCode = errorCode;
        this.message = message;
    }

}

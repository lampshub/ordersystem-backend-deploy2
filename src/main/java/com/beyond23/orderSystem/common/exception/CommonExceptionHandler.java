package com.beyond23.orderSystem.common.exception;


import com.beyond23.orderSystem.common.dtos.CommonErrorDto;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.NoSuchElementException;

//Controller 어노테이션이 붙어있는 모든 클래스의 예외를 아래 클래스에서 인터셉팅(가로채기).
@RestControllerAdvice   //예외를 캐치하는 Controller
@Hidden //swagger에서 제외 (이걸 제외한 모든  Controller 가 ui 로 만들어짐.
public class CommonExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegal(IllegalArgumentException e) {
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getMessage()).
                build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

//    검증어노테이션(@NotBlank 등)에서 예외가 터지면 .getFieldError().getDefaultMessage() 사용
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> notValidException(MethodArgumentNotValidException e) {
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(400)
                .error_message(e.getFieldError().getDefaultMessage())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<?> NoSuchElement(NoSuchElementException e) {
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(404)
                .error_message(e.getMessage()).
                build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> authorizedException(AuthorizationDeniedException e) {
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(403)
                .error_message(e.getMessage()).
                build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto);
    }



//    위의 에러들을 제외한 나머지 에러 처리(전역 예외처리의 기본형)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)   //Http상태코드 500을 의미
    public CommonErrorDto exception(Exception e) {
        e.printStackTrace();
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(500)
                .error_message(e.getMessage()).
                build();
        return dto;
    }
}
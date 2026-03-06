package com.beyond23.orderSystem.common.exception;


import com.beyond23.orderSystem.common.dtos.CommonErrorDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.PrintWriter;

@Component
public class JwtAuthenticationHandler implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper;

    public JwtAuthenticationHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override   //Controller에서 사용하는 에러 처리와 다름. 토큰필터 에러시 처리.
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        System.out.println("auth 에러 발생");
        authException.printStackTrace();
//        startline + header조립
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);    //401 상태코드
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

//        body세팅
        CommonErrorDto dto = CommonErrorDto.builder()
                .status_code(401)
                .error_message("token이 없거나 유효하지 않습니다.")
                .build();
//        ObjectMapper objectMapper = new ObjectMapper();   //new 할 필요없이 ObjectMapper는 내장되어있는 싱글톤객체로 주입받아 사용하면 됨. 커스텀이 필요할 경우 따로 빈객체 만들어서 사용 (ObjectMapperConfig 파일)
        String data = objectMapper.writeValueAsString(dto); //write : 객체-> json 직렬화
        PrintWriter  printWriter = response.getWriter();
        printWriter.write(data);
        printWriter.flush();
        

    }


}

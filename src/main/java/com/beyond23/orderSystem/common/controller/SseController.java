package com.beyond23.orderSystem.common.controller;

import com.beyond23.orderSystem.common.repository.SseEmitterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/sse")
public class SseController {

    private final SseEmitterRegistry sseEmitterRegistry;
    @Autowired
    public SseController(SseEmitterRegistry sseEmitterRegistry) {
        this.sseEmitterRegistry = sseEmitterRegistry;
    }
    @GetMapping("/connect")
    public SseEmitter connect() throws IOException {
        System.out.println("connect start");
//        key값인 email값 -> 인증객체에서 가져옴
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
//        SseEmitter 안에 (최초의 http req.요청안에 담긴) 사용자 정보 객체를 만듬
        SseEmitter sseEmitter = new SseEmitter(60 * 60 * 1000L);   //1시간 유효시간
        sseEmitterRegistry.addSseEmitter(email, sseEmitter); //sseEmitterRegistry 싱글톤객체에 저장됨.
        sseEmitter.send(SseEmitter.event().name("connect").data("연결완료"));
        return sseEmitter; //여기서 connect요청한 사람은 연결을 지속적으로 받으며 알람을 받게됨(주문시 알람받음)
    }

    @GetMapping("/disconnect")
    public void disconnect() throws IOException {
        System.out.println("disconnect start");
        String email = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        sseEmitterRegistry.removeEmitter(email);
    }

}

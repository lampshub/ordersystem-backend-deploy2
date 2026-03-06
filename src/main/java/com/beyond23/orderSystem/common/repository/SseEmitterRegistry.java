package com.beyond23.orderSystem.common.repository;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseEmitterRegistry {
//    key에는 email 넣을거고, SseEmitter객체는 사용자의 연결정보(ip, macaddress 등)을 의미 => 저장소
//    ConcurrentHashMap : thread-safe(syncronized 동시성 이슈 발생X)
    private Map<String, SseEmitter> emitterMap = new ConcurrentHashMap<>();

    public void addSseEmitter(String email, SseEmitter sseEmitter) throws IOException {
        this.emitterMap.put(email, sseEmitter);
        System.out.println(this.emitterMap.size());
    }

    public SseEmitter getEmitter(String email){ //여기서 email은 receiver

        return this.emitterMap.get(email);  //key값으로 get
    }

    public void removeEmitter(String email){
        this.emitterMap.remove(email);
        System.out.println(this.emitterMap.size());

    }
}

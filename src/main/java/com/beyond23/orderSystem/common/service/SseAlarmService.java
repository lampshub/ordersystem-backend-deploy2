package com.beyond23.orderSystem.common.service;

import com.beyond23.orderSystem.common.dtos.SseMessageDto;
import com.beyond23.orderSystem.common.repository.SseEmitterRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@Component
public class SseAlarmService implements MessageListener {
    private final SseEmitterRegistry sseEmitterRegistry;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    @Autowired
    public SseAlarmService(SseEmitterRegistry sseEmitterRegistry, ObjectMapper objectMapper,  @Qualifier("ssePubSub") RedisTemplate<String, String> redisTemplate) {
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    public void sendMessage(String receiver, String sender, String message){   //메세지 전달 메서드

        SseMessageDto dto = SseMessageDto.builder()
                .receiver(receiver)
                .sender(sender)
                .message(message)
                .build();

        try {
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(receiver); //receiver 이메일로 EmitterMap에 넣음
            String data = objectMapper.writeValueAsString(dto); //dto 직렬화
//            만약 emitter객체가 현재 서버에 있으면, 바로 알림 발송. 그렇지 않으면 redis pub/sub활용
            if(sseEmitter != null){
                sseEmitter.send(SseEmitter.event().name("ordered").data(data));   //사용자에게 전파
//                사용자가 새로고침후에 알림메세지를 조회하려면 DB에 추가적으로 저장 필요
            }else {
//            redis pub/sub 기능을 활용하여 메세지 publish (redis로)
                redisTemplate.convertAndSend("order-channel", data);    //data는 직렬화시킨 dto
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {    //메세지, 채널명
//        message : 실질적으로 메세지가 담겨있는 객체
//        pattern : 채널명
//        추후 여러개의 채널에 각기 메세지를 publish하고 subscribe할 경우, 채널명으로 분기처리 가능
        String channelName = new String(pattern);
        System.out.println("channelName : " + channelName);
        try {                                       //여기 message에 publish의 정보를 담은 객체
            SseMessageDto dto = objectMapper.readValue(message.getBody(), SseMessageDto.class); //dto로 파싱
            String data = objectMapper.writeValueAsString(dto);  //dto 직렬화
            SseEmitter sseEmitter = sseEmitterRegistry.getEmitter(dto.getReceiver());  //sseEmitter객체로 변환
//            해당 서버에 receiver가 emitter객체가 있으면 send
             if(sseEmitter != null){
                 sseEmitter.send(SseEmitter.event().name("ordered").data(data));    //다시 json으로 직렬화 해서 보냄.
            }
            System.out.println("message : "+ dto);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

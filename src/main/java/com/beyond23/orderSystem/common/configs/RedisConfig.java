package com.beyond23.orderSystem.common.configs;

import com.beyond23.orderSystem.common.service.SseAlarmService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.StringRedisSerializer;

//스프링 시작이 되면 많은 빈 객체가 생성 -> 이중 하나를 redis 빈 객체로 만듬 -> redis를 사용할때마다 빈객체를 주입받아 사용
@Configuration
public class RedisConfig {  //팀프로젝트시 그대로 복사하여 사용하면 됨
    @Value("${spring.redis.host}")
    private String host;
    @Value("${spring.redis.port}")
    private int port;

//    연결 빈객체 (redis에 대한 연결정보(환경설정정보))
    @Bean
//     Qualifier : 같은 Bean객체가 여러개 있을경우 Bean객체를 구분하기 위한 어노테이션
    @Qualifier("rtInventory")    //빈객체의 이름(객체마다 구분짓기위함)
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);    //로컬호스트인지 아닌지, ip정보 => host, port : 변경될수있어서 yml에 저장해두고 받아오도록 유동적으로 코딩
        configuration.setPort(port);
        configuration.setDatabase(0);    //db번호     //현대적으로는 0번db만 거의 사용하고, host를 다르게 두고 사용을 많이함.

        return new LettuceConnectionFactory(configuration); // 인터페이스 RedisConnectionFactory의 구현체 :LettuceConnectionFactory
    }

//    템플릿 빈객체 (자료구조 설계)
    @Bean
    @Qualifier("rtInventory")
//    모든 template중에 무조건  redisTemplate이라는 메서드명이 반드시 1개는 있어야함
//    bean객체 생성시, bean객체간에 DI(의존성주입)는 "메서드 파라미터 주입"이 가능
//    JwtTokenProvider에서 rt를 redis에 저장시 사용?!
    public RedisTemplate<String, String> redisTemplate(@Qualifier("rtInventory") RedisConnectionFactory redisConnectionFactory) {       //<key, value> : value의 값은 set, hashes 등 여러 자료구조가 있지만 결국은 String 문자열형식으로 들어감
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());    //Serializer는 직렬화도구로서, key를 String으로 가져와 저장
        redisTemplate.setValueSerializer(new StringRedisSerializer());  //value를 String으로 가져와 저장
        redisTemplate.setConnectionFactory(redisConnectionFactory);   //해당 빈에만 값을 저장 (선언한 매개변수-위의 redisConnectionFactory 빈객체 )
        return redisTemplate;
    }

    @Bean
    @Qualifier("stockInventory")
    public RedisConnectionFactory stockConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setDatabase(1);

        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("stockInventory")
    public RedisTemplate<String, String> stockRedisTemplate(@Qualifier("stockInventory") RedisConnectionFactory redisConnectionFactory) {       //<key, value> : value의 값은 set, hashes 등 여러 자료구조가 있지만 결국은 String
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());    //key를 String으로 가져와 저장
        redisTemplate.setValueSerializer(new StringRedisSerializer());  //value를 String으로 가져와 저장
        redisTemplate.setConnectionFactory(redisConnectionFactory);   //해당 빈에만 값을 저장 (선언한 매개변수-위의 redisConnectionFactory 빈객체 )
        return redisTemplate;
    }

//redis pub/sub
    @Bean
    @Qualifier("ssePubSub")
    public RedisConnectionFactory ssePubSubConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
//      db 에 값을 저장하는 기능이 아니므로, db에 의존적이지 않음
        return new LettuceConnectionFactory(configuration);
    }

    @Bean
    @Qualifier("ssePubSub")     //이거가지고 message publish
    public RedisTemplate<String, String> ssePubSubRedisTemplate(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }

//    redis 리스너(subscribe) 객체 => 메세지를 받는 객체
//    호출 구조 : RedisMessageListenerContainer -> MessageListenerAdapter -> SseAlarmService(MessageListener)
    @Bean
    @Qualifier("ssePubSub")
    public RedisMessageListenerContainer redisMessageListenerContainer(@Qualifier("ssePubSub") RedisConnectionFactory redisConnectionFactory, @Qualifier("ssePubSub")MessageListenerAdapter messageListenerAdapter){
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        container.addMessageListener(messageListenerAdapter, new PatternTopic("order-channel"));    //메세지를 처리하는 객체, 채널명
//        만약에 여러채널을 구독해야하는경우, 여러개의 PatternTopic을 add하거나, 별도의 Listener Bean 객체 생성
        return container;
    }

//    redis에서 수신된 메세지를 처리하는 객체
    @Bean
    @Qualifier("ssePubSub")
    public MessageListenerAdapter messageListenerAdapter(SseAlarmService sseAlarmService){
//        채널로부터 수신되는 message처리를 SseAlarmService의 onMessage메서도로 위임
        return new MessageListenerAdapter(sseAlarmService, "onMessage");
    }

}

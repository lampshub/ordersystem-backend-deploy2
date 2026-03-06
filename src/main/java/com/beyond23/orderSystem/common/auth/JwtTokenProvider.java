package com.beyond23.orderSystem.common.auth;

import com.beyond23.orderSystem.member.domain.Member;
import com.beyond23.orderSystem.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.annotations.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.Signature;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class JwtTokenProvider { //시스템 실행되자마자 싱글톤객체가 생성됨.=> login시 at, rt값 세팅 => 인증객체 생성

    @Value("${jwt.secretKey}")
    private String st_secret_key;

    @Value("${jwt.expiration}")
    private int expiration;

//    인코딩된 문자열 -> 문자열 디코딩 -> HS512알고리즘으로 암호화
//    st_secret_key를 디코딩 + 암호화
    private Key secret_key ;


    @Value("${jwt.secretKeyRt}")
    private String st_secret_key_rt;

    @Value("${jwt.expirationRt}")
    private int expirationRt;

    private Key secret_key_rt ;

    private final RedisTemplate<String, String> redisTemplate;
    private final MemberRepository memberRepository;
    @Autowired
    public JwtTokenProvider(@Qualifier("rtInventory") RedisTemplate<String, String> redisTemplate, MemberRepository memberRepository) {
        this.redisTemplate = redisTemplate;
        this.memberRepository = memberRepository;
    }

    //    생성자 호출 이후에 아래 메서드를 실행하게 함으로서 @Value보다 늦게(생성자 만들어지고 나서) 실행하게되어 각 주입의 문제해결
    @PostConstruct
    public void init(){
        secret_key = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key), SignatureAlgorithm.HS512.getJcaName());
        secret_key_rt = new SecretKeySpec(Base64.getDecoder().decode(st_secret_key_rt), SignatureAlgorithm.HS512.getJcaName());
    }


    public String createToken(Member member){
//        Claims : JWT안에 들어있는 사용자정보묶음(payload)를 Map형태로 담은 객체
//        sub : abc@naver.com 형태
        Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role",member.getRole().toString());

        Date now = new Date();

//        토큰의 구성요소 : 헤더, 페이로드, 시그니처(서명부)
        String token = Jwts.builder()
//                아래 3가지 요소는 페이로드
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime()+expiration*60*1000L)) //30분*60초*1000밀리초 : 30분을 밀리초형태로 변환 => 실무에선 yml에서 코드 관리(30->expiration)
//                아래 메서드는 secret키를 통해 서명값(signature) 생성
                .signWith(secret_key)
                .compact();    //compact 는 return을 String으로 만들어줌
        return token;
    }


//    refreshToken
    public String createRtToken(Member member){
//        유효기간이 긴 rt토큰 생성
    Claims claims = Jwts.claims().setSubject(member.getEmail());
        claims.put("role",member.getRole().toString());
    Date now = new Date();
    String token = Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(new Date(now.getTime()+expirationRt*60*1000L))   //expirationRt : 3000분
            .signWith(secret_key_rt)
            .compact();
//     rt토큰을 redis에 저장 (터미널창에서 key *, get test@naver.com 로 확인할수있음)
//     opsForValue : 일반 String 자료구조. opsForSet(또는 Zset 또는 List 등) 존재.
//    redisTemplate.opsForValue().set(member.getEmail(), token);  //key:email, value:token
    redisTemplate.opsForValue().set(member.getEmail(), token, expirationRt, TimeUnit.MINUTES);  //3000분 ttl(유효기간 설정)
    return token;
}

    public Member validateRt(String refreshToken){
        Claims claims =null;
//        1. rt토큰 그 자체를 검증
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(st_secret_key_rt)    //이걸로 토큰검증
                    .build()
                    .parseClaimsJws(refreshToken)  // parsing해서 decoding
                    .getBody();
        } catch (Exception e) {
             throw new IllegalArgumentException("잘못된 토큰입니다.");  //400번에러
        }
        String email = claims.getSubject();
        Member member = memberRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("entity is not found"));
//        2. redis rt와 비교 검증
        String redisRt = redisTemplate.opsForValue().get(email);  //email : key값
        if(!redisRt.equals(refreshToken)){
            throw new IllegalArgumentException("잘못된 토큰입니다.");

        }
        return member;
    }
}


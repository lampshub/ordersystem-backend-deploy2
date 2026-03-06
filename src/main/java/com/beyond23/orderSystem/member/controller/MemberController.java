package com.beyond23.orderSystem.member.controller;

import com.beyond23.orderSystem.common.auth.JwtTokenProvider;
import com.beyond23.orderSystem.common.dtos.CommonErrorDto;
import com.beyond23.orderSystem.common.repository.SseEmitterRegistry;
import com.beyond23.orderSystem.member.domain.Member;
import com.beyond23.orderSystem.member.dtos.*;
import com.beyond23.orderSystem.member.repository.MemberRepository;
import com.beyond23.orderSystem.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/member")
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    @Autowired
    public MemberController(MemberService memberService, MemberRepository memberRepository, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

//    create, doLogin, list, myinfo, detail/1

    @PostMapping("/create")
    @Operation( // swagger 에 안내문구 입력
            summary = "회원가입", description = "이메일, 비밀번호를 통한 회원가입"
    )
    public ResponseEntity<?> create(@RequestBody MemberCreateDto dto){  //json형식
        System.out.println(dto);
        Long memberId = memberService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberId);
    }

    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberResDto> findAll(){
        List<MemberResDto> dtoList = memberService.findAll();
        return dtoList;
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findById(@PathVariable Long id){
//        try {
            MemberResDto dto = memberService.findById(id);
            return ResponseEntity.status(HttpStatus.OK).body(dto);
//        } catch (NoSuchElementException e){
//            CommonErrorDto dto = CommonErrorDto.builder()
//                    .status_code(404)
//                    .error_message(e.getMessage())
//                    .build();
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
//        }
    }

    @GetMapping("/myinfo")
    @PreAuthorize("hasRole('USER')")
//    AuthenticationPrincipal 어노테이션 : 현재 로그인한 사용자 정보를 컨트롤러 메서드 파라미터로 바로 주입
    public ResponseEntity<?> myinfo(@AuthenticationPrincipal String email) {
        MemberResDto dto = memberService.myinfo(email);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PostMapping("/doLogin")
    public ResponseEntity<?> login(@RequestBody MemberLoginReqDto dto){
        Member member = memberService.login(dto);
        String accessToken = jwtTokenProvider.createToken(member);    //토큰생성 및 리턴
//        refresh토큰 생성 및 저장
        String refreshToken = jwtTokenProvider.createRtToken(member);
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResDto);
    }

//    새 rt토큰 생성
    @PostMapping("/refresh-at")
    public ResponseEntity<?> refreshAt(@RequestBody RefreshTokenDto dto){        //rt가 에러시 401에러
//        rt검증(1.토큰 자체 검증 2.redis조회 검증)
        Member member = jwtTokenProvider.validateRt(dto.getRefreshToken());

//        at신규 생성 -> return
        String accessToken = jwtTokenProvider.createToken(member);
//        refresh토큰 생성 및 저장
        MemberLoginResDto memberLoginResDto = MemberLoginResDto.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResDto);
    }


}

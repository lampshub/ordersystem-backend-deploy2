package com.beyond23.orderSystem.common.init;

import com.beyond23.orderSystem.member.domain.Member;
import com.beyond23.orderSystem.member.domain.Role;
import com.beyond23.orderSystem.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional      //서버 키자마자 admin 계정 생성됨.
public class InitialDataLoad implements CommandLineRunner {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public InitialDataLoad(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if(memberRepository.findByEmail("admin@naver.com").isPresent()){
            return;
        }
        memberRepository.save(Member.builder()
                        .name("admin")
                        .email("admin@naver.com")
                        .role(Role.ADMIN)
                        .password(passwordEncoder.encode("12341234"))
                        .build());
    }
}

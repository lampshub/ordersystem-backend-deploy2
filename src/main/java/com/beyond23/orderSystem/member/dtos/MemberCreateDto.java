package com.beyond23.orderSystem.member.dtos;

import com.beyond23.orderSystem.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberCreateDto {
    @NotBlank(message = "이름을 입력해주세요")
    private String name;
    @NotBlank(message = "email을 입력해주세요")
    private String email;
    @NotBlank(message = "password를 입력해주세요")
    @Size(min =8, message = "패스워드의 길이가 너무 짧습니다")
    private String password;

    public Member toEntity(String encodedPw){
        return Member.builder()
                .name(this.name)
                .email(this.email)
                .password(encodedPw)
                .build();
    }
}

package com.beyond23.orderSystem.member.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberLoginReqDto {
    @NotBlank(message = "email을 입력해주세요")
    private String email;
    @NotBlank(message = "password를 입력해주세요")
    private String password;


}

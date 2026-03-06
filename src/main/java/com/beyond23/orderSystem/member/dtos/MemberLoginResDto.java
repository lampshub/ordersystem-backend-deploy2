package com.beyond23.orderSystem.member.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberLoginResDto {
    private String accessToken;
    private String refreshToken;


}

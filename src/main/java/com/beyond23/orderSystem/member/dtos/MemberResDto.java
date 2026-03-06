package com.beyond23.orderSystem.member.dtos;

import com.beyond23.orderSystem.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//MemberListDto 랑 MemberDetailDto 의 응답값이 같아서 한개의 Dto 로 사용
//List<MemberResDto>, MemberResDto
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberResDto {
    private Long id;
    private String name;
    private String email;

    public static MemberResDto fromEntity(Member member){
        return MemberResDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .build();
    }
}

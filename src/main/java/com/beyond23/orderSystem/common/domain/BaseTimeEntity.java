package com.beyond23.orderSystem.common.domain;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

//기본적으로는 Entity는 상속이 불가능한 구조. MappedSuperclass 어노테이션 사용시 상속관계 가능 (엔티티 상속용 부모클래스)
//상속받는 Entity에서 createdTime,updatedTime을 따로 선언안해도 DB에 필드처럼 포함됨
@MappedSuperclass
@Getter //상속받는 자식클래스에서 Getter가 여기에는 순서상 안붙을수 있어서 추가해줘야함
public class BaseTimeEntity {

    @CreationTimestamp  //초기값 세팅 어노테이션
    private LocalDateTime createdTime;
    @UpdateTimestamp    //엔티티 수정시 값 변경되는 어노테이션
    private LocalDateTime updatedTime;

}

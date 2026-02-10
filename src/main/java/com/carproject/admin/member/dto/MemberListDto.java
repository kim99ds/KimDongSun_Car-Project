package com.carproject.admin.member.dto;

import com.carproject.member.entity.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MemberListDto {
    private Long id;
    private String loginId;
    private String name;
    private String email;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private boolean admin;
}

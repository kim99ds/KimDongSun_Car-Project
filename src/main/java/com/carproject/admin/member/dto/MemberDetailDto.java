package com.carproject.admin.member.dto;

import com.carproject.member.entity.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MemberDetailDto {
    private Long id;
    private String userId;
    private String name;
    private String email;
    private LocalDate birthDate;
    private MemberStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean admin;
}

package com.carproject.admin.member.dto;

import com.carproject.member.entity.MemberStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MemberSearchCond {
    private String keyword;         // loginId/email/name 통합 검색
    private MemberStatus status;    // ACTIVE/BLOCKED/DELETED (선택)
}

package com.carproject.admin.role;

import java.util.Set;

/**
 * 역할관리 화면 출력용 Row DTO
 */
public record RoleAdminRow(
        Long memberId,
        String loginId,
        String name,
        String roleNames,
        Set<Long> assignedRoleIds
) {}

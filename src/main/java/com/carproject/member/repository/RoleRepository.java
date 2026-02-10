package com.carproject.member.repository;

import com.carproject.member.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional; // 이 줄을 꼭 추가하세요!

public interface RoleRepository extends JpaRepository<Role, Long> {
    // 특정 권한 이름(예: 'USER', 'ADMIN')으로 권한 엔티티를 찾는 메서드
    Optional<Role> findByRoleName(String roleName);

}
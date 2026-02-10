package com.carproject.member.repository;

import com.carproject.member.entity.MemberRole;
import com.carproject.member.entity.MemberRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MemberRoleRepository extends JpaRepository<MemberRole, MemberRoleId> {

    // 1. 회원의 ID로 권한 목록 조회
    List<MemberRole> findByMember_MemberId(Long memberId);

    // 2. 특정 회원에게 특정 권한이 있는지 확인 (관리자 여부 확인용)
    boolean existsByMember_MemberIdAndRole_RoleName(Long memberId, String roleName);

    // 3. 특정 권한 삭제 (관리자 권한 박탈용)
    void deleteByMember_MemberIdAndRole_RoleName(Long memberId, String roleName);

    // ✅ 회원 삭제용: 해당 회원의 모든 권한 관계 제거
    void deleteByMember_MemberId(Long memberId);

    // 4. 보안(Security) 연동을 위한 전용 쿼리
    @Query("SELECT mr FROM MemberRole mr JOIN FETCH mr.role WHERE mr.member.memberId = :memberId")
    List<MemberRole> findByMemberIdWithRole(@Param("memberId") Long memberId);

    // ✅ 역할 삭제 전 사용 여부 체크
    boolean existsByRole_RoleId(Long roleId);

    @Query("""
        select mr.role.roleName
        from MemberRole mr
        where mr.member.memberId = :memberId
    """)
    List<String> findRoleNamesByMemberId(@Param("memberId") Long memberId);
}
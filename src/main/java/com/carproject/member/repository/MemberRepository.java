package com.carproject.member.repository;

import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByEmail(String email);

    Optional<Member> findByEmail(String email);

    Page<Member> findByLoginIdContaining(String loginId, Pageable pageable);

    Page<Member> findByStatusNot(MemberStatus status, Pageable pageable);

    Page<Member> findByLoginIdContainingAndStatusNot(String loginId, MemberStatus status, Pageable pageable);

    List<Member> findByStatusNot(MemberStatus status);

    /**
     * 회원 탈퇴(soft delete) 상태 변경을 '직접 UPDATE'로 수행.
     * - 엔티티가 영속 상태가 아니거나
     * - 컨텍스트가 꼬여서 dirty-check가 안 먹는 경우에도
     *   DB 반영을 강제할 수 있음.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Member m set m.status = :status, m.updatedAt = CURRENT_TIMESTAMP where m.loginId = :loginId")
    int updateStatusByLoginId(@Param("loginId") String loginId, @Param("status") MemberStatus status);
}

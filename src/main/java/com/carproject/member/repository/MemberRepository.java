package com.carproject.member.repository;

import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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

}






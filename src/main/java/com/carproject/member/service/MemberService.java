package com.carproject.member.service;

import com.carproject.member.entity.*;
import com.carproject.member.repository.MemberRepository;
import com.carproject.member.repository.MemberRoleRepository;
import com.carproject.member.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 회원가입
     * - ACTIVE 회원: 중복 에러
     * - DELETED 회원: 재가입으로 보고 복구 처리
     */
    public Long signup(String userId, String rawPassword, String name, String email, LocalDate birthDate) {

        /* =====================================================
           1) loginId 기준 처리
           ===================================================== */
        Optional<Member> byLoginId = memberRepository.findByLoginId(userId);
        if (byLoginId.isPresent()) {
            Member existing = byLoginId.get();

            // ✅ 탈퇴 회원이면 "재가입" 처리
            if (existing.getStatus() == MemberStatus.DELETED) {
                reactivateMember(existing, userId, rawPassword, name, email, birthDate);
                return existing.getId();
            }

            // ACTIVE면 중복
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        /* =====================================================
           2) email 기준 처리
           ===================================================== */
        Optional<Member> byEmail = memberRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            Member existing = byEmail.get();

            if (existing.getStatus() == MemberStatus.DELETED) {
                reactivateMember(existing, userId, rawPassword, name, email, birthDate);
                return existing.getId();
            }

            throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
        }

        /* =====================================================
           3) 완전 신규 회원 생성
           ===================================================== */
        String encoded = passwordEncoder.encode(rawPassword);

        Member member = Member.create(userId, encoded, name, email, birthDate);
        member.setStatus(MemberStatus.ACTIVE);

        Member saved = memberRepository.save(member);

        ensureUserRole(saved);

        // 혹시라도 ADMIN이 붙어있으면 제거
        if (memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(saved.getMemberId(), "ADMIN")) {
            memberRoleRepository.deleteByMember_MemberIdAndRole_RoleName(saved.getMemberId(), "ADMIN");
        }

        return saved.getId();
    }

    /* =====================================================
       탈퇴 회원 복구(재가입) 공통 로직
       ===================================================== */
    private void reactivateMember(
            Member member,
            String userId,
            String rawPassword,
            String name,
            String email,
            LocalDate birthDate
    ) {
        // ✅ 기존 기능(탈퇴 계정 복구)은 유지하면서,
        // ✅ "다른 계정이 이미 쓰는 이메일"만 선제 차단 (DB UNIQUE 예외 방지)
        memberRepository.findByEmail(email)
                .filter(other -> !other.getMemberId().equals(member.getMemberId()))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
                });

        member.setStatus(MemberStatus.ACTIVE);
        member.setLoginId(userId);
        member.setPassword(passwordEncoder.encode(rawPassword));
        member.setName(name);
        member.setEmail(email);
        member.setBirthDate(birthDate);

        memberRepository.save(member);

        ensureUserRole(member);

        if (memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(member.getMemberId(), "ADMIN")) {
            memberRoleRepository.deleteByMember_MemberIdAndRole_RoleName(member.getMemberId(), "ADMIN");
        }
    }



    /* =====================================================
       USER 권한 보장
       ===================================================== */
    private void ensureUserRole(Member member) {
        Role userRole = roleRepository.findByRoleName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setRoleName("USER");
            return roleRepository.save(r);
        });

        if (!memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(
                member.getMemberId(), "USER")) {
            memberRoleRepository.save(MemberRole.link(member, userRole));
        }
    }

    /**
     * 일반 유저 프로필 수정
     */
    public void updateProfile(String loginId, String name, String email) {

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음: " + loginId));

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        String normalizedEmail = email.trim();

        // 이메일 중복 체크(본인 제외)
        memberRepository.findByEmail(normalizedEmail)
                .filter(other -> !other.getId().equals(member.getId()))
                .ifPresent(other -> {
                    throw new IllegalArgumentException("이미 사용중인 이메일입니다.");
                });

        member.setName(name.trim());
        member.setEmail(normalizedEmail);
    }
}

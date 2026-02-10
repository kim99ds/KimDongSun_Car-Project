package com.carproject.member.service;

import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberStatus;
import com.carproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWithdrawService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void withdrawLocal(Authentication authentication, String rawPassword) {
        String loginId = authentication.getName();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("회원 없음: " + loginId));

        // 이미 탈퇴면 그냥 종료(선택)
        if (member.getStatus() != MemberStatus.ACTIVE) {
            return;
        }

        // ✅ 폼 로그인: 비밀번호 확인
        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        member.setStatus(MemberStatus.DELETED);
    }

    @Transactional
    public void withdrawSocial(Authentication authentication) {
        // ✅ 네 소셜 로그인 Principal name은 "loginId"로 설정해놨음 (DefaultOAuth2User(..., "loginId"))
        String loginId = authentication.getName();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalStateException("회원 없음: " + loginId));

        if (member.getStatus() != MemberStatus.ACTIVE) {
            return;
        }

        // ✅ 소셜은 비번 확인 없이 탈퇴 처리
        member.setStatus(MemberStatus.DELETED);
    }
}

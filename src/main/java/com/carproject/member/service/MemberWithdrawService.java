package com.carproject.member.service;

import com.carproject.member.entity.Member;
import com.carproject.member.entity.MemberStatus;
import com.carproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberWithdrawService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 로컬 로그인 회원: 비밀번호 검증
     */
    @Transactional(readOnly = true)
    public void verifyPassword(Authentication authentication, String rawPassword) {
        String loginId = authentication.getName();
        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(rawPassword, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
    }

    /**
     * 로컬 로그인 회원: 탈퇴 처리 (status=DELETED)
     * - 엔티티 dirty checking 대신 update 쿼리를 사용해서 "반드시" DB 반영되게 만듦
     */
    @Transactional
    public void withdrawLocal(Authentication authentication, String withdrawPw) {
        String loginId = authentication.getName();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다. loginId=" + loginId));

        // ✅ 로컬 회원은 비밀번호 검증 필수
        if (withdrawPw == null || withdrawPw.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해 주세요.");
        }
        if (!passwordEncoder.matches(withdrawPw, member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        int updated = memberRepository.updateStatusByLoginId(loginId, MemberStatus.DELETED);
        if (updated != 1) {
            throw new IllegalStateException("회원 탈퇴 처리에 실패했습니다. updated=" + updated);
        }
    }


    /**
     * 소셜 로그인 회원: 탈퇴 처리 (status=DELETED)
     */
    @Transactional
    public void withdrawSocial(Authentication authentication) {
        // OAuth2AuthenticationToken#getName() 값이 provider/userNameAttribute 에 따라 달라질 수 있으니
        // 프로젝트에서는 email 기반으로 member를 찾는 방식이 더 안전할 수 있음.
        // 현재는 기존 방식(loginId) 유지.
        String loginId = authentication.getName();

        Member member = memberRepository.findByLoginId(loginId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        if (member.getStatus() == MemberStatus.DELETED) {
            return;
        }

        int updated = memberRepository.updateStatusByLoginId(loginId, MemberStatus.DELETED);
        if (updated != 1) {
            throw new IllegalStateException("회원 탈퇴 반영에 실패했습니다.");
        }

        SecurityContextHolder.clearContext();
    }
}

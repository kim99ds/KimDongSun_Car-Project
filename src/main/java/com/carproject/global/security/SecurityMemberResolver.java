package com.carproject.global.security;

import com.carproject.member.entity.Member;
import com.carproject.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class SecurityMemberResolver {

    private final MemberRepository memberRepository;

    /** 로그인 안 했으면 null */
    public Long resolveMemberIdOrNull(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        // AnonymousAuthenticationFilter가 들어오는 경우도 있으니 방어
        if ("anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) return null;

        String loginId = authentication.getName(); // 보통 username/loginId
        return memberRepository.findByLoginId(loginId)
                .map(Member::getMemberId)
                .orElse(null);
    }

    /** 로그인 필수: 없으면 401 */
    public Long requireMemberId(Authentication authentication) {
        Long memberId = resolveMemberIdOrNull(authentication);
        if (memberId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        }
        return memberId;
    }
}

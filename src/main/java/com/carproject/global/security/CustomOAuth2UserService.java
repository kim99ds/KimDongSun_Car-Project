package com.carproject.global.security;

import com.carproject.member.entity.*;
import com.carproject.member.repository.MemberRepository;
import com.carproject.member.repository.MemberRoleRepository;
import com.carproject.member.repository.RoleRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        System.out.println("### CustomOAuth2UserService loadUser CALLED");

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration()
                .getRegistrationId()
                .toUpperCase();

        System.out.println("### OAUTH LOGIN PROVIDER = " + provider);

        Map<String, Object> attrs = oAuth2User.getAttributes();

        String email;
        String name;

        switch (provider) {
            case "KAKAO":
                email = extractKakaoEmail(attrs);
                name  = extractKakaoName(attrs);
                if (email == null || email.isBlank()) {
                    throw new OAuth2AuthenticationException("카카오 이메일 동의 필요");
                }
                break;

            case "GOOGLE":
                email = extractGoogleEmail(attrs);
                name  = extractGoogleName(attrs);
                if (email == null || email.isBlank()) {
                    throw new OAuth2AuthenticationException("구글 이메일 정보 없음");
                }
                break;

            case "NAVER":
                email = extractNaverEmail(attrs);
                name  = extractNaverName(attrs);
                if (email == null || email.isBlank()) {
                    throw new OAuth2AuthenticationException("네이버 이메일 정보 없음");
                }
                break;

            default:
                throw new OAuth2AuthenticationException("Unsupported provider: " + provider);
        }

        email = email.trim();

        Optional<Member> existingOpt = memberRepository.findByEmail(email);
        boolean isNew = existingOpt.isEmpty();

        /* =====================================================
           ✅ [핵심 교체] 기존 회원이면 DELETED일 때 "재가입(복구)" 처리
           ===================================================== */
        Member member;

        if (existingOpt.isPresent()) {
            member = existingOpt.get();

            // ✅ 탈퇴(DELETED)면 재가입으로 보고 복구
            if (member.getStatus() == MemberStatus.DELETED) {
                member.setStatus(MemberStatus.ACTIVE);

                // (선택) 이름 최신값으로 갱신하고 싶으면 유지
                if (name != null && !name.isBlank()) {
                    member.setName(name.trim());
                }

                // ✅ USER 권한 보장 (혹시 탈퇴 과정에서 role을 정리했을 수도 있으니)
                Role userRole = roleRepository.findByRoleName("USER").orElseGet(() -> {
                    Role r = new Role();
                    r.setRoleName("USER");
                    return roleRepository.save(r);
                });

                if (!memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(member.getMemberId(), "USER")) {
                    memberRoleRepository.save(MemberRole.link(member, userRole));
                }

                memberRepository.save(member);
                memberRepository.flush();
            }

        } else {
            member = createSocialMember(provider, email, name);
        }

        /* =====================================================
           ✅ [수정] DELETED는 위에서 복구했으니,
              그 외 ACTIVE가 아닌 상태만 차단
           ===================================================== */
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new OAuth2AuthenticationException(
                    "비활성 회원입니다: " + member.getStatus()
            );
        }

        ServletRequestAttributes sra =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra != null) {
            HttpSession session = sra.getRequest().getSession();
            session.setAttribute("OAUTH_IS_NEW", isNew);
        }

        Collection<? extends GrantedAuthority> authorities =
                loadAuthorities(member.getMemberId());

        Map<String, Object> principalAttrs = new HashMap<>(attrs);
        principalAttrs.put("loginId", member.getLoginId());
        principalAttrs.put("memberId", member.getMemberId());
        principalAttrs.put("email", member.getEmail());
        principalAttrs.put("provider", provider);

        return new DefaultOAuth2User(authorities, principalAttrs, "loginId");
    }

    /* ===================== KAKAO ===================== */

    private String extractKakaoEmail(Map<String, Object> attrs) {
        Object accountObj = attrs.get("kakao_account");
        if (!(accountObj instanceof Map<?, ?> account)) return null;
        Object emailObj = account.get("email");
        return emailObj != null ? String.valueOf(emailObj) : null;
    }

    private String extractKakaoName(Map<String, Object> attrs) {
        Object propsObj = attrs.get("properties");
        if (propsObj instanceof Map<?, ?> props) {
            Object nick = props.get("nickname");
            if (nick != null) return String.valueOf(nick);
        }
        Object accountObj = attrs.get("kakao_account");
        if (accountObj instanceof Map<?, ?> account) {
            Object profileObj = account.get("profile");
            if (profileObj instanceof Map<?, ?> profile) {
                Object nick = profile.get("nickname");
                if (nick != null) return String.valueOf(nick);
            }
        }
        return null;
    }

    /* ===================== GOOGLE ===================== */

    private String extractGoogleEmail(Map<String, Object> attrs) {
        Object emailObj = attrs.get("email");
        if (emailObj != null) return String.valueOf(emailObj);
        Object upn = attrs.get("upn");
        if (upn != null) return String.valueOf(upn);
        return null;
    }

    private String extractGoogleName(Map<String, Object> attrs) {
        Object nameObj = attrs.get("name");
        if (nameObj != null) return String.valueOf(nameObj);

        Object given = attrs.get("given_name");
        Object family = attrs.get("family_name");
        String full = ((given != null) ? given : "") + " " + ((family != null) ? family : "");
        full = full.trim();
        return full.isBlank() ? null : full;
    }

    /* ===================== NAVER ===================== */

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNaverResponse(Map<String, Object> attrs) {
        Object resp = attrs.get("response");
        if (resp instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private String extractNaverEmail(Map<String, Object> attrs) {
        Map<String, Object> resp = getNaverResponse(attrs);
        if (resp == null) return null;
        Object email = resp.get("email");
        return email != null ? String.valueOf(email) : null;
    }

    private String extractNaverName(Map<String, Object> attrs) {
        Map<String, Object> resp = getNaverResponse(attrs);
        if (resp == null) return null;
        Object name = resp.get("name");
        return name != null ? String.valueOf(name) : null;
    }

    /* ===================== MEMBER CREATE ===================== */

    private Member createSocialMember(String provider, String email, String name) {

        String loginId = normalizeLoginId(email);

        if (memberRepository.existsByLoginId(loginId)) {
            String prefix = provider.toLowerCase();
            loginId = prefix + "_" +
                    UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        }

        String encoded = passwordEncoder.encode(randomPassword(20));

        Member member = Member.create(
                loginId,
                encoded,
                (name == null || name.isBlank()) ? provider + "회원" : name.trim(),
                email,
                LocalDate.of(1900, 1, 1)
        );
        member.setStatus(MemberStatus.ACTIVE);

        Member saved = memberRepository.save(member);
        memberRepository.flush();

        Role userRole = roleRepository.findByRoleName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setRoleName("USER");
            return roleRepository.save(r);
        });

        if (!memberRoleRepository.existsByMember_MemberIdAndRole_RoleName(
                saved.getMemberId(), "USER")) {
            memberRoleRepository.save(MemberRole.link(saved, userRole));
        }

        return saved;
    }

    /* ===================== UTIL ===================== */

    private String normalizeLoginId(String email) {
        String v = email.trim();
        return v.length() <= 100 ? v : v.substring(0, 100);
    }

    private String randomPassword(int len) {
        final String chars =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()_-+=?";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private Collection<? extends GrantedAuthority> loadAuthorities(Long memberId) {
        List<MemberRole> mrs = memberRoleRepository.findByMember_MemberId(memberId);

        List<GrantedAuthority> list = new ArrayList<>();
        for (MemberRole mr : mrs) {
            String rn = mr.getRole().getRoleName();
            list.add(new SimpleGrantedAuthority(
                    rn.startsWith("ROLE_") ? rn : "ROLE_" + rn
            ));
        }

        if (list.isEmpty()) {
            list.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return list;
    }
}

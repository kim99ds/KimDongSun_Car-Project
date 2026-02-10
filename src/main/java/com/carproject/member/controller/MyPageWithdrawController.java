package com.carproject.member.controller;

import com.carproject.member.service.MemberWithdrawService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MyPageWithdrawController {

    private final MemberWithdrawService memberWithdrawService;

    @PostMapping("/mypage/withdraw")
    public String withdraw(
            @RequestParam(value = "agreeCheck", required = false) String agreeCheck,
            @RequestParam(value = "withdrawPw", required = false) String withdrawPw,
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 1) 동의 체크 필수
        if (agreeCheck == null) {
            return "redirect:/mypage?withdrawError=agree";
        }

        // 2) 소셜/폼 분기
        boolean isSocial = (authentication instanceof OAuth2AuthenticationToken);

        if (isSocial) {
            memberWithdrawService.withdrawSocial(authentication);
        } else {
            if (withdrawPw == null || withdrawPw.trim().isEmpty()) {
                return "redirect:/mypage?withdrawError=pw";
            }
            memberWithdrawService.withdrawLocal(authentication, withdrawPw);
        }

        // 3) ✅ 탈퇴 즉시 "강제 로그아웃" (세션/쿠키/컨텍스트 완전 정리)
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // SecurityContext 제거
        SecurityContextHolder.clearContext();

        // JSESSIONID 쿠키 제거 (톰캣 세션 쿠키)
        new CookieClearingLogoutHandler("JSESSIONID").logout(request, response, authentication);

        // Spring Security 로그아웃 처리
        new SecurityContextLogoutHandler().logout(request, response, authentication);

        return "redirect:/main?auth=withdraw_success";
    }
}

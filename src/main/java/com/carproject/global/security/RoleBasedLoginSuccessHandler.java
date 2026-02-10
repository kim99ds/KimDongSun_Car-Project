package com.carproject.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

/**
 * ✅ 로그인 성공 시 원하는 "첫 화면"으로 보내기 위한 SuccessHandler
 * - 이전에 접근하려다 막힌 URL(SavedRequest)이 있으면 그쪽 우선
 * - SavedRequest가 없으면 역할에 따라 기본 이동
 *   - ADMIN -> adminTargetUrl (보통 /admin/members : 리스트)
 *   - 그 외  -> userTargetUrl  (보통 /member/detail : 내 상세)
 */
public class RoleBasedLoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();
    private final String adminTargetUrl;
    private final String userTargetUrl;

    public RoleBasedLoginSuccessHandler(String adminTargetUrl, String userTargetUrl) {
        this.adminTargetUrl = adminTargetUrl;
        this.userTargetUrl = userTargetUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        // ✅ SavedRequest(원래 가려던 페이지)가 있으면 그쪽으로 우선 이동
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            super.onAuthenticationSuccess(request, response, authentication);
            return;
        }

        // ✅ SavedRequest가 없으면 역할 기반으로 첫 화면 결정
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals("ROLE_ADMIN"));

        String targetUrl = isAdmin ? adminTargetUrl : userTargetUrl;
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

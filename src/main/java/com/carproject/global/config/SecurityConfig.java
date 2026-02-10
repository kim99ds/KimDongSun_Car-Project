package com.carproject.global.config;

import com.carproject.global.security.CustomOAuth2UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider,
            CustomOAuth2UserService customOAuth2UserService
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/main",
                                "/main/**",
                                "/health/**",
                                "/signup",
                                "/auth/**",
                                "/oauth2/**", "/login/oauth2/**",
                                "/css/**", "/js/**", "/images/**",

                                // ✅ 업로드된 랜딩 페이지(HTML/CSS/JS/IMG/VIDEO) 접근 허용
                                "/uploads/**",

                                "/cars/**",
                                "/event/**",                                // HomeController exposes /events as a public redirect to /event
                                "/unique", "/unique/**",
                                "/events", "/events/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/trims/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // ✅ 폼 로그인: 성공 시 auth 파라미터 붙여서 메인으로 이동 + 콘솔에 DB 접속 확인용 로그
                .formLogin(login -> login
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("loginId")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
                            System.out.println("### FORM LOGIN SUCCESS name=" + authentication.getName());
                            response.sendRedirect("/main?auth=login_success");
                        })
                        .failureHandler((request, response, exception) -> {
                            System.out.println("### FORM LOGIN FAIL usernameParam(loginId) = " + request.getParameter("loginId"));
                            System.out.println("### FORM LOGIN FAIL: " + exception.getClass().getName());
                            System.out.println("### FORM LOGIN FAIL MSG: " + exception.getMessage());
                            exception.printStackTrace();
                            response.sendRedirect("/auth/login?error=true");
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/main")
                );

        // ✅ 소셜 로그인: 신규/기존 분기 + "신규 생성(save)됐는지" 콘솔로 확실히 확인 가능하게 로그 추가
        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth
                    .loginPage("/auth/login")
                    .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                    .successHandler((request, response, authentication) -> {

                        HttpSession session = request.getSession(false);
                        boolean isNew = false;

                        if (session != null) {
                            Object v = session.getAttribute("OAUTH_IS_NEW");
                            if (v instanceof Boolean b) isNew = b;
                            session.removeAttribute("OAUTH_IS_NEW");
                        }

                        System.out.println("### OAUTH2 LOGIN SUCCESS isNew=" + isNew + " name=" + authentication.getName());

                        if (isNew) {
                            response.sendRedirect("/main?auth=signup_success");
                        } else {
                            response.sendRedirect("/main?auth=login_success");
                        }
                    })
                    .failureHandler((request, response, exception) -> {
                        System.out.println("### OAUTH2 LOGIN FAIL: " + exception.getClass().getName());
                        System.out.println("### OAUTH2 LOGIN FAIL MSG: " + exception.getMessage());
                        exception.printStackTrace();
                        response.sendRedirect("/auth/login?error=true");
                    })
            );
        }

        return http.build();
    }
}

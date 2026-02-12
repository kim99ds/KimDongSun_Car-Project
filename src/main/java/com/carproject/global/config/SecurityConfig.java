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

import com.carproject.global.security.ApiAccessDeniedHandler;
import com.carproject.global.security.ApiAuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            CustomOAuth2UserService customOAuth2UserService,
            ObjectMapper objectMapper
    ) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                // ✅ Swagger(OpenAPI) - 로그인 없이 접근
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",

                                "/", "/main",
                                "/main/**",
                                "/health/**",            // (기존 유지)
                                "/actuator/health",      // ✅ actuator 쓰는 경우 대비
                                "/actuator/health/**",   // ✅ actuator 쓰는 경우 대비

                                "/signup",
                                "/auth/**",
                                "/oauth2/**", "/login/oauth2/**",
                                "/css/**", "/js/**", "/images/**",

                                // ✅ 업로드된 랜딩 페이지(HTML/CSS/JS/IMG/VIDEO) 접근 허용
                                "/uploads/**",

                                "/cars/**",
                                "/event/**",
                                "/unique", "/unique/**",
                                "/events", "/events/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/trims/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                // ✅ /api/**에 대해서만 401/403을 JSON으로 내려주게 설정
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new ApiAuthenticationEntryPoint(objectMapper),
                                (request) -> request.getRequestURI().startsWith("/api/")
                        )
                        .defaultAccessDeniedHandlerFor(
                                new ApiAccessDeniedHandler(objectMapper),
                                (request) -> request.getRequestURI().startsWith("/api/")
                        )
                )

                // ✅ 폼 로그인
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

        // ✅ 소셜 로그인
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

package com.carproject.global.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class OAuthDebugConfig {

    /**
     * 로컬 디버깅용: Kakao clientId가 제대로 로딩됐는지 콘솔에 찍어줌.
     * - OAuth2 설정이 아예 없는 환경에서는 ClientRegistrationRepository 빈이 없을 수 있으니,
     *   ObjectProvider로 받아서 안전하게 처리.
     */
    @Bean
    CommandLineRunner printKakaoClientId(ObjectProvider<ClientRegistrationRepository> repoProvider) {
        return args -> {
            ClientRegistrationRepository repo = repoProvider.getIfAvailable();
            if (repo == null) {
                System.out.println(">>> OAuth2 ClientRegistrationRepository NOT configured (skipping kakao clientId log)");
                return;
            }

            if (repo instanceof InMemoryClientRegistrationRepository r) {
                var kakao = r.findByRegistrationId("kakao");
                System.out.println(">>> KAKAO clientId = " + (kakao == null ? "null" : kakao.getClientId()));
            } else {
                System.out.println(">>> ClientRegistrationRepository class = " + repo.getClass());
            }
        };
    }
}

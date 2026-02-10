package com.carproject.global.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
public class OAuthDebugConfig {

    @Bean
    CommandLineRunner printKakaoClientId(ClientRegistrationRepository repo) {
        return args -> {
            if (repo instanceof InMemoryClientRegistrationRepository r) {
                var kakao = r.findByRegistrationId("kakao");
                System.out.println(">>> KAKAO clientId = " + (kakao == null ? "null" : kakao.getClientId()));
            } else {
                System.out.println(">>> repo class = " + repo.getClass());
            }
        };
    }
}

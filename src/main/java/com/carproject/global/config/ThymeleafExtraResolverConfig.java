package com.carproject.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

@Configuration
public class ThymeleafExtraResolverConfig {

    @Bean
    public SpringResourceTemplateResolver uploadedLandingTemplateResolver() {

        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();

        // 업로드된 템플릿 위치
        resolver.setPrefix("file:./uploads/landing-tpl/");
        resolver.setSuffix(".html");

        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCheckExistence(true);

        resolver.setCacheable(false); // 개발용
        resolver.setOrder(2);

        return resolver;
    }
}

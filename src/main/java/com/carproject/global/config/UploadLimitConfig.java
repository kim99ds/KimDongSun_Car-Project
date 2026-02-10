package com.carproject.global.config;

import jakarta.servlet.MultipartConfigElement;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

@Configuration
public class UploadLimitConfig {

    /**
     * 산타페.zip 약 117MB 기준
     * - 파일 350MB
     * - 요청 400MB(파일+폼데이터)
     *
     * ✅ yml이 안 먹히는 상황에서도 무조건 적용되게 "코드로" 강제 설정
     */

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();

        factory.setMaxFileSize(DataSize.ofMegabytes(350));     // 파일 1개 최대
        factory.setMaxRequestSize(DataSize.ofMegabytes(400));  // 요청 전체 최대
        factory.setFileSizeThreshold(DataSize.ofMegabytes(2)); // 임계치

        return factory.createMultipartConfig();
    }

    @Bean
    public TomcatServletWebServerFactory tomcatServletWebServerFactory() {

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();

        // Tomcat 쪽도 같이 풀어줘야 413/리셋 안 남
        factory.addConnectorCustomizers((Connector connector) -> {
            // bytes 단위 (int 범위 내)
            connector.setMaxPostSize(400 * 1024 * 1024); // 400MB

            // Tomcat 10/Boot3에서 form post 제한
            connector.setProperty("maxHttpFormPostSize", String.valueOf(400 * 1024 * 1024));

            // swallow 제한(업로드 큰 요청에서 연결 끊김 방지)
            connector.setProperty("maxSwallowSize", String.valueOf(400 * 1024 * 1024));
        });

        return factory;
    }
}

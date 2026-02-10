package com.carproject.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.carproject.global.converter.BudgetRangeConverter;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // ✅ 설정 없으면 기본 uploads 사용 (로컬/배포 둘 다 안전)
    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String location = uploadPath.toUri().toString(); // file:/C:/.../uploads/

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(0); // ✅ 개발 중 캐시 때문에 css/js 안 바뀌는 것 방지
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new BudgetRangeConverter());
    }
}

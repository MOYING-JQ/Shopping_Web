package com.moying.project_test.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author 墨莹
 * @date 2026/9/3 10:48
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${image_upload.path}")
    private String uploadPath;

    @Value("${image_upload.access-prefix}")
    private String accessPrefix;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射： /images/**  → file:D:/upload/images/
        registry.addResourceHandler(accessPrefix + "/**")
                .addResourceLocations("file:" + uploadPath);
    }
}
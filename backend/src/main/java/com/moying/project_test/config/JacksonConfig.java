package com.moying.project_test.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 全局时间序列化配置：LocalDateTime 统一输出 yyyy-MM-dd HH:mm:ss
 * 注意：Spring Boot 4 使用 Jackson 3（tools.jackson 包），自定义接口为 JsonMapperBuilderCustomizer
 *
 * @author 墨莹
 * @date 2026/9/9
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Bean
    public JsonMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> {
            SimpleModule module = new SimpleModule();
            module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(FORMATTER));
            module.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(FORMATTER));
            builder.addModule(module);
        };
    }
}

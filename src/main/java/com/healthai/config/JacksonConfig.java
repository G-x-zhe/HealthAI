package com.healthai.config;

import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.format.DateTimeFormatter;

/**
 * Jackson 全局配置
 * 统一 LocalDateTime 与 JSON 之间的格式为 yyyy-MM-dd HH:mm:ss：
 * 入参支持 "2026-10-15 09:00:00"（默认只认 ISO 的 2026-10-15T09:00:00），
 * 出参也统一不带 T，前端展示更友好
 */
@Configuration
public class JacksonConfig {

    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN);
        return builder -> {
            // 序列化：LocalDateTime → "yyyy-MM-dd HH:mm:ss"
            builder.serializers(new LocalDateTimeSerializer(formatter));
            // 反序列化："yyyy-MM-dd HH:mm:ss" → LocalDateTime
            builder.deserializers(new LocalDateTimeDeserializer(formatter));
        };
    }
}

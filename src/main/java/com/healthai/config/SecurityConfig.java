package com.healthai.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/api/knowledge/**").permitAll()
                        .requestMatchers("/api/chat/**", "/api/collect/**", "/api/remind/**").authenticated()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        // 未认证 → 返回 401 JSON（兜底，正常情况 Filter 已经拦截）
                        .authenticationEntryPoint((req, resp, e) -> writeJson(resp, 401, "未登录或 Token 无效"))
                        // 已认证但权限不足 → 返回 403 JSON
                        .accessDeniedHandler((req, resp, e) -> writeJson(resp, 403, "无访问权限")))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void writeJson(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"code\":" + code + ",\"message\":\"" + msg + "\"}");
    }
}
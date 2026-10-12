package com.healthai.config;

import com.healthai.common.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redis;
    private static final String BLACKLIST = "healthai:token:blacklist:";

    /** 无需鉴权的路径前缀 */
    private static final Set<String> PUBLIC_PREFIXES = Set.of(
            "/api/auth/",
            "/api/knowledge/"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp,
                                    FilterChain chain) throws ServletException, IOException {
        String uri = req.getRequestURI();

        // 1. 放行公开接口
        if (PUBLIC_PREFIXES.stream().anyMatch(uri::startsWith)) {
            chain.doFilter(req, resp);
            return;
        }

        // 2. 取 token，兼容 "Bearer " 前缀
        String token = req.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7).trim();
        }

        // 3. 保护路径无 token → 直接返回 401（不再让 Spring Security 抛 403）
        if (token == null || token.isEmpty()) {
            writeUnauthorized(resp, "未登录或缺少 Token");
            return;
        }

        // 4. 过期检查
        if (JwtUtils.isExpired(token)) {
            writeUnauthorized(resp, "Token 已过期");
            return;
        }
        // 5. 黑名单检查
        if (Boolean.TRUE.equals(redis.hasKey(BLACKLIST + token))) {
            writeUnauthorized(resp, "Token 已失效");
            return;
        }
        // 6. 解析用户
        Long userId = JwtUtils.getUserId(token);
        if (userId == null) {
            writeUnauthorized(resp, "Token 无效");
            return;
        }
        req.setAttribute("userId", userId);
        req.setAttribute("username", JwtUtils.getUsername(token));
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, resp);
    }

    private void writeUnauthorized(HttpServletResponse resp, String msg) throws IOException {
        resp.setStatus(401);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write("{\"code\":401,\"message\":\"" + msg + "\"}");
    }
}
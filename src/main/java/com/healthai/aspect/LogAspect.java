package com.healthai.aspect;

import com.healthai.common.OperationLog;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

@Slf4j
@Aspect
@Component
public class LogAspect {

    @Around("@annotation(com.healthai.common.OperationLog)")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        // 1. 取注解上的 module/operation
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        OperationLog anno = sig.getMethod().getAnnotation(OperationLog.class);

        // 2. 取 request，提取 URI、IP、userId
        ServletRequestAttributes attr = (ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = attr.getRequest();
        Object userId = req.getAttribute("userId");   // JWT 过滤器设置
        if (userId == null) userId = "anonymous";

        long start = System.currentTimeMillis();
        log.info("[{} - {}] {} {} 用户={} IP={}",
                anno.module(), anno.operation(),
                req.getMethod(), req.getRequestURI(), userId, getIp(req));

        try {
            Object result = pjp.proceed();   // 执行目标方法
            log.info("[{} - {}] 成功，耗时 {} ms",
                    anno.module(), anno.operation(),
                    System.currentTimeMillis() - start);
            return result;
        } catch (Throwable e) {
            log.error("[{} - {}] 异常：{}，耗时 {} ms",
                    anno.module(), anno.operation(), e.toString(),
                    System.currentTimeMillis() - start);
            throw e;   // 关键：记录后必须重新抛出，不能吞异常
        }
    }

    // 穿透代理拿真实 IP
    private String getIp(HttpServletRequest req) {
        String[] headers = {"X-Forwarded-For","X-Real-IP","Proxy-Client-IP"};
        for (String h : headers) {
            String ip = req.getHeader(h);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.contains(",") ? ip.split(",")[0].trim() : ip;
            }
        }
        return req.getRemoteAddr();
    }
}
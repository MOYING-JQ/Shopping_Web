package com.moying.project_test.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author 墨莹
 * @date 2026/8/27 11:45
 */

@Aspect
@Component
@Slf4j
public class LogAspect {


    @Before("execution(* com.moying.project_test.controller..*.*(..)) ||" +
            "execution(* com.moying.project_test.service..*.*(..))")
    public void Before(JoinPoint joinPoint){


        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String time = String.valueOf(LocalDateTime.now());

        log.info("类名{}，方法{}，时间{}",className,methodName,time);
    }
}

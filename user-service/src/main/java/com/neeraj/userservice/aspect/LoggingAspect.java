package com.neeraj.userservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

// Aspect are used for cross-cutting concerns like logging, security, transaction management, etc.

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    // Pointcut is the expression that defines where the aspect should be applied --> in this case, all methods in the service package
    @Pointcut("execution(* com.neeraj.userservice.service.*.*(..))")
    public void serviceMethods() {
    }

    // Advice is the action that is taken when the pointcut is matched --> in this case, logging before and after the method call
    @Before("serviceMethods()")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Called service method: {} with arguments: {}", joinPoint.getSignature().getName(), joinPoint.getArgs());
    }


    // JoinPoint is the object that represents the pointcut --> in this case, the method call
    @AfterReturning(pointcut = "serviceMethods()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Service method {} returned: {}", joinPoint.getSignature().getName(), result);
    }
}

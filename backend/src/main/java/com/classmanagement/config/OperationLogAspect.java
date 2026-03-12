package com.classmanagement.config;

import com.alibaba.fastjson2.JSON;
import com.classmanagement.entity.OperationLog;
import com.classmanagement.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;

    @Pointcut("execution(* com.classmanagement.controller.*.*(..)) " +
            "&& !execution(* com.classmanagement.controller.OperationLogController.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        String method = request.getMethod();

        // 只记录写操作
        if ("GET".equalsIgnoreCase(method)) {
            return joinPoint.proceed();
        }

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        try {
            OperationLog operationLog = new OperationLog();
            operationLog.setRequestMethod(method);
            operationLog.setRequestUrl(request.getRequestURI());
            operationLog.setOperatorName("年级主任");
            operationLog.setExecutionTime(executionTime);
            operationLog.setIpAddress(getIpAddress(request));

            // 解析模块
            String uri = request.getRequestURI();
            operationLog.setOperationModule(parseModule(uri));
            operationLog.setOperationType(parseOperationType(method));
            operationLog.setOperationDesc(buildDesc(method, uri));

            // 记录参数（截取前500字符）
            try {
                String params = JSON.toJSONString(joinPoint.getArgs());
                if (params.length() > 500) {
                    params = params.substring(0, 500) + "...";
                }
                operationLog.setRequestParams(params);
            } catch (Exception e) {
                operationLog.setRequestParams("参数序列化失败");
            }

            operationLogService.save(operationLog);
        } catch (Exception e) {
            log.error("操作日志记录失败", e);
        }

        return result;
    }

    private String parseModule(String uri) {
        if (uri.contains("/student")) return "student";
        if (uri.contains("/exam")) return "exam";
        if (uri.contains("/attendance")) return "attendance";
        if (uri.contains("/diary")) return "diary";
        if (uri.contains("/notification")) return "notification";
        if (uri.contains("/semester")) return "semester";
        if (uri.contains("/ai")) return "ai";
        return "other";
    }

    private String parseOperationType(String method) {
        return switch (method.toUpperCase()) {
            case "POST" -> "CREATE";
            case "PUT" -> "UPDATE";
            case "DELETE" -> "DELETE";
            default -> method;
        };
    }

    private String buildDesc(String method, String uri) {
        String module = parseModule(uri);
        String action = parseOperationType(method);
        return switch (action) {
            case "CREATE" -> "新增" + getModuleName(module);
            case "UPDATE" -> "修改" + getModuleName(module);
            case "DELETE" -> "删除" + getModuleName(module);
            default -> action + " " + module;
        };
    }

    private String getModuleName(String module) {
        return switch (module) {
            case "student" -> "学生";
            case "exam" -> "成绩";
            case "attendance" -> "考勤";
            case "diary" -> "日志";
            case "notification" -> "通知";
            case "semester" -> "学期";
            case "ai" -> "AI操作";
            default -> "其他";
        };
    }

    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

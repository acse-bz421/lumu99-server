package com.lumu99.forum.audit.aspect;

import com.lumu99.forum.audit.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditAspect {

    private final AuditLogService auditLogService;

    public AuditAspect(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object auditControllerCall(ProceedingJoinPoint joinPoint) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        if (!shouldAudit(request)) {
            return joinPoint.proceed();
        }

        AuthInfo authInfo = resolveAuthInfo();
        String action = request.getMethod() + " " + request.getRequestURI();
        String targetType = resolveTargetType(request.getRequestURI());
        String targetId = resolveTargetId(request.getRequestURI(), authInfo.userUuid());
        Object payload = resolvePayload(joinPoint.getArgs());
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        try {
            Object result = joinPoint.proceed();
            auditLogService.record(authInfo.userUuid(), authInfo.role(), action, targetType, targetId, payload, "SUCCESS", ip, userAgent);
            return result;
        } catch (Throwable ex) {
            auditLogService.record(authInfo.userUuid(), authInfo.role(), action, targetType, targetId, payload, "FAILURE", ip, userAgent);
            throw ex;
        }
    }

    private boolean shouldAudit(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/admin/")) {
            return true;
        }
        String method = request.getMethod();
        return "PUT".equalsIgnoreCase(method) && "/users/me/username".equals(path)
                || "PUT".equalsIgnoreCase(method) && "/users/me/password".equals(path)
                || "DELETE".equalsIgnoreCase(method) && "/users/me".equals(path);
    }

    private String resolveTargetType(String path) {
        if (path.startsWith("/admin/users/") || path.startsWith("/users/me")) {
            return "USER";
        }
        return "ADMIN_API";
    }

    private String resolveTargetId(String path, String currentUserUuid) {
        if (path.startsWith("/admin/users/")) {
            String remainder = path.substring("/admin/users/".length());
            int slashIdx = remainder.indexOf('/');
            return slashIdx >= 0 ? remainder.substring(0, slashIdx) : remainder;
        }
        if (path.startsWith("/users/me")) {
            return currentUserUuid;
        }
        return null;
    }

    private AuthInfo resolveAuthInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return new AuthInfo(null, null);
        }

        String userUuid = null;
        Object principal = authentication.getPrincipal();
        if (principal instanceof String principalValue && StringUtils.hasText(principalValue) && !"anonymousUser".equals(principalValue)) {
            userUuid = principalValue;
        }

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .map(authority -> authority.startsWith("ROLE_") ? authority.substring(5) : authority)
                .orElse(null);
        return new AuthInfo(userUuid, role);
    }

    private Object resolvePayload(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        for (Object arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg instanceof String || arg instanceof Number || arg instanceof Boolean || arg instanceof Enum<?>) {
                continue;
            }
            String name = arg.getClass().getName();
            if (name.startsWith("jakarta.servlet.") || name.startsWith("org.springframework.")) {
                continue;
            }
            return arg;
        }
        return null;
    }

    private record AuthInfo(String userUuid, String role) {
    }
}

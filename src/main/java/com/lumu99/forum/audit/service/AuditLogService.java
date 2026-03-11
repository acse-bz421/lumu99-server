package com.lumu99.forum.audit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private static final String MASKED_VALUE = "******";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AuditLogService(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    public void record(String operatorUuid,
                       String operatorRole,
                       String action,
                       String targetType,
                       String targetId,
                       Object requestPayload,
                       String result,
                       String ip,
                       String userAgent) {
        String maskedPayload = serializeWithMask(requestPayload);
        jdbcTemplate.update(
                "INSERT INTO audit_logs (operator_uuid, operator_role, action, target_type, target_id, request_payload, result, ip, user_agent) VALUES (?,?,?,?,?,?,?,?,?)",
                operatorUuid,
                operatorRole,
                action,
                targetType,
                targetId,
                maskedPayload,
                result,
                ip,
                userAgent
        );
        log.info("audit action={} result={} operatorUuid={} role={} targetType={} targetId={} payload={}",
                action, result, operatorUuid, operatorRole, targetType, targetId, maskedPayload);
    }

    public String serializeWithMask(Object payload) {
        if (payload == null) {
            return null;
        }
        Object masked = maskNode(toGenericNode(payload));
        try {
            return objectMapper.writeValueAsString(masked);
        } catch (JsonProcessingException ex) {
            return String.valueOf(masked);
        }
    }

    private Object toGenericNode(Object payload) {
        try {
            return objectMapper.convertValue(payload, Object.class);
        } catch (IllegalArgumentException ex) {
            return payload;
        }
    }

    private Object maskNode(Object node) {
        if (node instanceof Map<?, ?> mapNode) {
            Map<String, Object> masked = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapNode.entrySet()) {
                String key = String.valueOf(entry.getKey());
                if (isPasswordField(key)) {
                    masked.put(key, MASKED_VALUE);
                    continue;
                }
                masked.put(key, maskNode(entry.getValue()));
            }
            return masked;
        }

        if (node instanceof List<?> listNode) {
            List<Object> masked = new ArrayList<>(listNode.size());
            for (Object item : listNode) {
                masked.add(maskNode(item));
            }
            return masked;
        }

        return node;
    }

    private boolean isPasswordField(String key) {
        String normalized = key.toLowerCase(Locale.ROOT);
        return normalized.contains("password");
    }
}

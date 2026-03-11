package com.lumu99.forum.audit;

import com.lumu99.forum.audit.service.AuditLogService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "spring.flyway.enabled=true")
@ActiveProfiles("test")
class AuditMaskingTest {

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void passwordMustBeMaskedInAuditPayload() {
        String payload = auditLogService.serializeWithMask(
                Map.of("newPassword", "Secret123", "otherField", "value")
        );

        assertThat(payload).doesNotContain("Secret123");
        assertThat(payload).contains("******");
    }
}

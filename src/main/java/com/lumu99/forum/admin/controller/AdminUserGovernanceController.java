package com.lumu99.forum.admin.controller;

import com.lumu99.forum.common.exception.BusinessException;
import com.lumu99.forum.user.repository.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminUserGovernanceController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserGovernanceController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PutMapping("/{userUuid}/ban")
    public ResponseEntity<Map<String, Object>> ban(@PathVariable String userUuid,
                                                    @Valid @RequestBody BanRequest request) {
        requireUser(userUuid);
        String status = request.banned() ? "BANNED" : "ACTIVE";
        userRepository.updateStatus(userUuid, status);
        return ResponseEntity.ok(Map.of("data", Map.of("userUuid", userUuid, "status", status)));
    }

    @PutMapping("/{userUuid}/mute")
    public ResponseEntity<Map<String, Object>> mute(@PathVariable String userUuid,
                                                     @Valid @RequestBody MuteRequest request) {
        requireUser(userUuid);
        String muteStatus = request.muted() ? "MUTED" : "NORMAL";
        userRepository.updateMuteStatus(userUuid, muteStatus);
        return ResponseEntity.ok(Map.of("data", Map.of("userUuid", userUuid, "muteStatus", muteStatus)));
    }

    @PutMapping("/{userUuid}/reset-username")
    public ResponseEntity<Map<String, Object>> resetUsername(@PathVariable String userUuid,
                                                              @Valid @RequestBody ResetUsernameRequest request) {
        UserRepository.UserRecord user = requireUser(userUuid);
        userRepository.findByUsername(request.newUsername()).ifPresent(existing -> {
            if (!existing.userUuid().equals(user.userUuid())) {
                throw new BusinessException(HttpStatus.CONFLICT, "REG_409_USERNAME_EXISTS", "Username already exists");
            }
        });
        userRepository.updateUsername(userUuid, request.newUsername());
        return ResponseEntity.ok(Map.of("data", Map.of("userUuid", userUuid, "username", request.newUsername())));
    }

    @PutMapping("/{userUuid}/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@PathVariable String userUuid,
                                                              @Valid @RequestBody ResetPasswordRequest request) {
        requireUser(userUuid);
        String passwordHash = passwordEncoder.encode(request.newPassword());
        userRepository.updatePasswordHash(userUuid, passwordHash);
        return ResponseEntity.ok(Map.of("message", "OK"));
    }

    private UserRepository.UserRecord requireUser(String userUuid) {
        return userRepository.findByUserUuid(userUuid).orElseThrow(() ->
                new BusinessException(HttpStatus.NOT_FOUND, "REQ_404_NOT_FOUND", "User not found"));
    }

    public record BanRequest(@NotNull Boolean banned) {
    }

    public record MuteRequest(@NotNull Boolean muted) {
    }

    public record ResetUsernameRequest(@NotBlank String newUsername) {
    }

    public record ResetPasswordRequest(@NotBlank String newPassword) {
    }
}

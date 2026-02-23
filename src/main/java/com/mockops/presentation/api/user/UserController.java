package com.mockops.presentation.api.user;

import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.entity.UserAgreement;
import com.mockops.domain.user.service.UserAgreementService;
import com.mockops.domain.user.service.UserService;
import com.mockops.domain.project.service.InvitationService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.global.util.CookieUtils;
import com.mockops.presentation.api.user.docs.UserDocs;
import com.mockops.presentation.api.user.dto.UserAgreementRequest;
import com.mockops.presentation.api.user.dto.UserAgreementResponse;
import com.mockops.presentation.api.user.dto.UserDetailResponse;
import com.mockops.presentation.api.user.dto.UserUpdateNicknameRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserDocs {

    private final UserService userService;
    private final UserAgreementService userAgreementService;
    private final InvitationService invitationService;
    private final CookieUtils cookieUtils;

    @Value("${server.ssl.enabled:false}")
    private boolean isSecure;

    @Override
    @GetMapping("/me")
    public ResponseEntity<UnifiedResponse<UserDetailResponse>> getCurrentUser(@AuthenticationPrincipal Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UnifiedResponse.success(UserDetailResponse.from(user)));
    }

    @Override
    @PatchMapping("/me/nickname")
    public ResponseEntity<UnifiedResponse<UserDetailResponse>> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserUpdateNicknameRequest request) {
        User user = userService.updateNickname(userId, request.nickname());
        return ResponseEntity.ok(UnifiedResponse.success(UserDetailResponse.from(user)));
    }

    @Override
    @DeleteMapping("/me")
    public ResponseEntity<UnifiedResponse<Void>> withdrawUser(
            @AuthenticationPrincipal Long userId,
            HttpServletResponse response) {
        userService.withdrawUser(userId);

        // Refresh Token 쿠키 만료 처리 (로그아웃)
        cookieUtils.clearRefreshTokenCookie(response, isSecure);

        return ResponseEntity.ok(UnifiedResponse.success(null));
    }

    @Override
    @PostMapping("/me/agreements")
    public ResponseEntity<UnifiedResponse<List<UserAgreementResponse>>> agreeToTerms(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserAgreementRequest request) {
        List<UserAgreement> agreements = userAgreementService.agreeToTerms(userId, request);
        if (request.invitationToken() != null && !request.invitationToken().isBlank()) {
            invitationService.acceptInvitation(request.invitationToken(), userId);
        }
        return ResponseEntity.ok(UnifiedResponse.success(UserAgreementResponse.from(agreements)));
    }

    @Override
    @GetMapping("/me/agreements")
    public ResponseEntity<UnifiedResponse<List<UserAgreementResponse>>> getUserAgreements(
            @AuthenticationPrincipal Long userId) {
        List<UserAgreement> agreements = userAgreementService.getUserAgreements(userId);
        return ResponseEntity.ok(UnifiedResponse.success(UserAgreementResponse.from(agreements)));
    }
}

package com.mockops.presentation.api.user;

import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.service.UserService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentation.api.user.docs.UserDocs;
import com.mockops.presentation.api.user.dto.UserDetailResponse;
import com.mockops.presentation.api.user.dto.UserUpdateNicknameRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController implements UserDocs {

    private final UserService userService;

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
}

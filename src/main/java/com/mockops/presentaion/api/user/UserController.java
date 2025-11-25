package com.mockops.presentaion.api.user;

import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.service.UserService;
import com.mockops.global.common.UnifiedResponse;
import com.mockops.presentaion.api.user.dto.UserDetailResponse;
import com.mockops.presentaion.api.user.dto.UserUpdateNicknameRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UnifiedResponse<UserDetailResponse>> getCurrentUser(@AuthenticationPrincipal Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(UnifiedResponse.success(UserDetailResponse.from(user)));
    }

    @PatchMapping("/me/nickname")
    public ResponseEntity<UnifiedResponse<UserDetailResponse>> updateNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UserUpdateNicknameRequest request) {
        User user = userService.updateNickname(userId, request.getNickname());
        return ResponseEntity.ok(UnifiedResponse.success(UserDetailResponse.from(user)));
    }
}

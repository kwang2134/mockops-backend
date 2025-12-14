package com.mockops.domain.user.service;

import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.repository.UserRepository;
import com.mockops.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.domainException(
                        "해당하는 사용자가 존재하지 않습니다. userId=" + userId
                ));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> ErrorCode.USER_NOT_FOUND.domainException(
                        "해당하는 사용자가 존재하지 않습니다. email=" + email
                ));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public User updateNickname(Long userId, String newNickname) {
        User user = getUserById(userId);
        user.updateNickname(newNickname);
        return user;
    }

    /**
     * 회원 탈퇴
     * 사용자 정보를 익명화하여 하드 딜리트 효과를 냄 (컬럼은 유지)
     */
    @Transactional
    public void withdrawUser(Long userId) {
        User user = getUserById(userId);
        user.withdraw();
        log.info("회원 탈퇴 처리 완료: userId={}, email={}", userId, user.getEmail());
    }
}

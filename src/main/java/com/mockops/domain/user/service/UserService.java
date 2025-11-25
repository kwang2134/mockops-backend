package com.mockops.domain.user.service;

import com.mockops.domain.user.entity.User;
import com.mockops.domain.user.repository.UserRepository;
import com.mockops.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public User updateNickname(Long userId, String newNickname) {
        User user = getUserById(userId);
        user.updateNickname(newNickname);
        return user;
    }
}

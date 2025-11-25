package com.mockops.domain.user.entity;

import com.mockops.domain.common.entity.BaseEntity;
import com.mockops.domain.user.role.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String email, String nickname, Role role) {
        this.email = email;
        this.nickname = nickname;
        this.role = role;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}

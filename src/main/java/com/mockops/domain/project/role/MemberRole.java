package com.mockops.domain.project.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberRole {
    OWNER(4),       // 최고 권한: 프로젝트 소유자
    MANAGER(3),     // 관리 권한: 멤버 관리 가능
    DEVELOPER(2),   // 개발 권한: CORS, API 수정 가능
    VIEWER(1);      // 조회 권한: 읽기만 가능

    private final int level;

    /**
     * 요구되는 권한 레벨 이상인지 확인
     */
    public boolean hasPermission(MemberRole requiredRole) {
        return this.level >= requiredRole.level;
    }
}

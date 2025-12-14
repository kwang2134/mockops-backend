package com.mockops.presentation.api.project.dto.projectmember;

import com.mockops.presentation.api.project.dto.project.ProjectMemberResponse;

import java.util.List;

/**
 * 프로젝트 멤버 목록 응답 (Offset 기반 페이징)
 */
public record MemberListResponse(
        List<ProjectMemberResponse> members,
        Boolean hasNext,
        Integer nextOffset
) {
}

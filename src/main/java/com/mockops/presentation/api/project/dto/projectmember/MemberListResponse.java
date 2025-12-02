package com.mockops.presentation.api.project.dto.projectmember;

import com.mockops.presentation.api.project.dto.project.ProjectMemberResponse;

import java.util.List;

public record MemberListResponse(
        List<ProjectMemberResponse> members,
        Boolean hasNext,
        Long nextCursorId
) {
}

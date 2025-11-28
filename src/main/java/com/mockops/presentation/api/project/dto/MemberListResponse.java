package com.mockops.presentation.api.project.dto;

import java.util.List;

public record MemberListResponse(
        List<ProjectMemberResponse> members,
        Boolean hasNext,
        Long nextCursorId
) {
}

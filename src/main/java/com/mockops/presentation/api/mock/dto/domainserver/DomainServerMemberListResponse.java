package com.mockops.presentation.api.mock.dto.domainserver;

import com.mockops.presentation.api.project.dto.project.ProjectMemberResponse;

import java.util.List;

public record DomainServerMemberListResponse(
        List<ProjectMemberResponse> members,
        boolean hasNext,
        Integer nextOffset,
        Long myDomainServerId,
        boolean isParticipating
) {
    public static DomainServerMemberListResponse of(
            List<ProjectMemberResponse> members,
            boolean hasNext,
            Integer nextOffset,
            Long myDomainServerId,
            Long currentServerId
    ) {
        boolean isParticipating = myDomainServerId != null && myDomainServerId.equals(currentServerId);
        return new DomainServerMemberListResponse(members, hasNext, nextOffset, myDomainServerId, isParticipating);
    }
}

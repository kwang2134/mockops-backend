package com.mockops.presentation.api.project.dto.invitation;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 초대 목록 페이징 응답 DTO
 */
public record PagedInvitationListResponse(
    List<InvitationInfo> invitations,
    Long totalElements,
    Integer totalPages,
    Integer currentPage
) {
    public static PagedInvitationListResponse from(Page<InvitationInfo> page) {
        return new PagedInvitationListResponse(
            page.getContent(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.getNumber()
        );
    }
}

package com.mockops.presentation.api.project.dto;

import java.util.List;

public record ProjectPageResponse(
        List<ProjectResponse> data,
        Integer totalPages,
        Long totalElements,
        Integer currentPage
) {
}

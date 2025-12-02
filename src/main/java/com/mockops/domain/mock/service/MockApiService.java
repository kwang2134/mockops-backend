package com.mockops.domain.mock.service;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;
import com.mockops.domain.mock.event.BulkCreationEvent;
import com.mockops.domain.mock.infrastructure.MockApiCachePort;
import com.mockops.domain.mock.repository.MockApiRepository;
import com.mockops.domain.project.role.MemberRole;
import com.mockops.domain.project.service.ProjectMemberService;
import com.mockops.global.exception.ErrorCode;
import com.mockops.presentation.api.mock.dto.mockapi.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Mock API 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MockApiService {

    private final MockApiRepository mockApiRepository;
    private final DomainServerService domainServerService;
    private final ProjectMemberService projectMemberService;
    private final MockApiCachePort mockApiCachePort;
    private final ApplicationEventPublisher eventPublisher;
    private final com.mockops.domain.job.service.JobTrackingService jobTrackingService;

    /**
     * Mock API ID로 조회 - DTO 반환 (상세 정보, responseBody 포함)
     */
    public MockApiDetailResponse getMockApiByIdWithResponse(Long mockApiId, Long currentUserId) {
        MockApi mockApi = getMockApiById(mockApiId, currentUserId);
        return MockApiDetailResponse.from(mockApi);
    }

    /**
     * Mock API ID로 조회 - 내부용
     */
    public MockApi getMockApiById(Long mockApiId, Long currentUserId) {
        MockApi mockApi = mockApiRepository.findById(mockApiId)
            .orElseThrow(() -> ErrorCode.MOCK_API_NOT_FOUND.domainException(
                "존재하지 않는 Mock API입니다. mockApiId=" + mockApiId
            ));

        // 권한 검증: 프로젝트 멤버만 조회 가능
        DomainServer server = domainServerService.getDomainServerById(mockApi.getServerId());
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.VIEWER);

        return mockApi;
    }

    /**
     * 서버의 Mock API 목록 조회 (커서 기반 페이징, 그룹핑) - DTO 반환
     */
    public MockApiListResponse getMockApisByServerWithResponse(Long serverId, Long cursorId, int size, Long currentUserId) {
        // size + 1 개를 조회하여 hasNext 판단
        List<MockApi> mockApis = getMockApisByServer(serverId, cursorId, size + 1, currentUserId);

        // hasNext 판단
        boolean hasNext = mockApis.size() > size;
        List<MockApi> actualMockApis = hasNext ? mockApis.subList(0, size) : mockApis;

        // name으로 그룹핑
        var groupedByName = actualMockApis.stream()
            .map(MockApiResponse::from)
            .collect(java.util.stream.Collectors.groupingBy(
                MockApiResponse::name,
                java.util.LinkedHashMap::new,
                java.util.stream.Collectors.toList()
            ));

        // MockApiGroupDto 리스트 생성
        List<MockApiGroupDto> groups = groupedByName.entrySet().stream()
            .map(entry -> MockApiGroupDto.of(entry.getKey(), entry.getValue()))
            .toList();

        // nextCursorId 계산 (hasNext가 true면 마지막 항목의 ID)
        Long nextCursorId = hasNext ? actualMockApis.get(actualMockApis.size() - 1).getId() : null;

        return MockApiListResponse.of(groups, nextCursorId, hasNext);
    }

    /**
     * 서버의 Mock API 목록 조회 (커서 기반 페이징) - 내부용
     */
    public List<MockApi> getMockApisByServer(Long serverId, Long cursorId, int pageSize, Long currentUserId) {
        // 권한 검증
        DomainServer server = domainServerService.getDomainServerById(serverId);
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.VIEWER);

        PageRequest pageRequest = PageRequest.of(0, pageSize);

        if (cursorId == null) {
            // 첫 페이지 조회
            return mockApiRepository.findByServerIdOrderByIdAsc(serverId, pageRequest);
        } else {
            // 커서 이후 데이터 조회
            return mockApiRepository.findByServerIdAndIdGreaterThanOrderByIdAsc(serverId, cursorId, pageRequest);
        }
    }

    /**
     * Mock API 생성 - MockApiCreateResponse 반환
     */
    @Transactional
    public MockApiCreateResponse createMockApiWithResponse(Long serverId, String name, HttpMethod httpMethod,
                                                           String endpointPath, String responseBody,
                                                           Integer statusCode, Boolean isActive, Long currentUserId) {
        MockApi mockApi = createMockApi(serverId, name, httpMethod, endpointPath, responseBody, statusCode, isActive, currentUserId);
        return MockApiCreateResponse.from(mockApi);
    }

    /**
     * Mock API 생성 - 내부용
     */
    @Transactional
    public MockApi createMockApi(Long serverId, String name, HttpMethod httpMethod,
                                 String endpointPath, String responseBody,
                                 Integer statusCode, Boolean isActive, Long currentUserId) {
        // 권한 검증: DEVELOPER 이상만 생성 가능
        DomainServer server = domainServerService.getDomainServerById(serverId);
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // 중복 확인 (서버 내에서 HTTP 메서드 + 엔드포인트 경로 조합은 유일해야 함)
        mockApiRepository.findByServerIdAndHttpMethodAndEndpointPath(serverId, httpMethod, endpointPath)
            .ifPresent(existingMock -> {
                throw ErrorCode.MOCK_API_DUPLICATED.serviceException(
                    "이미 존재하는 Mock API입니다. method=" + httpMethod + ", path=" + endpointPath
                );
            });

        // name이 null인 경우 endpointPath에서 파싱하여 자동 생성
        String finalName = name;
        if (finalName == null || finalName.isBlank()) {
            finalName = parseGroupNameFromPath(endpointPath);
        }

        MockApi mockApi = MockApi.builder()
            .serverId(serverId)
            .name(finalName)
            .httpMethod(httpMethod)
            .endpointPath(endpointPath)
            .responseBody(responseBody)
            .statusCode(statusCode != null ? statusCode : 200)
            .isActive(isActive != null ? isActive : true)
            .build();

        MockApi savedMockApi = mockApiRepository.save(mockApi);

        log.info("Mock API 생성 완료: mockApiId={}, serverId={}, method={}, path={}, name={}",
            savedMockApi.getId(), serverId, httpMethod, endpointPath, finalName);

        return savedMockApi;
    }

    /**
     * endpointPath에서 그룹 이름 파싱
     * 예: /users/profile → users
     * 예: /api/v1/products → api
     */
    private String parseGroupNameFromPath(String endpointPath) {
        if (endpointPath == null || endpointPath.isEmpty()) {
            return "default";
        }

        // 앞뒤 슬래시 제거 후 첫 번째 세그먼트 추출
        String trimmed = endpointPath.trim();
        if (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }

        String[] segments = trimmed.split("/");
        if (segments.length > 0 && !segments[0].isEmpty()) {
            return segments[0];
        }

        return "default";
    }

    /**
     * Mock API 수정 - MockApiUpdateResponse 반환
     */
    @Transactional
    public MockApiUpdateResponse updateMockApiWithResponse(Long mockApiId, String name, HttpMethod httpMethod,
                                                     String endpointPath, String responseBody,
                                                     Integer statusCode, Boolean isActive, Long currentUserId) {
        MockApi mockApi = updateMockApi(mockApiId, name, httpMethod, endpointPath, responseBody, statusCode, isActive, currentUserId);
        return MockApiUpdateResponse.from(mockApi);
    }

    /**
     * Mock API 수정 - 내부용
     */
    @Transactional
    public MockApi updateMockApi(Long mockApiId, String name, HttpMethod httpMethod,
                                 String endpointPath, String responseBody,
                                 Integer statusCode, Boolean isActive, Long currentUserId) {
        MockApi mockApi = mockApiRepository.findById(mockApiId)
            .orElseThrow(() -> ErrorCode.MOCK_API_NOT_FOUND.domainException(
                "존재하지 않는 Mock API입니다. mockApiId=" + mockApiId
            ));

        // 권한 검증: DEVELOPER 이상만 수정 가능
        DomainServer server = domainServerService.getDomainServerById(mockApi.getServerId());
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // 기존 캐시 삭제 (변경 전 엔드포인트 경로 기준)
        mockApiCachePort.evictMockApi(
            server.getProjectId(),
            server.getName(),
            mockApi.getHttpMethod(),
            mockApi.getEndpointPath()
        );

        // 엔드포인트 경로나 메서드가 변경되는 경우 중복 확인
        if (!mockApi.getEndpointPath().equals(endpointPath) || mockApi.getHttpMethod() != httpMethod) {
            mockApiRepository.findByServerIdAndHttpMethodAndEndpointPath(mockApi.getServerId(), httpMethod, endpointPath)
                .ifPresent(existingMock -> {
                    if (!existingMock.getId().equals(mockApiId)) {
                        throw ErrorCode.MOCK_API_DUPLICATED.serviceException(
                            "이미 존재하는 Mock API입니다. method=" + httpMethod + ", path=" + endpointPath
                        );
                    }
                });
        }

        mockApi.updateMockApi(name, httpMethod, responseBody, statusCode, isActive);

        // 변경된 경로의 캐시도 삭제 (경로가 변경된 경우)
        if (!mockApi.getEndpointPath().equals(endpointPath) || mockApi.getHttpMethod() != httpMethod) {
            mockApiCachePort.evictMockApi(
                server.getProjectId(),
                server.getName(),
                httpMethod,
                endpointPath
            );
        }

        log.info("Mock API 수정 완료: mockApiId={}, method={}, path={}",
            mockApiId, httpMethod, endpointPath);

        return mockApi;
    }

    /**
     * Mock API 삭제
     */
    @Transactional
    public void deleteMockApi(Long mockApiId, Long currentUserId) {
        MockApi mockApi = mockApiRepository.findById(mockApiId)
            .orElseThrow(() -> ErrorCode.MOCK_API_NOT_FOUND.domainException(
                "존재하지 않는 Mock API입니다. mockApiId=" + mockApiId
            ));

        // 권한 검증: DEVELOPER 이상만 삭제 가능
        DomainServer server = domainServerService.getDomainServerById(mockApi.getServerId());
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // 캐시 삭제
        mockApiCachePort.evictMockApi(
            server.getProjectId(),
            server.getName(),
            mockApi.getHttpMethod(),
            mockApi.getEndpointPath()
        );

        mockApiRepository.delete(mockApi);

        log.info("Mock API 삭제 완료: mockApiId={}, serverId={}", mockApiId, mockApi.getServerId());
    }

    /**
     * Mock API 활성화 상태 토글 - DTO 반환
     */
    @Transactional
    public MockApiResponse toggleMockApiStatusWithResponse(Long mockApiId, Long currentUserId) {
        MockApi mockApi = toggleMockApiStatus(mockApiId, currentUserId);
        return MockApiResponse.from(mockApi);
    }

    /**
     * Mock API 활성화 상태 토글 - 내부용
     */
    @Transactional
    public MockApi toggleMockApiStatus(Long mockApiId, Long currentUserId) {
        MockApi mockApi = mockApiRepository.findById(mockApiId)
            .orElseThrow(() -> ErrorCode.MOCK_API_NOT_FOUND.domainException(
                "존재하지 않는 Mock API입니다. mockApiId=" + mockApiId
            ));

        // 권한 검증: DEVELOPER 이상만 토글 가능
        DomainServer server = domainServerService.getDomainServerById(mockApi.getServerId());
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // 캐시 삭제 (활성화 상태 변경으로 인한 캐시 무효화)
        mockApiCachePort.evictMockApi(
            server.getProjectId(),
            server.getName(),
            mockApi.getHttpMethod(),
            mockApi.getEndpointPath()
        );

        mockApi.toggleActive();

        log.info("Mock API 상태 토글 완료: mockApiId={}, isActive={}", mockApiId, mockApi.getIsActive());

        return mockApi;
    }

    /**
     * OpenAPI 스펙 파일 업로드를 통한 Mock API 일괄 생성 (이벤트 발행)
     *
     * @param serverId 서버 ID
     * @param file OpenAPI 스펙 파일 (YAML 또는 JSON)
     * @param currentUserId 현재 사용자 ID
     * @return HTTP 202 Accepted 응답 (jobId 포함)
     */
    public MockApiBulkResponse createMockApisFromFile(Long serverId, MultipartFile file, Long currentUserId) {
        // 권한 검증: DEVELOPER 이상만 생성 가능
        DomainServer server = domainServerService.getDomainServerById(serverId);
        projectMemberService.validateMemberPermission(server.getProjectId(), currentUserId, MemberRole.DEVELOPER);

        // Job 생성 (PROCESSING 상태로 시작)
        com.mockops.domain.job.entity.JobEntity job = jobTrackingService.createJob(serverId);
        Long jobId = job.getId();

        log.info("Mock API 대량 생성 요청: jobId={}, serverId={}, filename={}, userId={}",
                jobId, serverId, file.getOriginalFilename(), currentUserId);

        // BulkCreationEvent 발행 (Non-Blocking)
        BulkCreationEvent event = new BulkCreationEvent(this, serverId, file, jobId);
        eventPublisher.publishEvent(event);

        log.info("BulkCreationEvent 발행 완료: jobId={}, serverId={}", jobId, serverId);

        // 즉시 202 Accepted 응답 반환
        return MockApiBulkResponse.accepted(jobId.toString());
    }
}

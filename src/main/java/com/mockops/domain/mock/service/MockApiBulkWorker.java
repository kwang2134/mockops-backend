package com.mockops.domain.mock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.domain.job.service.JobTrackingService;
import com.mockops.domain.mock.dto.ParsedMockApi;
import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.event.BulkCreationEvent;
import com.mockops.domain.mock.repository.DomainServerRepository;
import com.mockops.domain.mock.repository.MockApiRepository;
import com.mockops.domain.notification.entity.NotificationType;
import com.mockops.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Mock API 대량 생성 비동기 Worker
 * BulkCreationEvent를 수신하여 bulkTaskExecutor 스레드 풀에서 비동기적으로 실행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockApiBulkWorker {

    private final OpenApiParserService openApiParserService;
    private final MockApiBulkInsertService mockApiBulkInsertService;
    private final MockApiRepository mockApiRepository;
    private final JobTrackingService jobTrackingService;
    private final DomainServerRepository domainServerRepository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    /**
     * BulkCreationEvent 수신하여 비동기적으로 Mock API 대량 생성 처리
     *
     * @param event 대량 생성 이벤트
     */
    @EventListener
    @Async("bulkTaskExecutor")
    public void handleBulkCreationEvent(BulkCreationEvent event) {
        String threadName = Thread.currentThread().getName();
        Long jobId = event.getJobId();
        Long serverId = event.getServerId();

        log.info("[{}] Mock API 대량 생성 이벤트 수신: jobId={}, serverId={}, filename={}",
                threadName, jobId, serverId, event.getFile().getOriginalFilename());

        try {
            // 1. OpenAPI 스펙 파일 파싱
            List<ParsedMockApi> parsedMockApis = openApiParserService.parseOpenApiSpec(event.getFile());
            log.info("[{}] OpenAPI 파싱 완료: jobId={}, {}개 API 발견",
                    threadName, jobId, parsedMockApis.size());

            // 2. 벌크 셀렉트로 기존 API 조회 및 메모리 기반 중복 제거
            List<ParsedMockApi> uniqueMockApis = filterDuplicatesInMemory(serverId, parsedMockApis);
            int duplicateCount = parsedMockApis.size() - uniqueMockApis.size();

            log.info("[{}] 중복 제거 완료: jobId={}, 중복 {}개, 삽입 대상 {}개",
                    threadName, jobId, duplicateCount, uniqueMockApis.size());

            // 3. JDBC Batch Insert로 대량 삽입
            int insertedCount = 0;
            if (!uniqueMockApis.isEmpty()) {
                insertedCount = mockApiBulkInsertService.bulkInsert(serverId, uniqueMockApis);
            }

            log.info("[{}] Mock API 대량 생성 완료: jobId={}, serverId={}, 성공 {}개, 중복 {}개",
                    threadName, jobId, serverId, insertedCount, duplicateCount);

            // 작업 성공 처리
            jobTrackingService.markJobAsSuccess(jobId, parsedMockApis.size(), insertedCount);

            // Mock API 벌크 생성 성공 알림 생성
            createBulkSuccessNotification(serverId, jobId, insertedCount, duplicateCount, parsedMockApis.size());

        } catch (Exception e) {
            log.error("[{}] Mock API 대량 생성 실패: jobId={}, serverId={}, error={}",
                    threadName, jobId, serverId, e.getMessage(), e);

            // 작업 실패 처리
            String errorMessage = String.format("파일 처리 실패: %s - %s",
                    e.getClass().getSimpleName(), e.getMessage());
            jobTrackingService.markJobAsFailure(jobId, errorMessage);

            // Mock API 벌크 생성 실패 알림 생성
            createBulkFailureNotification(serverId, jobId, errorMessage);
        }
    }

    /**
     * 메모리 기반 중복 필터링 (Bulk Select 후 메모리에서 O(1) 조회)
     *
     * @param serverId 서버 ID
     * @param parsedMockApis 파싱된 Mock API 목록
     * @return 중복이 제거된 Mock API 목록
     */
    private List<ParsedMockApi> filterDuplicatesInMemory(Long serverId, List<ParsedMockApi> parsedMockApis) {
        // 1. Bulk Select로 기존 API의 메서드+경로 조합 조회 (단일 쿼리)
        List<String> existingCombinations = mockApiRepository.findMethodPathCombinationsByServerId(serverId);
        Set<String> existingSet = new HashSet<>(existingCombinations);

        log.debug("기존 Mock API 로드 완료: serverId={}, 개수={}", serverId, existingSet.size());

        // 2. 메모리 내 Set으로 O(1) 중복 체크
        List<ParsedMockApi> uniqueMockApis = new ArrayList<>();

        for (ParsedMockApi parsedApi : parsedMockApis) {
            String combination = parsedApi.getHttpMethod() + ":" + parsedApi.getEndpointPath();

            if (!existingSet.contains(combination)) {
                uniqueMockApis.add(parsedApi);
                // 현재 배치에서 중복 방지를 위해 Set에 추가
                existingSet.add(combination);
            } else {
                log.debug("중복된 Mock API 스킵: {}", combination);
            }
        }

        return uniqueMockApis;
    }

    /**
     * Mock API 벌크 생성 성공 알림 생성
     */
    private void createBulkSuccessNotification(Long serverId, Long jobId, int insertedCount, int duplicateCount, int totalParsed) {
        try {
            DomainServer server = domainServerRepository.findById(serverId).orElse(null);
            if (server == null) {
                log.warn("서버를 찾을 수 없어 알림 생성 실패: serverId={}", serverId);
                return;
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("serverId", serverId);
            metadata.put("serverName", server.getName());
            metadata.put("projectId", server.getProjectId());
            metadata.put("jobId", jobId);
            metadata.put("totalParsed", totalParsed);
            metadata.put("insertedCount", insertedCount);
            metadata.put("duplicateCount", duplicateCount);
            String metadataJson = objectMapper.writeValueAsString(metadata);

            notificationService.createNotification(
                    null,  // recipientUserId (server-attributed)
                    serverId,  // domainServerId
                    NotificationType.MOCK_BULK_SUCCESS,
                    "Mock API 일괄 생성 성공",
                    String.format("서버 '%s'에 Mock API %d개가 성공적으로 생성되었습니다. (중복 제외: %d개)",
                            server.getName(), insertedCount, duplicateCount),
                    metadataJson
            );
            log.info("Mock API 벌크 생성 성공 알림 생성: serverId={}, jobId={}", serverId, jobId);
        } catch (Exception e) {
            log.error("Mock API 벌크 생성 성공 알림 생성 실패: serverId={}, jobId={}, error={}",
                    serverId, jobId, e.getMessage());
        }
    }

    /**
     * Mock API 벌크 생성 실패 알림 생성
     */
    private void createBulkFailureNotification(Long serverId, Long jobId, String errorMessage) {
        try {
            DomainServer server = domainServerRepository.findById(serverId).orElse(null);
            if (server == null) {
                log.warn("서버를 찾을 수 없어 알림 생성 실패: serverId={}", serverId);
                return;
            }

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("serverId", serverId);
            metadata.put("serverName", server.getName());
            metadata.put("projectId", server.getProjectId());
            metadata.put("jobId", jobId);
            metadata.put("errorMessage", errorMessage);
            String metadataJson = objectMapper.writeValueAsString(metadata);

            notificationService.createNotification(
                    null,  // recipientUserId (server-attributed)
                    serverId,  // domainServerId
                    NotificationType.MOCK_BULK_FAILURE,
                    "Mock API 일괄 생성 실패",
                    String.format("서버 '%s'의 Mock API 일괄 생성이 실패했습니다. 원인: %s",
                            server.getName(), errorMessage),
                    metadataJson
            );
            log.info("Mock API 벌크 생성 실패 알림 생성: serverId={}, jobId={}", serverId, jobId);
        } catch (Exception e) {
            log.error("Mock API 벌크 생성 실패 알림 생성 실패: serverId={}, jobId={}, error={}",
                    serverId, jobId, e.getMessage());
        }
    }
}
package com.mockops.global.handler;

import com.mockops.domain.mock.dto.MockCacheDto;
import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;
import com.mockops.domain.mock.infrastructure.MockApiCachePort;
import com.mockops.domain.mock.repository.MockApiRepository;
import com.mockops.domain.mock.service.DomainServerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import java.util.List;
import java.util.Optional;

/**
 * /mock/** 요청에 대한 커스텀 HandlerMapping
 * 요청 경로: /mock/{projectId}/{serverSlug}/**
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockOpsHandlerMapping implements HandlerMapping, Ordered {

    private static final String MOCK_PREFIX = "/mock/";
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final MockApiCachePort mockApiCachePort;
    private final MockApiRepository mockApiRepository;
    private final DomainServerService domainServerService;

    @Override
    public HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        String requestPath = request.getRequestURI();

        // /mock/ 으로 시작하지 않으면 처리하지 않음
        if (!requestPath.startsWith(MOCK_PREFIX)) {
            return null;
        }

        try {
            // 경로에서 projectId, serverSlug, 실제 경로 추출
            // 형식: /mock/{projectId}/{serverSlug}/**
            String pathAfterMock = requestPath.substring(MOCK_PREFIX.length());
            String[] parts = pathAfterMock.split("/", 3);

            if (parts.length < 3) {
                log.warn("잘못된 Mock 요청 경로: {}", requestPath);
                return null;
            }

            Long projectId = Long.parseLong(parts[0]);
            String serverSlug = parts[1];
            String apiPath = "/" + parts[2];
            HttpMethod httpMethod = HttpMethod.valueOf(request.getMethod().toUpperCase());

            log.debug("Mock 요청 파싱: projectId={}, serverSlug={}, apiPath={}, method={}",
                projectId, serverSlug, apiPath, httpMethod);

            // 1. 캐시에서 조회 시도
            Optional<MockCacheDto> cachedMock = mockApiCachePort.getMockApiFromCache(
                projectId, serverSlug, httpMethod, apiPath
            );

            if (cachedMock.isPresent()) {
                log.info("캐시 히트: projectId={}, serverSlug={}, path={}, method={}",
                    projectId, serverSlug, apiPath, httpMethod);
                MockApiHandler handler = new MockApiHandler(projectId, serverSlug, requestPath, cachedMock.get());
                return new HandlerExecutionChain(handler);
            }

            // 2. 캐시 미스 - DB에서 조회
            log.debug("캐시 미스 - DB 조회 시작");

            // 2-1. 서버 조회
            var server = domainServerService.getServerByProjectIdAndSlug(projectId, serverSlug);

            // 2-2. Mock API 목록 조회 (활성화 여부와 관계없이)
            List<MockApi> mockApis = mockApiRepository.findByServerId(server.getId());

            // 2-3. AntPathMatcher로 패턴 매칭
            MockApi matchedMock = null;
            for (MockApi mockApi : mockApis) {
                if (mockApi.getHttpMethod() == httpMethod &&
                    pathMatcher.match(mockApi.getEndpointPath(), apiPath)) {
                    matchedMock = mockApi;
                    log.info("패턴 매칭 성공: pattern={}, actual={}", mockApi.getEndpointPath(), apiPath);
                    break;
                }
            }

            if (matchedMock == null) {
                log.warn("매칭되는 Mock API 없음: projectId={}, serverSlug={}, path={}, method={}",
                    projectId, serverSlug, apiPath, httpMethod);
                return null;
            }

            // 2-4. 비활성화된 Mock API 확인
            if (!matchedMock.getIsActive()) {
                log.warn("비활성화된 Mock API 요청: projectId={}, serverSlug={}, path={}, method={}, mockApiId={}",
                    projectId, serverSlug, apiPath, httpMethod, matchedMock.getId());
                InactiveMockApiHandler inactiveHandler = new InactiveMockApiHandler(
                    projectId, serverSlug, requestPath, matchedMock.getId()
                );
                return new HandlerExecutionChain(inactiveHandler);
            }

            // 2-4. 캐시에 저장
            MockCacheDto cacheDto = MockCacheDto.from(matchedMock);
            mockApiCachePort.cacheMockApi(projectId, serverSlug, httpMethod, apiPath, cacheDto);

            MockApiHandler handler = new MockApiHandler(projectId, serverSlug, requestPath, cacheDto);
            return new HandlerExecutionChain(handler);

        } catch (NumberFormatException e) {
            log.error("projectId 파싱 실패: {}", requestPath, e);
            return null;
        } catch (IllegalArgumentException e) {
            log.error("HTTP 메서드 파싱 실패: {}", request.getMethod(), e);
            return null;
        } catch (Exception e) {
            log.error("Mock 요청 처리 중 오류 발생: {}", requestPath, e);
            return null;
        }
    }

    @Override
    public int getOrder() {
        // 다른 HandlerMapping보다 먼저 실행되도록 높은 우선순위 설정
        return Ordered.HIGHEST_PRECEDENCE;
    }
}

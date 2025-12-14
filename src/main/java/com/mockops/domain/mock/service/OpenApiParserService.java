package com.mockops.domain.mock.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mockops.domain.mock.dto.ParsedMockApi;
import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * OpenAPI 3.0 스펙 파일 파서 서비스
 * YAML 또는 JSON 형식의 OpenAPI 스펙을 파싱하여 Mock API 정보를 추출
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiParserService {

    private final ObjectMapper objectMapper;

    /**
     * OpenAPI 스펙 파일을 파싱하여 Mock API 목록 추출
     *
     * @param file OpenAPI 스펙 파일 (YAML 또는 JSON)
     * @return 파싱된 Mock API 목록
     */
    public List<ParsedMockApi> parseOpenApiSpec(MultipartFile file) {
        try {
            String filename = file.getOriginalFilename();
            if (filename == null) {
                throw ErrorCode.INVALID_FILE_FORMAT.serviceException("파일 이름이 없습니다.");
            }

            Map<String, Object> spec;
            if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
                spec = parseYaml(file);
            } else if (filename.endsWith(".json")) {
                spec = parseJson(file);
            } else {
                throw ErrorCode.INVALID_FILE_FORMAT.serviceException(
                        "지원하지 않는 파일 형식입니다. YAML(.yaml, .yml) 또는 JSON(.json)만 지원합니다."
                );
            }

            return extractMockApis(spec);

        } catch (IOException e) {
            log.error("파일 파싱 실패: {}", e.getMessage());
            throw ErrorCode.FILE_PARSE_ERROR.serviceException("파일 파싱 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * YAML 파일 파싱
     */
    private Map<String, Object> parseYaml(MultipartFile file) throws IOException {
        Yaml yaml = new Yaml();
        return yaml.load(file.getInputStream());
    }

    /**
     * JSON 파일 파싱
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(MultipartFile file) throws IOException {
        return objectMapper.readValue(file.getInputStream(), Map.class);
    }

    /**
     * OpenAPI 스펙에서 Mock API 정보 추출
     */
    @SuppressWarnings("unchecked")
    private List<ParsedMockApi> extractMockApis(Map<String, Object> spec) {
        List<ParsedMockApi> mockApis = new ArrayList<>();

        // OpenAPI 버전 확인
        String openApiVersion = (String) spec.get("openapi");
        if (openApiVersion == null || !openApiVersion.startsWith("3.")) {
            throw ErrorCode.INVALID_OPENAPI_VERSION.serviceException(
                    "OpenAPI 3.0 버전만 지원합니다. 현재 버전: " + openApiVersion
            );
        }

        // paths에서 엔드포인트 추출
        Map<String, Object> paths = (Map<String, Object>) spec.get("paths");
        if (paths == null || paths.isEmpty()) {
            log.warn("OpenAPI 스펙에 paths가 없습니다.");
            return mockApis;
        }

        // 각 경로에 대해 처리
        for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
            String path = pathEntry.getKey();
            Map<String, Object> pathItem = (Map<String, Object>) pathEntry.getValue();

            // 각 HTTP 메서드에 대해 처리
            for (Map.Entry<String, Object> methodEntry : pathItem.entrySet()) {
                String methodStr = methodEntry.getKey().toUpperCase();

                // HTTP 메서드인지 확인
                if (!isHttpMethod(methodStr)) {
                    continue;
                }

                Map<String, Object> operation = (Map<String, Object>) methodEntry.getValue();
                ParsedMockApi mockApi = extractMockApiFromOperation(path, methodStr, operation);

                if (mockApi != null) {
                    mockApis.add(mockApi);
                }
            }
        }

        log.info("OpenAPI 스펙에서 {} 개의 Mock API를 추출했습니다.", mockApis.size());
        return mockApis;
    }

    /**
     * Operation에서 Mock API 정보 추출
     */
    @SuppressWarnings("unchecked")
    private ParsedMockApi extractMockApiFromOperation(String path, String methodStr, Map<String, Object> operation) {
        try {
            // HTTP 메서드 변환
            HttpMethod httpMethod = HttpMethod.valueOf(methodStr);

            // 이름 추출 (tags 또는 summary에서)
            String name = extractName(operation, path);

            // 응답에서 상태 코드와 응답 본문 추출
            Map<String, Object> responses = (Map<String, Object>) operation.get("responses");
            if (responses == null || responses.isEmpty()) {
                log.warn("경로 {} {}에 응답 정의가 없습니다.", methodStr, path);
                return null;
            }

            // 첫 번째 성공 응답 코드 사용 (200, 201 등)
            Map.Entry<String, Object> firstResponse = responses.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith("2"))
                    .findFirst()
                    .orElse(responses.entrySet().iterator().next());

            String statusCodeStr = firstResponse.getKey();
            Integer statusCode = Integer.parseInt(statusCodeStr);
            Map<String, Object> responseObj = (Map<String, Object>) firstResponse.getValue();

            // 응답 본문 추출 (content > application/json > example)
            String responseBody = extractResponseBody(responseObj);

            return ParsedMockApi.builder()
                    .name(name)
                    .httpMethod(httpMethod)
                    .endpointPath(path)
                    .responseBody(responseBody)
                    .statusCode(statusCode)
                    .isActive(true)
                    .build();

        } catch (Exception e) {
            log.error("Operation 파싱 실패: {} {}, error: {}", methodStr, path, e.getMessage());
            return null;
        }
    }

    /**
     * 이름 추출 (tags 또는 summary에서)
     */
    @SuppressWarnings("unchecked")
    private String extractName(Map<String, Object> operation, String path) {
        // 1. tags에서 추출
        List<String> tags = (List<String>) operation.get("tags");
        if (tags != null && !tags.isEmpty()) {
            return tags.get(0);
        }

        // 2. summary에서 추출
        String summary = (String) operation.get("summary");
        if (summary != null && !summary.isEmpty()) {
            return summary;
        }

        // 3. 경로에서 추출 (예: /users → users)
        return parseGroupNameFromPath(path);
    }

    /**
     * 경로에서 그룹 이름 파싱
     */
    private String parseGroupNameFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return "default";
        }

        String trimmed = path.trim();
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
     * 응답 본문 추출
     * 우선순위: example (단수) > examples (복수) > schema
     */
    @SuppressWarnings("unchecked")
    private String extractResponseBody(Map<String, Object> responseObj) {
        try {
            Map<String, Object> content = (Map<String, Object>) responseObj.get("content");
            if (content == null) {
                return "{}";
            }

            Map<String, Object> applicationJson = (Map<String, Object>) content.get("application/json");
            if (applicationJson == null) {
                return "{}";
            }

            // 1. example (단수형) - OpenAPI 3.0 스타일
            Object example = applicationJson.get("example");
            if (example != null) {
                log.debug("example (단수형) 사용");
                return objectMapper.writeValueAsString(example);
            }

            // 2. examples (복수형) - OpenAPI 3.0 권장 스타일
            Map<String, Object> examples = (Map<String, Object>) applicationJson.get("examples");
            if (examples != null && !examples.isEmpty()) {
                // 첫 번째 example의 value 사용
                Map.Entry<String, Object> firstExample = examples.entrySet().iterator().next();
                String exampleKey = firstExample.getKey();
                Map<String, Object> exampleObj = (Map<String, Object>) firstExample.getValue();

                Object value = exampleObj.get("value");
                if (value != null) {
                    log.debug("examples (복수형) 사용: exampleKey={}", exampleKey);
                    return objectMapper.writeValueAsString(value);
                }
            }

            // 3. schema - 예시가 없을 때 fallback
            Object schema = applicationJson.get("schema");
            if (schema != null) {
                log.debug("schema fallback 사용 (example 없음)");
                return objectMapper.writeValueAsString(schema);
            }

            return "{}";

        } catch (Exception e) {
            log.warn("응답 본문 추출 실패: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * HTTP 메서드 여부 확인
     */
    private boolean isHttpMethod(String method) {
        try {
            HttpMethod.valueOf(method);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
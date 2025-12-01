package com.mockops.domain.mock.service;

import com.mockops.domain.mock.dto.ParsedMockApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Mock API 대량 삽입 서비스
 * JDBC Batching을 사용하여 대량의 Mock API를 효율적으로 DB에 삽입
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MockApiBulkInsertService {

    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL = """
        INSERT INTO mock_apis (
            server_id, name, http_method, endpoint_path,
            response_body, status_code, is_active,
            created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    /**
     * Mock API 목록을 단일 배치 요청으로 일괄 삽입
     *
     * @param serverId 서버 ID
     * @param parsedMockApis 파싱된 Mock API 목록
     * @return 삽입된 레코드 수
     */
    @Transactional
    public int bulkInsert(Long serverId, List<ParsedMockApi> parsedMockApis) {
        if (parsedMockApis == null || parsedMockApis.isEmpty()) {
            log.warn("삽입할 Mock API가 없습니다. serverId={}", serverId);
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();

        int[] updateCounts = jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                ParsedMockApi mockApi = parsedMockApis.get(i);

                ps.setLong(1, serverId);
                ps.setString(2, mockApi.getName());
                ps.setString(3, mockApi.getHttpMethod().name());
                ps.setString(4, mockApi.getEndpointPath());
                ps.setString(5, mockApi.getResponseBody());
                ps.setInt(6, mockApi.getStatusCode());
                ps.setBoolean(7, mockApi.getIsActive());
                ps.setTimestamp(8, Timestamp.valueOf(now));
                ps.setTimestamp(9, Timestamp.valueOf(now));
            }

            @Override
            public int getBatchSize() {
                return parsedMockApis.size();
            }
        });

        int totalInserted = 0;
        for (int count : updateCounts) {
            if (count > 0) {
                totalInserted++;
            }
        }

        log.info("Mock API 대량 삽입 완료: serverId={}, 총 {}개 중 {}개 성공",
                serverId, parsedMockApis.size(), totalInserted);

        return totalInserted;
    }
}
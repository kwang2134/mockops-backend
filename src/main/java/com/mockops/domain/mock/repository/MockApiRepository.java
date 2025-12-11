package com.mockops.domain.mock.repository;

import com.mockops.domain.mock.entity.HttpMethod;
import com.mockops.domain.mock.entity.MockApi;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MockApiRepository extends JpaRepository<MockApi, Long> {

    /**
     * 서버 ID로 Mock API 목록 조회 (커서 기반 페이징)
     * @deprecated 복합 커서 페이징 사용
     */
    @Deprecated
    List<MockApi> findByServerIdAndIdGreaterThanOrderByIdAsc(Long serverId, Long cursorId, Pageable pageable);

    /**
     * 서버 ID로 Mock API 목록 조회 (커서 없이 처음부터)
     * @deprecated 복합 커서 페이징 사용
     */
    @Deprecated
    List<MockApi> findByServerIdOrderByIdAsc(Long serverId, Pageable pageable);

    /**
     * 서버 ID로 Mock API 목록 조회 (name 정렬, 처음부터)
     * name 오름차순 -> ID 오름차순
     */
    List<MockApi> findByServerIdOrderByNameAscIdAsc(Long serverId, Pageable pageable);

    /**
     * 서버 ID로 Mock API 목록 조회 (name + id 복합 커서 페이징)
     * name 오름차순 -> ID 오름차순
     *
     * @param serverId 서버 ID
     * @param lastNameCursor 마지막 name 커서
     * @param lastIdCursor 마지막 ID 커서
     * @param pageable 페이지 정보
     * @return Mock API 목록
     */
    @Query("SELECT m FROM MockApi m WHERE m.serverId = :serverId " +
           "AND (m.name > :lastNameCursor OR (m.name = :lastNameCursor AND m.id > :lastIdCursor)) " +
           "ORDER BY m.name ASC, m.id ASC")
    List<MockApi> findByServerIdWithCompositeCursor(
            @Param("serverId") Long serverId,
            @Param("lastNameCursor") String lastNameCursor,
            @Param("lastIdCursor") Long lastIdCursor,
            Pageable pageable
    );

    /**
     * 서버 ID로 모든 Mock API 조회
     */
    List<MockApi> findByServerId(Long serverId);

    /**
     * 서버 ID, HTTP 메서드, 엔드포인트 경로로 조회
     */
    Optional<MockApi> findByServerIdAndHttpMethodAndEndpointPath(Long serverId, HttpMethod httpMethod, String endpointPath);

    /**
     * 서버 ID, HTTP 메서드, 엔드포인트 경로 존재 여부 확인
     */
    boolean existsByServerIdAndHttpMethodAndEndpointPath(Long serverId, HttpMethod httpMethod, String endpointPath);

    /**
     * 활성화된 Mock API만 조회
     */
    List<MockApi> findByServerIdAndIsActiveTrue(Long serverId);

    /**
     * 서버의 모든 Mock API의 HTTP 메서드와 엔드포인트 경로 조합을 조회
     * 중복 체크를 위한 벌크 셀렉트 (메모리 기반 필터링용)
     *
     * @param serverId 서버 ID
     * @return "METHOD:PATH" 형식의 문자열 리스트 (예: "GET:/users", "POST:/products")
     */
    @Query("SELECT CONCAT(m.httpMethod, ':', m.endpointPath) FROM MockApi m WHERE m.serverId = :serverId")
    List<String> findMethodPathCombinationsByServerId(@Param("serverId") Long serverId);
}

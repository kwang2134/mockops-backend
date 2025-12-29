package com.mockops.infrastructure.repository.mock;

import com.mockops.domain.mock.entity.DomainServer;
import com.mockops.domain.mock.entity.QDomainServer;
import com.mockops.domain.mock.entity.ServerStatus;
import com.mockops.domain.mock.repository.DomainServerRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QueryDSL 기반 도메인 서버 검색 구현체
 */
@Repository
@RequiredArgsConstructor
public class DomainServerRepositoryImpl implements DomainServerRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<DomainServer> searchDomainServers(Long projectId, String name, ServerStatus status, Pageable pageable) {
        QDomainServer domainServer = QDomainServer.domainServer;

        BooleanBuilder builder = new BooleanBuilder();

        // 프로젝트 ID 필터 (필수)
        builder.and(domainServer.projectId.eq(projectId));

        // 도메인 서버 이름 검색 (부분 일치)
        if (name != null && !name.isBlank()) {
            builder.and(domainServer.name.containsIgnoreCase(name));
        }

        // 서버 상태 필터
        if (status != null) {
            builder.and(domainServer.status.eq(status));
        }

        // 전체 카운트 조회
        Long total = queryFactory
                .select(domainServer.count())
                .from(domainServer)
                .where(builder)
                .fetchOne();

        long totalCount = (total != null) ? total : 0L;

        // 페이징 조회
        List<DomainServer> content = queryFactory
                .selectFrom(domainServer)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(domainServer.createdAt.desc())
                .fetch();

        return new PageImpl<>(content, pageable, totalCount);
    }
}
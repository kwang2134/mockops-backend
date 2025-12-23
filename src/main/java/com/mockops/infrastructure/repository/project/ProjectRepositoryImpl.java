package com.mockops.infrastructure.repository.project;

import com.mockops.domain.project.entity.Project;
import com.mockops.domain.project.entity.QProject;
import com.mockops.domain.project.repository.ProjectRepositoryCustom;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QueryDSL 기반 프로젝트 검색 구현체
 */
@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Project> searchProjects(String name, Long ownerId, Pageable pageable) {
        QProject project = QProject.project;

        BooleanBuilder builder = new BooleanBuilder();

        // 프로젝트 제목 검색 (부분 일치)
        if (name != null && !name.isBlank()) {
            builder.and(project.name.containsIgnoreCase(name));
        }

        // 오너 ID 검색 (정확히 일치)
        if (ownerId != null) {
            builder.and(project.ownerId.eq(ownerId));
        }

        // 전체 카운트 조회
        long total = queryFactory
                .selectFrom(project)
                .where(builder)
                .fetchCount();

        // 페이징 조회
        List<Project> content = queryFactory
                .selectFrom(project)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(project.createdAt.desc())
                .fetch();

        return new PageImpl<>(content, pageable, total);
    }
}
package com.the11job.backend.job.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.the11job.backend.job.dto.JobFilterRequest;
import com.the11job.backend.job.entity.Job;
import com.the11job.backend.job.entity.QJob;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JobRepositoryImpl implements JobRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QJob job = QJob.job;

    @Override
    public Page<Job> findJobsByFilter(JobFilterRequest request, Pageable pageable) {

        BooleanBuilder builder = buildFilterCondition(request);

        // 1. 데이터 조회 (Offset, Limit, Ordering 적용)
        List<Job> results = queryFactory
                .selectFrom(job)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifiers(pageable.getSort()))
                .fetch();

        // 2. 전체 개수 조회
        long total = Optional.ofNullable(queryFactory
                .select(job.count())
                .from(job)
                .where(builder)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(results, pageable, total);
    }

    /**
     * Pageable의 Sort 정보를 Querydsl의 OrderSpecifier 배열로 변환합니다.
     */
    private OrderSpecifier[] getOrderSpecifiers(Sort sort) {
        Path<Job> entityPath = job;
        PathBuilder<Job> builder = new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());

        return sort.stream()
                .map(order -> {
                    // company.name과 같은 관계 필드에 대한 정렬을 PathBuilder가 처리하도록 위임
                    Path<?> path = builder.get(order.getProperty());

                    return order.isAscending()
                            ? new OrderSpecifier(com.querydsl.core.types.Order.ASC,
                            (com.querydsl.core.types.Expression) path)
                            : new OrderSpecifier(com.querydsl.core.types.Order.DESC,
                                    (com.querydsl.core.types.Expression) path);
                })
                .toArray(OrderSpecifier[]::new);
    }

    /**
     * 요청 DTO를 기반으로 동적 WHERE 절 (BooleanBuilder)을 생성합니다.
     */
    private BooleanBuilder buildFilterCondition(JobFilterRequest request) {
        BooleanBuilder builder = new BooleanBuilder();

        // 근무 지역 필터링
        Optional.ofNullable(request.getWorkLocation())
                .filter(s -> !s.isEmpty())
                .ifPresent(location ->
                        builder.and(job.workAddress.containsIgnoreCase(location))
                );

        // 지원 자격 필터링 (경력, 신입, 무관 등)
        Optional.ofNullable(request.getCareerConditionName())
                .filter(s -> !s.isEmpty() && !s.equalsIgnoreCase("무관"))
                .ifPresent(career -> {
                    // 선택한 career와 '무관'인 공고를 모두 포함
                    BooleanExpression careerCondition = job.careerName.eq(career);
                    BooleanExpression irrelevantCondition = job.careerName.eq("무관");

                    builder.and(careerCondition.or(irrelevantCondition));
                });

        // 검색어 필터링
        Optional.ofNullable(request.getSearchKeyword())
                .filter(s -> !s.isEmpty())
                .ifPresent(keyword -> {
                    String type = Optional.ofNullable(request.getSearchType()).orElse("ALL");

                    BooleanExpression companySearchCondition = job.company.name.containsIgnoreCase(keyword);
                    BooleanExpression titleSearchCondition = job.title.containsIgnoreCase(keyword);

                    switch (type) {
                        case "COMPANY":
                            builder.and(companySearchCondition);
                            break;
                        case "TITLE":
                            builder.and(titleSearchCondition);
                            break;
                        case "ALL":
                        default:
                            // 회사명 또는 채용 제목에서 검색
                            builder.and(companySearchCondition.or(titleSearchCondition));
                            break;
                    }
                });

        return builder;
    }
}
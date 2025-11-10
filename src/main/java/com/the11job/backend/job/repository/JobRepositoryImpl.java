package com.the11job.backend.job.repository;

import com.querydsl.core.BooleanBuilder;
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
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JobRepositoryImpl implements JobRepositoryCustom {

    private final JPAQueryFactory queryFactory; // Querydsl 사용을 위한 JPAQueryFactory 주입

    // Q-Class를 인스턴스화
    private final QJob job = QJob.job;

    @Override
    public Page<Job> findJobsByFilter(JobFilterRequest request, Pageable pageable) {

        // 동적 쿼리 조건 생성
        BooleanBuilder builder = buildFilterCondition(request);

        // 데이터 조회
        List<Job> results = queryFactory
                .selectFrom(job)
                .where(builder)
                .offset(pageable.getOffset()) // 페이징 시작 오프셋
                .limit(pageable.getPageSize())  // 페이지 크기
                .fetch();

        // 전체 개수 조회 (count 쿼리 최적화 필요)
        long total = queryFactory
                .selectFrom(job)
                .where(builder)
                .fetchCount();

        // Page 객체로 변환하여 반환
        return new PageImpl<>(results, pageable, total);
    }

    /**
     * 요청 DTO를 기반으로 동적 WHERE 절 (BooleanBuilder)을 생성합니다.
     */
    private BooleanBuilder buildFilterCondition(JobFilterRequest request) {
        BooleanBuilder builder = new BooleanBuilder();

        // 근무 지역 필터링 (주소 검색)
        Optional.ofNullable(request.getWorkLocation())
                .filter(s -> !s.isEmpty())
                .ifPresent(location ->
                        // workAddress 필드에 해당 지역 문자열이 포함되는지 확인
                        builder.and(job.workAddress.containsIgnoreCase(location))
                );

        // 지원 자격 필터링 (경력, 신입, 인턴 등)
        Optional.ofNullable(request.getCareerConditionName())
                .filter(s -> !s.isEmpty())
                .ifPresent(career ->
                        // careerName 필드가 정확히 일치하는지 확인
                        builder.and(job.careerName.eq(career))
                );

        // 검색어 필터링 (회사명, 직무명 등)
        Optional.ofNullable(request.getSearchKeyword())
                .filter(s -> !s.isEmpty())
                .ifPresent(keyword -> {
                    String type = Optional.ofNullable(request.getSearchType()).orElse("ALL");

                    switch (type) {
                        case "COMPANY":
                            // Company.name 대신 Job.companyName 필드를 사용
                            builder.and(job.companyName.containsIgnoreCase(keyword));
                            break;
                        case "TITLE":
                            builder.and(job.title.containsIgnoreCase(keyword));
                            break;
                        case "ALL":
                        default:
                            // 회사명 또는 채용 제목에서 검색
                            builder.and(job.companyName.containsIgnoreCase(keyword)
                                    .or(job.title.containsIgnoreCase(keyword)));
                            break;
                    }
                });

        return builder;
    }
}
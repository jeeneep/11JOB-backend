package com.the11job.backend.job.repository;

import com.the11job.backend.job.dto.JobFilterRequest;
import com.the11job.backend.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 복잡한 필터링 쿼리를 처리하기 위한 커스텀 Repository 인터페이스
 */
public interface JobRepositoryCustom {

    /**
     * 필터링 조건과 검색어를 적용하여 채용 공고 목록을 페이징 처리하여 조회합니다.
     *
     * @param request  필터링 조건 DTO
     * @param pageable 페이징 정보
     * @return 필터링된 Job 목록 (Page 객체)
     */
    Page<Job> findJobsByFilter(JobFilterRequest request, Pageable pageable);
}
package com.the11job.backend.job.service;

import com.the11job.backend.job.dto.JobFilterRequest;
import com.the11job.backend.job.dto.JobResponse;
import com.the11job.backend.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 요청에 따라 DB에 저장된 채용 정보를 조회하고 제공하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobService {

    private final JobRepository jobRepository;

    /**
     * 채용 공고 목록 조회 및 필터링 API의 서비스 로직. 필터링 조건이 없으면 전체 조회가 됩니다.
     *
     * @param request  검색어, 근무지역, 지원자격 등의 필터링 조건
     * @param pageable 페이징 및 정렬 정보
     * @return 필터링된 Job 목록 (Page<JobResponse> 형태)
     */
    public Page<JobResponse> getFilteredJobs(JobFilterRequest request, Pageable pageable) {

        // 1. 필터링 조건을 커스텀 Repository 메서드에 전달하여 Page<Job> 조회
        Page<JobResponse> jobPage = jobRepository.findJobsByFilter(request, pageable)
                .map(JobResponse::from); // 2. 조회된 Job 엔티티를 JobResponse DTO로 변환

        return jobPage;
    }

}
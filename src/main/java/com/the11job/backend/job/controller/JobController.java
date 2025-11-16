package com.the11job.backend.job.controller;

import com.the11job.backend.job.dto.JobFilterRequest;
import com.the11job.backend.job.dto.JobResponse;
import com.the11job.backend.job.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 채용 공고(Job) 조회 및 필터링을 담당하는 API Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * 채용 공고 목록 조회 및 필터링 API (전체 조회 역할까지 겸하며, 무한 스크롤 및 필터링을 지원합니다.)
     *
     * @param request  검색어, 근무지역, 지원자격 등의 필터링 조건
     * @param pageable Spring Data JPA의 Pageable 객체 (page, size, sort 처리)
     * @return 필터링된 Job 목록 (페이지네이션 정보 포함)
     */
    @GetMapping // GET /api/v1/jobs?workLocation=서울&careerConditionName=신입&page=0&size=10
    public ResponseEntity<Page<JobResponse>> getFilteredJobList(
            // 쿼리 파라미터를 JobFilterRequest DTO에 자동으로 바인딩
            JobFilterRequest request,

            // 페이징 및 정렬 기본값 설정: 페이지 0부터 10개, 등록일(registrationDate) 최신순 정렬
            @PageableDefault(size = 10, sort = "registrationDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("채용 공고 필터링 요청: Request={}, Pageable={}", request, pageable);

        Page<JobResponse> result = jobService.getFilteredJobs(request, pageable);

        // 결과 반환
        return ResponseEntity.ok(result);
    }
}
package com.the11job.backend.job.controller;

import com.the11job.backend.job.dto.JobFilterRequest;
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
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /**
     * 채용 공고 목록 조회 및 필터링 API * @param request 검색어, 근무지역, 지원자격 등의 필터링 조건
     *
     * @param pageable Spring Data JPA의 Pageable 객체 (page, size, sort 처리)
     * @return 필터링된 Job 목록 (페이지네이션 정보 포함)
     */
    @GetMapping
    public ResponseEntity<Page<Object>> getFilteredJobList(
            // 쿼리 파라미터를 JobFilterRequest DTO에 자동으로 바인딩
            JobFilterRequest request,

            // 페이징 및 정렬 기본값 설정: 페이지 0부터 10개, 최신순 정렬
            @PageableDefault(size = 10, sort = "createdDate", direction = Sort.Direction.DESC)
            Pageable pageable) {

        log.info("채용 공고 필터링 요청: Request={}, Pageable={}", request, pageable);

        // Service 계층 호출
        Page<Object> result = jobService.getFilteredJobs(request, pageable);

        // 결과 반환
        return ResponseEntity.ok(result);
    }

    // ... 기타 Job 상세 조회, 등록 등의 API 엔드포인트가 추가
}
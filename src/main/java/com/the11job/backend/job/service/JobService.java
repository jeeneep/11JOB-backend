package com.the11job.backend.job.service;

import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.job.entity.Job;
import com.the11job.backend.job.exception.JobException;
import com.the11job.backend.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 요청에 따라 DB에 저장된 채용 정보를 조회하고 제공하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor // final 필드를 사용하는 생성자를 자동 생성
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 설정
public class JobService {

    private final JobRepository jobRepository;

    /**
     * 전체 채용 공고 목록을 페이징하여 조회합니다. (예시로 가장 최근 등록된 순으로 정렬)
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지당 항목 수
     * @return Job 엔터티의 페이징된 목록
     */
    public Page<Job> getJobList(int page, int size) {
        // 페이지 번호와 크기를 기반으로 Pageable 객체 생성
        // 등록일(registrationDate)을 기준으로 내림차순(최신순) 정렬
        Pageable pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("registrationDate").descending());

        // Repository를 통해 DB에서 데이터를 조회
        return jobRepository.findAll(pageable);
    }

    /**
     * 특정 공고 ID로 상세 정보를 조회합니다.
     *
     * @param jobId Job 엔터티의 PK (Long id)
     * @return 해당 ID의 Job 엔터티
     */
    public Job getJobDetail(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(
                        () -> new JobException(ErrorCode.NOT_FOUND_JOB, "공고 ID " + jobId + "에 해당하는 정보를 찾을 수 없습니다."));
    }
}
package com.the11job.backend.job.dto;

import com.the11job.backend.job.entity.Job;
import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;

/**
 * 채용 공고 목록 조회 시 사용되는 응답 DTO
 */
@Getter
@Builder
public class JobResponse {

    private Long jobId;
    private String requestNo;
    private String companyName; // Company 엔티티로부터 조회
    private String title;
    private String workAddress;
    private String jobCodeName;
    private String academicName;
    private String careerName;
    private LocalDate registrationDate;
    private LocalDate expirationDate;
    private String detailUrl;

    /**
     * Job 엔티티를 JobResponse DTO로 변환하는 정적 팩토리 메서드
     */
    public static JobResponse from(Job job) {
        // LAZY 로딩된 company 엔티티에서 companyName을 안전하게 추출
        String companyName = (job.getCompany() != null) ? job.getCompany().getName() : "N/A";

        return JobResponse.builder()
                .jobId(job.getId())
                .requestNo(job.getRequestNo())
                .companyName(companyName)
                .title(job.getTitle())
                .workAddress(job.getWorkAddress())
                .jobCodeName(job.getJobCodeName())
                .academicName(job.getAcademicName())
                .careerName(job.getCareerName())
                .registrationDate(job.getRegistrationDate())
                .expirationDate(job.getExpirationDate())
                .detailUrl(job.getDetailUrl())
                .build();
    }
}
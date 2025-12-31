package com.the11job.backend.job.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 채용 공고 리스트 필터링 조건을 담는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
public class JobFilterRequest {

    // 근무지역 필터링
    private String workLocation; // 시/구/동 전체 주소 문자열

    // 지원 자격 필터링 (경력, 신입, 무관 등)
    private String careerConditionName;

    // 검색어 필터링
    private String searchKeyword; // 회사명, 직무명 등에 사용될 검색어
    private String searchType;    // 검색 대상 지정 (예: "COMPANY", "TITLE", "ALL")

}
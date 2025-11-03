package com.the11job.backend.job.service;

import com.the11job.backend.api.seouljob.SeoulJobInfo.JobDetail;
import com.the11job.backend.job.entity.Job;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component // [수정] 스프링 빈으로 등록
public class JobMapper {

    // 포맷터는 불변(immutable)이므로 final 필드로 선언
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * JobDetail DTO를 Job Entity로 변환합니다.
     */
    // [수정] static 제거
    public Job toEntity(JobDetail detail) {

        // 1. 날짜 변환 처리
        LocalDate registrationDate = safeParseDate(detail.getJobRegistrationDate());

        // 2. Builder를 사용하여 필요한 필드만 엔터티에 매핑
        return Job.builder()
                .requestNo(detail.getJoRequestNo())
                .companyName(detail.getCompanyName())
                .title(detail.getJobSubject())
                .workAddress(detail.getWorkAddress())
                .jobCodeName(detail.getJobCodeName())
                .academicName(detail.getAcademicName())
                .careerName(detail.getCareerConditionName())
                .hopeWage(detail.getHopeWage())
                // String -> Integer 안전 변환 적용
                .weeklyWorkHours(safeParseInt(detail.getWeeklyWorkHours()))
                .registrationDate(registrationDate)
                .expirationDate(safeParseClosingDate(detail.getReceiptClosingName())) // 마감일 추출 시도
                .detailUrl(null)
                .build();
    }

    /**
     * 안전하게 String을 LocalDate로 변환합니다.
     */
    // [수정] static 제거
    private LocalDate safeParseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            // 인스턴스 필드인 DATE_FORMATTER 사용
            return LocalDate.parse(dateString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("날짜 파싱 실패 (값: {}). 포맷 확인 필요.", dateString);
            return null;
        }
    }

    /**
     * 안전하게 String을 Integer로 변환합니다. (빈 값, 숫자가 아닌 값 처리)
     */
    // [수정] static 제거
    private Integer safeParseInt(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            // trim 후 순수 숫자만 파싱 시도
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("숫자 파싱 실패 (값: {}). Null 처리.", value);
            return null;
        }
    }

    /**
     * RCEPT_CLOS_NM ("마감일 (YYYY-MM-DD)")에서 마감일자를 추출합니다.
     */
    // [수정] static 제거
    private LocalDate safeParseClosingDate(String closingName) {
        if (closingName == null || !closingName.contains("(")) {
            return null;
        }
        try {
            // '마감일 (2025-12-10)' 형태에서 괄호 안의 날짜만 추출
            int start = closingName.indexOf('(') + 1;
            int end = closingName.indexOf(')');
            if (start > 0 && end > start) {
                String dateString = closingName.substring(start, end);
                return safeParseDate(dateString); // 내부 safeParseDate 호출
            }
        } catch (RuntimeException e) { // [수정] RuntimeException으로 범위 축소
            log.warn("마감일 문자열에서 날짜 추출 실패: {}", closingName);
        }
        return null;
    }
}
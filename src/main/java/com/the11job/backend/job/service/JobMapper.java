package com.the11job.backend.job.service;

import com.the11job.backend.api.seouljob.SeoulJobInfo.JobDetail;
import com.the11job.backend.job.entity.Job;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobMapper {

    // 포맷터는 불변(immutable)이므로 final 필드로 선언
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 서울시 일자리 상세 URL 기본 패턴 정의 (JO_REQST_NO를 파라미터로 사용)
    private static final String SEOUL_JOB_DETAIL_URL = "https://job.seoul.go.kr/www/jobInfo/getJobInfoDetail.do?joReqstNo=";


    /**
     * JobDetail DTO를 Job Entity로 변환합니다. (Company 엔터티 연결은 JobSaverService에서 처리하므로 여기서 매핑하지 않습니다.)
     */
    public Job toEntity(JobDetail detail) {

        // 날짜 변환 처리
        LocalDate registrationDate = safeParseDate(detail.getJobRegistrationDate());
        String joRequestNo = detail.getJoRequestNo(); // 상세 URL 생성을 위해 JO_REQST_NO 추출

        // Builder를 사용하여 필요한 필드만 엔터티에 매핑
        return Job.builder()
                .requestNo(joRequestNo)
                .title(detail.getJobSubject())
                .workAddress(detail.getWorkAddress())
                .jobCodeName(detail.getJobCodeName())
                .academicName(detail.getAcademicName())
                .careerName(detail.getCareerConditionName())
                .registrationDate(registrationDate)
                .expirationDate(safeParseClosingDate(detail.getReceiptClosingName())) // 마감일 추출 시도
                .detailUrl(createDetailUrl(joRequestNo)) // JO_REQST_NO 기반으로 동적 URL 생성
                .build();
    }

    /**
     * 안전하게 String을 LocalDate로 변환합니다.
     */
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
        } catch (RuntimeException e) {
            log.warn("마감일 문자열에서 날짜 추출 실패: {}", closingName);
        }
        return null;
    }

    /**
     * JO_REQST_NO를 기반으로 상세 페이지 URL을 동적으로 생성합니다.
     */
    private String createDetailUrl(String joRequestNo) {
        if (joRequestNo == null || joRequestNo.trim().isEmpty()) {
            return null;
        }
        return SEOUL_JOB_DETAIL_URL + joRequestNo;
    }
}
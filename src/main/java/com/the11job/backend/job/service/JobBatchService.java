package com.the11job.backend.job.service;

import com.the11job.backend.api.exception.ApiClientException;
import com.the11job.backend.api.seouljob.SeoulJobApiClient;
import com.the11job.backend.api.seouljob.SeoulJobInfo;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
//public class JobBatchService {
//
//    private final SeoulJobApiClient apiClient;
//    private final JobSaverService jobSaverService; // 👈 새로 분리된 서비스 주입
//
//    // 순환 참조를 일으키던 @Autowired private JobBatchService self; 는 제거되었습니다.
//
//    // API 호출 관련 상수
//    private static final int MAX_RECORDS_PER_CALL = 1000;
//    private static final int MAX_DAILY_CALLS = 1000;
//
//    // @Transactional 제거: 트랜잭션은 JobSaverService에서 처리합니다.
//    public void fetchAllJobPostings() {
//        log.info("=== [배치 시작] 서울시 채용 정보 가져오기 시작 ===");
//
//        int totalCount = 0;
//        int currentCallCount = 0;
//        int totalProcessedRecords = 0;
//        boolean hasMoreData = true;
//
//        while (hasMoreData && currentCallCount < MAX_DAILY_CALLS) {
//
//            int startIndex = currentCallCount * MAX_RECORDS_PER_CALL + 1;
//            int endIndex = startIndex + MAX_RECORDS_PER_CALL - 1;
//
//            Optional<SeoulJobInfo> jobInfoOptional;
//            try {
//                log.info("API 호출 시도: {}-{}번째 데이터 (현재 호출 횟수: {}회)", startIndex, endIndex, currentCallCount + 1);
//                jobInfoOptional = apiClient.getJobInfo(startIndex, endIndex);
//
//                // API 호출 성공 시에만 횟수 증가
//                currentCallCount++;
//            } catch (ApiClientException e) {
//                log.error("API 호출 중 예외 발생. 배치를 중단합니다. 횟수: {}", currentCallCount, e);
//                break;
//            }
//
//            if (jobInfoOptional.isEmpty()) {
//                log.warn("API 응답이 비어있습니다. 마지막 페이지로 간주하고 종료합니다.");
//                break;
//            }
//
//            SeoulJobInfo jobInfo = jobInfoOptional.get();
//
//            // 첫 호출 시에만 totalCount 확정
//            if (currentCallCount == 1) {
//                totalCount = parseTotalCount(jobInfo.getListTotalCount());
//            }
//
//            if (jobInfo.getJobDetails() == null || jobInfo.getJobDetails().isEmpty()) {
//                log.info("더 이상 가져올 데이터가 없습니다. (총 {}건 중 {}건 처리)", totalCount, totalProcessedRecords);
//                break;
//            }
//
//            // 🌟 JobSaverService를 통해 트랜잭션 처리된 DB 저장 로직 호출 🌟
//            int recordsInThisCall = jobSaverService.processAndSaveData(jobInfo.getJobDetails());
//            totalProcessedRecords += recordsInThisCall;
//
//            // 다음 호출 판단 및 대기 로직 유지
//            if (totalProcessedRecords >= totalCount) {
//                // ... (생략)
//            } else if (currentCallCount >= MAX_DAILY_CALLS) {
//                // ... (생략)
//            } else {
//                try {
//                    Thread.sleep(1000); // 부하 방지 대기
//                } catch (InterruptedException ignored) {}
//            }
//        }
//
//        // ... (배치 완료 로그)
//    }
//
//    private int parseTotalCount(String totalCountString) {
//        if (totalCountString == null || totalCountString.trim().isEmpty()) {
//            return 0;
//        }
//        try {
//            return Integer.parseInt(totalCountString.trim());
//        } catch (NumberFormatException e) {
//            log.error("총 건수(list_total_count) 파싱 오류: '{}'", totalCountString, e);
//            return 0;
//        }
//    }
//
//}

public class JobBatchService {

    private final SeoulJobApiClient apiClient;
    private final JobSaverService jobSaverService; // JobSaverService는 트랜잭션 분리 및 저장을 담당

    // API 호출 관련 상수 (이제 단일 호출이므로 MAX_DAILY_CALLS는 무시됨)
    private static final int MAX_RECORDS_PER_CALL = 1000;

    // fetchAllJobPostings는 단 한번의 API 호출만 실행
    public void fetchAllJobPostings() {
        log.info("=== [배치 시작] 서울시 채용 정보 단일 호출 시작 (1-1000건) ===");

        int startIndex = 1;
        int endIndex = MAX_RECORDS_PER_CALL;

        Optional<SeoulJobInfo> jobInfoOptional;

        try {
            log.info("API 호출 시도: {}-{}번째 데이터", startIndex, endIndex);
            jobInfoOptional = apiClient.getJobInfo(startIndex, endIndex);

        } catch (ApiClientException e) {
            log.error("API 호출 중 예외 발생. 배치를 중단합니다.", e);
            return; // 예외 발생 시 바로 종료
        }

        if (jobInfoOptional.isEmpty()) {
            log.warn("API 응답이 비어있습니다. 처리할 데이터가 없습니다.");
            return;
        }

        SeoulJobInfo jobInfo = jobInfoOptional.get();
        List<SeoulJobInfo.JobDetail> details = jobInfo.getJobDetails();

        // 데이터가 없거나 비어있는 경우
        if (details == null || details.isEmpty()) {
            int totalCount = parseTotalCount(jobInfo.getListTotalCount());
            log.info("가져온 데이터는 없습니다. (API 총 {}건)", totalCount);
            return;
        }

        // 단일 호출 결과 저장/갱신 로직 실행
        int recordsInThisCall = jobSaverService.processAndSaveData(details);

        log.info("=== [배치 완료] 단일 호출 처리 레코드: {}건 (API 총 {}건) ===",
                recordsInThisCall, parseTotalCount(jobInfo.getListTotalCount()));
    }

    private int parseTotalCount(String totalCountString) {
        if (totalCountString == null || totalCountString.trim().isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(totalCountString.trim());
        } catch (NumberFormatException e) {
            log.error("총 건수(list_total_count) 파싱 오류: '{}'", totalCountString, e);
            return 0;
        }
    }

}
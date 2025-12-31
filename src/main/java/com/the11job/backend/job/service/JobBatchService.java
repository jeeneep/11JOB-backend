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
public class JobBatchService {

    private final SeoulJobApiClient apiClient;
    private final JobSaverService jobSaverService; // JobSaverService는 트랜잭션 분리 및 저장을 담당

    // API 호출 관련 상수: 1회 호출 시 최대 건수 1,000건으로 설정
    private static final int MAX_RECORDS_PER_CALL = 1000;
    // 호출 횟수 제한은 없으나, 무한 루프 방지를 위한 안전 장치로 임시 제한 설정
    private static final int MAX_DAILY_CALLS = 10000; // 충분히 큰 값

    /**
     * 서울시 채용 정보 API를 반복 호출하여 모든 데이터를 가져와 저장합니다.
     */
    public void fetchAllJobPostings() {
        log.info("=== [배치 시작] 서울시 채용 정보 전체 가져오기 시작 (최대 {}건 단위) ===", MAX_RECORDS_PER_CALL);

        int totalCount = 0;
        int currentCallCount = 0;
        int totalProcessedRecords = 0;
        boolean hasMoreData = true;

        while (hasMoreData && currentCallCount < MAX_DAILY_CALLS) {

            // API 호출을 위한 시작점과 끝점 계산
            int startIndex = currentCallCount * MAX_RECORDS_PER_CALL + 1;
            int endIndex = startIndex + MAX_RECORDS_PER_CALL - 1;

            Optional<SeoulJobInfo> jobInfoOptional;
            try {
                log.info("API 호출 시도: {}-{}번째 데이터 (현재 호출 횟수: {}회)", startIndex, endIndex, currentCallCount + 1);
                jobInfoOptional = apiClient.getJobInfo(startIndex, endIndex);

                // API 호출 성공 시에만 횟수 증가
                currentCallCount++;
            } catch (ApiClientException e) {
                log.error("API 호출 중 예외 발생. 배치를 중단합니다. 횟수: {}", currentCallCount, e);
                break;
            }

            if (jobInfoOptional.isEmpty()) {
                log.warn("API 응답이 비어있습니다. 데이터를 가져오지 못했습니다.");
                break;
            }

            SeoulJobInfo jobInfo = jobInfoOptional.get();

            // 첫 호출 시에만 totalCount 확정
            if (currentCallCount == 1) {
                totalCount = parseTotalCount(jobInfo.getListTotalCount());
                if (totalCount == 0) {
                    log.info("총 채용 건수가 0입니다. 배치를 종료합니다.");
                    break;
                }
            }

            List<SeoulJobInfo.JobDetail> details = jobInfo.getJobDetails();

            if (details == null || details.isEmpty()) {
                log.info("더 이상 가져올 데이터가 없습니다. (총 {}건 중 {}건 처리)", totalCount, totalProcessedRecords);
                hasMoreData = false;
                break;
            }

            // 🌟 JobSaverService를 통해 트랜잭션 처리된 DB 저장 로직 호출 🌟
            int recordsInThisCall = jobSaverService.processAndSaveData(details);
            totalProcessedRecords += recordsInThisCall;

            // 다음 호출 판단
            if (totalProcessedRecords >= totalCount) {
                log.info("총 {}건의 데이터 처리를 완료했습니다. 배치를 종료합니다.", totalCount);
                hasMoreData = false;
            } else if (currentCallCount >= MAX_DAILY_CALLS) {
                log.warn("일일 최대 호출 횟수({})에 도달하여 배치를 중단합니다. (총 {}건 중 {}건 처리)",
                        MAX_DAILY_CALLS, totalCount, totalProcessedRecords);
                hasMoreData = false;
            } else {
                try {
                    // 서버 부하 방지 및 API 사용 매너를 위해 대기
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        log.info("=== [배치 완료] 최종 처리 레코드: {}건 (API 총 {}건) ===",
                totalProcessedRecords, totalCount);
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
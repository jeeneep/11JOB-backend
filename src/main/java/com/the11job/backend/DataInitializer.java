package com.the11job.backend;

import com.the11job.backend.job.service.JobBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 후 초기 데이터를 로드하고 배치 서비스를 실행하는 클래스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final JobBatchService jobBatchService;

    @Override
    public void run(String... args) throws Exception {
        log.info("--- [초기화] CommandLineRunner 실행: JobBatchService 호출 시작 ---");

        try {
            // JobBatchService의 배치 로직을 호출하여 API 데이터 저장 시작
            jobBatchService.fetchAllJobPostings();
        } catch (Exception e) {
            log.error("채용 정보 초기 로딩 중 치명적인 오류 발생", e);
            // 초기화 실패 시 애플리케이션을 중단할 수도 있습니다 (선택 사항)
            // throw new RuntimeException("데이터 초기화 실패", e);
        }

        log.info("--- [초기화] JobBatchService 호출 완료 ---");

        // 🌟 H2 DB 커밋 및 정리 시간을 벌기 위한 임시 지연 🌟
        try {
            Thread.sleep(3000); // 3초 대기
        } catch (InterruptedException ignored) {
        }

    }
}
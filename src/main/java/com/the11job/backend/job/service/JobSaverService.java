package com.the11job.backend.job.service;

import com.the11job.backend.api.seouljob.SeoulJobInfo.JobDetail;
import com.the11job.backend.job.entity.Job;
import com.the11job.backend.job.repository.JobRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * JobBatchService로부터 전달받은 데이터를 DB에 저장/갱신하는 트랜잭션 전담 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobSaverService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper; // JobMapper 주입

    /**
     * 가져온 채용 공고 데이터를 DB에 저장하거나 갱신하는 트랜잭션 단위 메서드. 1000건의 청크(Chunk)를 하나의 트랜잭션으로 처리합니다.
     */
    @Transactional
    public int processAndSaveData(List<JobDetail> details) {
        int newCount = 0;
        int updateCount = 0;

        for (JobDetail detail : details) {

            // [개선] 로그에 사용할 requestNo를 미리 안전하게 추출합니다.
            String requestNo = Optional.ofNullable(detail.getJoRequestNo()).orElse("Unknown_ID");

            try {
                // 1. DTO를 Entity로 변환 (Mapper에서 이미 Null/파싱 오류는 ApiClientException으로 래핑됨)
                Job newJob = jobMapper.toEntity(detail);

                // 2. DB에 이미 존재하는 공고인지 requestNo(외부 고유 ID)로 확인
                Optional<Job> existingJob = jobRepository.findByRequestNo(newJob.getRequestNo());

                // ... (저장/갱신 로직 유지)
                if (existingJob.isPresent()) {
                    Job jobToUpdate = existingJob.get();
                    jobToUpdate.update(newJob);
                    jobRepository.save(jobToUpdate);
                    updateCount++;
                } else {
                    jobRepository.save(newJob);
                    newCount++;
                }

                // [수정] Exception 대신 RuntimeException으로 범위 축소
            } catch (RuntimeException e) {
                // DB 관련 오류나 Mapper에서 발생한 런타임 예외(ApiClientException 등) 처리
                log.error("채용 정보 저장/갱신 중 오류 발생 (공고 ID: {}). 현재 청크 롤백 예정.", requestNo, e);

                // RuntimeException을 다시 던져 트랜잭션 롤백
                throw new RuntimeException("DB 저장 오류로 인해 현재 청크 롤백 (ID: " + requestNo + ")", e);
            }
        }
        log.info("  -> 이번 호출 처리: 신규 {}건, 갱신 {}건", newCount, updateCount);
        return details.size();
    }
}
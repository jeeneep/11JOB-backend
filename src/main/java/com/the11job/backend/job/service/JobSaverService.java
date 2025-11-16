package com.the11job.backend.job.service;

import com.the11job.backend.api.seouljob.SeoulJobInfo.JobDetail;
import com.the11job.backend.company.entity.Company;
import com.the11job.backend.company.repository.CompanyRepository;
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
    private final JobMapper jobMapper;
    private final CompanyRepository companyRepository;

    /**
     * 가져온 채용 공고 데이터를 DB에 저장하거나 갱신하는 트랜잭션 단위 메서드. 1000건의 청크(Chunk)를 하나의 트랜잭션으로 처리합니다.
     */
    @Transactional
    public int processAndSaveData(List<JobDetail> details) {
        int newCount = 0;
        int updateCount = 0;

        for (JobDetail detail : details) {
            String requestNo = Optional.ofNullable(detail.getJoRequestNo()).orElse("Unknown_ID");

            try {
                // DTO를 Entity로 변환 (아직 Company 연결 전)
                Job newJob = jobMapper.toEntity(detail);

                // JobDetail DTO에서 기업명을 직접 추출하여 NullPointerException 방지
                String companyName = detail.getCompanyName();

                // Company 엔터티 처리 (신규 또는 재활용)
                Company company = getOrCreateCompany(companyName);

                // DB에 이미 존재하는 공고인지 requestNo(외부 고유 ID)로 확인
                Optional<Job> existingJob = jobRepository.findByRequestNo(newJob.getRequestNo());

                if (existingJob.isPresent()) {
                    Job jobToUpdate = existingJob.get();
                    jobToUpdate.update(newJob);
                    jobToUpdate.setCompany(company); // Company 연결
                    jobRepository.save(jobToUpdate);
                    updateCount++;
                } else {
                    newJob.setCompany(company); // Company 연결
                    jobRepository.save(newJob);
                    newCount++;
                }

            } catch (RuntimeException e) {
                log.error("채용 정보 저장/갱신 중 오류 발생 (공고 ID: {}). 현재 청크 롤백 예정.", requestNo, e);
                throw new RuntimeException("DB 저장 오류로 인해 현재 청크 롤백 (ID: " + requestNo + ")", e);
            }
        }
        log.info("  -> 이번 호출 처리: 신규 {}건, 갱신 {}건", newCount, updateCount);
        return details.size();
    }

    /**
     * 기업명으로 기존 Company를 찾거나 새로 생성하여 저장합니다.
     *
     * @param companyName 처리할 기업명
     * @return Company 엔터티 (신규 또는 기존)
     */
    private Company getOrCreateCompany(String companyName) {
        // 기업명 Null 체크 및 정규화
        if (companyName == null || companyName.trim().isEmpty()) {
            companyName = "미상"; // Null/Empty인 경우 기본값 할당
        }
        String normalizedName = companyName.trim();

        return companyRepository.findByName(normalizedName)
                .orElseGet(() -> {
                    // 신규 Company 생성 및 저장
                    Company newCompany = Company.builder().name(normalizedName).build();
                    log.info("신규 Company 등록: {}", normalizedName);
                    return companyRepository.save(newCompany);
                });
    }
}
package com.the11job.backend.schedule.service;

import com.the11job.backend.company.entity.Company;
import com.the11job.backend.company.exception.CompanyException;
import com.the11job.backend.company.repository.CompanyRepository;
import com.the11job.backend.file.service.FileService;
import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.schedule.dto.ScheduleDetailRequest;
import com.the11job.backend.schedule.dto.ScheduleRequest;
import com.the11job.backend.schedule.entity.Schedule;
import com.the11job.backend.schedule.entity.ScheduleDetail;
import com.the11job.backend.schedule.exception.ScheduleException;
import com.the11job.backend.schedule.repository.ScheduleDetailRepository;
import com.the11job.backend.schedule.repository.ScheduleRepository;
import com.the11job.backend.user.entity.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ScheduleDetailRepository scheduleDetailRepository;
    private final CompanyRepository companyRepository;
    private final FileService fileService;

    // --- C (Create) ---
    @Transactional
    public Schedule createSchedule(User user, ScheduleRequest request, List<MultipartFile> files) {
        // 1. Company 엔티티 검증 및 조회 (Company 엔티티는 영속 상태)
        Company company = companyRepository.findByName(request.getCompanyName())
                .orElseThrow(() -> new CompanyException(ErrorCode.NOT_FOUND_COMPANY,
                        "해당 기업 이름에 대한 정보를 찾을 수 없습니다: " + request.getCompanyName()));

        // 2. Schedule 엔티티 생성
        Schedule schedule = Schedule.builder()
                .user(user)
                .company(company)
                .title(request.getTitle())
                .scheduleDate(request.getScheduleDate())
                .build();

        // save() 대신 saveAndFlush()를 사용하여 ID가 DB에 기록됨을 보장
        Schedule savedSchedule = scheduleRepository.saveAndFlush(schedule);

        log.info("Schedule saved with ID: {}", savedSchedule.getId());

        // 3. ScheduleDetail 목록 저장 (savedSchedule 사용)
        if (request.getDetails() != null && !request.getDetails().isEmpty()) {
            saveScheduleDetails(savedSchedule, request.getDetails());
        }

        if (files != null && !files.isEmpty()) { // request.getFiles() 대신 외부 files 인자 사용
            fileService.uploadAndLinkFiles(savedSchedule, files);
        }

        return savedSchedule;
    }

    // --- R (Read - Detail) ---
    @Transactional(readOnly = true)
    public Schedule getScheduleDetail(User user, Long scheduleId) {
        return findScheduleByIdAndCheckOwnership(user, scheduleId);
    }

    // --- R (Read - All) ---
    @Transactional(readOnly = true)
    public List<Schedule> getUserSchedules(User user) {

        return scheduleRepository.findAllByUserOrderByScheduleDateAsc(user);
    }

    // --- U (Update) ---
    @Transactional
    public Schedule updateSchedule(User user, Long scheduleId, ScheduleRequest request,
                                   List<MultipartFile> newFiles) { // 🔥 인자 추가
        Schedule schedule = findScheduleByIdAndCheckOwnership(user, scheduleId); // 1. 조회 및 소유권 확인

        // 2. 일정 기본 내용 갱신
        schedule.update(
                request.getTitle(),
                request.getScheduleDate()
        );

        // 3. ScheduleDetail 갱신 로직
        updateScheduleDetails(schedule, request.getDetails());

        // 4. 파일 갱신 로직
        if (request.getFilesToDelete() != null || (newFiles != null && !newFiles.isEmpty())) {
            fileService.updateFiles(schedule, request.getFilesToDelete(), newFiles);
        }

        return schedule; // 변경 감지(Dirty Checking)로 자동 업데이트 후 반환
    }

    // --- D (Delete) ---
    @Transactional
    public void deleteSchedule(User user, Long scheduleId) {

        Schedule schedule = findScheduleByIdAndCheckOwnership(user, scheduleId); // 1. 조회 및 소유권 확인

        // 2. S3에 저장된 실제 파일 삭제
        fileService.deleteS3FilesForSchedule(schedule.getFiles());

        // 3. Schedule 엔티티 삭제
        scheduleRepository.delete(schedule);
    }

    /**
     * 일정 조회 및 소유권 검증 (내부 헬퍼 메서드)
     */
    private Schedule findScheduleByIdAndCheckOwnership(User user, Long scheduleId) {
        // 1. 일정 ID로 엔티티 조회
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ErrorCode.NOT_FOUND_SCHEDULE, "요청하신 일정을 찾을 수 없습니다."));

        // 2. [보안] "내 것"이 맞는지 확인
        if (!schedule.getUser().getId().equals(user.getId())) {
            throw new ScheduleException(ErrorCode.SCHEDULE_ACCESS_DENIED, "해당 일정에 대한 접근 권한이 없습니다.");
        }

        return schedule;
    }

    // ----------------------------------------------------
    // ScheduleDetail 관련 헬퍼 메서드
    // ----------------------------------------------------

    private void saveScheduleDetails(Schedule schedule, List<ScheduleDetailRequest> detailRequests) {
        List<ScheduleDetail> details = detailRequests.stream()
                .map(detailRequest -> ScheduleDetail.builder()
                        .schedule(schedule)
                        .title(detailRequest.getDetailTitle())
                        .content(detailRequest.getDetailContent())
                        .build())
                .toList();

        details.forEach(schedule::addDetail);
        scheduleDetailRepository.saveAll(details);
    }

    private void updateScheduleDetails(Schedule schedule, List<ScheduleDetailRequest> detailRequests) {
        // 기존 세부 항목 모두 삭제
        schedule.getDetails().clear();
        scheduleDetailRepository.deleteBySchedule(schedule);

        if (detailRequests != null && !detailRequests.isEmpty()) {
            // 새 항목 저장
            saveScheduleDetails(schedule, detailRequests);
        }
    }
}
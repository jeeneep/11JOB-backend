// src/main/java/com/the11job.backend.schedule.service/ScheduleService.java (최종 수정)
package com.the11job.backend.schedule.service;

import com.the11job.backend.company.entity.Company;
import com.the11job.backend.company.exception.CompanyException;
import com.the11job.backend.company.repository.CompanyRepository;
import com.the11job.backend.file.service.FileService;
import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.schedule.dto.ScheduleRequest;
import com.the11job.backend.schedule.entity.Schedule;
import com.the11job.backend.schedule.exception.ScheduleException;
import com.the11job.backend.schedule.repository.ScheduleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final CompanyRepository companyRepository;
    private final FileService fileService; // 👈 FileService 주입

    // TODO: 현재 로그인한 사용자 ID를 가져오는 로직 (보안 컨텍스트)이 필요합니다.
    private final Long MOCK_USER_ID = 1L;

    // --- C (Create) ---
    @Transactional
    public Schedule createSchedule(ScheduleRequest request) {

        // Company 엔티티 검증 및 조회 (Company ID는 필수)
        Company company = companyRepository.findById(request.getCompanyId())
                .orElseThrow(() -> new CompanyException(ErrorCode.NOT_FOUND_COMPANY, "해당 기업 정보를 찾을 수 없습니다."));

        // Schedule 엔티티 생성 (파일 관련 인자 제거됨)
        Schedule schedule = Schedule.builder()
                .userId(MOCK_USER_ID) // Mock 사용자 ID 사용
                .company(company)
                .title(request.getTitle())
                .scheduleDate(request.getScheduleDate())
                .scheduleTime(request.getScheduleTime())
                .detailTitle(request.getDetailTitle())
                .detailContent(request.getDetailContent())
                .build();

        // Schedule 저장 (ID를 할당받기 위해 먼저 저장)
        Schedule savedSchedule = scheduleRepository.save(schedule);

        // 파일 업로드 및 연결 로직 (FileService 호출)
        // Schedule 엔티티에 ID가 부여된 후 파일을 연결
        if (request.getFiles() != null && !request.getFiles().isEmpty()) {
            fileService.uploadAndLinkFiles(savedSchedule, request.getFiles());
        }

        return savedSchedule;
    }

    // --- R (Read) ---
    @Transactional(readOnly = true)
    public Schedule getScheduleDetail(Long scheduleId) {
        // TODO: userId 검증 로직 추가 (자신의 일정만 조회 가능하도록)
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ErrorCode.NOT_FOUND_SCHEDULE, "요청하신 일정을 찾을 수 없습니다."));

        // 접근 권한 검증 (MOCK_USER_ID 기준)
        if (!schedule.getUserId().equals(MOCK_USER_ID)) {
            throw new ScheduleException(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }

        return schedule;
    }

    @Transactional(readOnly = true)
    public List<Schedule> getUserSchedules() {
        return scheduleRepository.findAllByUserIdOrderByScheduleDateAsc(MOCK_USER_ID);
    }

    // --- U (Update) ---
    @Transactional
    public Schedule updateSchedule(Long scheduleId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ErrorCode.NOT_FOUND_SCHEDULE, "수정할 일정을 찾을 수 없습니다."));

        // 접근 권한 검증
        if (!schedule.getUserId().equals(MOCK_USER_ID)) {
            throw new ScheduleException(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }

        // 일정 내용 갱신 (파일 관련 인자 제거됨)
        schedule.update(
                request.getTitle(),
                request.getScheduleDate(),
                request.getScheduleTime(),
                request.getDetailTitle(),
                request.getDetailContent()
        );

        // 파일 갱신 로직: FileService 호출
        // ScheduleRequest DTO에 파일을 삭제할 ID 리스트(getFilesToDelete)와 새로 추가할 파일(getNewFiles)이 있다고 가정
        // if (request.getFilesToDelete() != null || request.getNewFiles() != null) {
        //     fileService.updateFiles(schedule, request.getFilesToDelete(), request.getNewFiles()); 
        // }
        // 현재는 DTO의 필드가 불명확하므로, 파일 갱신 로직은 주석 처리된 상태로 남깁니다.

        return schedule; // 변경 감지로 자동 업데이트
    }

    // --- D (Delete) ---
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleException(ErrorCode.NOT_FOUND_SCHEDULE, "삭제할 일정을 찾을 수 없습니다."));

        // 접근 권한 검증
        if (!schedule.getUserId().equals(MOCK_USER_ID)) {
            throw new ScheduleException(ErrorCode.SCHEDULE_ACCESS_DENIED);
        }

        // S3에 저장된 실제 파일을 먼저 삭제
        fileService.deleteS3FilesForSchedule(schedule.getFiles());

        // Schedule 엔티티 삭제 (CascadeType.ALL에 의해 DB의 File 메타데이터도 자동 삭제됨)
        scheduleRepository.delete(schedule);
    }
}
package com.the11job.backend.schedule.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ScheduleRequest {

    // 1. 기업 ID (Company와의 관계 설정용)
    @NotBlank(message = "기업 이름은 필수입니다.")
    private String companyName;

    // 2. 캘린더 일정
    @NotNull(message = "일정 제목은 필수입니다.")
    private String title;

    @NotNull(message = "일정 날짜는 필수입니다.")
    private LocalDate scheduleDate;

    // 3. 상세 내용
    // 화면에서 "제목"과 "내용" 쌍이 여러 개 들어옴
    private List<ScheduleDetailRequest> details;

    // 4. 파일 업로드 및 관리

    // C: 새로 업로드할 파일 리스트
    private List<MultipartFile> files;

    // U/D: (선택적) 기존 파일 중 삭제할 파일 ID 리스트
    private List<Long> filesToDelete;
}
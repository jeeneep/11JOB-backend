// src/main/java/com/the11job.backend.schedule.dto/ScheduleRequest.java (수정)
package com.the11job.backend.schedule.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class ScheduleRequest {

    // 기업 ID (Company와의 관계 설정용)
    private Long companyId;

    // 캘린더 일정
    private String title;
    private LocalDate scheduleDate;
    private LocalDateTime scheduleTime;

    // 상세 내용
    private String detailTitle;
    private String detailContent;


    // 다중 파일 업로드를 위한 필드
    // 클라이언트로부터 받는 실제 파일 데이터 (MultipartFile) 리스트
    private List<MultipartFile> files;

    // 🌟(선택적) 파일 수정/삭제를 위한 필드 추가
    // 파일 수정/삭제 시 필요한 필드는 현재 로직에서 호출되진 않았지만,
    // updateFiles 메서드 구현을 위해 일반적으로 필요합니다.
    // private List<Long> filesToDelete;
    // private List<MultipartFile> newFiles;
}
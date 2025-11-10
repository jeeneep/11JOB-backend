package com.the11job.backend.schedule.controller;

import com.the11job.backend.schedule.dto.ScheduleRequest;
import com.the11job.backend.schedule.entity.Schedule;
import com.the11job.backend.schedule.service.ScheduleService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자별 채용 일정(Schedule)의 CRUD를 담당하는 API Controller (현재는 Mock User ID를 사용)
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // ----------------------------------------------------
    // C (Create) : 일정 등록
    // ----------------------------------------------------

    /**
     * 새로운 세부 일정을 등록합니다.
     *
     * @param request 일정 등록 요청 DTO (회사 ID, 제목, 날짜, 내용 등)
     * @return 등록된 Schedule 객체
     */
    @PostMapping
    public ResponseEntity<Schedule> createSchedule(@RequestBody ScheduleRequest request) {
        log.info("새로운 일정 등록 요청: {}", request);
        Schedule newSchedule = scheduleService.createSchedule(request);

        // HTTP 201 Created 응답
        return ResponseEntity.status(HttpStatus.CREATED).body(newSchedule);
    }

    // ----------------------------------------------------
    // R (Read) : 조회
    // ----------------------------------------------------

    /**
     * 현재 사용자의 전체 일정을 조회합니다. (캘린더 뷰, 대시보드 리스트 등에 사용)
     *
     * @return 현재 사용자의 전체 Schedule 리스트
     */
    @GetMapping
    public ResponseEntity<List<Schedule>> getUserSchedules() {
        log.info("사용자 전체 일정 조회 요청");
        List<Schedule> schedules = scheduleService.getUserSchedules();
        return ResponseEntity.ok(schedules);
    }

    /**
     * 특정 일정의 상세 정보를 조회합니다.
     *
     * @param scheduleId 조회할 일정의 ID
     * @return Schedule 상세 객체
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<Schedule> getScheduleDetail(@PathVariable Long scheduleId) {
        log.info("일정 상세 조회 요청: ID={}", scheduleId);
        Schedule schedule = scheduleService.getScheduleDetail(scheduleId);
        return ResponseEntity.ok(schedule);
    }

    // ----------------------------------------------------
    // U (Update) : 일정 수정
    // ----------------------------------------------------

    /**
     * 특정 일정의 내용을 수정합니다.
     *
     * @param scheduleId 수정할 일정의 ID
     * @param request    수정 요청 DTO
     * @return 수정된 Schedule 객체
     */
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Schedule> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleRequest request) {

        log.info("일정 수정 요청: ID={}, 데이터={}", scheduleId, request);
        Schedule updatedSchedule = scheduleService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok(updatedSchedule);
    }

    // ----------------------------------------------------
    // D (Delete) : 일정 삭제
    // ----------------------------------------------------

    /**
     * 특정 일정을 삭제합니다.
     *
     * @param scheduleId 삭제할 일정의 ID
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        log.warn("일정 삭제 요청: ID={}", scheduleId);
        scheduleService.deleteSchedule(scheduleId);

        // HTTP 204 No Content 응답 (성공적으로 삭제되었으나 본문 없음)
        return ResponseEntity.noContent().build();
    }
}
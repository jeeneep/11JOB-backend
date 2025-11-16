// src/main/java/com/the11job.backend.schedule.controller/ScheduleController.java (개선)
package com.the11job.backend.schedule.controller;

import com.the11job.backend.schedule.dto.ScheduleRequest;
import com.the11job.backend.schedule.dto.ScheduleResponse;
import com.the11job.backend.schedule.entity.Schedule;
import com.the11job.backend.schedule.service.ScheduleService;
import com.the11job.backend.user.entity.User;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // --- C (Create) ---
    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ScheduleResponse> createSchedule(
            @AuthenticationPrincipal User user,
            @RequestPart("dto") @Valid ScheduleRequest request
    ) {
        Schedule schedule = scheduleService.createSchedule(user, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ScheduleResponse(schedule));
    }

    // --- R (Read - All) ---
    @GetMapping
    public ResponseEntity<List<ScheduleResponse>> getMySchedules(@AuthenticationPrincipal User user) {
        List<Schedule> schedules = scheduleService.getUserSchedules(user);

        List<ScheduleResponse> response = schedules.stream().map(ScheduleResponse::new).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // --- R (Read - Detail) ---
    @GetMapping("/{scheduleId}")
    public ResponseEntity<ScheduleResponse> getScheduleDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long scheduleId
    ) {
        Schedule schedule = scheduleService.getScheduleDetail(user, scheduleId);
        return ResponseEntity.ok(new ScheduleResponse(schedule));
    }

    // --- U (Update) ---
    @PutMapping(value = "/{scheduleId}", consumes = {"multipart/form-data"})
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @AuthenticationPrincipal User user,
            @PathVariable Long scheduleId,
            @RequestPart("dto") @Valid ScheduleRequest request
    ) {
        Schedule updatedSchedule = scheduleService.updateSchedule(user, scheduleId, request);

        return ResponseEntity.ok(new ScheduleResponse(updatedSchedule));
    }

    // --- D (Delete) ---
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<String> deleteSchedule(
            @AuthenticationPrincipal User user,
            @PathVariable Long scheduleId
    ) {
        scheduleService.deleteSchedule(user, scheduleId);

        return ResponseEntity.ok("일정이 성공적으로 삭제되었습니다.");
    }
}
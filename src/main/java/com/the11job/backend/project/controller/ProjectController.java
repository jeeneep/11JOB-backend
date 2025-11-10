package com.the11job.backend.project.controller;

import com.the11job.backend.project.dto.ProjectDto;
import com.the11job.backend.project.service.ProjectService;
import com.the11job.backend.user.entity.User;
import com.the11job.backend.user.exception.UserException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.the11job.backend.project.dto.ProjectResponseDto;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> addProject(
            @AuthenticationPrincipal User user,
            @RequestPart("dto") @Valid ProjectDto projectDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        projectService.addProject(user, projectDto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body("프로젝트가 성공적으로 추가되었습니다.");
    }

    @GetMapping
    public ResponseEntity<?> getMyProjects(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        // ✅ 3. DTO 리스트를 받아서 OK(200) 응답
        List<ProjectResponseDto> projectsDto = projectService.findMyProjects(user);
        return ResponseEntity.ok(projectsDto);
    }

    @PutMapping(value = "/{projectId}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updateProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @RequestPart("dto") @Valid ProjectDto projectDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            projectService.updateProject(user, projectId, projectDto, image);
            return ResponseEntity.ok("프로젝트가 성공적으로 수정되었습니다.");

        } catch (IllegalArgumentException e) {
            // (Service에서 "프로젝트를 찾을 수 없습니다." 예외 발생 시)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (UserException e) {
            // (Service에서 "내 것이 아닙니다." 예외 발생 시)
            return ResponseEntity.status(e.getExceptionType().getHttpStatus()).body(e.getMessage());
        }
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId
    ) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            projectService.deleteProject(user, projectId);
            return ResponseEntity.ok("프로젝트가 성공적으로 삭제되었습니다.");

        } catch (IllegalArgumentException e) {
            // (Service에서 "프로젝트를 찾을 수 없습니다." 예외 발생 시)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (UserException e) {
            // (Service에서 "내 것이 아닙니다." 예외 발생 시)
            return ResponseEntity.status(e.getExceptionType().getHttpStatus()).body(e.getMessage());
        }
    }
}
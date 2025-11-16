package com.the11job.backend.project.controller;

import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.project.dto.ProjectDto;
import com.the11job.backend.project.dto.ProjectResponseDto;
import com.the11job.backend.project.service.ProjectService;
import com.the11job.backend.user.entity.User;
import com.the11job.backend.user.exception.UserException;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 인증된 사용자(`user != null`)만 접근 가능하도록 Spring Security 설정을 가정
    private User validateUser(User user) {
        if (user == null) {
            // User가 null인 경우, UserException을 던져 전역 예외 핸들러가 처리하도록 위임
            // ErrorCode.UNAUTHORIZED_USER는 인증 정보가 유효하지 않음 (401)을 의미합니다.
            throw new UserException(ErrorCode.UNAUTHORIZED_USER);
        }
        return user;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> addProject(
            @AuthenticationPrincipal User user,
            @RequestPart("dto") @Valid ProjectDto projectDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        // User가 null인 경우 validateUser에서 UserException(UNAUTHORIZED_USER)를 던져 401 응답 유도
        User validatedUser = validateUser(user);

        projectService.addProject(validatedUser, projectDto, image);
        return ResponseEntity.status(HttpStatus.CREATED).body("프로젝트가 성공적으로 추가되었습니다.");
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDto>> getMyProjects(@AuthenticationPrincipal User user) {
        // User가 null인 경우 validateUser에서 UserException(UNAUTHORIZED_USER)를 던져 401 응답 유도
        User validatedUser = validateUser(user);

        // ✅ 3. DTO 리스트를 받아서 OK(200) 응답
        List<ProjectResponseDto> projectsDto = projectService.findMyProjects(validatedUser);
        return ResponseEntity.ok(projectsDto);
    }

    @PutMapping(value = "/{projectId}", consumes = {"multipart/form-data"})
    public ResponseEntity<String> updateProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId,
            @RequestPart("dto") @Valid ProjectDto projectDto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        // User가 null인 경우 validateUser에서 UserException(UNAUTHORIZED_USER)를 던져 401 응답 유도
        User validatedUser = validateUser(user);

        // 서비스에서 발생하는 모든 예외(NOT_FOUND, FORBIDDEN 등)는
        // Controller 밖에서 ExceptionControllerAdvice가 처리합니다.
        projectService.updateProject(validatedUser, projectId, projectDto, image);
        return ResponseEntity.ok("프로젝트가 성공적으로 수정되었습니다.");
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<String> deleteProject(
            @AuthenticationPrincipal User user,
            @PathVariable Long projectId
    ) {
        // User가 null인 경우 validateUser에서 UserException(UNAUTHORIZED_USER)를 던져 401 응답 유도
        User validatedUser = validateUser(user);

        // 서비스에서 발생하는 모든 예외(NOT_FOUND, FORBIDDEN 등)는
        // Controller 밖에서 ExceptionControllerAdvice가 처리합니다.
        projectService.deleteProject(validatedUser, projectId);
        return ResponseEntity.ok("프로젝트가 성공적으로 삭제되었습니다.");
    }
}
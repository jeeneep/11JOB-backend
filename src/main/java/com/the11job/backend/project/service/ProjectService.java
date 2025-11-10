package com.the11job.backend.project.service;

// FileStorageService 임포트 없음
import com.the11job.backend.project.dto.ProjectDto;
import com.the11job.backend.project.entity.Project;
import com.the11job.backend.project.repository.ProjectRepository;
import com.the11job.backend.user.entity.User;
import com.the11job.backend.user.exception.UserException;       // ✅ 예외 임포트
import com.the11job.backend.user.exception.UserExceptionType; // ✅ 예외 타입 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import com.the11job.backend.project.dto.ProjectResponseDto;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    // FileStorageService 의존성 없음

    /**
     * 새 프로젝트 1개 등록
     */
    @Transactional
    public void addProject(User user, ProjectDto projectDto, MultipartFile image) {

        // ❗️(주의) 'image' 파라미터는 받지만 사용하지 않음
        String imageUrl = null; // 파일 저장 기능 비활성화

        Project project = new Project(
                projectDto.getTitle(),
                projectDto.getDescription(),
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                projectDto.getLinkUrl(),
                imageUrl, // null
                user
        );

        projectRepository.save(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDto> findMyProjects(User user) {
        return projectRepository.findByUser(user)
                .stream()
                .map(ProjectResponseDto::new) // 4. 엔티티를 DTO로 변환
                .collect(Collectors.toList());
    }

    /**
     * [신규] "내" 프로젝트 1개 수정
     */
    @Transactional
    public void updateProject(User user, Long projectId, ProjectDto projectDto, MultipartFile image) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId); // 1. 조회 및 권한 확인

        String imageUrl = null; // 파일 저장 기능 비활성화
        // (나중에 파일 저장 기능 추가 시)
        // if (image != null && !image.isEmpty()) {
        //     imageUrl = fileStorageService.storeFile(image);
        // }

        // 2. 엔티티 업데이트
        project.update(
                projectDto.getTitle(),
                projectDto.getDescription(),
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                projectDto.getLinkUrl(),
                imageUrl // null
        );

        projectRepository.save(project); // (JPA가 변경 감지하여 UPDATE)
    }

    /**
     * ✅ [신규] "내" 프로젝트 1개 삭제
     */
    @Transactional
    public void deleteProject(User user, Long projectId) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId); // 1. 조회 및 권한 확인

        // (나중에 파일 저장 기능 추가 시, 여기서 S3/로컬 파일 삭제)
        // fileStorageService.deleteFile(project.getImageUrl());

        // 2. DB에서 삭제
        projectRepository.delete(project);
    }

    /**
     * ✅ [신규] 프로젝트 조회 및 소유권 검증 (내부 헬퍼 메서드)
     */
    private Project findProjectByIdAndCheckOwnership(User user, Long projectId) {
        // 1. 프로젝트 ID로 엔티티 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다. (ID: " + projectId + ")"));

        // 2. [보안] "내 것"이 맞는지 확인
        if (!project.getUser().getId().equals(user.getId())) {
            throw new UserException(UserExceptionType.FORBIDDEN_ACCESS);
        }

        return project;
    }
}
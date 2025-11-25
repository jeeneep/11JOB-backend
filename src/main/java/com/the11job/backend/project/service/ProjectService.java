package com.the11job.backend.project.service;

import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.file.service.FileService;
import com.the11job.backend.global.exception.BaseException; // FileService 예외 처리용 (유지)
import com.the11job.backend.project.dto.ProjectDto;
import com.the11job.backend.project.dto.ProjectResponseDto;
import com.the11job.backend.project.entity.Project;
import com.the11job.backend.project.exception.ProjectException;
import com.the11job.backend.project.repository.ProjectRepository;
import com.the11job.backend.user.entity.User;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final FileService fileService;

    private static final String S3_DIRECTORY_NAME = "project"; // S3 저장 디렉토리명

    // 1. 새 프로젝트 1개 등록 (FileService 사용)
    @Transactional
    public void addProject(User user, ProjectDto projectDto, MultipartFile image) {

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = fileService.uploadAndReplaceSingleFile(null, image, S3_DIRECTORY_NAME);
        }

        Project project = new Project(
                projectDto.getTitle(),
                projectDto.getDescription(),
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                projectDto.getLinkUrl(),
                imageUrl, // S3 URL 저장
                user
        );

        projectRepository.save(project);
    }

    // 2. 프로젝트 1개 수정 (FileService 사용)
    @Transactional
    public void updateProject(User user, Long projectId, ProjectDto projectDto, MultipartFile image) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId); // 1. 조회 및 권한 확인

        String oldImageUrl = project.getImageUrl();
        String newImageUrl = oldImageUrl;

        newImageUrl = fileService.uploadAndReplaceSingleFile(oldImageUrl, image, S3_DIRECTORY_NAME);

        // 2. 엔티티 업데이트
        project.update(
                projectDto.getTitle(),
                projectDto.getDescription(),
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                projectDto.getLinkUrl(),
                newImageUrl // S3 URL 저장
        );
    }

    // 3. 프로젝트 1개 삭제 (FileService 사용)
    @Transactional
    public void deleteProject(User user, Long projectId) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId); // 1. 조회 및 권한 확인

        String imageUrl = project.getImageUrl();

        if (StringUtils.hasText(imageUrl)) {
            fileService.deleteSingleFile(imageUrl);
        }

        // 2. DB에서 삭제
        projectRepository.delete(project);
    }

    // (findMyProjects 및 findProjectByIdAndCheckOwnership 메서드는 이전과 동일)
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> findMyProjects(User user) {
        return projectRepository.findByUser(user)
                .stream()
                .map(ProjectResponseDto::new)
                .collect(Collectors.toList());
    }

    private Project findProjectByIdAndCheckOwnership(User user, Long projectId) {
        // 1. 프로젝트 ID로 엔티티 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ErrorCode.NOT_FOUND_PROJECT));
        // 2. [보안] "내 것"이 맞는지 확인
        if (!project.getUser().getId().equals(user.getId())) {
            throw new ProjectException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        return project;
    }
}
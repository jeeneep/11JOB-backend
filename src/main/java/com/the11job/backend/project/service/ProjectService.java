package com.the11job.backend.project.service;

import com.the11job.backend.file.service.FileService;
import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;
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
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final FileService fileService;

    private static final String S3_DIRECTORY_NAME = "project";

    // 1. 새 프로젝트 1개 등록 (Full URL 저장)
    @Transactional
    public void addProject(User user, ProjectDto projectDto, MultipartFile image) {

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                // FileService의 uploadAndReplaceSingleFile은 Full URL을 반환 (저장 로직 통일)
                imageUrl = fileService.uploadAndReplaceSingleFile(null, image, S3_DIRECTORY_NAME);
            } catch (BaseException e) {
                throw new ProjectException(ErrorCode.PROJECT_IMAGE_UPLOAD_FAIL, "이미지 업로드에 실패했습니다.", e);
            }
        }
        Project project = new Project(
                projectDto.getTitle(),
                projectDto.getDescription(),
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                projectDto.getLinkUrl(),
                imageUrl, // Full URL 저장
                user
        );
        projectRepository.save(project);
    }

    // 2. 프로젝트 1개 수정 (Full URL 저장)
    @Transactional
    public void updateProject(User user, Long projectId, ProjectDto projectDto, MultipartFile image) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId);
        String oldImageUrl = project.getImageUrl();

        try {
            String newImageUrl = fileService.uploadAndReplaceSingleFile(oldImageUrl, image, S3_DIRECTORY_NAME);

            // 2. 엔티티 업데이트 (Full URL 저장)
            project.update(
                    projectDto.getTitle(),
                    projectDto.getDescription(),
                    projectDto.getStartDate(),
                    projectDto.getEndDate(),
                    projectDto.getLinkUrl(),
                    newImageUrl // Full URL 저장
            );
        } catch (BaseException e) {
            throw new ProjectException(ErrorCode.PROJECT_IMAGE_UPLOAD_FAIL, "이미지 업데이트에 실패했습니다.", e);
        }
    }

    // 3. 프로젝트 1개 삭제
    @Transactional
    public void deleteProject(User user, Long projectId) {
        Project project = findProjectByIdAndCheckOwnership(user, projectId);
        String imageUrl = project.getImageUrl();

        // FileService를 사용하여 S3 파일 삭제
        if (StringUtils.hasText(imageUrl)) {
            fileService.deleteSingleFile(imageUrl);
        }
        projectRepository.delete(project);
    }

    // 4. 내 프로젝트 전체 조회 (DTO에 FileService 전달 제거)
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> findMyProjects(User user) {
        return projectRepository.findByUser(user)
                .stream()
                // 🚨 수정: FileService 전달 로직 제거. DTO가 (Project project)만 받도록 변경
                .map(ProjectResponseDto::new)
                .collect(Collectors.toList());
    }

    private Project findProjectByIdAndCheckOwnership(User user, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ErrorCode.NOT_FOUND_PROJECT));
        if (!project.getUser().getId().equals(user.getId())) {
            throw new ProjectException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        return project;
    }

    // ----------------------------------------------------
    // 회원 삭제를 위해 필요한 메서드
    // ----------------------------------------------------
    @Transactional
    public void deleteAllByUser(User user) {

        List<Project> projects = projectRepository.findAllByUser(user);

        if (projects.isEmpty()) {
            return;
        }

        // Project 엔티티에 imageUrl이 S3 URL일 경우, S3에서 파일도 삭제
        projects.forEach(project -> {
            if (project.getImageUrl() != null) {
                fileService.deleteSingleFile(project.getImageUrl()); // FileService를 주입받아 사용
            }
        });

        // DB에서 Project 엔티티들을 일괄 삭제
        projectRepository.deleteAll(projects);
    }
}
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
import java.io.IOException;
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

    private static final String S3_DIRECTORY_NAME = "project"; // S3 저장 디렉토리명

    // 1. 새 프로젝트 1개 등록 (이미지 저장 로직 FileService 위임)
    @Transactional
    public void addProject(User user, ProjectDto projectDto, MultipartFile image) {

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                // FileService를 사용하여 업로드하고 최종 URL을 받음 (FileService 내부에서 S3Uploader 사용)
                // uploadAndReplaceSingleFile 메서드는 기존 파일이 없으면 새 파일 URL만 반환하는 로직으로 재사용 가능
                imageUrl = fileService.uploadAndReplaceSingleFile(null, image, S3_DIRECTORY_NAME);
            } catch (BaseException e) { // FileService에서 던지는 예외를 받음
                throw new ProjectException(ErrorCode.PROJECT_IMAGE_UPLOAD_FAIL, "이미지 업로드에 실패했습니다.", e);
            }
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

    // 2. 프로젝트 1개 수정 (기존 이미지 관리 로직 FileService 위임)
    @Transactional
    public void updateProject(User user, Long projectId, ProjectDto projectDto, MultipartFile image) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId);

        String oldImageUrl = project.getImageUrl();
        String newImageUrl = oldImageUrl;

        // 이미지 파일 처리 로직 (FileService.uploadAndReplaceSingleFile 재사용)
        // FileService의 해당 메소드는 기존 파일 삭제, 새 파일 업로드, 최종 URL 반환을 모두 처리합니다.
        try {
            newImageUrl = fileService.uploadAndReplaceSingleFile(oldImageUrl, image, S3_DIRECTORY_NAME);
        } catch (BaseException e) {
            throw new ProjectException(ErrorCode.PROJECT_IMAGE_UPLOAD_FAIL, "이미지 업데이트에 실패했습니다.", e);
        }

        // 프론트엔드에서 '이미지 삭제' 요청을 별도로 처리해야 한다면 로직 추가 필요.
        // 현재는 새 파일이 없으면 oldImageUrl 유지하는 로직이 FileService 내부에 있음.

        // 2. 엔티티 업데이트
        project.update(
                projectDto.getTitle(),
                projectDto.getDescription(),
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                projectDto.getLinkUrl(),
                newImageUrl
        );
    }

    // 3. 프로젝트 1개 삭제 (S3 파일 삭제 FileService 위임)
    @Transactional
    public void deleteProject(User user, Long projectId) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId);

        String imageUrl = project.getImageUrl();

        // S3 파일 삭제 로직 FileService 위임
        if (StringUtils.hasText(imageUrl)) {
            // FileService를 사용하여 S3 파일 삭제
            fileService.deleteSingleFile(imageUrl);
        }

        // 2. DB에서 삭제
        projectRepository.delete(project);
    }

    // 4. 내 프로젝트 전체 조회 (URL 변환 적용)
    @Transactional(readOnly = true)
    public List<ProjectResponseDto> findMyProjects(User user) {
        return projectRepository.findByUser(user)
                .stream()
                // 🌟 DTO 생성 시 FileService를 함께 전달하여 URL 변환 처리
                .map(project -> new ProjectResponseDto(project, fileService))
                .collect(Collectors.toList());
    }

    // ... (findProjectByIdAndCheckOwnership 메서드는 이전과 동일)
    private Project findProjectByIdAndCheckOwnership(User user, Long projectId) {
        // ... (내용 생략)
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectException(ErrorCode.NOT_FOUND_PROJECT));
        if (!project.getUser().getId().equals(user.getId())) {
            throw new ProjectException(ErrorCode.PROJECT_ACCESS_DENIED);
        }

        return project;
    }
}
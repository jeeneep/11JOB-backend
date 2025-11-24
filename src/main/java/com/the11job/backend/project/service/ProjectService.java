package com.the11job.backend.project.service;

import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.global.util.S3Uploader;
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
    private final S3Uploader s3Uploader;

    private static final String S3_DIRECTORY_NAME = "project"; // S3 저장 디렉토리명

    // 1. 새 프로젝트 1개 등록 (이미지 저장 추가)
    @Transactional
    public void addProject(User user, ProjectDto projectDto, MultipartFile image) {

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            try {
                // 이미지를 S3에 업로드
                imageUrl = s3Uploader.upload(image, S3_DIRECTORY_NAME);
            } catch (IOException e) {
                // S3Uploader 내부에서 발생하는 예외를 적절히 처리하거나 BaseException으로 변환해야 함
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

    // 2. 프로젝트 1개 수정 (기존 이미지 관리 로직 추가)
    @Transactional
    public void updateProject(User user, Long projectId, ProjectDto projectDto, MultipartFile image) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId); // 1. 조회 및 권한 확인

        String oldImageUrl = project.getImageUrl();
        String newImageUrl = oldImageUrl; // 기본적으로 기존 이미지 URL을 유지

        // ❗️ 이미지 파일 처리 로직
        if (image != null && !image.isEmpty()) {
            // 새 파일이 넘어온 경우 (변경)
            try {
                // 1) 새 파일을 S3에 업로드
                newImageUrl = s3Uploader.upload(image, S3_DIRECTORY_NAME);

                // 2) 기존 파일이 있었다면 S3에서 삭제
                if (StringUtils.hasText(oldImageUrl)) {
                    s3Uploader.deleteFile(oldImageUrl);
                }
            } catch (IOException e) {
                // 💡 수정된 부분: 프로젝트 도메인 고유 코드로 변경
                throw new ProjectException(ErrorCode.PROJECT_IMAGE_UPLOAD_FAIL, "이미지 업로드에 실패했습니다.", e);
            }
        } else {
            // 파일이 넘어오지 않은 경우 (기존 이미지 유지 또는 삭제)

            // 프론트엔드에서 '이미지 삭제' 요청을 별도로 처리하지 않고,
            // 새로운 이미지가 넘어오지 않은 경우 기존 이미지를 유지한다고 가정
            newImageUrl = oldImageUrl;

            // 만약 프론트엔드에서 '이미지 삭제' 요청을 따로 보내는 경우:
            // if (requestDto.isImageDeleted() && StringUtils.hasText(oldImageUrl)) {
            //     s3Uploader.deleteFile(oldImageUrl);
            //     newImageUrl = null;
            // } else {
            //     newImageUrl = oldImageUrl;
            // }
        }

        // 2. 엔티티 업데이트
        project.update(
                projectDto.getTitle(),
                projectDto.getDescription(),
                projectDto.getStartDate(),
                projectDto.getEndDate(),
                projectDto.getLinkUrl(),
                newImageUrl // S3 URL 저장
        );

        // projectRepository.save(project); // JPA 변경 감지(Dirty Checking)로 생략 가능
    }

    // 3. 프로젝트 1개 삭제 (S3 파일 삭제 추가)
    @Transactional
    public void deleteProject(User user, Long projectId) {

        Project project = findProjectByIdAndCheckOwnership(user, projectId); // 1. 조회 및 권한 확인

        String imageUrl = project.getImageUrl();

        // ❗️ S3 파일 삭제 로직 추가
        if (StringUtils.hasText(imageUrl)) {
            s3Uploader.deleteFile(imageUrl);
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
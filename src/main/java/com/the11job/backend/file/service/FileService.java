package com.the11job.backend.file.service;

import com.the11job.backend.file.entity.File;
import com.the11job.backend.file.repository.FileRepository;
import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.global.util.S3Uploader;
import com.the11job.backend.schedule.entity.Schedule;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final S3Uploader s3Uploader;
    private final FileRepository fileRepository;

    /**
     * S3에 파일을 업로드하고, File 엔티티를 생성하여 Schedule에 연결합니다.
     */
    @Transactional
    public List<File> uploadAndLinkFiles(Schedule schedule, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .map(file -> {
                    if (file.isEmpty()) {
                        return null;
                    }

                    try {
                        // S3 업로드 (Schedule ID를 사용하여 폴더 경로 설정)
                        String dirName = "schedule/" + schedule.getId();
                        // S3Uploader가 Full URL을 반환한다고 가정
                        String fileUrl = s3Uploader.upload(file, dirName);

                        // File 엔티티 생성 및 Schedule 연결
                        File fileEntity = File.builder()
                                .originalName(file.getOriginalFilename())
                                .storagePath(fileUrl) // DB에 Full URL 저장
                                .contentType(file.getContentType())
                                .build();

                        schedule.addFile(fileEntity);
                        File savedFile = fileRepository.save(fileEntity);
                        return savedFile;

                    } catch (IOException e) {
                        log.error("파일 업로드 중 IO 예외 발생: {}", file.getOriginalFilename(), e);
                        throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 입출력 오류가 발생했습니다.", e);
                    } catch (IllegalArgumentException e) {
                        log.error("MultipartFile 변환/처리 실패: {}", file.getOriginalFilename(), e);
                        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
                    } catch (BaseException e) {
                        throw e;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 기존 파일 삭제 목록(filesToDelete)과 신규 파일(newFiles)을 처리하여 일정을 갱신합니다.
     */
    @Transactional
    public void updateFiles(Schedule schedule, List<Long> filesToDelete, List<MultipartFile> newFiles) {

        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            List<File> filesToDeleteEntities = fileRepository.findAllById(filesToDelete);
            deleteS3FilesForSchedule(filesToDeleteEntities);
            fileRepository.deleteAll(filesToDeleteEntities);
            schedule.getFiles().removeAll(filesToDeleteEntities);
        }

        if (newFiles != null && !newFiles.isEmpty()) {
            uploadAndLinkFiles(schedule, newFiles);
        }
    }

    /**
     * Schedule 삭제 시 연결된 모든 S3 파일을 삭제합니다.
     */
    @Transactional
    public void deleteS3FilesForSchedule(List<File> files) {

        if (files == null || files.isEmpty()) {
            return;
        }

        files.forEach(file -> {
            try {
                s3Uploader.deleteFile(file.getStoragePath());
            } catch (BaseException e) {
                log.warn("S3 파일 삭제 중 오류 발생 (DB 삭제는 진행): {}", file.getStoragePath(), e);
            }
        });
    }

    /**
     * 단일 파일을 S3에 업로드하고 기존 파일을 삭제하여 최종 URL을 반환합니다. 이 메소드는 Portfolio와 Project의 대표 이미지 관리에 사용되며, Full URL을 반환합니다.
     */
    @Transactional
    public String uploadAndReplaceSingleFile(String oldFileUrl, MultipartFile newFile, String dirName) {
        if (newFile == null || newFile.isEmpty()) {
            return oldFileUrl; // 새 파일이 없으면 기존 URL 유지
        }

        try {
            // 새 파일 업로드 (Full URL 반환)
            String newFileUrl = s3Uploader.upload(newFile, dirName);

            // 기존 파일 삭제
            if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
                deleteSingleFile(oldFileUrl);
            }
            return newFileUrl;

        } catch (IOException e) {
            log.error("단일 파일 업로드 중 IO 예외 발생: {}", newFile.getOriginalFilename(), e);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 입출력 오류가 발생했습니다.", e);
        } catch (BaseException e) {
            throw e;
        }
    }

    /**
     * 특정 URL의 S3 파일을 삭제합니다.
     */
    public void deleteSingleFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            s3Uploader.deleteFile(fileUrl);
            log.info("S3 단일 파일 삭제 성공: {}", fileUrl);
        } catch (BaseException e) {
            log.warn("S3 단일 파일 삭제 중 오류 발생: {}", fileUrl, e);
            throw e;
        }
    }
}
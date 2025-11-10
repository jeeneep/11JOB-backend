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
                        // S3 업로드 (S3Uploader 사용)
                        String dirName = "schedule/" + schedule.getId();
                        String fileUrl = s3Uploader.upload(file, dirName);

                        // File 엔티티 생성 및 Schedule 연결
                        File fileEntity = File.builder()
                                .originalName(file.getOriginalFilename())
                                .storagePath(fileUrl)
                                .contentType(file.getContentType())
                                .schedule(schedule)
                                .build();

                        // DB 저장 및 Schedule 리스트에 추가
                        File savedFile = fileRepository.save(fileEntity);
                        schedule.getFiles().add(savedFile);
                        return savedFile;

                    } catch (IOException e) {
                        // 파일 저장 중 입출력 오류 (INTERNAL_SERVER_ERROR)
                        log.error("파일 업로드 중 IO 예외 발생: {}", file.getOriginalFilename(), e);
                        throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 입출력 오류가 발생했습니다.", e);
                    } catch (IllegalArgumentException e) {
                        // S3Uploader의 파일 전환 실패 (INVALID_INPUT_VALUE)
                        log.error("MultipartFile 변환/처리 실패: {}", file.getOriginalFilename(), e);
                        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
                    } catch (BaseException e) {
                        // S3Uploader 내부에서 발생한 AWS 통신 오류 (API_EXTERNAL_COMMUNICATION_ERROR 등)
                        throw e;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Schedule 삭제 시 연결된 모든 S3 파일을 삭제합니다. S3 삭제 실패는 로그만 남기고 트랜잭션은 강제 종료하지 않습니다.
     */
    @Transactional
    public void deleteS3FilesForSchedule(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        files.forEach(file -> {
            try {
                // S3Uploader를 사용하여 S3에서 파일 삭제
                s3Uploader.deleteFile(file.getStoragePath());
            } catch (BaseException e) {
                // S3 삭제는 Core Business Logic이 아니므로 예외를 던져 트랜잭션을 롤백시키지 않고 로그를 남기고 진행
                log.warn("S3 파일 삭제 중 오류 발생 (DB 삭제는 진행): {}", file.getStoragePath(), e);
            }
        });
    }
}
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
import org.springframework.beans.factory.annotation.Value; // 🌟 Value import 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    private final S3Uploader s3Uploader;
    private final FileRepository fileRepository;

    // 🌟 S3 Base URL 주입 필드 추가 (application.yml에서 설정 필요)
    @Value("${cloud.aws.s3.endpoint-url}")
    private String s3BaseUrl;

    // 이미지 URL 변환 로직 추가 🌟
    /**
     * S3 객체 키(DB에 저장된 경로)를 완전한 HTTP/HTTPS URL로 변환합니다.
     * 이 메소드는 Portfolio, Project 등의 조회 DTO 생성 시 사용됩니다.
     */
    public String convertToFullUrl(String storagePath) {
        if (storagePath == null || storagePath.isEmpty()) {
            return null; // 경로가 없으면 null 반환
        }

        // 경로가 이미 완전한 URL인 경우 (Schedule처럼 이미 URL이 저장된 경우)
        if (storagePath.startsWith("http://") || storagePath.startsWith("https://")) {
            return storagePath;
        }

        // 경로의 맨 앞 '/'를 제거하여 URL 연결 시 중복 슬래시를 방지합니다.
        String cleanedPath = storagePath.startsWith("/") ? storagePath.substring(1) : storagePath;

        // S3 Base URL과 경로를 조합하여 완전한 URL을 반환합니다.
        // Base URL 뒤에 '/'를 붙여 경로를 연결합니다.
        return s3BaseUrl + "/" + cleanedPath;
    }

    /**
     * S3에 파일을 업로드하고, File 엔티티를 생성하여 Schedule에 연결합니다.
     */
    @Transactional
    public List<File> uploadAndLinkFiles(Schedule schedule, List<MultipartFile> files) {
        // ... (기존 코드와 동일)
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
                        String fileUrl = s3Uploader.upload(file, dirName);

                        // File 엔티티 생성 및 Schedule 연결
                        File fileEntity = File.builder()
                                .originalName(file.getOriginalFilename())
                                .storagePath(fileUrl)
                                .contentType(file.getContentType())
                                //.schedule(schedule)
                                .build();

                        // DB 저장 전, Schedule 엔티티의 편의 메서드를 통해 양방향 관계 설정 및 동기화
                        schedule.addFile(fileEntity);

                        // DB 저장 및 Schedule 리스트에 추가 (JPA 연관관계 관리)
                        File savedFile = fileRepository.save(fileEntity);
                        //schedule.getFiles().add(savedFile);
                        return savedFile;

                    } catch (IOException e) {
                        log.error("파일 업로드 중 IO 예외 발생: {}", file.getOriginalFilename(), e);
                        // 파일 저장 중 입출력 오류는 INTERNAL_SERVER_ERROR로 처리
                        throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 입출력 오류가 발생했습니다.", e);
                    } catch (IllegalArgumentException e) {
                        log.error("MultipartFile 변환/처리 실패: {}", file.getOriginalFilename(), e);
                        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
                    } catch (BaseException e) {
                        // S3 통신 오류 등 S3Uploader 내부에서 발생한 예외
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
        // ... (기존 코드와 동일)
        // 1. 기존 파일 삭제 처리 (DB 및 S3)
        if (filesToDelete != null && !filesToDelete.isEmpty()) {
            // 삭제할 파일 엔티티 목록 조회
            List<File> filesToDeleteEntities = fileRepository.findAllById(filesToDelete);

            // S3에서 파일 삭제 (S3 삭제 실패는 로그만 남기고 트랜잭션 롤백 방지)
            deleteS3FilesForSchedule(filesToDeleteEntities);

            // DB에서 파일 엔티티 삭제
            fileRepository.deleteAll(filesToDeleteEntities);

            // Schedule 엔티티의 files 컬렉션에서도 제거 (JPA 연관관계 관리)
            schedule.getFiles().removeAll(filesToDeleteEntities);
        }

        // 2. 신규 파일 업로드 및 연결
        if (newFiles != null && !newFiles.isEmpty()) {
            // uploadAndLinkFiles 내부에서 S3 업로드 및 DB 저장이 모두 처리됨
            uploadAndLinkFiles(schedule, newFiles);
        }
    }

    /**
     * Schedule 삭제 시 연결된 모든 S3 파일을 삭제합니다. S3 삭제 실패는 로그만 남기고 트랜잭션은 강제 종료하지 않습니다.
     */
    @Transactional
    public void deleteS3FilesForSchedule(List<File> files) {
        // ... (기존 코드와 동일)
        if (files == null || files.isEmpty()) {
            return;
        }

        files.forEach(file -> {
            try {
                // S3Uploader를 사용하여 S3에서 파일 삭제
                s3Uploader.deleteFile(file.getStoragePath());
            } catch (BaseException e) {
                // S3 삭제 실패는 핵심 비즈니스 로직(일정 삭제)을 롤백시키지 않도록 예외를 catch하고 로그를 남김
                log.warn("S3 파일 삭제 중 오류 발생 (DB 삭제는 진행): {}", file.getStoragePath(), e);
            }
        });
    }

    /**
     * 단일 파일을 S3에 업로드하고 기존 파일을 삭제하여 최종 URL을 반환합니다.
     * 이 메서드는 Portfolio와 Project의 대표 이미지 관리에 사용됩니다.
     */
    @Transactional
    public String uploadAndReplaceSingleFile(String oldFileUrl, MultipartFile newFile, String dirName) {
        // ... (기존 코드와 동일)
        if (newFile == null || newFile.isEmpty()) {
            return oldFileUrl; // 새 파일이 없으면 기존 URL 유지
        }

        try {
            // 1. 새 파일 업로드
            String newFileUrl = s3Uploader.upload(newFile, dirName);

            // 2. 기존 파일 삭제 (S3 삭제 실패는 로그만 남김)
            if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
                deleteSingleFile(oldFileUrl); // S3에서 파일 삭제
            }
            return newFileUrl;

        } catch (IOException e) {
            log.error("단일 파일 업로드 중 IO 예외 발생: {}", newFile.getOriginalFilename(), e);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 입출력 오류가 발생했습니다.", e);
        } catch (BaseException e) {
            // S3Uploader가 던진 BaseException을 그대로 던져 상위 서비스가 처리하게 함
            throw e;
        }
    }

    /**
     * 특정 URL의 S3 파일을 삭제합니다. (DB 엔티티 삭제는 호출자가 처리)
     * 이 메서드는 Portfolio와 Project의 삭제 로직 및 단일 파일 교체 로직에서 사용됩니다.
     */
    public void deleteSingleFile(String fileUrl) {
        // ... (기존 코드와 동일)
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }
        try {
            s3Uploader.deleteFile(fileUrl);
            log.info("S3 단일 파일 삭제 성공: {}", fileUrl);
        } catch (BaseException e) {
            // S3 삭제 실패는 로그만 남기고 호출자에게 예외를 던져 트랜잭션 관리를 위임
            log.warn("S3 단일 파일 삭제 중 오류 발생: {}", fileUrl, e);
            throw e;
        }
    }
}
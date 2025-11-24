package com.the11job.backend.global.util;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.the11job.backend.global.exception.BaseException;
import com.the11job.backend.global.exception.ErrorCode;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    // --- 1. 파일 업로드 로직 ---

    /**
     * MultipartFile을 전달받아 File로 전환한 후 S3에 업로드합니다.
     *
     * @return 업로드된 파일의 S3 URL 주소
     * @throws IOException              (로컬 파일 전환 시 발생)
     * @throws IllegalArgumentException (로컬 파일 전환 실패 시 발생)
     * @throws BaseException            (S3 통신 실패 시 발생)
     */
    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("MultipartFile -> File 전환 실패"));

        String uploadImageUrl;
        try {
            uploadImageUrl = upload(uploadFile, dirName);
        } finally {
            removeNewFile(uploadFile);
        }

        return uploadImageUrl;
    }

    private String upload(File uploadFile, String dirName) {
        // uploadFile.getName()은 이제 고유한 UUID 파일명(확장자 포함)을 가집니다.
        String fileName = dirName + "/" + uploadFile.getName();
        return putS3(uploadFile, fileName);
    }

    /**
     * 실질적인 S3에 객체를 저장하는 부분 🔥 [ACL 제거]: The bucket does not allow ACLs 오류 해결을 위해 withCannedAcl 설정을 제거했습니다.
     */
    private String putS3(File uploadFile, String fileName) {
        try {
            amazonS3Client.putObject(
                    new PutObjectRequest(bucket, fileName, uploadFile)
                    // .withCannedAcl(CannedAccessControlList.PublicRead) // 🚨 제거됨
            );
            return amazonS3Client.getUrl(bucket, fileName).toString();
        } catch (SdkClientException e) {
            // AWS SDK 통신 오류 (네트워크, 인증 등) 발생 시
            log.error("S3 파일 업로드 중 AWS 통신 오류 발생: {}", fileName, e);
            throw new BaseException(ErrorCode.API_EXTERNAL_COMMUNICATION_ERROR, "S3 통신 오류로 파일 업로드에 실패했습니다.", e);
        }
    }

    /**
     * 로컬에 생성된 임시 파일을 삭제합니다.
     */
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("로컬 임시 파일이 삭제되었습니다.");
        } else {
            log.error("로컬 임시 파일 삭제에 실패했습니다. 파일명: {}", targetFile.getName());
        }
    }

    /**
     * MultipartFile을 java.io.File로 변환합니다. (로컬 임시 파일 생성) 🔥 [안정화]: 파일명 충돌 방지 및 시스템 임시 디렉토리 사용으로 변경했습니다.
     */
    private Optional<File> convert(MultipartFile file) throws IOException {
        // 1. 고유한 임시 파일명 생성
        String uniqueFileName = changedImageName(file.getOriginalFilename());

        // 2. 시스템 임시 디렉토리에 고유한 파일명으로 File 객체 생성
        File convertFile = new File(System.getProperty("java.io.tmpdir"), uniqueFileName);

        try {
            if (convertFile.createNewFile()) {
                try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                    fos.write(file.getBytes());
                }
                return Optional.of(convertFile);
            }
        } catch (IOException e) {
            log.error("로컬 임시 파일 생성 중 IO 예외 발생: {}", convertFile.getAbsolutePath(), e);
            throw e;
        }

        return Optional.empty(); // 파일 생성에 실패한 경우
    }

    /**
     * 파일 이름 중복 방지를 위해 UUID와 확장자를 조합합니다.
     */
    private String changedImageName(String originName) {
        String ext = getFileExtension(originName);
        String random = UUID.randomUUID().toString();
        return random + ext;
    }

    /**
     * 파일 확장자를 추출합니다.
     */
    private String getFileExtension(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."));
        } catch (StringIndexOutOfBoundsException e) {
            return ""; // 확장자가 없는 경우 빈 문자열 반환
        }
    }

    // --- 2. 파일 삭제 로직 ---

    /**
     * S3 URL을 기반으로 S3 객체를 삭제합니다.
     *
     * @param fileUrl S3에 저장된 파일의 전체 URL
     * @throws BaseException (S3 통신 실패 시 발생)
     */
    public void deleteFile(String fileUrl) {
        try {
            String key = getKeyFromUrl(fileUrl);
            amazonS3Client.deleteObject(bucket, key);
            log.info("S3 파일 삭제 성공: {}", key);
        } catch (IllegalArgumentException e) {
            // URL에서 키 추출 실패는 호출자에게 그대로 던져서 FileService가 처리하도록 위임
            throw e;
        } catch (SdkClientException e) {
            // AWS SDK 통신 오류 발생 시
            log.error("S3 파일 삭제 중 AWS 통신 오류 발생: {}", fileUrl, e);
            throw new BaseException(ErrorCode.API_EXTERNAL_COMMUNICATION_ERROR, "S3 통신 오류로 파일 삭제에 실패했습니다.", e);
        } catch (Exception e) {
            // 기타 예상치 못한 오류
            log.error("S3 파일 삭제 중 예상치 못한 오류 발생: {}", fileUrl, e);
            throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, "S3 파일 삭제 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * S3 URL에서 객체 Key(경로)를 추출합니다.
     */
    private String getKeyFromUrl(String fileUrl) {
        try {
            // URL 디코딩
            String decodedUrl = java.net.URLDecoder.decode(fileUrl, "UTF-8");

            // S3 URL에서 버킷 경로를 제거한 나머지 부분을 Key로 사용합니다.
            // 예: https://bucket-name.s3.ap-northeast-2.amazonaws.com/schedule/xyz_abc.png
            // getKeyFromUrl -> schedule/xyz_abc.png

            // new java.net.URL(decodedUrl).getPath()는 /schedule/xyz_abc.png를 반환
            // substring(1)로 앞의 '/'를 제거
            String urlPath = new java.net.URL(decodedUrl).getPath();
            return urlPath.substring(1);

        } catch (Exception e) {
            log.error("S3 URL에서 키 추출 실패: {}", fileUrl, e);
            throw new IllegalArgumentException("유효하지 않은 S3 URL이거나 키 추출 실패입니다.");
        }
    }
}
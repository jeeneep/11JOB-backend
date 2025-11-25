/*
package com.the11job.backend.portfolio.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service
public class FileStorageService {

    public String storeFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        // (실제 S3 또는 로컬 저장 로직 구현)
        String storedFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        // 예: "s3://.../" + storedFilename 또는 "/uploads/" + storedFilename
        return "/uploads/" + storedFilename;
    }
}
*/
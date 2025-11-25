package com.the11job.backend.portfolio.service;

import com.the11job.backend.file.service.FileService;
import com.the11job.backend.portfolio.dto.PortfolioRegistrationRequestDto;
import com.the11job.backend.portfolio.dto.PortfolioResponseDto;
import com.the11job.backend.portfolio.entity.ActivityItem;
import com.the11job.backend.portfolio.entity.CertificateItem;
import com.the11job.backend.portfolio.entity.EducationItem;
import com.the11job.backend.portfolio.entity.ExperienceItem;
import com.the11job.backend.portfolio.entity.LinkItem;
import com.the11job.backend.portfolio.entity.Portfolio;
import com.the11job.backend.portfolio.repository.PortfolioRepository;
import com.the11job.backend.user.entity.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final FileService fileService;

    private static final String S3_DIRECTORY_NAME = "portfolio"; // S3 디렉토리 설정

    @Transactional
    public void savePortfolio(User user,
                              PortfolioRegistrationRequestDto requestDto,
                              MultipartFile profileImage) {

        Optional<Portfolio> existingPortfolioOpt = portfolioRepository.findByUser(user);

        String oldImagePath = existingPortfolioOpt.map(Portfolio::getProfileImagePath).orElse(null);
        String imagePath = oldImagePath; // 기본적으로 기존 경로 유지

        // 1. 이미지 경로 처리 로직 (FileService 위임 및 try-catch 제거)
        // FileService가 던지는 BaseException은 RuntimeException이므로 try-catch 생략
        imagePath = fileService.uploadAndReplaceSingleFile(oldImagePath, profileImage, S3_DIRECTORY_NAME);

        Portfolio portfolio;
        if (existingPortfolioOpt.isPresent()) {
            portfolio = existingPortfolioOpt.get();
            portfolio.clearChildLists();
            portfolio.updateInfo(requestDto.getPhone(), requestDto.getAddress(), imagePath);
        } else {
            portfolio = new Portfolio(
                    user,
                    requestDto.getPhone(),
                    requestDto.getAddress(),
                    imagePath
            );
        }

        // --- DTO -> Entity 변환 및 단일 리스트에 추가 ---
        if (requestDto.getEducations() != null) {
            requestDto.getEducations().forEach(dto ->
                    portfolio.addItem(new EducationItem(dto.getInstitutionName(), dto.getStartDate(), dto.getEndDate()))
            );
        }
        if (requestDto.getExperiences() != null) {
            requestDto.getExperiences().forEach(dto ->
                    portfolio.addItem(
                            new ExperienceItem(dto.getInstitutionName(), dto.getStartDate(), dto.getEndDate()))
            );
        }
        if (requestDto.getActivities() != null) {
            requestDto.getActivities().forEach(dto ->
                    portfolio.addItem(new ActivityItem(dto.getInstitutionName(), dto.getStartDate(), dto.getEndDate()))
            );
        }
        if (requestDto.getLinks() != null) {
            requestDto.getLinks().forEach(dto ->
                    portfolio.addItem(new LinkItem(dto.getTitle(), dto.getUrl()))
            );
        }
        if (requestDto.getCertificates() != null) {
            requestDto.getCertificates().forEach(dto ->
                    portfolio.addItem(new CertificateItem(dto.getTitle(), dto.getAcquireDate()))
            );
        }

        portfolioRepository.save(portfolio);
    }

    // 포트폴리오 삭제 시 파일 정리 로직 (추가)
    @Transactional
    public void deletePortfolio(User user) {
        Portfolio portfolio = portfolioRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 포트폴리오가 존재하지 않습니다."));

        String imageUrl = portfolio.getProfileImagePath();

        if (imageUrl != null && !imageUrl.isEmpty()) {
            fileService.deleteSingleFile(imageUrl);
        }

        // DB에서 포트폴리오 엔티티 삭제
        portfolioRepository.delete(portfolio);
    }


    @Transactional(readOnly = true)
    public PortfolioResponseDto findMyPortfolioDto(User user) {
        // 1. 유저로 포트폴리오 "껍데기"를 찾음
        Portfolio portfolio = portfolioRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오가 존재하지 않습니다."));

        // 2. ID로 페치 조인(N+1 방지)을 실행해 "완전한" 엔티티를 조회
        Portfolio detailedPortfolio = findPortfolioById(portfolio.getId());

        // 3. "완전한" 엔티티를 DTO로 변환하여 반환
        return new PortfolioResponseDto(detailedPortfolio);
    }

    @Transactional(readOnly = true)
    private Portfolio findPortfolioById(Long portfolioId) {
        return portfolioRepository.findByIdWithDetails(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));
    }
}
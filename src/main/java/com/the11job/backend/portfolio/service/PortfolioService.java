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
    private final FileService fileService; // 파일 관리 위임을 위해 유지

    private static final String S3_DIRECTORY_NAME = "portfolio"; // S3 디렉토리 설정

    @Transactional
    public void savePortfolio(User user,
                              PortfolioRegistrationRequestDto requestDto,
                              MultipartFile profileImage) {

        Optional<Portfolio> existingPortfolioOpt = portfolioRepository.findByUser(user);

        String oldImagePath = existingPortfolioOpt.map(Portfolio::getProfileImagePath).orElse(null);
        String imagePath = oldImagePath; // 기본적으로 기존 경로 유지

        // 1. 이미지 경로 처리 로직 (FileService 위임 - Full URL 반환)
        imagePath = fileService.uploadAndReplaceSingleFile(oldImagePath, profileImage, S3_DIRECTORY_NAME);

        Portfolio portfolio;
        if (existingPortfolioOpt.isPresent()) {
            portfolio = existingPortfolioOpt.get();
            portfolio.clearChildLists();
            // Full URL 저장
            portfolio.updateInfo(requestDto.getPhone(), requestDto.getAddress(), imagePath);
        } else {
            // Full URL 저장
            portfolio = new Portfolio(
                    user,
                    requestDto.getPhone(),
                    requestDto.getAddress(),
                    imagePath
            );
        }

        // --- DTO -> Entity 변환 및 단일 리스트에 추가 (생략) ---
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

    // 포트폴리오 삭제 시 파일 정리 로직
    @Transactional
    public void deletePortfolio(User user) {
        Portfolio portfolio = portfolioRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 포트폴리오가 존재하지 않습니다."));

        String imageUrl = portfolio.getProfileImagePath();

        // FileService를 사용하여 S3 파일 삭제
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
        // 🚨 수정: DTO 생성자에서 FileService 인자 제거
        return new PortfolioResponseDto(detailedPortfolio);
    }

    @Transactional(readOnly = true)
    public Portfolio findPortfolioById(Long portfolioId) {
        return portfolioRepository.findByIdWithDetails(portfolioId)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다."));
    }

    // ----------------------------------------------------
    // 회원 삭제를 위해 필요한 메서드
    // ----------------------------------------------------
    @Transactional
    public void deleteByUser(User user) {

        // 1. 해당 유저의 Portfolio 조회 (User와 Portfolio는 1:1 관계이며 Portfolio 엔티티에 unique=true가 설정되어 있음)
        portfolioRepository.findByUser(user)
                .ifPresent(portfolio -> {
                    // Portfolio 엔티티에 ProfileImagePath가 S3 URL일 경우, S3에서 파일도 삭제
                    if (portfolio.getProfileImagePath() != null) {
                        fileService.deleteSingleFile(portfolio.getProfileImagePath()); // FileService를 주입받아 사용
                    }

                    // Portfolio 엔티티에 PortfolioItem에 CascadeType.ALL이 설정되어 있으므로,
                    // Portfolio를 삭제하면 하위 PortfolioItem도 자동으로 삭제
                    portfolioRepository.delete(portfolio);
                });
    }
}
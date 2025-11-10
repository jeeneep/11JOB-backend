package com.the11job.backend.portfolio.service;

import com.the11job.backend.portfolio.dto.PortfolioRegistrationRequestDto;
import com.the11job.backend.portfolio.dto.PortfolioResponseDto;
import com.the11job.backend.portfolio.entity.*;
import com.the11job.backend.portfolio.repository.PortfolioRepository;
import com.the11job.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public void savePortfolio(User user,
                              PortfolioRegistrationRequestDto requestDto,
                              MultipartFile profileImage) {

        Optional<Portfolio> existingPortfolioOpt = portfolioRepository.findByUser(user);

        String imagePath = fileStorageService.storeFile(profileImage);

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
                    portfolio.addItem(new ExperienceItem(dto.getInstitutionName(), dto.getStartDate(), dto.getEndDate()))
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
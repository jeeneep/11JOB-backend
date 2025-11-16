package com.the11job.backend.portfolio.controller;

import com.the11job.backend.portfolio.dto.PortfolioRegistrationRequestDto;
import com.the11job.backend.portfolio.dto.PortfolioResponseDto;
import com.the11job.backend.portfolio.service.PortfolioService;
import com.the11job.backend.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<String> savePortfolio(
            @AuthenticationPrincipal User user,
            @RequestPart("dto") @Valid PortfolioRegistrationRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        portfolioService.savePortfolio(user, requestDto, profileImage);

        return ResponseEntity.ok("포트폴리오가 성공적으로 저장되었습니다.");
    }

    @GetMapping
    public ResponseEntity<?> getMyPortfolio(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        try {
            PortfolioResponseDto portfolioDto = portfolioService.findMyPortfolioDto(user);
            return ResponseEntity.ok(portfolioDto);

        } catch (IllegalArgumentException e) {
            // (포트폴리오가 없는 사용자의 경우)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
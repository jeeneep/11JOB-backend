package com.the11job.backend.user.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.mail.MessagingException;

import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class EmailAuthService {

    private final RedisTemplate<String, String> redisTemplate;
    private final MailService mailService;

    // 1. 키가 겹치지 않도록 Prefix를 사용합니다.
    private static final String AUTH_PREFIX = "AUTH_NUM:";
    private static final String VERIFIED_PREFIX = "VERIFIED_EMAIL:"; // "인증 완료" 상태

    // 인증번호 유효 기간 (3분으로 설정)
    private static final long AUTH_EXPIRATION_TIME_MINUTES = 3L;
    // 2. "인증 완료" 상태 유효 기간 (5분)
    // (인증 후 5분 이내에 회원가입을 완료해야 함)
    private static final long VERIFIED_EXPIRATION_TIME_MINUTES = 5L;


    public EmailAuthService(@Qualifier("redisTemplate") RedisTemplate<String, String> redisTemplate, MailService mailService) {
        this.redisTemplate = redisTemplate;
        this.mailService = mailService;
    }

    // 인증번호 발송 메서드
    public String sendAuthNumber(String email) throws MessagingException, UnsupportedEncodingException {
        String authNumber = generateAuthNumber();

        // 3. Prefix를 붙여서 Redis에 저장
        redisTemplate.opsForValue().set(
                AUTH_PREFIX + email,
                authNumber,
                AUTH_EXPIRATION_TIME_MINUTES,
                TimeUnit.MINUTES
        );

        mailService.sendMail(email, authNumber);
        return authNumber;
    }

    // 인증번호 검증 메서드
    public boolean validateAuthNumber(String email, String authNumber) {
        // 4. Prefix를 붙여서 Redis에서 인증 번호를 가져옴
        String storedAuthNumber = redisTemplate.opsForValue().get(AUTH_PREFIX + email);

        if (storedAuthNumber != null && storedAuthNumber.equals(authNumber)) {
            // 1. 기존 인증번호 키는 삭제
            redisTemplate.delete(AUTH_PREFIX + email);

            // 2. "인증 완료" 상태를 5분간 새로 저장
            redisTemplate.opsForValue().set(
                    VERIFIED_PREFIX + email,
                    "true", // 값은 "true" 또는 "OK" 등 아무거나 상관없음
                    Duration.ofMinutes(VERIFIED_EXPIRATION_TIME_MINUTES)
            );
            return true;
        }

        return false;
    }

    /**
     * 5. [새로운 메서드] UserService가 회원가입 시 "인증 완료" 상태인지 확인
     * * @return 인증되었으면 true, 아니면 false
     */
    public boolean checkAndRemoveVerificationStatus(String email) {
        // "인증 완료" 키로 조회
        String status = redisTemplate.opsForValue().get(VERIFIED_PREFIX + email);

        if ("true".equals(status)) {
            // "사용"했으므로 즉시 삭제 (회원가입에 한 번만 사용하도록)
            redisTemplate.delete(VERIFIED_PREFIX + email);
            return true;
        }

        return false;
    }

    // 인증번호 생성 메서드 (랜덤한 6자리 숫자)
    private String generateAuthNumber() {
        int authNum = (int) (Math.random() * 1000000);
        return String.format("%06d", authNum);
    }
}
package com.the11job.backend.user.controller;

import com.the11job.backend.user.dto.*;
import com.the11job.backend.user.exception.UserException;
import com.the11job.backend.user.service.EmailAuthService;
import com.the11job.backend.user.service.UserService;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final EmailAuthService emailAuthService;

    public UserController(UserService userService, EmailAuthService emailAuthService) {
        this.userService = userService;
        this.emailAuthService = emailAuthService;
    }

    // 테스트 완료
    @PostMapping("/emailSend")
    public ResponseEntity<CheckResponse> emailSend(@RequestBody EmailCheckRequest request) throws MessagingException, UnsupportedEncodingException {
        userService.emailValidate(request);
        String authNum = emailAuthService.sendAuthNumber(request.getEmail());
        log.info("이메일 인증번호 발송: {}", authNum);

        return ResponseEntity.ok(new CheckResponse(true, "이메일 인증번호가 발송되었습니다. 인증을 진행해주세요."));
    }


    // 테스트 완료
    @PostMapping("/emailCheck")
    public ResponseEntity<EmailCheckResponse> emailCheck(@RequestBody EmailCheckRequest request) {
        log.info("이메일 인증 요청 - 이메일: {}, 인증번호: {}", request.getEmail(), request.getAuthNum());

        boolean isValid = emailAuthService.validateAuthNumber(request.getEmail(), request.getAuthNum());

        if (isValid) {
            return new ResponseEntity<>(new EmailCheckResponse(true, "이메일 인증 성공"), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(new EmailCheckResponse(false, "인증번호가 잘못되었습니다."), HttpStatus.BAD_REQUEST);
        }
    }

    // 테스트 완료
    @PostMapping("/join")
    public ResponseEntity<Long> addMember(@RequestBody JoinRequest request) {
        Long savedUserId = userService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserId);
    }

    // 비밀번호 변경
    @PatchMapping("/change-password")
    public ResponseEntity<CheckResponse> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                        @RequestBody ChangePasswordRequest request) {
        String email = userDetails.getUsername();
        try {
            CheckResponse response = userService.changePassword(email, request.getOldPassword(), request.getNewPassword());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UserException e) {
            return new ResponseEntity<>(new CheckResponse(false, e.getMessage()), e.getExceptionType().getHttpStatus());
        }
    }

    // 회원탈퇴
    @DeleteMapping("/delete-user")
    public ResponseEntity<CheckResponse> deleteMember(@AuthenticationPrincipal UserDetails userDetails,
                                                      @RequestBody DeleteRequest request) {
        String email = userDetails.getUsername();
        try {
            CheckResponse response = userService.deleteUser(email, request.getPassword());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (UserException e) {
            return new ResponseEntity<>(new CheckResponse(false, e.getMessage()), e.getExceptionType().getHttpStatus());
        }
    }

}


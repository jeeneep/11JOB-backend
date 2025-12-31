package com.the11job.backend.user.controller;

import com.the11job.backend.user.dto.ChangePasswordRequest;
import com.the11job.backend.user.dto.CheckResponse;
import com.the11job.backend.user.dto.DeleteRequest;
import com.the11job.backend.user.dto.EmailCheckRequest;
import com.the11job.backend.user.dto.EmailCheckResponse;
import com.the11job.backend.user.dto.JoinRequest;
import com.the11job.backend.user.service.EmailAuthService;
import com.the11job.backend.user.service.UserService;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import java.io.UnsupportedEncodingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<CheckResponse> emailSend(@Valid @RequestBody EmailCheckRequest request)
            throws MessagingException, UnsupportedEncodingException {
        // emailValidate에서 예외 발생 시, 전역 예외 처리기가 처리합니다.
        userService.emailValidate(request);
        String authNum = emailAuthService.sendAuthNumber(request.getEmail());
        log.info("이메일 인증번호 발송: {}", authNum);

        return ResponseEntity.ok(new CheckResponse(true, "이메일 인증번호가 발송되었습니다. 인증을 진행해주세요."));
    }


    // 테스트 완료
    @PostMapping("/emailCheck")
    public ResponseEntity<EmailCheckResponse> emailCheck(@Valid @RequestBody EmailCheckRequest request) {
        log.info("이메일 인증 요청 - 이메일: {}, 인증번호: {}", request.getEmail(), request.getAuthNum());

        boolean isValid = emailAuthService.validateAuthNumber(request.getEmail(), request.getAuthNum());

        // 인증 실패 시, UserException(ErrorCode.WRONG_EMAIL_AUTHCODE)를 던지도록 서비스 로직 변경이 필요하지만,
        // 현재 컨트롤러 코드만 수정해야 하므로 기존의 성공/실패 응답 분기 구조를 유지했습니다.
        if (isValid) {
            return new ResponseEntity<>(new EmailCheckResponse(true, "이메일 인증 성공"), HttpStatus.OK);
        } else {
            // NOTE: 이 부분을 UserException을 던지도록 서비스/이메일 인증 로직에서 변경하는 것이 가장 좋은 방법입니다.
            // 현재는 기존 코드의 응답 형태를 따릅니다.
            return new ResponseEntity<>(new EmailCheckResponse(false, "인증번호가 잘못되었습니다."), HttpStatus.BAD_REQUEST);
        }
    }

    // 테스트 완료
    @PostMapping("/join")
    public ResponseEntity<Long> addMember(@Valid @RequestBody JoinRequest request) {
        // 유효성 검사 실패 시 MethodArgumentNotValidException 발생 -> 전역 처리
        // 비즈니스 로직(예: 중복 이메일) 예외 발생 시 UserException 발생 -> 전역 처리
        Long savedUserId = userService.join(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUserId);
    }

    // 비밀번호 변경
    @PatchMapping("/change-password")
    public ResponseEntity<CheckResponse> changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                        @Valid @RequestBody ChangePasswordRequest request) {
        String email = userDetails.getUsername();

        // 예외 처리를 전역 Advice에게 위임하기 위해 try-catch 블록 제거
        CheckResponse response = userService.changePassword(email, request.getOldPassword(), request.getNewPassword());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 회원탈퇴
    @DeleteMapping("/delete-user")
    public ResponseEntity<CheckResponse> deleteMember(@AuthenticationPrincipal UserDetails userDetails,
                                                      @Valid @RequestBody DeleteRequest request) {
        String email = userDetails.getUsername();

        // 예외 처리를 전역 Advice에게 위임하기 위해 try-catch 블록 제거
        CheckResponse response = userService.deleteUser(email, request.getPassword());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/name")
    public ResponseEntity<String> getName(@AuthenticationPrincipal UserDetails userDetails) {
        // AuthenticationPrincipal에서 현재 로그인된 사용자의 이메일(UserDetails.getUsername())을 가져옵니다.
        String email = userDetails.getUsername();

        // 서비스 계층을 호출하여 이름 정보를 가져옵니다.
        String name = userService.getNameByEmail(email);

        // 이름 정보를 응답 본문에 담아 반환합니다.
        return new ResponseEntity<>(name, HttpStatus.OK);
    }

}
package com.the11job.backend.user.service;

import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.user.dto.CheckResponse;
import com.the11job.backend.user.dto.EmailCheckRequest;
import com.the11job.backend.user.dto.JoinRequest;
import com.the11job.backend.user.entity.User;
import com.the11job.backend.user.exception.UserException;
import com.the11job.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailAuthService emailAuthService;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder bCryptPasswordEncoder,
                       EmailAuthService emailAuthService) {
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.emailAuthService = emailAuthService;
    }

    // 이메일 체크
    public void emailValidate(EmailCheckRequest emailCheckRequest) throws UserException {
        String email = emailCheckRequest.getEmail();

        // 이메일 중복 체크 (ErrorCode 사용)
        if (userRepository.existsByEmail(email)) {
            throw new UserException(ErrorCode.ALREADY_EXIST_EMAIL);
        }

        // 이메일 형식 체크
        checkEmailValid(email);
    }

    // 회원가입
    public Long join(JoinRequest joinRequest) {
        String email = joinRequest.getEmail();
        String password = joinRequest.getPassword();
        String name = joinRequest.getName();

        // 3. [보안] 이메일 인증을 완료했는지 확인 (ErrorCode 사용)
        if (!emailAuthService.checkAndRemoveVerificationStatus(email)) {
            throw new UserException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        // 4. [필수] DB 중복 체크 (경쟁 상태 방지) (ErrorCode 사용)
        if (userRepository.existsByEmail(email)) {
            throw new UserException(ErrorCode.ALREADY_EXIST_EMAIL);
        }

        // 5. [필수] 데이터 형식 검증
        checkEmailValid(email);
        checkPasswordValid(password);

        // 6. DB에 저장
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.setName(name);

        User savedUser = userRepository.save(user);

        System.out.println("Saved User ID: " + savedUser.getId());

        return savedUser.getId();
    }

    private void checkEmailValid(String email) {
        // 이메일 유효성 검사 정규표현식
        String EMAIL_FORMAT = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";

        if (email == null || !email.matches(EMAIL_FORMAT)) {
            // 이메일 형식 체크 (ErrorCode 사용)
            throw new UserException(ErrorCode.INVALID_EMAIL_FORMAT);
        }
    }

    private void checkPasswordValid(String password) {
        // 사용자 비밀번호는 영문, 숫자, 하나 이상의 특수문자를 포함하는 8 ~ 16자
        String PASSWORD_FORMAT = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[~!@#$%^&*()+|=])[A-Za-z\\d~!@#$%^&*()+|=]{8,16}$";

        if (password == null || !password.matches(PASSWORD_FORMAT)) {
            // 비밀번호 형식 체크 (ErrorCode 사용)
            throw new UserException(ErrorCode.INVALID_PASSWORD_FORMAT);
        }
    }

    // 비밀번호 변경 - 테스트 완료
    public CheckResponse changePassword(String email, String oldPassword, String newPassword) throws UserException {

        // 이메일로 회원 찾기 (ErrorCode 사용)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_EXIST));

        // 기존 비밀번호 확인 (ErrorCode 사용)
        if (!bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
            throw new UserException(ErrorCode.USER_WRONG_PASSWORD); // 기존 비밀번호가 틀린 경우
        }

        // 변경 비밀번호 형식 체크
        checkPasswordValid(newPassword);

        // 새로운 비밀번호로 변경 (암호화하여 저장)
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));

        // 변경된 비밀번호를 DB에 저장
        userRepository.save(user);

        // 성공 메시지 반환
        return new CheckResponse(true, "비밀번호가 성공적으로 변경되었습니다.");
    }

    // 회원 삭제 - 테스트 완료
    @Transactional
    public CheckResponse deleteUser(String email, String password) throws UserException {

        // 이메일로 회원 찾기 (ErrorCode 사용)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_EXIST));

        // 비밀번호 확인 (ErrorCode 사용)
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new UserException(ErrorCode.USER_WRONG_PASSWORD); // 비밀번호가 맞지 않으면 예외
        }

        // 회원 삭제
        userRepository.delete(user);

        // 성공적으로 삭제되었음을 알리는 메시지 반환
        return new CheckResponse(true, "회원이 성공적으로 삭제되었습니다."); // 메시지 수정: 관리자 -> 회원
    }


}
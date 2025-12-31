package com.the11job.backend.user.service;

import com.the11job.backend.global.exception.ErrorCode;
import com.the11job.backend.portfolio.service.PortfolioService;
import com.the11job.backend.project.service.ProjectService;
import com.the11job.backend.schedule.service.ScheduleService;
import com.the11job.backend.user.dto.CheckResponse;
import com.the11job.backend.user.dto.EmailCheckRequest;
import com.the11job.backend.user.dto.JoinRequest;
import com.the11job.backend.user.entity.User;
import com.the11job.backend.user.exception.UserException;
import com.the11job.backend.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final EmailAuthService emailAuthService;
    private final ScheduleService scheduleService;
    private final PortfolioService portfolioService;
    private final ProjectService projectService;

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

//    // 회원 삭제 - 테스트 완료
//    @Transactional
//    public CheckResponse deleteUser(String email, String password) throws UserException {
//
//        // 이메일로 회원 찾기 (ErrorCode 사용)
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_EXIST));
//
//        // 비밀번호 확인 (ErrorCode 사용)
//        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
//            throw new UserException(ErrorCode.USER_WRONG_PASSWORD); // 비밀번호가 맞지 않으면 예외
//        }
//
//        // 회원 삭제
//        userRepository.delete(user);
//
//        // 성공적으로 삭제되었음을 알리는 메시지 반환
//        return new CheckResponse(true, "회원이 성공적으로 삭제되었습니다."); // 메시지 수정: 관리자 -> 회원
//    }


    @Transactional
    public CheckResponse deleteUser(String email, String password) throws UserException {

        // 1. 이메일로 회원 찾기
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_EXIST));

        // 2. 비밀번호 확인
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            throw new UserException(ErrorCode.USER_WRONG_PASSWORD);
        }

        // 회원 삭제 전에 연관된 모든 데이터를 수동으로 삭제합니다.

        // A. 일정 및 첨부 파일 삭제 (Schedule -> File 연쇄 삭제 포함)
        scheduleService.deleteAllByUser(user);

        // B. 포트폴리오 및 프로필 이미지 삭제 (Portfolio -> PortfolioItem 연쇄 삭제 포함)
        portfolioService.deleteByUser(user);

        // C. 프로젝트 및 관련 이미지 삭제
        projectService.deleteAllByUser(user);

        // 4. 연관된 데이터가 모두 정리된 후, 회원 삭제
        userRepository.delete(user);

        // 5. 성공 응답
        return new CheckResponse(true, "회원이 성공적으로 삭제되었습니다.");
    }

    // 이름 조회 (프론트엔드에서 이름만 받아오기 위해)
    public String getNameByEmail(String email) throws UserException {
        // 이메일을 통해 User 엔티티를 찾습니다.
        User user = userRepository.findByEmail(email)
                // 사용자가 존재하지 않으면 예외 처리 (인증된 사용자이므로 발생할 확률은 낮지만, 방어적 코드 작성)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_EXIST));

        // 찾은 사용자의 이름을 반환합니다.
        return user.getName();
    }

}
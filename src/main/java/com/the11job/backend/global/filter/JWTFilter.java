package com.the11job.backend.global.filter;

import com.the11job.backend.global.util.JWTUtil;
import com.the11job.backend.user.entity.User;
import com.the11job.backend.user.exception.UserException;
import com.the11job.backend.user.exception.UserExceptionType;
import com.the11job.backend.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JWTFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final UserRepository userRepository;

    public JWTFilter(JWTUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // "access" 헤더 대신 표준인 "Authorization" 헤더를 사용하는 것을 권장
        String accessToken = request.getHeader("access");

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("access token expired");
            return;
        }

        String email = jwtUtil.getEmail(accessToken);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(UserExceptionType.USER_NOT_EXIST)); // 4. DB에서 User 객체를 찾음

        // Principal 자리에 'User' 객체 자체를 넣습니다.
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities() // 👈 userDetails 대신 user 객체 사용
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
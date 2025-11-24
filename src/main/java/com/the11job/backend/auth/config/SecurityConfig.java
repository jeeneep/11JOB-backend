package com.the11job.backend.auth.config;

import com.the11job.backend.global.filter.JWTFilter;
import com.the11job.backend.global.filter.LoginFilter;
import com.the11job.backend.global.filter.LogoutFilter;
import com.the11job.backend.global.util.JWTUtil;
import com.the11job.backend.user.repository.RefreshRepository;
import com.the11job.backend.user.repository.UserRepository;
import java.util.List;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;
    private final RefreshRepository refreshRepository;
    private final UserRepository userRepository;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                          JWTUtil jwtUtil,
                          RefreshRepository refreshRepository,
                          UserRepository userRepository) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.userRepository = userRepository;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ 1. CORS 설정을 별도 Bean으로 분리하여 가독성 향상
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 실제 운영 환경에서는 허용할 출처를 명확히 지정해야 합니다.
        // configuration.setAllowedOrigins(List.of("http://localhost:3000", "https://your-frontend.com"));
        configuration.setAllowedOriginPatterns(List.of("*")); // 개발 편의성을 위해 모든 출처 허용
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization")); // access 헤더 대신 Authorization 사용 권장

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CORS 설정을 Bean을 통해 적용
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // CSRF, Form 로그인, HTTP Basic 인증 비활성화 (JWT 사용에 맞게)
        http.csrf((csrf) -> csrf.disable());
        http.formLogin((formLogin) -> formLogin.disable());
        http.httpBasic((httpBasic) -> httpBasic.disable());

        // h2-console을 위한 frameOptions 비활성화
        http.headers((headers) -> headers
                .frameOptions((frameOptions) -> frameOptions.disable())
        );

        // 경로별 인가(Authorization) 설정
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/", "/login", "/api/user/join", "/api/user/emailSend", "/api/user/emailCheck",
                        "/api/reissue").permitAll()
                //.requestMatchers("/api/jobs/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                .anyRequest().authenticated()
        );

        // ✅ 2. 필터 등록 순서를 명확하게 수정

        // 1. JWT 검증 필터: 가장 기본적인 인증 필터보다 먼저 실행되어야 모든 요청을 가로채 토큰을 검증할 수 있다.
        http
                .addFilterBefore(new JWTFilter(jwtUtil, userRepository), UsernamePasswordAuthenticationFilter.class);

        // 2. 로그인 필터: 기존의 UsernamePasswordAuthenticationFilter 위치를 우리가 만든 LoginFilter로 대체한다.
        http
                .addFilterAt(
                        new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository),
                        UsernamePasswordAuthenticationFilter.class);

        // 3. 로그아웃 필터: 로그인 필터보다는 뒤, JWT 필터와 비슷한 위치에서 작동하도록 설정한다.
        http
                .addFilterBefore(new LogoutFilter(jwtUtil, refreshRepository), LoginFilter.class);
        return http.build();
    }
}
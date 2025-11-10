package com.the11job.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호를 H2 Console 경로에 대해 비활성화
                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))

                // X-Frame-Options 비활성화 (H2 Console UI가 프레임 내부에서 작동하도록 허용)
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::disable))

                // /h2-console 경로에 대한 접근 권한을 인증 없이 모두 허용
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/h2-console/**").permitAll()

                        // H2 Console 이외의 모든 요청은 인증 필요 (기본 보안 설정)
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
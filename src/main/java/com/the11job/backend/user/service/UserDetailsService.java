package com.the11job.backend.user.service;

import com.the11job.backend.user.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// UserDetailsService.java (클래스명 오타 수정)
@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. orElseThrow에서 Spring Security가 기대하는 UsernameNotFoundException을 던집니다.
        // 2. User가 UserDetails를 구현했으므로, 찾은 user 객체를 바로 반환할 수 있습니다.
        // 3. 불필요한 if문은 완전히 제거합니다.
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}

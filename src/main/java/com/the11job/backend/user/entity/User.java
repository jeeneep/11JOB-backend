package com.the11job.backend.user.entity;

import com.the11job.backend.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.Collection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")

public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    // ==========================================================
    // UserDetails 인터페이스의 메서드들을 구현한 코드

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 권한을 반환하는 곳. 지금은 간단히 null을 반환합니다.
        // 예: return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return null;
    }

    @Override
    public String getPassword() {
        // User 엔티티의 password 필드를 반환합니다.
        return this.password;
    }

    @Override
    public String getUsername() {
        // Spring Security에서 username은 ID를 의미합니다.
        // 여기서는 email을 ID로 사용하므로 email 필드를 반환합니다.
        return this.email;
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정이 만료되었는지 (true: 만료되지 않음)
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정이 잠겼는지 (true: 잠기지 않음)
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 비밀번호가 만료되었는지 (true: 만료되지 않음)
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정이 활성화되었는지 (true: 활성화됨)
        return true;
    }
}
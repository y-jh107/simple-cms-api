package com.malgn.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserEntityTest {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    @DisplayName("User 빌더를 통해 객체를 생성하고 권한이 정상적으로 부여되는지 확인한다")
    void userBuilderAndRoleTest() {
        // given
        String username = "testUser";
        String rawPassword = "password123!";
        Set<Role> roles = Set.of(Role.ROLE_USER);

        // when
        User user = User.builder()
                .username(username)
                .password(rawPassword)
                .roles(roles)
                .build();

        // then
        assertThat(user.getUsername()).isEqualTo(username);
        // 빌더 내부에서 encode가 호출되므로 평문과 일치하지 않아야 함
        assertThat(user.getPassword()).isNotEqualTo(rawPassword);
        assertThat(passwordEncoder.matches(rawPassword, user.getPassword())).isTrue();
        assertThat(user.getRoles()).contains(Role.ROLE_USER);
        assertThat(user.getRoles()).hasSize(1);
    }

    @Test
    @DisplayName("관리자 권한이 포함된 유저 생성을 검증한다")
    void adminRoleAssignmentTest() {
        // given
        Set<Role> adminRoles = Set.of(Role.ROLE_USER, Role.ROLE_ADMIN);

        // when
        User admin = User.builder()
                .username("admin")
                .password("admin123")
                .roles(adminRoles)
                .build();

        // then
        assertThat(admin.getRoles()).containsExactlyInAnyOrder(Role.ROLE_USER, Role.ROLE_ADMIN);
    }
}
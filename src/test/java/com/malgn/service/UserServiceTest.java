package com.malgn.service;

import com.malgn.dto.UserRequest;
import com.malgn.dto.UserResponse;
import com.malgn.entity.Role;
import com.malgn.entity.User;
import com.malgn.exception.NotAuthorizedException;
import com.malgn.exception.UserNotFoundException;
import com.malgn.repository.UserRepository;
import com.malgn.security.CmsGrantedAuthority;
import com.malgn.security.CmsUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("일반 사용자는 다른 사용자를 수정할 수 없다")
    void updateOtherUserInfoForbidden() {
        // given
        Long authUserId = 3L;
        Long targetUserId = 5L;

        CmsUserDetails user = new CmsUserDetails(
                authUserId,
                "user",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        UserRequest request = new UserRequest("someuser", "newPassword");

        // when & then
        assertThatThrownBy(() -> userService.updateUser(user, targetUserId, request))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessage("권한이 없습니다.");
    }

    @Test
    @DisplayName("관리자는 다른 사용자를 수정할 수 있다")
    void updateOtherUserInfoByAdmin() {
        // given
        Long adminId = 1L;
        Long targetUserId = 5L;

        CmsUserDetails admin = new CmsUserDetails(
                adminId,
                "admin",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_ADMIN))
        );

        User targetUser = User.builder()
                .id(targetUserId)
                .username("oldUsername")
                .password("encodedPassword")
                .roles(new HashSet<>(List.of(Role.ROLE_USER)))
                .build();

        UserRequest request = new UserRequest("newUsername", "newPassword");

        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));
        given(userRepository.save(any(User.class))).willReturn(targetUser);

        // when
        UserResponse response = userService.updateUser(admin, targetUserId, request);

        // then
        assertThat(response).isNotNull();
    }

    @Test
    @DisplayName("사용자가 본인의 정보를 수정할 수 있다")
    void updateOwnUserInfo() {
        // given
        Long userId = 3L;

        CmsUserDetails user = new CmsUserDetails(
                userId,
                "user",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        User targetUser = User.builder()
                .id(userId)
                .username("oldUsername")
                .password("encodedPassword")
                .roles(new HashSet<>(List.of(Role.ROLE_USER)))
                .build();

        UserRequest request = new UserRequest("newUsername", "newPassword");

        given(userRepository.findById(userId)).willReturn(Optional.of(targetUser));
        given(userRepository.save(any(User.class))).willReturn(targetUser);

        // when
        UserResponse response = userService.updateUser(user, userId, request);

        // then
        assertThat(response).isNotNull();
    }
}

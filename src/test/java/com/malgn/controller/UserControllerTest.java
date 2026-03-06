package com.malgn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malgn.configure.security.SecurityConfiguration;
import com.malgn.dto.UserRequest;
import com.malgn.dto.UserResponse;
import com.malgn.entity.Role;
import com.malgn.security.CmsGrantedAuthority;
import com.malgn.security.CmsUserDetails;
import com.malgn.security.JwtAuthenticationEntryPoint;
import com.malgn.security.JwtRequestFilter;
import com.malgn.security.JwtUtil;
import com.malgn.service.CmsUserDetailsService;
import com.malgn.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({SecurityConfiguration.class, UserControllerTest.TestSecurityConfiguration.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockitoBean
    private CmsUserDetailsService cmsUserDetailsService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @TestConfiguration
    static class TestSecurityConfiguration {
        @Bean
        @Primary
        public JwtRequestFilter jwtRequestFilter() {
            return new JwtRequestFilter() {
                @Override
                public void doFilterInternal(
                        HttpServletRequest request,
                        HttpServletResponse response,
                        FilterChain filterChain) throws ServletException, IOException {
                    // 테스트용 필터: Authorization 헤더 검증 생략
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    @Test
    @DisplayName("관리자 권한으로 사용자 정보를 수정할 수 있다")
    void updateUserInfoByAdmin() throws Exception {
        // given
        Long targetUserId = 2L;
        UserRequest request = new UserRequest("updatedUser", "newPassword123");
        UserResponse response = UserResponse.builder()
                .id(targetUserId)
                .username("updatedUser")
                .password("newPassword123")
                .build();

        given(userService.updateUser(any(), eq(targetUserId), any(UserRequest.class)))
                .willReturn(response);

        // 관리자 권한을 가진 인증된 사용자 생성
        CmsUserDetails admin = new CmsUserDetails(
                1L,
                "admin",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_ADMIN))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        // when & then
        mockMvc.perform(
                        put("/api/v1/user/{userId}", targetUserId)
                                .with(authentication(auth))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updatedUser"))
                .andExpect(jsonPath("$.id").value(targetUserId.intValue()));
    }

    @Test
    @DisplayName("사용자가 본인의 정보를 수정할 수 있다")
    void updateOwnUserInfo() throws Exception {
        // given
        Long userId = 3L;
        UserRequest request = new UserRequest("myUpdatedUsername", "newPassword456");
        UserResponse response = UserResponse.builder()
                .id(userId)
                .username("myUpdatedUsername")
                .password("newPassword456")
                .build();

        given(userService.updateUser(any(), eq(userId), any(UserRequest.class)))
                .willReturn(response);

        // 일반 사용자(ROLE_USER만 가진) 인증 객체 생성
        CmsUserDetails user = new CmsUserDetails(
                userId,
                "user",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when & then
        mockMvc.perform(
                        put("/api/v1/user/{userId}", userId)
                                .with(authentication(auth))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("myUpdatedUsername"))
                .andExpect(jsonPath("$.id").value(userId.intValue()));
    }

    @Test
    @DisplayName("사용자 목록을 조회할 수 있다")
    void getUsers() throws Exception {
        // given
        UserResponse user1 = UserResponse.builder()
                .id(1L)
                .username("user1")
                .password("password1")
                .build();
        UserResponse user2 = UserResponse.builder()
                .id(2L)
                .username("user2")
                .password("password2")
                .build();

        given(userService.getUsers()).willReturn(java.util.List.of(user1, user2));

        // when & then
        mockMvc.perform(get("/api/v1/user"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 사용자를 조회할 수 있다")
    void getUser() throws Exception {
        // given
        Long userId = 1L;
        UserResponse response = UserResponse.builder()
                .id(userId)
                .username("testuser")
                .password("password")
                .build();

        given(userService.getUserById(userId)).willReturn(java.util.Optional.of(response));

        // when & then
        mockMvc.perform(get("/api/v1/user/{userId}", userId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userId.intValue()))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자를 조회하면 404를 반환한다")
    void getUserNotFound() throws Exception {
        // given
        Long userId = 999L;
        given(userService.getUserById(userId)).willReturn(java.util.Optional.empty());

        // when & then
        mockMvc.perform(get("/api/v1/user/{userId}", userId))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("새로운 사용자를 생성할 수 있다")
    void createUser() throws Exception {
        // given
        UserRequest request = new UserRequest("newuser", "password123");
        UserResponse response = UserResponse.builder()
                .id(10L)
                .username("newuser")
                .password("password123")
                .build();

        given(userService.createUser(any(UserRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("관리자만 사용자를 삭제할 수 있다")
    void deleteUserByAdmin() throws Exception {
        // given
        Long targetUserId = 5L;

        // 관리자 인증 객체 생성
        CmsUserDetails admin = new CmsUserDetails(
                1L,
                "admin",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_ADMIN))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        // when & then
        mockMvc.perform(delete("/api/v1/user/{userId}", targetUserId)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
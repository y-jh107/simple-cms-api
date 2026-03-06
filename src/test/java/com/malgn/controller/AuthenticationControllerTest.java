package com.malgn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malgn.configure.security.SecurityConfiguration;
import com.malgn.dto.AuthenticationRequest;
import com.malgn.dto.AuthenticationResponse;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfiguration.class, AuthenticationControllerTest.TestSecurityConfiguration.class})
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CmsUserDetailsService cmsUserDetailsService;

    @MockitoBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

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
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    @Test
    @DisplayName("올바른 자격증명으로 로그인하면 JWT 토큰을 받는다")
    void loginSuccess() throws Exception {
        // given
        String username = "testuser";
        String password = "password123";
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        CmsUserDetails userDetails = new CmsUserDetails(
                1L,
                username,
                "encodedPassword",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .username(username)
                .password("encodedPassword")
                .build();

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));
        given(cmsUserDetailsService.loadUserByUsername(username)).willReturn(userDetails);
        given(userService.getUserByUsername(username)).willReturn(userResponse);
        given(jwtUtil.generateToken(any(), any())).willReturn("valid.jwt.token");

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").value("valid.jwt.token"));
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 401을 반환한다")
    void loginFailedInvalidPassword() throws Exception {
        // given
        String username = "testuser";
        String password = "wrongPassword";
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 로그인하면 401을 반환한다")
    void loginFailedUserNotFound() throws Exception {
        // given
        String username = "nonexistent";
        String password = "password123";
        AuthenticationRequest request = new AuthenticationRequest(username, password);

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}

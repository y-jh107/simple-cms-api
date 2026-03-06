package com.malgn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malgn.configure.security.SecurityConfiguration;
import com.malgn.dto.UserRequest;
import com.malgn.dto.ContentCreateRequest;
import com.malgn.entity.Role;
import com.malgn.security.CmsGrantedAuthority;
import com.malgn.security.CmsUserDetails;
import com.malgn.security.JwtAuthenticationEntryPoint;
import com.malgn.security.JwtRequestFilter;
import com.malgn.security.JwtUtil;
import com.malgn.service.CmsUserDetailsService;
import com.malgn.service.ContentService;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({UserController.class, ContentController.class})
@Import({SecurityConfiguration.class, ValidationTest.TestSecurityConfiguration.class})
class ValidationTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ContentService contentService;

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
                    filterChain.doFilter(request, response);
                }
            };
        }
    }

    @Test
    @DisplayName("username이 없으면 사용자 생성 실패")
    void createUserWithoutUsername() throws Exception {
        // given - username 필드 생략
        String jsonBody = "{\"password\":\"password123\"}";

        // when & then
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("password가 없으면 사용자 생성 실패")
    void createUserWithoutPassword() throws Exception {
        // given - password 필드 생략
        String jsonBody = "{\"username\":\"testuser\"}";

        // when & then
        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("title이 없으면 컨텐츠 생성 실패")
    void createContentWithoutTitle() throws Exception {
        // given - title 필드 생략
        String jsonBody = "{\"description\":\"description\", \"createdBy\":\"testuser\"}";

        CmsUserDetails user = new CmsUserDetails(
                1L,
                "testuser",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when & then
        mockMvc.perform(post("/api/v1/content")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("createdBy가 없으면 컨텐츠 생성 실패")
    void createContentWithoutCreatedBy() throws Exception {
        // given - createdBy 필드 생략
        String jsonBody = "{\"title\":\"Test Title\", \"description\":\"description\"}";

        CmsUserDetails user = new CmsUserDetails(
                1L,
                "testuser",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when & then
        mockMvc.perform(post("/api/v1/content")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }
}

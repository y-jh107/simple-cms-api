package com.malgn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malgn.configure.security.SecurityConfiguration;
import com.malgn.dto.ContentResponse;
import com.malgn.dto.ContentUpdateRequest;
import com.malgn.entity.Role;
import com.malgn.security.CmsGrantedAuthority;
import com.malgn.security.CmsUserDetails;
import com.malgn.security.JwtAuthenticationEntryPoint;
import com.malgn.security.JwtRequestFilter;
import com.malgn.security.JwtUtil;
import com.malgn.service.CmsUserDetailsService;
import com.malgn.service.ContentService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@Import({SecurityConfiguration.class, PartialUpdateTestController.TestSecurityConfiguration.class})
class PartialUpdateTestController {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

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
    @DisplayName("제목만 수정할 수 있다")
    void updateOnlyTitle() throws Exception {
        // given
        Long contentId = 1L;
        String username = "testuser";

        ContentUpdateRequest request = new ContentUpdateRequest(
                "New Title Only",
                null,  // description은 null
                username
        );

        ContentResponse response = ContentResponse.builder()
                .contentId(contentId)
                .title("New Title Only")
                .description("Original Description")  // 기존 값 유지
                .createdBy(username)
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 0, 0))
                .build();

        given(contentService.updateContent(any(), eq(contentId), any(ContentUpdateRequest.class)))
                .willReturn(response);

        CmsUserDetails user = new CmsUserDetails(
                1L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when & then
        mockMvc.perform(put("/api/v1/content/{contentId}", contentId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title Only"))
                .andExpect(jsonPath("$.description").value("Original Description"));
    }

    @Test
    @DisplayName("설명만 수정할 수 있다")
    void updateOnlyDescription() throws Exception {
        // given
        Long contentId = 1L;
        String username = "testuser";

        ContentUpdateRequest request = new ContentUpdateRequest(
                null,  // title은 null
                "New Description Only",
                username
        );

        ContentResponse response = ContentResponse.builder()
                .contentId(contentId)
                .title("Original Title")  // 기존 값 유지
                .description("New Description Only")
                .createdBy(username)
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 0, 0))
                .build();

        given(contentService.updateContent(any(), eq(contentId), any(ContentUpdateRequest.class)))
                .willReturn(response);

        CmsUserDetails user = new CmsUserDetails(
                1L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when & then
        mockMvc.perform(put("/api/v1/content/{contentId}", contentId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Original Title"))
                .andExpect(jsonPath("$.description").value("New Description Only"));
    }

    @Test
    @DisplayName("모든 필드를 수정할 수 있다")
    void updateAllFields() throws Exception {
        // given
        Long contentId = 1L;
        String username = "testuser";

        ContentUpdateRequest request = new ContentUpdateRequest(
                "New Title",
                "New Description",
                username
        );

        ContentResponse response = ContentResponse.builder()
                .contentId(contentId)
                .title("New Title")
                .description("New Description")
                .createdBy(username)
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 0, 0))
                .build();

        given(contentService.updateContent(any(), eq(contentId), any(ContentUpdateRequest.class)))
                .willReturn(response);

        CmsUserDetails user = new CmsUserDetails(
                1L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when & then
        mockMvc.perform(put("/api/v1/content/{contentId}", contentId)
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.description").value("New Description"));
    }
}

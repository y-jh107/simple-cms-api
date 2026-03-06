package com.malgn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.malgn.configure.security.SecurityConfiguration;
import com.malgn.dto.ContentCreateRequest;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ContentController.class)
@Import({SecurityConfiguration.class, ContentControllerTest.TestSecurityConfiguration.class})
class ContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    public ContentControllerTest() {
        this.objectMapper.registerModule(new JavaTimeModule());
    }

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
    @DisplayName("인증된 사용자가 컨텐츠를 생성할 수 있다")
    void createContent() throws Exception {
        // given
        String username = "testuser";
        ContentCreateRequest request = new ContentCreateRequest(
                "Test Title",
                "Test Description",
                username
        );
        ContentResponse response = ContentResponse.builder()
                .contentId(1L)
                .title("Test Title")
                .description("Test Description")
                .createdBy(username)
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 0, 0))
                .build();

        given(contentService.createContent(any(ContentCreateRequest.class), eq(username)))
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
        mockMvc.perform(post("/api/v1/content")
                        .with(authentication(auth))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contentId").value(1L))
                .andExpect(jsonPath("$.title").value("Test Title"));
    }

    @Test
    @DisplayName("전체 컨텐츠를 페이징으로 조회할 수 있다")
    void getAllContents() throws Exception {
        // given
        int page = 0;
        int size = 10;

        ContentResponse content1 = ContentResponse.builder()
                .contentId(1L)
                .title("Content 1")
                .description("Description 1")
                .createdBy("user1")
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 0, 0))
                .build();

        ContentResponse content2 = ContentResponse.builder()
                .contentId(2L)
                .title("Content 2")
                .description("Description 2")
                .createdBy("user2")
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 1, 0))
                .build();

        Page<ContentResponse> pageResponse = new PageImpl<>(
                List.of(content1, content2),
                org.springframework.data.domain.PageRequest.of(page, size),
                2
        );

        given(contentService.getAllContents(page, size)).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/content")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("특정 사용자가 작성한 컨텐츠를 조회할 수 있다")
    void getContentsByCreatedBy() throws Exception {
        // given
        String username = "testuser";
        int page = 0;
        int size = 10;

        ContentResponse content = ContentResponse.builder()
                .contentId(1L)
                .title("Test Title")
                .description("Test Description")
                .createdBy(username)
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 0, 0))
                .build();

        Page<ContentResponse> pageResponse = new PageImpl<>(
                List.of(content),
                org.springframework.data.domain.PageRequest.of(page, size),
                1
        );

        given(contentService.getContentByCreatedBy(username, page, size)).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/content/user/{createdBy}", username)
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("컨텐츠 작성자가 자신의 컨텐츠를 수정할 수 있다")
    void updateContent() throws Exception {
        // given
        Long contentId = 1L;
        String username = "testuser";

        ContentUpdateRequest request = new ContentUpdateRequest(
                "Updated Title",
                "Updated Description",
                username
        );

        ContentResponse response = ContentResponse.builder()
                .contentId(contentId)
                .title("Updated Title")
                .description("Updated Description")
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
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    @DisplayName("컨텐츠 작성자가 자신의 컨텐츠를 삭제할 수 있다")
    void deleteContent() throws Exception {
        // given
        Long contentId = 1L;
        String username = "testuser";

        doNothing().when(contentService).deleteContent(any(), eq(contentId));

        CmsUserDetails user = new CmsUserDetails(
                1L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        // when & then
        mockMvc.perform(delete("/api/v1/content/{contentId}", contentId)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 컨텐츠를 삭제할 수 있다")
    void deleteContentByAdmin() throws Exception {
        // given
        Long contentId = 1L;

        doNothing().when(contentService).deleteContent(any(), eq(contentId));

        CmsUserDetails admin = new CmsUserDetails(
                1L,
                "admin",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_ADMIN))
        );
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities());

        // when & then
        mockMvc.perform(delete("/api/v1/content/{contentId}", contentId)
                        .with(authentication(auth)))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}

package com.malgn.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.malgn.configure.security.SecurityConfiguration;
import com.malgn.dto.ContentResponse;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentController.class)
@Import({SecurityConfiguration.class, PagingTestController.TestSecurityConfiguration.class})
class PagingTestController {

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
    @DisplayName("페이징 파라미터를 올바르게 전달하면 해당 페이지 컨텐츠를 반환한다")
    void getContentsWithPaging() throws Exception {
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
                PageRequest.of(page, size),
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
    @DisplayName("첫 번째 페이지를 요청할 수 있다")
    void getContentsFirstPage() throws Exception {
        // given
        int page = 0;
        int size = 10;

        ContentResponse content = ContentResponse.builder()
                .contentId(1L)
                .title("Content 1")
                .description("Description")
                .createdBy("user1")
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 0, 0))
                .build();

        Page<ContentResponse> pageResponse = new PageImpl<>(
                List.of(content),
                PageRequest.of(page, size),
                1
        );

        given(contentService.getAllContents(page, size)).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/content")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("사용자별 컨텐츠를 페이징으로 조회할 수 있다")
    void getContentsByUserWithPaging() throws Exception {
        // given
        String username = "testuser";
        int page = 0;
        int size = 5;

        ContentResponse content = ContentResponse.builder()
                .contentId(1L)
                .title("My Content")
                .description("My Description")
                .createdBy(username)
                .createdAt(LocalDateTime.of(2026, 3, 6, 10, 0, 0))
                .build();

        Page<ContentResponse> pageResponse = new PageImpl<>(
                List.of(content),
                PageRequest.of(page, size),
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
    @DisplayName("다양한 페이지 크기로 요청할 수 있다")
    void getContentsWithDifferentPageSizes() throws Exception {
        // given - 페이지 크기 20으로 요청
        int page = 0;
        int size = 20;

        Page<ContentResponse> pageResponse = new PageImpl<>(
                List.of(),
                PageRequest.of(page, size),
                0
        );

        given(contentService.getAllContents(page, size)).willReturn(pageResponse);

        // when & then
        mockMvc.perform(get("/api/v1/content")
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size)))
                .andDo(print())
                .andExpect(status().isOk());
    }
}

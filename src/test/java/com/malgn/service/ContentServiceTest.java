package com.malgn.service;

import com.malgn.dto.ContentUpdateRequest;
import com.malgn.entity.Content;
import com.malgn.entity.Role;
import com.malgn.exception.NotAuthorizedException;
import com.malgn.repository.ContentRepository;
import com.malgn.repository.UserRepository;
import com.malgn.security.CmsGrantedAuthority;
import com.malgn.security.CmsUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ContentService contentService;

    @Test
    @DisplayName("컨텐츠 작성자만 자신의 컨텐츠를 수정할 수 있다")
    void updateContentByAuthor() {
        // given
        Long contentId = 1L;
        String username = "testuser";

        CmsUserDetails user = new CmsUserDetails(
                1L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        Content content = Content.builder()
                .id(contentId)
                .title("Old Title")
                .description("Old Description")
                .createdBy(username)
                .build();

        ContentUpdateRequest request = new ContentUpdateRequest(
                "New Title",
                "New Description",
                username
        );

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
        given(contentRepository.save(any(Content.class))).willReturn(content);

        // when & then
        assertThat(contentService.updateContent(user, contentId, request)).isNotNull();
    }

    @Test
    @DisplayName("다른 사용자는 컨텐츠를 수정할 수 없다")
    void updateContentByOtherUser() {
        // given
        Long contentId = 1L;
        String createdBy = "author";
        String username = "otheruser";

        CmsUserDetails user = new CmsUserDetails(
                2L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        Content content = Content.builder()
                .id(contentId)
                .title("Title")
                .description("Description")
                .createdBy(createdBy)
                .build();

        ContentUpdateRequest request = new ContentUpdateRequest(
                "New Title",
                "New Description",
                username
        );

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when & then
        assertThatThrownBy(() -> contentService.updateContent(user, contentId, request))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessage("게시글 작성자만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 컨텐츠를 수정할 수 있다")
    void updateContentByAdmin() {
        // given
        Long contentId = 1L;
        String createdBy = "otheruser";

        CmsUserDetails admin = new CmsUserDetails(
                1L,
                "admin",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_ADMIN))
        );

        Content content = Content.builder()
                .id(contentId)
                .title("Title")
                .description("Description")
                .createdBy(createdBy)
                .build();

        ContentUpdateRequest request = new ContentUpdateRequest(
                "New Title",
                "New Description",
                "admin"
        );

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
        given(contentRepository.save(any(Content.class))).willReturn(content);

        // when & then
        assertThat(contentService.updateContent(admin, contentId, request)).isNotNull();
    }

    @Test
    @DisplayName("컨텐츠 작성자만 자신의 컨텐츠를 삭제할 수 있다")
    void deleteContentByAuthor() {
        // given
        Long contentId = 1L;
        String username = "testuser";

        CmsUserDetails user = new CmsUserDetails(
                1L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        Content content = Content.builder()
                .id(contentId)
                .title("Title")
                .description("Description")
                .createdBy(username)
                .build();

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when & then
        contentService.deleteContent(user, contentId);
    }

    @Test
    @DisplayName("다른 사용자는 컨텐츠를 삭제할 수 없다")
    void deleteContentByOtherUser() {
        // given
        Long contentId = 1L;
        String createdBy = "author";
        String username = "otheruser";

        CmsUserDetails user = new CmsUserDetails(
                2L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        Content content = Content.builder()
                .id(contentId)
                .title("Title")
                .description("Description")
                .createdBy(createdBy)
                .build();

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when & then
        assertThatThrownBy(() -> contentService.deleteContent(user, contentId))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessage("게시글 작성자만 삭제할 수 있습니다.");
    }

    @Test
    @DisplayName("관리자는 다른 사용자의 컨텐츠를 삭제할 수 있다")
    void deleteContentByAdmin() {
        // given
        Long contentId = 1L;
        String createdBy = "otheruser";

        CmsUserDetails admin = new CmsUserDetails(
                1L,
                "admin",
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_ADMIN))
        );

        Content content = Content.builder()
                .id(contentId)
                .title("Title")
                .description("Description")
                .createdBy(createdBy)
                .build();

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when & then
        contentService.deleteContent(admin, contentId);
    }
}

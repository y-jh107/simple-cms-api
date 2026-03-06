package com.malgn.exception;

import com.malgn.entity.Role;
import com.malgn.repository.ContentRepository;
import com.malgn.security.CmsGrantedAuthority;
import com.malgn.security.CmsUserDetails;
import com.malgn.service.ContentService;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExceptionHandlerTest {

    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private ContentService contentService;

    @Test
    @DisplayName("존재하지 않는 컨텐츠를 조회하면 ContentNotFoundException이 발생한다")
    void getContentNotFoundThrowsException() {
        // given
        Long contentId = 999L;
        given(contentRepository.findById(contentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> contentService.getContentById(contentId))
                .isInstanceOf(ContentNotFoundException.class)
                .hasMessageContaining("컨텐츠를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("컨텐츠 작성자가 아니면 NotAuthorizedException이 발생한다")
    void updateContentUnauthorizedThrowsException() {
        // given
        Long contentId = 1L;
        String createdBy = "originalAuthor";
        String username = "otherUser";

        com.malgn.entity.Content content = com.malgn.entity.Content.builder()
                .id(contentId)
                .title("Title")
                .description("Description")
                .createdBy(createdBy)
                .build();

        CmsUserDetails user = new CmsUserDetails(
                2L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when & then
        assertThatThrownBy(() -> contentService.updateContent(user, contentId, new com.malgn.dto.ContentUpdateRequest(null, null, null)))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("게시글 작성자만 수정할 수 있습니다");
    }

    @Test
    @DisplayName("컨텐츠 삭제 시 권한이 없으면 NotAuthorizedException이 발생한다")
    void deleteContentUnauthorizedThrowsException() {
        // given
        Long contentId = 1L;
        String createdBy = "originalAuthor";
        String username = "otherUser";

        com.malgn.entity.Content content = com.malgn.entity.Content.builder()
                .id(contentId)
                .title("Title")
                .description("Description")
                .createdBy(createdBy)
                .build();

        CmsUserDetails user = new CmsUserDetails(
                2L,
                username,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_USER))
        );

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));

        // when & then
        assertThatThrownBy(() -> contentService.deleteContent(user, contentId))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessageContaining("게시글 작성자만 삭제할 수 있습니다");
    }

    @Test
    @DisplayName("관리자는 NotAuthorizedException이 발생하지 않는다")
    void adminCanUpdateOtherUserContent() {
        // given
        Long contentId = 1L;
        String createdBy = "otherUser";
        String adminUsername = "admin";

        com.malgn.entity.Content content = com.malgn.entity.Content.builder()
                .id(contentId)
                .title("Title")
                .description("Description")
                .createdBy(createdBy)
                .build();

        CmsUserDetails admin = new CmsUserDetails(
                1L,
                adminUsername,
                "password",
                List.of(new CmsGrantedAuthority(Role.ROLE_ADMIN))
        );

        com.malgn.dto.ContentUpdateRequest request = new com.malgn.dto.ContentUpdateRequest(
                "New Title",
                "New Description",
                adminUsername
        );

        given(contentRepository.findById(contentId)).willReturn(Optional.of(content));
        given(contentRepository.save(any(com.malgn.entity.Content.class))).willReturn(content);

        // when & then - no exception thrown
        contentService.updateContent(admin, contentId, request);
    }
}

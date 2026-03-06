package com.malgn.service;

import com.malgn.dto.ContentCreateRequest;
import com.malgn.dto.ContentResponse;
import com.malgn.dto.ContentUpdateRequest;
import com.malgn.entity.Content;
import com.malgn.entity.User;
import com.malgn.exception.ContentNotFoundException;
import com.malgn.exception.NotAuthorizedException;
import com.malgn.exception.UserNotFoundException;
import com.malgn.repository.ContentRepository;
import com.malgn.repository.UserRepository;
import com.malgn.security.CmsUserDetails;
import com.malgn.util.EntityDtoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = Isolation.REPEATABLE_READ)
public class ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    @Autowired
    public ContentService(ContentRepository contentRepository, UserRepository userRepository) {
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    public ContentResponse createContent(ContentCreateRequest request, String username) {
        User user =  userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("해당 이름(%s)을 가진 사용자를 찾을 수 없습니다.", username)
                ));

        Content content = Content.builder()
                .title(request.title())
                .description(request.description())
                .createdBy(request.createdBy())
                .build();

        contentRepository.save(content);

        return EntityDtoMapper.toDto(content);
    }

    public void deleteContent(CmsUserDetails userDetails, Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(
                        String.format("해당 아이디(%d)를 가진 컨텐츠를 찾을 수 없습니다.",  contentId)
                ));

        if (!userDetails.isAdmin() && !content.getCreatedBy().equals(userDetails.getUsername())) {
            throw new NotAuthorizedException("게시글 작성자만 삭제할 수 있습니다.");
        }

        contentRepository.deleteById(content.getId());
    }

    public ContentResponse updateContent(CmsUserDetails userDetails, Long contentId, ContentUpdateRequest request) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(
                        String.format("해당 아이디(%d)를 가진 컨텐츠를 찾을 수 없습니다.",  contentId)
                ));

        if (!userDetails.isAdmin() && !content.getCreatedBy().equals(userDetails.getUsername())) {
            throw new NotAuthorizedException("게시글 작성자만 수정할 수 있습니다.");
        }

        // 부분 수정을 허용하기 위해 null 체크 로직을 사용했습니다.
        if (request.title() != null) {
            content.setTitle(request.title());
        }
        if (request.description() != null) {
            content.setDescription(request.description());
        }
        content.setLastModifiedBy(request.lastModifiedBy());

        Content updatedContent = contentRepository.save(content);

        return EntityDtoMapper.toDto(updatedContent);
    }

    @Transactional(readOnly = true)
    public ContentResponse getContentById(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(
                        String.format("해당 아이디(%d)를 가진 컨텐츠를 찾을 수 없습니다.",  contentId)
                ));

        return EntityDtoMapper.toDto(content);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> getContentByCreatedBy(String username, int page, int size) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("해당 이름(%s)을 가진 사용자를 찾을 수 없습니다.", username)
                ));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        return contentRepository.findAllByCreatedBy(username, pageable)
                .map(EntityDtoMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<ContentResponse> getAllContents(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdDate").descending());

        Page<Content> contentPage = contentRepository.findAll(pageable);

        return contentPage.map(EntityDtoMapper::toDto);
    }
}

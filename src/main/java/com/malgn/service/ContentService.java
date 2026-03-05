package com.malgn.service;

import com.malgn.dto.ContentCreateRequest;
import com.malgn.dto.ContentResponse;
import com.malgn.dto.ContentUpdateRequest;
import com.malgn.entity.Content;
import com.malgn.entity.User;
import com.malgn.exception.ContentNotFoundException;
import com.malgn.exception.UserNotFoundException;
import com.malgn.repository.ContentRepository;
import com.malgn.repository.UserRepository;
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

    public ContentResponse createContent(ContentCreateRequest request, Long userId) {
        User user =  userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("해당 아이디(%d)를 가진 사용자를 찾을 수 없습니다.", userId)
                ));

        Content content = Content.builder()
                .title(request.title())
                .description(request.description())
                .createdBy(request.createdBy())
                .build();

        contentRepository.save(content);

        return EntityDtoMapper.toDto(content);
    }

    public void deleteContent(Long contentId) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(
                        String.format("해당 아이디(%d)를 가진 컨텐츠를 찾을 수 없습니다.",  contentId)
                ));

        contentRepository.deleteById(content.getId());
    }

    public ContentResponse updateContent(Long contentId, ContentUpdateRequest request) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ContentNotFoundException(
                        String.format("해당 아이디(%d)를 가진 컨텐츠를 찾을 수 없습니다.",  contentId)
                ));

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

package com.malgn.util;

import com.malgn.dto.ContentResponse;
import com.malgn.dto.UserResponse;
import com.malgn.entity.Content;
import com.malgn.entity.User;

public class EntityDtoMapper {
    public static ContentResponse toDto(Content content) {
        return ContentResponse.builder()
                .contentId(content.getId())
                .title(content.getTitle())
                .description(content.getDescription())
                .createdBy(content.getCreatedBy())
                .createdAt(content.getCreatedDate())
                .build();
    }

    public static UserResponse toDto(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();
    }
}

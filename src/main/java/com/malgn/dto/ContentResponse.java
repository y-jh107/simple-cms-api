package com.malgn.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ContentResponse(
        Long contentId,
        String title,
        String description,
        String createdBy,
        LocalDateTime createdAt
) {}

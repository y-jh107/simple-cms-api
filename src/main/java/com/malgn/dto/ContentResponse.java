package com.malgn.dto;

import java.time.LocalDateTime;

public record ContentResponse(
        Long contentId,
        String title,
        String description,
        String createdBy,
        LocalDateTime createdAt
) {}

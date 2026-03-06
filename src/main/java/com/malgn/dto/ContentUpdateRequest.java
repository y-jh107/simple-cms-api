package com.malgn.dto;

public record ContentUpdateRequest(
        String title,
        String description,
        String lastModifiedBy
) {}

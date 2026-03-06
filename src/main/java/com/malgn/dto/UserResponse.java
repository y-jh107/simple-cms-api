package com.malgn.dto;

import lombok.Builder;

@Builder
public record UserResponse(
        Long id,
        String username,
        String password
) {}

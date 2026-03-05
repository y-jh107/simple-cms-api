package com.malgn.dto;

import lombok.Builder;

@Builder
public record UserResponse(
        String username,
        String password
) {}

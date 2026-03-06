package com.malgn.dto;

import lombok.Builder;

@Builder
public record AuthenticationResponse(
        String jwt
) {}

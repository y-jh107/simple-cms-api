package com.malgn.dto;

public record AuthenticationRequest(
        String username,
        String password
) {}

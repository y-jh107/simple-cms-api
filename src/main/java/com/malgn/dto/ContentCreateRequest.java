package com.malgn.dto;

import jakarta.validation.constraints.NotBlank;

public record ContentCreateRequest(
        @NotBlank(message = "제목을 입력해주세요.")
        String title,
        String description,
        @NotBlank(message = "작성자 정보가 필요합니다.")
        String createdBy
) {}

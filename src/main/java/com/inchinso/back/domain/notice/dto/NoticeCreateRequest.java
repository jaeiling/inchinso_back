package com.inchinso.back.domain.notice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class NoticeCreateRequest {
    @NotBlank private String title;
    @NotBlank private String content;
    private boolean pinned;
}

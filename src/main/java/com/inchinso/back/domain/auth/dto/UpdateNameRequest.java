package com.inchinso.back.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UpdateNameRequest {
    @NotBlank(message = "이름을 입력해주세요.")
    @Size(min = 1, max = 20, message = "이름은 1~20자 사이여야 합니다.")
    private String name;
}

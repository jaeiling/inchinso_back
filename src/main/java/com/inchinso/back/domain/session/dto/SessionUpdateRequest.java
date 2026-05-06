package com.inchinso.back.domain.session.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
public class SessionUpdateRequest {
    @NotNull private LocalDate sessionDate;
    @NotNull private LocalTime startTime;
    @NotNull private LocalTime endTime;
    @NotBlank private String location;
    private String rules;
    @Min(1) private int maxParticipants;
}

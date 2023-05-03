package com.nix.managecafe.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class TimeSheetRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long shiftId;
    private Long newShiftId;
    private String startDate;
    private Long salary;
}

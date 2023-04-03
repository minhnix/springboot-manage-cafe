package com.nix.managecafe.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeSheetRequest {
    @NotNull
    private Long userId;
    @NotNull
    private Long shiftId;
    private Long newShiftId;
}

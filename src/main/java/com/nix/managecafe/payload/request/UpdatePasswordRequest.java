package com.nix.managecafe.payload.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePasswordRequest {
    private String oldPassword;
    @Size(min = 6)
    private String newPassword;
}

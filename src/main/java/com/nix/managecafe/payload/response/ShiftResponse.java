package com.nix.managecafe.payload.response;

import com.nix.managecafe.model.Shift;
import com.nix.managecafe.model.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ShiftResponse {
    private Shift shift;
    private List<User> users;
}

package com.nix.managecafe.payload.response;

import com.nix.managecafe.model.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
public class UserSummary {
    private Long id;
    private String username;
    private String email;
    private Set<Role> role;
}

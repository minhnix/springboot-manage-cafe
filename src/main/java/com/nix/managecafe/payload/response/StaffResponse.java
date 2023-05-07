package com.nix.managecafe.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StaffResponse {
    private Long userId;
    private Long shiftId;
    private String shift;
    private String username;
    private String firstname;
    private String lastname;
    private String email;
    private String phoneNumber;
    private Long salary;
    private LocalDate startDate;
}

package com.nix.managecafe.controller;

import com.nix.managecafe.exception.InvalidPasswordException;
import com.nix.managecafe.model.Order;
import com.nix.managecafe.model.User;
import com.nix.managecafe.model.enumname.RoleName;
import com.nix.managecafe.payload.request.SignUpRequest;
import com.nix.managecafe.payload.request.UpdatePasswordRequest;
import com.nix.managecafe.payload.request.UpdateUserRequest;
import com.nix.managecafe.payload.response.ApiResponse;
import com.nix.managecafe.payload.response.OrderResponse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.security.CurrentUser;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.service.OrderService;
import com.nix.managecafe.service.UserService;
import com.nix.managecafe.util.AppConstants;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final OrderService orderService;

    public UserController(OrderService orderService, UserService userService) {
        this.orderService = orderService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PagedResponse<User> getAllUsers(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_ID, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_ASC, required = false) String sortDir
    ) {
        return userService.getAll(page, size, sortBy, sortDir);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{username}")
    public User getOne(@PathVariable("username") String username) {
        return userService.getOneUser(username);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        User user = userService.createUser(signUpRequest, RoleName.ROLE_STAFF);

        URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(user.getUsername()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "Create user successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@PathVariable("id") Long id,
                                  @Valid @RequestBody UpdateUserRequest updateRequest) {
        return userService.updateUser(id, updateRequest);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{username}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByUsername(@PathVariable("username") String username
    ) {
        userService.removeByUsername(username);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public User getProfile(@CurrentUser UserPrincipal userPrincipal) {
        return userService.getProfile(userPrincipal);
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN')")
    public ApiResponse updateCurrentUser(@CurrentUser UserPrincipal userPrincipal,
                                         @Valid @RequestBody UpdateUserRequest updateRequest) {
        userService.updateUser(userPrincipal.getId(), updateRequest);
        return new ApiResponse(true, "Update successfully!!");
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse> updatePassword(@CurrentUser UserPrincipal userPrincipal,
                                                      @Valid @RequestBody UpdatePasswordRequest updatePasswordRequest
    ) {
        try {
            userService.updatePassword(userPrincipal, updatePasswordRequest);
        } catch (InvalidPasswordException ex) {
            return new ResponseEntity<>(new ApiResponse(false, ex.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ApiResponse(true, "Password updated"), HttpStatus.OK);
    }

    //TODO forgot password with no token
    @PutMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(@CurrentUser UserPrincipal userPrincipal) {
        try {
            userService.sendResetPasswordEmail(userPrincipal.getUser());
        } catch (MessagingException | UnsupportedEncodingException e) {
            return new ResponseEntity<>(new ApiResponse(false, e.getMessage()), HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(new ApiResponse(true, "Reset password"), HttpStatus.OK);
    }

    @GetMapping("/me/orders")
    public PagedResponse<OrderResponse> getAllOrder(@CurrentUser UserPrincipal userPrincipal,
                                                    @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
                                                    @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
                                                    @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_CREATED_AT, required = false) String sortBy,
                                                    @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DES, required = false) String sortDir
    ) {
        if (userPrincipal.getAuthorities().contains(new SimpleGrantedAuthority(RoleName.ROLE_CUSTOMER.name())))
            return orderService.getAllByCreateAt(page, size, sortBy, sortDir, userPrincipal.getId());
        else
            return orderService.getAllByStaffId(page, size, sortBy, sortDir, userPrincipal.getId());
    }
}

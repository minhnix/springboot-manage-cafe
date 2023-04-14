package com.nix.managecafe.controller;

import com.nix.managecafe.exception.AppException;
import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.model.Order;
import com.nix.managecafe.model.enumname.StatusName;
import com.nix.managecafe.payload.request.OrderRequest;
import com.nix.managecafe.payload.response.ApiResponse;
import com.nix.managecafe.payload.response.OrderResponse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.security.CurrentUser;
import com.nix.managecafe.security.UserPrincipal;
import com.nix.managecafe.service.OrderService;
import com.nix.managecafe.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/{orderId}")
    public OrderResponse getOne(@PathVariable("orderId") Long orderId, @CurrentUser UserPrincipal userPrincipal) {
        return orderService.getOne(orderId, userPrincipal);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public PagedResponse<OrderResponse> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_CREATED_AT, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DES, required = false) String sortDir
    ) {
        return orderService.getAll(page, size, sortBy, sortDir);
    }
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @GetMapping("/pending")
    public PagedResponse<OrderResponse> getAllOrderPending(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_CREATED_AT, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DES, required = false) String sortDir
    ) {
        return orderService.getAllByStatus(page, size, sortBy, sortDir, StatusName.PENDING);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/delivery")
    public PagedResponse<OrderResponse> getAllOrderDelivery(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_CREATED_AT, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DES, required = false) String sortDir
    ) {
        return orderService.getAllByStatus(page, size, sortBy, sortDir, StatusName.DELIVERING);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/paid")
    public PagedResponse<OrderResponse> getAllOrderPaid(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_CREATED_AT, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DES, required = false) String sortDir
    ) {
        return orderService.getAllByStatus(page, size, sortBy, sortDir, StatusName.DELIVERING);
    }
    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER', 'STAFF', 'ADMIN')")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest, @CurrentUser UserPrincipal currentUser) {
        return new ResponseEntity<>(orderService.create(orderRequest, currentUser), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrder(@PathVariable("orderId") Long orderId) {
        orderService.delete(orderId);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PatchMapping("/{orderId}/paid")
    public ResponseEntity<ApiResponse> pay(@PathVariable("orderId") Long orderId, @CurrentUser UserPrincipal currentUser) {
        orderService.payOrder(orderId, currentUser);
        return new ResponseEntity<>(new ApiResponse(true, "Đơn đã thanh toán thành công"),HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    @PatchMapping("/{orderId}/receive")
    public ResponseEntity<ApiResponse> receive(@PathVariable("orderId") Long orderId, @CurrentUser UserPrincipal currentUser) {
        orderService.receiveOrder(orderId, currentUser);
        return new ResponseEntity<>(new ApiResponse(true, "Nhận đơn thành công"), HttpStatus.OK);
    }
}

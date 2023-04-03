package com.nix.managecafe.controller;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.model.PurchaseOrder;
import com.nix.managecafe.model.Supplier;
import com.nix.managecafe.payload.request.PurchaseOrderRequest;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.payload.response.PurchaseOrderResponse;
import com.nix.managecafe.service.PurchaseOrderService;
import com.nix.managecafe.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@PreAuthorize("hasRole('ADMIN')")
public class PurchaseOrderController {
    private final PurchaseOrderService purchaseOrderService;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse getOne(@PathVariable("id") Long id) {
        return purchaseOrderService.getOne(id);
    }

    @GetMapping
    public PagedResponse<PurchaseOrderResponse> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "des", required = false) String sortDir
    ) {
        return purchaseOrderService.getAll(page, size, sortBy, sortDir);
    }

    @PostMapping
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequest purchaseOrder) throws URISyntaxException {
        PurchaseOrder result = purchaseOrderService.create(purchaseOrder);
        return ResponseEntity
                .created(new URI("/api/v1/purchase-orders/" + result.getId()))
                .body(result);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") final Long id) {
        purchaseOrderService.delete(id);
    }
}

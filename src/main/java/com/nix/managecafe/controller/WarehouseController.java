package com.nix.managecafe.controller;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.model.Warehouse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.service.WarehouseService;
import com.nix.managecafe.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {
    private final WarehouseService warehouseService;

    public WarehouseController(WarehouseService warehouseService) {
        this.warehouseService = warehouseService;
    }
    @PatchMapping(value = "/{id}")
    public ResponseEntity<Warehouse> partialUpdateSupplier(@PathVariable(value = "id") final Long id, @Valid @RequestBody Warehouse warehouse) {
        if (warehouse.getId() == null) {
            throw new BadRequestException("Id null");
        }
        if (!Objects.equals(id, warehouse.getId())) {
            throw new BadRequestException("Invalid Id");
        }

        Warehouse result = warehouseService.partialUpdate(warehouse);

        return ResponseEntity.ok().header("Id", warehouse.getId().toString()).body(result);
    }

    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable(value = "id") final Long id) {
        warehouseService.delete(id);
    }

    @GetMapping("/{id}")
    public Warehouse getOne(@PathVariable("id") Long id) {
        return warehouseService.getOne(id);
    }

    @GetMapping
    public PagedResponse<Warehouse> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_ID, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_ASC, required = false) String sortDir
    ) {
        return warehouseService.getAll(page, size, sortBy, sortDir);
    }
}

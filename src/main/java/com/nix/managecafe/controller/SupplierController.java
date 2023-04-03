package com.nix.managecafe.controller;

import com.nix.managecafe.exception.BadRequestException;

import com.nix.managecafe.model.Supplier;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.service.SupplierService;

import com.nix.managecafe.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("api/v1/suppliers")
@PreAuthorize("hasRole('ADMIN')")
public class SupplierController {
    private final SupplierService supplierService;
    private static final String ENTITY_NAME = "supplier";

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody Supplier supplier) throws URISyntaxException {
        if (supplier.getId() != null) {
            throw new BadRequestException("A new supplier cannot already have an ID");
        }
        if (supplier.getAddress().getId() != null) {
            throw new BadRequestException("A new address cannot already have an ID");
        }

        Supplier result = supplierService.create(supplier);
        return ResponseEntity
                .created(new URI("/api/v1/suppliers/" + result.getId()))
                .body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable(value = "id") final Long id, @Valid @RequestBody Supplier supplier) {
        if (supplier.getId() == null) {
            throw new BadRequestException("Id null");
        }
        if (!Objects.equals(id, supplier.getId())) {
            throw new BadRequestException("Invalid Id");
        }

        Supplier result = supplierService.update(supplier);
        return ResponseEntity
                .ok()
                .header("Id", supplier.getId().toString())
                .body(result);
    }

    @PatchMapping(value = "/{id}")
    public ResponseEntity<Supplier> partialUpdateSupplier(@PathVariable(value = "id") final Long id,@Valid @RequestBody Supplier supplier) {
        if (supplier.getId() == null) {
            throw new BadRequestException("Id null");
        }
        if (!Objects.equals(id, supplier.getId())) {
            throw new BadRequestException("Invalid Id");
        }

        Supplier result = supplierService.partialUpdate(supplier);

        return ResponseEntity.ok().header("Id", supplier.getId().toString()).body(result);
    }
    @DeleteMapping(value = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSupplier(@PathVariable(value = "id") final Long id) {
        supplierService.delete(id);
    }

    @GetMapping("/{id}")
    public Supplier getOne(@PathVariable("id") Long id) {
        return supplierService.getOne(id);
    }

    @GetMapping
    public PagedResponse<Supplier> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_ID, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_ASC, required = false) String sortDir
    ) {
        return supplierService.getAll(page, size, sortBy, sortDir);
    }
    @GetMapping("/search")
    public List<Supplier> searchByName(@RequestParam("q") String name) {
        return supplierService.searchByName(name);
    }
}

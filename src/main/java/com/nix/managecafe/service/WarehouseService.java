package com.nix.managecafe.service;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Warehouse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.repository.WarehouseRepo;
import com.nix.managecafe.util.ValidatePageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class WarehouseService {
    private final WarehouseRepo warehouseRepo;

    public WarehouseService(WarehouseRepo warehouseRepo) {
        this.warehouseRepo = warehouseRepo;
    }

    public Warehouse create(Warehouse warehouse) {
        return warehouseRepo.save(warehouse);
    }

    public Warehouse update(Warehouse warehouse) {
        if (warehouseRepo.findById(warehouse.getId()).isEmpty()) {
            throw new ResourceNotFoundException("Supplier", "id", warehouse.getId());
        }
        return warehouseRepo.save(warehouse);
    }

    public Warehouse partialUpdate(Warehouse warehouse) {
        Warehouse existingWarehouse = warehouseRepo.findById(warehouse.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", warehouse.getId()));

        if (warehouse.getStatus() != null) {
            existingWarehouse.setStatus(warehouse.getStatus());
        }
        if (warehouse.getQuantity() != null) {
            existingWarehouse.setQuantity(warehouse.getQuantity());
        }
        if (warehouse.getLowQuantity() != null) {
            existingWarehouse.setLowQuantity(warehouse.getLowQuantity());
        }
        return warehouseRepo.save(existingWarehouse);
    }

    public void delete(Long id) {
        Warehouse warehouse = warehouseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        warehouseRepo.delete(warehouse);
    }
    public Warehouse getOne(Long id) {
        Warehouse product = warehouseRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        return product;
    }

    public PagedResponse<Warehouse> getAll(int page, int size, String sortBy, String sortDir) {
        ValidatePageable.invoke(page, size);

        Sort sort = (sortDir.equalsIgnoreCase("des")) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Warehouse> warehouses = warehouseRepo.findAll(pageable);


        return new PagedResponse<>(warehouses.getContent(), warehouses.getNumber(),
                warehouses.getSize(), warehouses.getTotalElements(), warehouses.getTotalPages(), warehouses.isLast());
    }

}

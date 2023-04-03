package com.nix.managecafe.repository;

import com.nix.managecafe.model.Product;
import com.nix.managecafe.model.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseRepo extends JpaRepository<Warehouse, Long>{
    @EntityGraph(attributePaths = {"product"})
    Optional<Warehouse> findById(Long id);
    @EntityGraph(attributePaths = {"product"})
    Page<Warehouse> findAll(Pageable pageable);

    void deleteByProduct(Product product);

    Warehouse findByProductId(Long id);
}

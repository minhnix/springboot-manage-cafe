package com.nix.managecafe.repository;

import com.nix.managecafe.model.Supplier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface SupplierRepo extends JpaRepository<Supplier, Long> {
    @EntityGraph(attributePaths = {"address"})
    Page<Supplier> findAll(Pageable pageable);
    @Cacheable("supplier")
    Optional<Supplier> findById(Long id);
    @EntityGraph(attributePaths = {"address"})
    Page<Supplier> findByNameContains(Pageable pageable, String name);
}

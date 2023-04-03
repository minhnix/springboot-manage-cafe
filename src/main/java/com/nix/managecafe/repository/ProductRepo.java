package com.nix.managecafe.repository;

import com.nix.managecafe.model.Product;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepo extends JpaRepository<Product, Long> {
    @Cacheable("product")
    Optional<Product> findById(Long id);

    List<Product> findByNameContains(String name);
}

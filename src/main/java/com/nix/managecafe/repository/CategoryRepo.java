package com.nix.managecafe.repository;

import com.nix.managecafe.model.Category;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface CategoryRepo extends JpaRepository<Category, Long> {
    @Cacheable(cacheNames = "categories")
    Optional<Category> findById(Long id);
}

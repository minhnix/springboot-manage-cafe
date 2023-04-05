package com.nix.managecafe.repository;

import com.nix.managecafe.model.Menu;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MenuRepo extends JpaRepository<Menu, Long> {
    @Query("select menu from Menu menu where menu.category.id = :categoryId")
    List<Menu> findByCategoryId(@Param("categoryId") Long categoryId);

    @EntityGraph(attributePaths = {"category"})
    Page<Menu> findAll(Pageable pageable);
    @EntityGraph(attributePaths = {"category"})
    Optional<Menu> findById(Long id);

    List<Menu> findByNameContains(String name);
}
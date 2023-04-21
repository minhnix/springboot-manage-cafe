package com.nix.managecafe.repository;

import com.nix.managecafe.model.MenuDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuDetailRepo extends JpaRepository<MenuDetail, Long> {
    List<MenuDetail> findByMenuId(Long menuId);
    Optional<MenuDetail> findByMenuIdAndProductId(Long menuId,Long productId);
}

package com.nix.managecafe.repository;

import com.nix.managecafe.model.Cart;
import com.nix.managecafe.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
    List<Cart> findAllByUser(User user);
    Optional<Cart> findById(Long id);
    Optional<Cart> findByUserIdAndMenuIdAndSize(Long userId, Long menuId, String size);
    void deleteByUserId(Long userId);
    int countByUserId(Long userId);
}

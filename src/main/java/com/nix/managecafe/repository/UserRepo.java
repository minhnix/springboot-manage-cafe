package com.nix.managecafe.repository;

import com.nix.managecafe.model.User;
import com.nix.managecafe.model.enumname.RoleName;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {

    Optional<User> findByUsernameOrEmail(String username, String email);
    List<User> findByIdIn(List<Long> userIds);

    @EntityGraph(attributePaths = {"roles", "address"})
    @Cacheable("usersById")
    Optional<User> findById(Long id);
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Page<User> findUserByRolesId(Long roleId, Pageable pageable);

    long countByRolesId(Long roleId);
}

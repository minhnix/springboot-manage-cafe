package com.nix.managecafe.repository;

import com.nix.managecafe.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepo extends JpaRepository<Address, Long> {
}

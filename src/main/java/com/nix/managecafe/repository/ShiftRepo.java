package com.nix.managecafe.repository;

import com.nix.managecafe.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepo extends JpaRepository<Shift, Long> {
//    @EntityGraph(attributePaths = {"timeSheets"})
    List<Shift> findAll();
}

package com.nix.managecafe.repository;

import com.nix.managecafe.model.Shift;
import com.nix.managecafe.model.TimeSheet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TimeSheetRepo extends JpaRepository<TimeSheet, Long> {
    Optional<TimeSheet> findByShiftIdAndUserId(Long shiftId, Long userId);
    List<TimeSheet> findByUserId(Long userId);
    List<TimeSheet> findByShiftId(Long shiftId);
}

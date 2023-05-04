package com.nix.managecafe.repository;

import com.nix.managecafe.model.Shift;
import com.nix.managecafe.model.TimeSheet;
import com.nix.managecafe.model.User;
import com.nix.managecafe.payload.response.StaffResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TimeSheetRepo extends JpaRepository<TimeSheet, Long> {
    Optional<TimeSheet> findByShiftIdAndUserId(Long shiftId, Long userId);

    List<TimeSheet> findByUserId(Long userId);

    List<TimeSheet> findByShiftId(Long shiftId);

    @Query("SELECT t FROM TimeSheet t left join t.user as u left join t.shift as s where (lower(concat(u.lastname,' ', u.firstname)) LIKE LOWER(CONCAT('%', :keyword, '%')) "
            + "or lower(u.username) like lower(CONCAT('%', :keyword, '%')) or lower(u.email) like lower(CONCAT('%', :keyword, '%'))) "
            + "and t.startDate between :start and :end "
            + "and (:sid IS NULL or s.id = :sid)"
    )
    Page<TimeSheet> findStaffTimeSheet(Pageable pageable, @Param("sid") Long shiftId, @Param("start") LocalDate start,
                                       @Param("end") LocalDate end, @Param("keyword") String keyword);
    @Query("SELECT t FROM TimeSheet t left join t.user as u left join t.shift as s where "
            + "t.startDate between :start and :end "
            + "and (:sid IS NULL or s.id = :sid)"
    )
    Page<TimeSheet> findStaffTimeSheet(Pageable pageable, @Param("sid") Long shiftId,
                                       @Param("start") LocalDate start,
                                       @Param("end") LocalDate end);
    void deleteByUser(User user);
}

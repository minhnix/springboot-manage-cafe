package com.nix.managecafe.controller;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.model.Menu;
import com.nix.managecafe.model.TimeSheet;
import com.nix.managecafe.payload.request.TimeSheetRequest;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.payload.response.ShiftResponse;
import com.nix.managecafe.payload.response.StaffResponse;
import com.nix.managecafe.service.TimeSheetService;
import com.nix.managecafe.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/time-sheets")
@PreAuthorize("hasRole('ADMIN')")
public class TimeSheetController {
    private final TimeSheetService timeSheetService;

    public TimeSheetController(TimeSheetService timeSheetService) {
        this.timeSheetService = timeSheetService;
    }

    @PostMapping
    public TimeSheet create(@Valid @RequestBody TimeSheetRequest timeSheetRequest) {
        return timeSheetService.create(timeSheetRequest);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestParam(value = "shiftId") Long shiftId,
            @RequestParam(value = "userId") Long userId
    ) {
        timeSheetService.delete(shiftId, userId);
    }

    @PutMapping
    public TimeSheet update(@Valid @RequestBody TimeSheetRequest timeSheetRequest) {
        if (timeSheetRequest.getNewShiftId() == null) {
            throw new BadRequestException("New shift id is null");
        }
        return timeSheetService.update(timeSheetRequest);
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(value = "shiftId", required = false) Long shiftId,
            @RequestParam(value = "userId", required = false) Long userId
    ) {
        if (shiftId == null && userId == null)
            return ResponseEntity.ok(timeSheetService.getAllUserByShift());
        else if (shiftId != null && userId != null)
            return ResponseEntity.ok(timeSheetService.getOne(shiftId, userId));
        else if (shiftId == null)
            return ResponseEntity.ok(timeSheetService.getShiftsByUser(userId));
        else
            return ResponseEntity.ok(timeSheetService.getUsersByShift(shiftId));
    }

    @GetMapping("/staff")
    public PagedResponse<StaffResponse> getStaffTimeSheet(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sid", required = false) Long sid,
            @RequestParam(value = "start", required = false) String start,
            @RequestParam(value = "end", required = false) String end
            ) {
        return timeSheetService.getStaffTimeSheet(page, size, keyword, sid, start, end);
    }
}

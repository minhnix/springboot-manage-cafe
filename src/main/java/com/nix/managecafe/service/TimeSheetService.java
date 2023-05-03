package com.nix.managecafe.service;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.*;
import com.nix.managecafe.payload.request.TimeSheetRequest;
import com.nix.managecafe.payload.response.ShiftResponse;
import com.nix.managecafe.repository.ShiftRepo;
import com.nix.managecafe.repository.TimeSheetRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = {ResourceNotFoundException.class})
public class TimeSheetService {
    private final TimeSheetRepo timeSheetRepo;
    private final ShiftRepo shiftRepo;

    public TimeSheetService(TimeSheetRepo timeSheetRepo, ShiftRepo shiftRepo) {
        this.timeSheetRepo = timeSheetRepo;
        this.shiftRepo = shiftRepo;
    }

    public TimeSheet create(TimeSheetRequest timeSheetRequest) {
        User user = new User();
        user.setId(timeSheetRequest.getUserId());
        Shift shift = new Shift();
        shift.setId(timeSheetRequest.getShiftId());
        TimeSheet timeSheet = new TimeSheet();
        timeSheet.setUser(user);
        timeSheet.setShift(shift);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(timeSheetRequest.getStartDate(), formatter);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Lỗi định dạng ngày tháng");
        }
        timeSheet.setStartDay(startDate);
        timeSheet.setSalary(timeSheetRequest.getSalary());
        return timeSheetRepo.save(timeSheet);
    }

    public TimeSheet update(TimeSheetRequest timeSheetRequest) {
        TimeSheet timeSheet = timeSheetRepo.findByShiftIdAndUserId(timeSheetRequest.getShiftId(), timeSheetRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet", "id", 0));
        Shift shift = shiftRepo.findById(timeSheetRequest.getNewShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", timeSheetRequest.getNewShiftId()));
        timeSheet.setShift(shift);
        return timeSheetRepo.save(timeSheet);
    }

    public void delete(Long shiftId, Long userId) {
        TimeSheet timeSheet = timeSheetRepo.findByShiftIdAndUserId(shiftId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet", "id", 0));
        timeSheetRepo.delete(timeSheet);
    }

    public TimeSheet getOne(Long shiftId, Long userId) {
        return timeSheetRepo.findByShiftIdAndUserId(shiftId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet", "id", 0));
    }

    public List<Shift> getShiftsByUser(Long userId) {
        List<TimeSheet> timeSheets = timeSheetRepo.findByUserId(userId);
        return timeSheets.stream().map(TimeSheet::getShift).collect(Collectors.toList());
    }

    public List<User> getUsersByShift(Long shiftId) {
        List<TimeSheet> timeSheets = timeSheetRepo.findByShiftId(shiftId);
        return timeSheets.stream().map(TimeSheet::getUser).collect(Collectors.toList());
    }

    public List<ShiftResponse> getAllUserByShift() {
        List<Shift> shifts = shiftRepo.findAll();
        List<ShiftResponse> result = new ArrayList<>();
        shifts.forEach(
                shift -> {
                    ShiftResponse t = new ShiftResponse();
                    t.setShift(shift);
                    t.setUsers(shift.getTimeSheets().stream().map(TimeSheet::getUser).collect(Collectors.toList()));
                    result.add(t);
                }
        );
        return result;
    }
}

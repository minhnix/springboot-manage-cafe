package com.nix.managecafe.service;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.*;
import com.nix.managecafe.payload.request.TimeSheetRequest;
import com.nix.managecafe.payload.response.OrderResponse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.payload.response.ShiftResponse;
import com.nix.managecafe.payload.response.StaffResponse;
import com.nix.managecafe.repository.ShiftRepo;
import com.nix.managecafe.repository.TimeSheetRepo;
import com.nix.managecafe.util.ModelMapper;
import com.nix.managecafe.util.ValidateDate;
import com.nix.managecafe.util.ValidatePageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = {ResourceNotFoundException.class, BadRequestException.class, DateTimeParseException.class})
public class TimeSheetService {
    Logger logger = LoggerFactory.getLogger(TimeSheetService.class);
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
        timeSheet.setStartDate(startDate);
        timeSheet.setSalary(timeSheetRequest.getSalary());
        return timeSheetRepo.save(timeSheet);
    }

    public TimeSheet update(TimeSheetRequest timeSheetRequest) {
        TimeSheet timeSheet = timeSheetRepo.findByShiftIdAndUserId(timeSheetRequest.getShiftId(), timeSheetRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("TimeSheet", "id", 0));
        Shift shift = shiftRepo.findById(timeSheetRequest.getNewShiftId())
                .orElseThrow(() -> new ResourceNotFoundException("Shift", "id", timeSheetRequest.getNewShiftId()));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate;
        try {
            startDate = LocalDate.parse(timeSheetRequest.getStartDate(), formatter);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Lỗi định dạng ngày tháng");
        }
        timeSheet.setShift(shift);
        timeSheet.setStartDate(startDate);
        timeSheet.setSalary(timeSheetRequest.getSalary());
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

    public PagedResponse<StaffResponse> getStaffTimeSheet(int page, int size, String keyword, Long sid, String startDateString, String endDateString) {
        ValidatePageable.invoke(page, size);
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "startDate"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "user.firstname"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "user.lastname"));
        Sort sort = Sort.by(orders);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<TimeSheet> timeSheets;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate startDate, endDate;
        if (keyword != null && keyword.isBlank()) keyword = null;
        try {
            if (startDateString != null && !startDateString.isBlank()) {
                startDate = LocalDate.parse(startDateString, formatter);
            } else {
                startDate = LocalDate.of(2000, 1, 1);
            }
            if (endDateString != null && !endDateString.isBlank()) {
                endDate = LocalDate.parse(endDateString, formatter);
            } else {
                endDate = LocalDate.of(LocalDate.now().getYear() + 1, 11, 1);
            }
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Lỗi định dạng ngày tháng (yyyy-MM-dd)");
        }
        ValidateDate.invoke(startDate, endDate);
        if (keyword == null)
            timeSheets = timeSheetRepo.findStaffTimeSheet(pageable, sid, startDate, endDate);
        else
            timeSheets = timeSheetRepo.findStaffTimeSheet(pageable, sid, startDate, endDate, keyword);

        List<StaffResponse> staffResponses = timeSheets.getContent().stream().map(
                ModelMapper::mapTimeSheetToStaffResponse
        ).toList();

        return new PagedResponse<>(staffResponses, timeSheets.getNumber(),
                timeSheets.getSize(), timeSheets.getTotalElements(), timeSheets.getTotalPages(), timeSheets.isLast());
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

package com.nix.managecafe.controller;

import com.nix.managecafe.exception.ResourceNotFoundException;
import com.nix.managecafe.model.Shift;
import com.nix.managecafe.repository.ShiftRepo;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/shifts")
public class ShiftController {
    private final ShiftRepo shiftRepo;

    public ShiftController(ShiftRepo shiftRepo) {
        this.shiftRepo = shiftRepo;
    }

    @GetMapping("/{id}")
    public Shift getOne(@PathVariable("id") Long id) {
        return shiftRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Shift", "id", id));
    }

    @GetMapping
    public List<Shift> getAll() {
        return shiftRepo.findAll();
    }
    @PostMapping
    public void init() {
        LocalTime start = LocalTime.of(8,0,0);
        LocalTime end = LocalTime.of(11,59, 59);

        Shift sang = new Shift();
        sang.setName("Ca sáng");
        sang.setEndTime(end);
        sang.setStartTime(start);
        shiftRepo.save(sang);

        Shift chieu = new Shift();
        start = LocalTime.of(12,0,0);
        end = LocalTime.of(15,59, 59);
        chieu.setName("Ca chiều");
        chieu.setEndTime(end);
        chieu.setStartTime(start);
        shiftRepo.save(chieu);

        Shift toi = new Shift();
        start = LocalTime.of(16,0,0);
        end = LocalTime.of(19,59, 59);
        toi.setName("Ca tối");
        toi.setEndTime(end);
        toi.setStartTime(start);
        shiftRepo.save(toi);

        Shift dem = new Shift();
        start = LocalTime.of(20, 0, 0);
        end = LocalTime.of(23,59, 59);
        dem.setName("Ca đêm");
        dem.setEndTime(end);
        dem.setStartTime(start);
        shiftRepo.save(dem);
    }
}

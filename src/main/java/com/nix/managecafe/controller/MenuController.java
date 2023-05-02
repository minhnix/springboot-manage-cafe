package com.nix.managecafe.controller;

import com.nix.managecafe.exception.BadRequestException;
import com.nix.managecafe.model.Menu;
import com.nix.managecafe.payload.request.MenuRequest;
import com.nix.managecafe.payload.response.ApiResponse;
import com.nix.managecafe.payload.response.MenuDetailResponse;
import com.nix.managecafe.payload.response.PagedResponse;
import com.nix.managecafe.service.MenuService;
import com.nix.managecafe.util.AppConstants;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
//TODO: GET RELATED MENU
@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {
    @Autowired
    private MenuService menuService;

    @GetMapping
    public PagedResponse<Menu> getAll(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_ID, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_ASC, required = false) String sortDir
    ) {
        return menuService.getAll(page, size, sortBy, sortDir);
    }

    @GetMapping("/{id}")
    public Menu getOne(@PathVariable("id") Long id) {
        return menuService.getOne(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/detail")
    public MenuDetailResponse getOneDetail(@PathVariable("id") Long id) {
        return menuService.getMenuDetail(id);
    }
    @GetMapping("/detail")
    public PagedResponse<MenuDetailResponse> getAllDetails(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_BY_ID, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_ASC, required = false) String sortDir
    ) {
        return menuService.getAllMenuDetails(page, size, sortBy, sortDir);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody MenuRequest menu) {
        Menu menu1 = menuService.create(menu);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{menuId}")
                .buildAndExpand(menu1.getId()).toUri();

        return ResponseEntity.created(location).body(new ApiResponse(true, "Menu created successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @Valid @RequestBody MenuRequest menu) {
        if (menu.getId() == null) {
            throw new BadRequestException("Id null");
        }
        if (!Objects.equals(id, menu.getId())) {
            throw new BadRequestException("Invalid Id");
        }
        menuService.update(id, menu);
        return ResponseEntity.ok(new ApiResponse(true, "Menu updated successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        menuService.delete(id);
        return ResponseEntity.ok(new ApiResponse(true, "Menu deleted successfully"));
    }

    @GetMapping("/search")
    public List<Menu> searchByName(@RequestParam(value = "q", defaultValue = "") String name,
                                   @RequestParam(value = "cid", required = false) Long cid) {
        return menuService.searchByName(name, cid);
    }

    @GetMapping("/amount")
    @PreAuthorize("hasRole('ADMIN')")
    public long getAmountOfMenu() {
        return menuService.getAmountOfMenu();
    }
}

package com.tiffinbox.menuservice.controller;

import com.tiffinbox.menuservice.dto.*;
import com.tiffinbox.menuservice.exception.ForbiddenException;
import com.tiffinbox.menuservice.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Owner management endpoints. The gateway validates the JWT and forwards the caller's
 * role as X-User-Role; here we require it to be OWNER (services trust the header).
 */
@RestController
@RequestMapping("/api/menu/owner")
@RequiredArgsConstructor
public class OwnerMenuController {

    private final MenuService menuService;

    @PostMapping
    public ResponseEntity<MenuResponse> create(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CreateMenuRequest request) {
        requireOwner(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MenuResponse.from(menuService.createMenu(request)));
    }

    @GetMapping
    public ResponseEntity<List<MenuResponse>> listAll(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        requireOwner(role);
        return ResponseEntity.ok(menuService.getAllMenus().stream().map(MenuResponse::from).toList());
    }

    @PutMapping("/{id}")
    public ResponseEntity<MenuResponse> update(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @Valid @RequestBody UpdateMenuRequest request) {
        requireOwner(role);
        return ResponseEntity.ok(MenuResponse.from(menuService.updateMenu(id, request)));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<MenuResponse> close(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id) {
        requireOwner(role);
        return ResponseEntity.ok(MenuResponse.from(menuService.closeMenu(id)));
    }

    @PutMapping("/{id}/open")
    public ResponseEntity<MenuResponse> open(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id) {
        requireOwner(role);
        return ResponseEntity.ok(MenuResponse.from(menuService.openMenu(id)));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<MenuItemResponse> addItem(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long id,
            @Valid @RequestBody CreateMenuItemRequest request) {
        requireOwner(role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MenuItemResponse.from(menuService.addItem(id, request)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<MenuItemResponse> updateItem(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        requireOwner(role);
        return ResponseEntity.ok(MenuItemResponse.from(menuService.updateItem(itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @PathVariable Long itemId) {
        requireOwner(role);
        menuService.deleteItem(itemId);
        return ResponseEntity.noContent().build();
    }

    private void requireOwner(String role) {
        if (!"OWNER".equals(role)) {
            throw new ForbiddenException("Only the owner can manage menus");
        }
    }
}

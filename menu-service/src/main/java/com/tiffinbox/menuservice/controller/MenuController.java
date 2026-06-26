package com.tiffinbox.menuservice.controller;

import com.tiffinbox.menuservice.dto.MenuResponse;
import com.tiffinbox.menuservice.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer-facing read endpoints. {@code /api/menu/today} is public (gateway allowlist).
 */
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/today")
    public ResponseEntity<List<MenuResponse>> today() {
        List<MenuResponse> menus = menuService.getTodaysOpenMenus().stream()
                .map(MenuResponse::from)
                .toList();
        return ResponseEntity.ok(menus);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(MenuResponse.from(menuService.getMenu(id)));
    }
}

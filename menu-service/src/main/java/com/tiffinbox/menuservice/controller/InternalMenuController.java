package com.tiffinbox.menuservice.controller;

import com.tiffinbox.menuservice.dto.ValidateItemsRequest;
import com.tiffinbox.menuservice.dto.ValidatedItemResponse;
import com.tiffinbox.menuservice.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Internal, service-to-service endpoints called by order-service via Feign
 * (directly through Eureka, not through the gateway).
 */
@RestController
@RequestMapping("/api/menu/internal")
@RequiredArgsConstructor
public class InternalMenuController {

    private final MenuService menuService;

    /** Validates items against an open menu and returns authoritative name/price snapshots. */
    @PostMapping("/validate-items")
    public ResponseEntity<List<ValidatedItemResponse>> validateItems(
            @Valid @RequestBody ValidateItemsRequest request) {
        return ResponseEntity.ok(menuService.validateItems(request));
    }

    /** Whether the given menu is currently accepting orders. */
    @GetMapping("/{menuId}/is-open")
    public ResponseEntity<Map<String, Boolean>> isOpen(@PathVariable Long menuId) {
        return ResponseEntity.ok(Map.of("open", menuService.isOpen(menuId)));
    }
}

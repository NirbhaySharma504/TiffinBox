package com.tiffinbox.menuservice.dto;

import com.tiffinbox.menuservice.entity.MealType;
import com.tiffinbox.menuservice.entity.Menu;
import com.tiffinbox.menuservice.entity.MenuStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record MenuResponse(
        Long id,
        LocalDate date,
        MealType mealType,
        String description,
        LocalTime cutoffTime,
        MenuStatus status,
        List<MenuItemResponse> items
) {
    public static MenuResponse from(Menu menu) {
        List<MenuItemResponse> items = menu.getItems().stream()
                .map(MenuItemResponse::from)
                .toList();
        return new MenuResponse(
                menu.getId(),
                menu.getDate(),
                menu.getMealType(),
                menu.getDescription(),
                menu.getCutoffTime(),
                menu.getStatus(),
                items
        );
    }
}

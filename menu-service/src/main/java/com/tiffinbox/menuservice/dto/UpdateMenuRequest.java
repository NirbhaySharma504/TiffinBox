package com.tiffinbox.menuservice.dto;

import java.time.LocalTime;

/** Partial update of an open menu's own fields: any null field is left unchanged. */
public record UpdateMenuRequest(
        String description,
        LocalTime cutoffTime
) {
}

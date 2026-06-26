package com.tiffinbox.subscriptionservice.entity;

import java.time.DayOfWeek;
import java.time.LocalDate;

/** How often a subscription auto-orders. */
public enum Frequency {
    DAILY,
    WEEKDAYS;

    /** Whether this frequency should produce an order on the given date. */
    public boolean appliesOn(LocalDate date) {
        if (this == DAILY) {
            return true;
        }
        DayOfWeek d = date.getDayOfWeek();
        return d != DayOfWeek.SATURDAY && d != DayOfWeek.SUNDAY;
    }
}

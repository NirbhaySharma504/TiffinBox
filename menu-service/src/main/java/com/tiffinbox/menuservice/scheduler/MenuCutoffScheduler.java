package com.tiffinbox.menuservice.scheduler;

import com.tiffinbox.menuservice.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Periodically auto-closes menus whose ordering cutoff has passed, so the system stops
 * accepting orders even if the owner never manually closes the menu.
 */
@Component
@RequiredArgsConstructor
public class MenuCutoffScheduler {

    private static final Logger log = LoggerFactory.getLogger(MenuCutoffScheduler.class);

    private final MenuService menuService;

    @Scheduled(fixedRateString = "${menu.cutoff.check-interval-ms}")
    public void closeExpiredMenus() {
        int closed = menuService.closeExpiredMenus();
        if (closed > 0) {
            log.info("Auto-closed {} menu(s) past their cutoff", closed);
        }
    }
}

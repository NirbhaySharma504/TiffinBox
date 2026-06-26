package com.tiffinbox.subscriptionservice.client;

import com.tiffinbox.subscriptionservice.client.dto.MenuLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/** If menu-service is down, return no menus so the scheduler simply skips this run. */
@Component
public class MenuClientFallback implements MenuClient {

    private static final Logger log = LoggerFactory.getLogger(MenuClientFallback.class);

    @Override
    public List<MenuLite> getTodaysMenus() {
        log.warn("menu-service unavailable — auto-order run skipped (no menus)");
        return List.of();
    }
}

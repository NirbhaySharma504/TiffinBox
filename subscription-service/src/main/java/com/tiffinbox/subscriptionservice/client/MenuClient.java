package com.tiffinbox.subscriptionservice.client;

import com.tiffinbox.subscriptionservice.client.dto.MenuLite;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "menu-service", fallback = MenuClientFallback.class)
public interface MenuClient {

    @GetMapping("/api/menu/today")
    List<MenuLite> getTodaysMenus();
}

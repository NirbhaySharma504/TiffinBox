package com.tiffinbox.menuservice.service;

import com.tiffinbox.menuservice.dto.*;
import com.tiffinbox.menuservice.entity.Menu;
import com.tiffinbox.menuservice.entity.MenuItem;
import com.tiffinbox.menuservice.entity.MenuStatus;
import com.tiffinbox.menuservice.exception.MenuClosedException;
import com.tiffinbox.menuservice.exception.ResourceNotFoundException;
import com.tiffinbox.menuservice.repository.MenuItemRepository;
import com.tiffinbox.menuservice.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;

    // ---------- Owner operations ----------

    @Transactional
    public Menu createMenu(CreateMenuRequest request) {
        Menu menu = Menu.builder()
                .date(request.date())
                .mealType(request.mealType())
                .description(request.description())
                .cutoffTime(request.cutoffTime())
                .status(MenuStatus.OPEN)
                .build();

        request.items().forEach(i -> menu.addItem(MenuItem.builder()
                .name(i.name())
                .description(i.description())
                .price(i.price())
                .available(true)
                .build()));

        return menuRepository.save(menu);
    }

    @Transactional
    public Menu closeMenu(Long id) {
        Menu menu = getMenu(id);
        menu.setStatus(MenuStatus.CLOSED);
        return menu;
    }

    @Transactional
    public Menu openMenu(Long id) {
        Menu menu = getMenu(id);
        menu.setStatus(MenuStatus.OPEN);
        return menu;
    }

    /** Edits an open menu's own fields (partial: null fields are left unchanged). */
    @Transactional
    public Menu updateMenu(Long id, UpdateMenuRequest request) {
        Menu menu = getMenu(id);
        requireEditable(menu);
        if (request.description() != null) {
            menu.setDescription(request.description());
        }
        if (request.cutoffTime() != null) {
            menu.setCutoffTime(request.cutoffTime());
        }
        return menu;
    }

    @Transactional
    public MenuItem addItem(Long menuId, CreateMenuItemRequest request) {
        Menu menu = getMenu(menuId);
        requireEditable(menu);
        MenuItem item = MenuItem.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .available(true)
                .build();
        menu.addItem(item);
        menuRepository.save(menu);
        return item;
    }

    @Transactional
    public MenuItem updateItem(Long itemId, UpdateMenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemId));
        requireEditable(item.getMenu());
        if (request.price() != null) {
            item.setPrice(request.price());
        }
        if (request.available() != null) {
            item.setAvailable(request.available());
        }
        return item;
    }

    @Transactional
    public void deleteItem(Long itemId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + itemId));
        requireEditable(item.getMenu());
        // Detach from the menu so orphanRemoval deletes it.
        item.getMenu().getItems().remove(item);
    }

    /** A menu can only be edited while OPEN; once CLOSED it's a historical record. */
    private void requireEditable(Menu menu) {
        if (menu.getStatus() != MenuStatus.OPEN) {
            throw new MenuClosedException(
                    "Menu " + menu.getId() + " is closed and can no longer be edited");
        }
    }

    // ---------- Read operations ----------

    @Transactional(readOnly = true)
    public Menu getMenu(Long id) {
        return menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Menu> getAllMenus() {
        return menuRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Menu> getTodaysOpenMenus() {
        return menuRepository.findByDateAndStatus(LocalDate.now(), MenuStatus.OPEN);
    }

    // ---------- Internal (Feign) operations ----------

    /**
     * Validates that the menu is open for ordering and every requested item belongs to
     * it and is available. Returns authoritative name/price snapshots for denormalization.
     */
    @Transactional(readOnly = true)
    public List<ValidatedItemResponse> validateItems(ValidateItemsRequest request) {
        Menu menu = getMenu(request.menuId());
        if (!isOrderingOpen(menu)) {
            throw new MenuClosedException("Menu " + menu.getId() + " is not open for ordering");
        }
        return request.itemIds().stream().map(itemId -> {
            MenuItem item = menu.getItems().stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Item " + itemId + " is not on menu " + menu.getId()));
            if (!item.isAvailable()) {
                throw new MenuClosedException("Item " + itemId + " is currently unavailable");
            }
            return ValidatedItemResponse.from(item);
        }).toList();
    }

    @Transactional(readOnly = true)
    public boolean isOpen(Long menuId) {
        return isOrderingOpen(getMenu(menuId));
    }

    /**
     * Live check: a menu accepts orders only while its status is OPEN and the current
     * time is at/before the cutoff on the menu's date.
     */
    private boolean isOrderingOpen(Menu menu) {
        if (menu.getStatus() != MenuStatus.OPEN) {
            return false;
        }
        LocalDateTime cutoff = menu.getDate().atTime(menu.getCutoffTime());
        return !LocalDateTime.now().isAfter(cutoff);
    }

    // ---------- Scheduler support ----------

    /** Closes every OPEN menu whose cutoff has passed. Returns how many were closed. */
    @Transactional
    public int closeExpiredMenus() {
        List<Menu> openMenus = menuRepository.findByStatus(MenuStatus.OPEN);
        int closed = 0;
        for (Menu menu : openMenus) {
            if (!isOrderingOpen(menu)) {
                menu.setStatus(MenuStatus.CLOSED);
                closed++;
            }
        }
        return closed;
    }
}

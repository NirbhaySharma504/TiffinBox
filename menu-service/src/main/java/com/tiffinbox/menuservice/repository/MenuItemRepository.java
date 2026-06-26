package com.tiffinbox.menuservice.repository;

import com.tiffinbox.menuservice.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
}

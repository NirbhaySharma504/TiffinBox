package com.tiffinbox.menuservice.repository;

import com.tiffinbox.menuservice.entity.Menu;
import com.tiffinbox.menuservice.entity.MenuStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    /*
     * @EntityGraph eager-loads `items` in the same query so the entities can be mapped
     * to DTOs after the transaction closes (open-in-view is disabled). Without this,
     * touching the lazy collection in the controller throws LazyInitializationException.
     */

    @EntityGraph(attributePaths = "items")
    List<Menu> findByDate(LocalDate date);

    @EntityGraph(attributePaths = "items")
    List<Menu> findByDateAndStatus(LocalDate date, MenuStatus status);

    List<Menu> findByStatus(MenuStatus status);

    @EntityGraph(attributePaths = "items")
    @Override
    Optional<Menu> findById(Long id);

    @EntityGraph(attributePaths = "items")
    @Override
    List<Menu> findAll();
}

package com.tiffinbox.subscriptionservice.service;

import com.tiffinbox.subscriptionservice.client.MenuClient;
import com.tiffinbox.subscriptionservice.client.OrderClient;
import com.tiffinbox.subscriptionservice.client.dto.MenuLite;
import com.tiffinbox.subscriptionservice.client.dto.OrderLite;
import com.tiffinbox.subscriptionservice.client.dto.PlaceOrderRequest;
import com.tiffinbox.subscriptionservice.dto.CreateSubscriptionRequest;
import com.tiffinbox.subscriptionservice.dto.SubscriptionSummaryResponse;
import com.tiffinbox.subscriptionservice.entity.Subscription;
import com.tiffinbox.subscriptionservice.entity.SubscriptionStatus;
import com.tiffinbox.subscriptionservice.exception.BadRequestException;
import com.tiffinbox.subscriptionservice.exception.ForbiddenException;
import com.tiffinbox.subscriptionservice.exception.ResourceNotFoundException;
import com.tiffinbox.subscriptionservice.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionWriter subscriptionWriter;
    private final MenuClient menuClient;
    private final OrderClient orderClient;

    // ---------- CRUD (local-only transactions) ----------

    @Transactional
    public Subscription create(CreateSubscriptionRequest req, Long userId, String email) {
        Subscription sub = Subscription.builder()
                .userId(userId)
                .customerEmail(email)
                .mealType(req.mealType())
                .frequency(req.frequency())
                .status(SubscriptionStatus.ACTIVE)
                .startDate(req.startDate() != null ? req.startDate() : LocalDate.now())
                .endDate(req.endDate())
                .build();
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public Subscription pause(Long id, Long userId) {
        Subscription sub = getOwned(id, userId);
        if (sub.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new BadRequestException("Cannot pause a cancelled subscription");
        }
        sub.setStatus(SubscriptionStatus.PAUSED);
        return sub;
    }

    @Transactional
    public Subscription resume(Long id, Long userId) {
        Subscription sub = getOwned(id, userId);
        if (sub.getStatus() == SubscriptionStatus.CANCELLED) {
            throw new BadRequestException("Cannot resume a cancelled subscription");
        }
        sub.setStatus(SubscriptionStatus.ACTIVE);
        return sub;
    }

    @Transactional
    public Subscription cancel(Long id, Long userId) {
        Subscription sub = getOwned(id, userId);
        sub.setStatus(SubscriptionStatus.CANCELLED);
        return sub;
    }

    // ---------- Reads ----------

    @Transactional(readOnly = true)
    public Subscription getOwned(Long id, Long userId) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
        if (!sub.getUserId().equals(userId)) {
            throw new ForbiddenException("You can only manage your own subscriptions");
        }
        return sub;
    }

    @Transactional(readOnly = true)
    public List<Subscription> getByUser(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getAll() {
        return subscriptionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public SubscriptionSummaryResponse summary() {
        return new SubscriptionSummaryResponse(
                subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE),
                subscriptionRepository.countByStatus(SubscriptionStatus.PAUSED),
                subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED));
    }

    // ---------- Auto-order orchestration (NO surrounding DB transaction) ----------

    /**
     * Places an auto-order for every subscription due today. Each subscription is handled
     * independently: a failure for one does not abort the rest. Remote calls (menu/order)
     * happen here, outside any transaction; only the per-subscription stamp is transactional.
     *
     * @return number of orders successfully placed
     */
    public int processDueSubscriptions() {
        LocalDate today = LocalDate.now();
        List<Subscription> active = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        List<MenuLite> menus = menuClient.getTodaysMenus(); // Feign (fallback: empty)

        int placed = 0;
        for (Subscription sub : active) {
            if (!sub.isDueOn(today)) {
                continue;
            }
            try {
                if (placeForSubscription(sub, menus, today)) {
                    placed++;
                }
            } catch (Exception ex) {
                log.error("Auto-order failed for subscription {}: {}", sub.getId(), ex.getMessage());
            }
        }
        log.info("Auto-order run complete: {} order(s) placed", placed);
        return placed;
    }

    private boolean placeForSubscription(Subscription sub, List<MenuLite> menus, LocalDate today) {
        MenuLite menu = menus.stream()
                .filter(m -> "OPEN".equals(m.status()))
                .filter(m -> sub.getMealType().name().equals(m.mealType()))
                .findFirst()
                .orElse(null);
        if (menu == null) {
            log.info("No open {} menu today for subscription {} — skipping",
                    sub.getMealType(), sub.getId());
            return false;
        }

        List<PlaceOrderRequest.OrderLine> lines = menu.items().stream()
                .filter(MenuLite.MenuItemLite::available)
                .map(i -> new PlaceOrderRequest.OrderLine(i.id(), 1))
                .toList();
        if (lines.isEmpty()) {
            log.info("Menu {} has no available items — skipping subscription {}", menu.id(), sub.getId());
            return false;
        }

        OrderLite order = orderClient.placeOrder(sub.getUserId(), sub.getCustomerEmail(),
                new PlaceOrderRequest(menu.id(), lines)); // Feign (fallback: null)
        if (order == null) {
            return false; // order-service unavailable; retried next run
        }

        subscriptionWriter.markOrdered(sub.getId(), today); // local tx
        log.info("Auto-placed order {} for subscription {} (user {})",
                order.id(), sub.getId(), sub.getUserId());
        return true;
    }
}

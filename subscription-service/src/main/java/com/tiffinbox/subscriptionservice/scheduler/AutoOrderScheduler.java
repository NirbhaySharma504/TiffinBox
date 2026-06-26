package com.tiffinbox.subscriptionservice.scheduler;

import com.tiffinbox.subscriptionservice.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Fires once a day (cron from {@code subscription.scheduler.cron}) and auto-places orders
 * for every active subscription due that day. The actual work lives in SubscriptionService
 * so it can also be triggered on-demand (owner endpoint) for testing/demo.
 */
@Component
@RequiredArgsConstructor
public class AutoOrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutoOrderScheduler.class);

    private final SubscriptionService subscriptionService;

    @Scheduled(cron = "${subscription.scheduler.cron}")
    public void runDailyAutoOrders() {
        log.info("Daily auto-order scheduler firing");
        subscriptionService.processDueSubscriptions();
    }
}

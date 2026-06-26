# TiffinBox — Microservices Engineering Plan
## Digitizing a WhatsApp Tiffin Business into a Full-Stack Platform

---

## 1. PROBLEM STATEMENT

A local tiffin business currently operates entirely on WhatsApp:
- Owner posts daily menu to a WhatsApp group (inconsistent format, buried in chat)
- No fixed order deadline — orders arrive randomly throughout the day
- Customers DM the owner personally to place orders (untracked, manual)
- No payment tracking — cash/UPI confirmed manually
- No order status updates — customer has no idea when food is coming
- Owner has no data: no order counts before cooking, no customer history

This project replaces that WhatsApp workflow with a structured platform
while adding features expected from modern food apps.

---

## 2. FUNCTIONAL REQUIREMENTS

### Customer-Facing
FR-01  Customer can register, login, and manage their profile and delivery address
FR-02  Customer can view today's active menu with items, prices, and availability
FR-03  Customer can place an order before the daily cutoff time
FR-04  Customer receives an error if they try to order after cutoff
FR-05  Customer can track order status in real-time (PLACED → DELIVERED)
FR-06  Customer receives email notifications on every order status change
FR-07  Customer can view full order history
FR-08  Customer receives an AI-powered meal recommendation based on order history
FR-09  Customer can search the menu using natural language ("light vegetarian option")

### Owner-Facing
FR-10  Owner logs in with a separate role (ROLE_OWNER)
FR-11  Owner can create today's menu with items, prices, quantity, and cutoff time
FR-12  Owner can reuse a previous day's menu as a template
FR-13  Owner can update order status from a dashboard (confirm, dispatch, deliver)
FR-14  Owner sees today's order list with payment status per order
FR-15  Owner can mark an order as paid (cash received)
FR-16  Owner sees daily summary: total orders, total revenue, unpaid count

### Subscription (Phase 2 — add only after Phase 1 is complete)
FR-17  Customer can subscribe to a weekly meal plan (select days + slot)
FR-18  System auto-places orders daily for active subscribers at a scheduled time
FR-19  Owner sees subscriber count and projected order count before cooking
FR-20  Customer can pause, resume, or cancel their subscription

---

## 3. NON-FUNCTIONAL REQUIREMENTS

NFR-01  JWT-based stateless authentication — no session state in any service
NFR-02  API Gateway is the single entry point — no direct external access to services
NFR-03  Services communicate internally via Feign — no hardcoded URLs
NFR-04  Every service registers with Eureka on startup
NFR-05  Each service has its own PostgreSQL database — no shared databases
NFR-06  Payment service is a mock — no real payment gateway integration
NFR-07  Notification service uses Gmail SMTP — no paid SMS/push service
NFR-08  All services containerized with Docker — full stack via Docker Compose
NFR-09  No secrets hardcoded — all config via environment variables
NFR-10  All APIs documented via Swagger/OpenAPI on each service
NFR-11  React frontend communicates only with API Gateway, never directly with services
NFR-12  Feign clients have fallback behavior for service unavailability (Resilience4j)

---

## 4. SYSTEM ARCHITECTURE OVERVIEW

```
[React Frontend]
      |
      | HTTPS
      v
[API Gateway :8080]  ← Spring Cloud Gateway
      |
      | Routes requests to services by path prefix
      |
      |── /api/users/**      → [user-service      :8081]
      |── /api/menu/**       → [menu-service       :8082]
      |── /api/orders/**     → [order-service      :8083]
      |── /api/payments/**   → [payment-service    :8084]
      |── /api/notifications/**  → [notification-service :8085]
      └── /api/ai/**         → [ai-service         :8086]

All services register with:
[Eureka Server :8761]

Internal Feign calls (service-to-service):
order-service       → menu-service       (validate item, check cutoff)
order-service       → payment-service    (check payment status)
order-service       → notification-service (status change)
ai-service          → order-service      (get order history)
ai-service          → menu-service       (get today's menu)

Each service has its own PostgreSQL database:
user-service      → userdb      :5432
menu-service      → menudb      :5433
order-service     → orderdb     :5434
payment-service   → paymentdb   :5435
notification-service → notifdb  :5436
ai-service        → no DB (stateless)
```

---

## 5. TECHNOLOGY STACK — EVERY DECISION JUSTIFIED

### Backend: Spring Boot 3.x + Java 21

  Why Java 21?
    Latest LTS version. Virtual threads (Project Loom) improve throughput under load.
    Spring Boot 3.x requires Java 17+ anyway. Use 21 for the resume keyword.

  Why Spring Boot 3.x specifically?
    Native Spring Cloud integration. Jakarta EE namespace (not javax).
    Spring Security 6.x with updated lambda-style security config.
    Course curriculum aligns with this version.

### Service Discovery: Netflix Eureka (Spring Cloud Netflix)

  Why?
    Industry standard for Spring-based microservices.
    Feign clients resolve service names through Eureka automatically.
    Every interviewer knows Eureka. No explanation needed.
    Alternative (Consul, Kubernetes DNS) is overkill for a student project.

### API Gateway: Spring Cloud Gateway

  Why not Nginx?
    Spring Cloud Gateway integrates natively with Eureka for load balancing.
    Route configuration in application.yml — no separate server config.
    JWT validation filter can be added as a GatewayFilter in one class.

### Inter-service Communication: OpenFeign

  Why not RestTemplate or WebClient?
    RestTemplate is deprecated in Spring Boot 3.x.
    WebClient is reactive — adds complexity without benefit at this scale.
    Feign is declarative (interface + annotations), readable, integrates
    with Eureka and Resilience4j out of the box.

### Circuit Breaker: Resilience4j

  Why?
    When menu-service is down, order-service must not crash.
    @CircuitBreaker annotation on Feign clients provides automatic fallback.
    One annotation, one fallback method — minimal code, maximum interview impact.
    Interviewers at product companies always ask "what happens when a dependent
    service is unavailable?" This is your answer.

### Database: PostgreSQL (one instance per service)

  Why PostgreSQL over MySQL?
    Better JSON support (JSONB) — useful for storing menu item metadata.
    Stronger ACID compliance. Better support in Spring Data JPA.
    Industry standard at most product companies.

  Why one database per service?
    This is the defining microservices principle. Services that share a database
    are not microservices — they are a distributed monolith.
    Each service owns its data. No cross-service JOINs. Data consistency achieved
    through API calls, not database relationships.
    Interviewers will specifically ask "do your services share a database?" —
    the correct answer is no.

  In Docker Compose: multiple PostgreSQL containers, one per service.
  In production: this would be separate RDS instances per service.

### Frontend: React 18 + Vite + Tailwind CSS

  Why Vite over Create React App?
    CRA is deprecated. Vite is the current standard. Faster dev server.

  Why Tailwind?
    No CSS file maintenance. Consistent styling. Faster UI development.
    For a backend-focused student, Tailwind means you spend 20% of time on
    CSS instead of 60%.

  State management: React Context API + useState
  Why not Redux?
    Redux is overkill for this app's complexity. Context + useState is sufficient
    and avoids spending a week learning Redux patterns.

  HTTP client: Axios
  Why?
    Better than fetch for interceptors (add JWT header automatically on every request).
    Cleaner error handling.

### Authentication: Spring Security 6 + JWT (JJWT library)

  Architecture decision:
    JWT is issued by user-service on login.
    API Gateway validates JWT on every incoming request using a custom GatewayFilter.
    If token is invalid, Gateway rejects at the edge — services never see invalid requests.
    Services trust requests that reach them (Gateway has already validated).
    Services extract user ID and role from JWT claims in the request header
    (Gateway forwards validated claims as X-User-Id and X-User-Role headers).

  Why this approach?
    Eliminates the need for every service to implement JWT validation independently.
    Single point of auth enforcement. Cleaner service code.

### Email Notifications: JavaMailSender + Gmail SMTP

  Why not SendGrid or AWS SES?
    Both require account setup and have costs beyond free tier.
    Gmail SMTP is free for development volumes. Zero setup beyond a Google account.
    In production you would swap to SES — mention this in interviews.

  Configuration: Gmail account + App Password (not your real password).

### Spring AI: Ollama + Llama 3.1 8B

  Why Ollama again?
    Same reasoning as AI Research Assistant — free, local, no API key in GitHub.
    Spring AI's Ollama integration is a single dependency + 3 lines of config.

### Containerization: Docker + Docker Compose

  Every service gets a Dockerfile.
  docker-compose.yml brings up: Eureka, Gateway, all 6 services, all 5 PostgreSQL instances.
  One command: docker-compose up --build

---

## 6. DATABASE SCHEMAS

### user-service → userdb

```sql
-- users table
CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(100)        NOT NULL,
    email         VARCHAR(150) UNIQUE NOT NULL,
    password_hash VARCHAR(255)        NOT NULL,
    phone         VARCHAR(15),
    role          VARCHAR(20)         NOT NULL DEFAULT 'ROLE_CUSTOMER',
    created_at    TIMESTAMP           NOT NULL DEFAULT NOW(),
    is_active     BOOLEAN             NOT NULL DEFAULT TRUE
);

-- addresses table
CREATE TABLE addresses (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users(id),
    label       VARCHAR(50),              -- "Home", "Office"
    full_address TEXT        NOT NULL,
    pincode     VARCHAR(10),
    is_default  BOOLEAN      NOT NULL DEFAULT FALSE
);
```

### menu-service → menudb

```sql
-- menus table (one per day per slot)
CREATE TABLE menus (
    id           BIGSERIAL PRIMARY KEY,
    slot         VARCHAR(10)  NOT NULL,   -- 'LUNCH' or 'DINNER'
    menu_date    DATE         NOT NULL,
    cutoff_time  TIME         NOT NULL,   -- e.g. 10:30:00
    is_active    BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    UNIQUE(slot, menu_date)               -- one menu per slot per day
);

-- menu_items table
CREATE TABLE menu_items (
    id              BIGSERIAL PRIMARY KEY,
    menu_id         BIGINT       NOT NULL REFERENCES menus(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    description     TEXT,
    price           NUMERIC(8,2) NOT NULL,
    is_vegetarian   BOOLEAN      NOT NULL DEFAULT TRUE,
    quantity_available  INTEGER,          -- NULL means unlimited
    quantity_ordered    INTEGER  NOT NULL DEFAULT 0,
    image_url       VARCHAR(500),
    is_available    BOOLEAN      NOT NULL DEFAULT TRUE
);
```

### order-service → orderdb

```sql
-- orders table
CREATE TABLE orders (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,   -- from user-service (no FK — different DB)
    menu_id         BIGINT       NOT NULL,   -- from menu-service (no FK — different DB)
    slot            VARCHAR(10)  NOT NULL,
    order_date      DATE         NOT NULL,
    status          VARCHAR(30)  NOT NULL DEFAULT 'PLACED',
    -- PLACED | CONFIRMED | PREPARING | OUT_FOR_DELIVERY | DELIVERED | CANCELLED
    total_amount    NUMERIC(8,2) NOT NULL,
    delivery_address TEXT        NOT NULL,
    special_instructions TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- order_items table
CREATE TABLE order_items (
    id          BIGSERIAL PRIMARY KEY,
    order_id    BIGINT       NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id    BIGINT   NOT NULL,   -- from menu-service (no FK — different DB)
    item_name   VARCHAR(100) NOT NULL,   -- denormalized: store name at time of order
    item_price  NUMERIC(8,2) NOT NULL,   -- denormalized: store price at time of order
    quantity    INTEGER      NOT NULL DEFAULT 1
);

-- WHY denormalize item_name and item_price?
-- menu-service can change prices tomorrow. Order records must reflect
-- what was charged at time of ordering. Never join across service databases.
```

### payment-service → paymentdb

```sql
-- payments table
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    order_id        BIGINT       NOT NULL UNIQUE,  -- one payment per order
    customer_id     BIGINT       NOT NULL,
    amount          NUMERIC(8,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    -- PENDING | PAID | FAILED | REFUNDED
    payment_method  VARCHAR(20),    -- 'CASH' | 'UPI' (mock)
    paid_at         TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

### notification-service → notifdb

```sql
-- notifications table (audit log)
CREATE TABLE notifications (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    email           VARCHAR(150) NOT NULL,
    type            VARCHAR(50)  NOT NULL,
    -- ORDER_PLACED | ORDER_CONFIRMED | ORDER_OUT_FOR_DELIVERY |
    -- ORDER_DELIVERED | ORDER_CANCELLED
    subject         VARCHAR(255) NOT NULL,
    body            TEXT         NOT NULL,
    sent_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    is_sent         BOOLEAN      NOT NULL DEFAULT FALSE,
    error_message   TEXT         -- populated if sending failed
);
```

### subscription-service → subscriptiondb (Phase 2)

```sql
-- subscriptions table
CREATE TABLE subscriptions (
    id              BIGSERIAL PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    slot            VARCHAR(10)  NOT NULL,   -- 'LUNCH' or 'DINNER'
    days_of_week    VARCHAR(50)  NOT NULL,   -- 'MON,TUE,WED,THU,FRI' (CSV)
    delivery_address TEXT        NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    -- ACTIVE | PAUSED | CANCELLED | EXPIRED
    start_date      DATE         NOT NULL,
    end_date        DATE,                    -- NULL means ongoing
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- subscription_logs table
CREATE TABLE subscription_logs (
    id              BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT       NOT NULL REFERENCES subscriptions(id),
    log_date        DATE         NOT NULL,
    order_id        BIGINT,                  -- placed order ID (NULL if failed)
    status          VARCHAR(20)  NOT NULL,   -- 'ORDER_PLACED' | 'SKIPPED' | 'FAILED'
    reason          TEXT,                    -- populated if SKIPPED or FAILED
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);
```

---

## 7. API DESIGN — ALL ENDPOINTS

### user-service (:8081)

```
POST   /api/users/register
  Body: {name, email, password, phone}
  Returns: {userId, email, role, token}

POST   /api/users/login
  Body: {email, password}
  Returns: {token, userId, name, role}

GET    /api/users/profile
  Auth: Bearer token
  Returns: {userId, name, email, phone, addresses}

PUT    /api/users/profile
  Auth: Bearer token
  Body: {name, phone}

POST   /api/users/addresses
  Auth: Bearer token
  Body: {label, fullAddress, pincode, isDefault}

PUT    /api/users/addresses/{id}
DELETE /api/users/addresses/{id}

GET    /api/users/{id}               ← internal only (called by other services)
  Returns: {userId, name, email, phone}
```

### menu-service (:8082)

```
-- OWNER ENDPOINTS (ROLE_OWNER required)

POST   /api/menu
  Auth: Bearer token (ROLE_OWNER)
  Body: {slot, menuDate, cutoffTime}
  Returns: {menuId, slot, menuDate, cutoffTime, isActive}

POST   /api/menu/{menuId}/items
  Auth: Bearer token (ROLE_OWNER)
  Body: {name, description, price, isVegetarian, quantityAvailable, imageUrl}

PUT    /api/menu/{menuId}/items/{itemId}
DELETE /api/menu/{menuId}/items/{itemId}

PUT    /api/menu/{menuId}/activate     ← make menu live
PUT    /api/menu/{menuId}/deactivate

GET    /api/menu/history?page=0&size=10  ← past menus for reuse as template

-- CUSTOMER ENDPOINTS

GET    /api/menu/today?slot=LUNCH
  Returns: {menuId, slot, cutoffTime, isOpen, items: [...]}
  isOpen: true if current time < cutoffTime

GET    /api/menu/{menuId}
GET    /api/menu/{menuId}/items/{itemId}

-- INTERNAL FEIGN ENDPOINTS

GET    /api/menu/internal/{menuId}/validate-items
  Body: [{itemId, quantity}]
  Returns: {valid: true/false, errors: [...], totalAmount: 0.00}
  Called by: order-service before placing order

GET    /api/menu/internal/{menuId}/is-open
  Called by: order-service, subscription-service
  Returns: {isOpen: true/false, cutoffTime: "10:30"}
```

### order-service (:8083)

```
-- CUSTOMER ENDPOINTS

POST   /api/orders
  Auth: Bearer token (ROLE_CUSTOMER)
  Body: {menuId, slot, deliveryAddressId, items: [{itemId, quantity}],
         specialInstructions}
  Action: 1) Validate items via menu-service Feign call
          2) Check cutoff via menu-service Feign call
          3) Create order + order_items
          4) Create payment record via payment-service Feign call
          5) Send notification via notification-service Feign call
  Returns: {orderId, status, totalAmount, estimatedTime}

GET    /api/orders/{orderId}
  Auth: Bearer token
  Returns: full order with items and current status

GET    /api/orders/my-orders?page=0&size=10
  Auth: Bearer token (ROLE_CUSTOMER)
  Returns: paginated order history

-- OWNER ENDPOINTS

GET    /api/orders/today
  Auth: Bearer token (ROLE_OWNER)
  Returns: all orders for today with customer info and payment status

GET    /api/orders/summary/today
  Auth: Bearer token (ROLE_OWNER)
  Returns: {totalOrders, totalRevenue, paidCount, unpaidCount, bySlot: {...}}

PUT    /api/orders/{orderId}/status
  Auth: Bearer token (ROLE_OWNER)
  Body: {status: "CONFIRMED"}
  Action: update status + trigger notification via notification-service

POST   /api/orders/{orderId}/cancel
  Auth: Bearer token

-- INTERNAL FEIGN ENDPOINTS

GET    /api/orders/internal/customer/{customerId}/recent?limit=10
  Called by: ai-service
  Returns: last N orders with item names
```

### payment-service (:8084)

```
-- INTERNAL (called by order-service)

POST   /api/payments/create
  Body: {orderId, customerId, amount}
  Returns: {paymentId, orderId, status: "PENDING"}

GET    /api/payments/order/{orderId}
  Returns: {paymentId, status, amount, paidAt}

-- OWNER ENDPOINTS

PUT    /api/payments/{paymentId}/mark-paid
  Auth: Bearer token (ROLE_OWNER)
  Body: {paymentMethod: "CASH"}
  Returns: {paymentId, status: "PAID", paidAt}

GET    /api/payments/unpaid-today
  Auth: Bearer token (ROLE_OWNER)
  Returns: list of unpaid orders with customer info

GET    /api/payments/summary/today
  Auth: Bearer token (ROLE_OWNER)
  Returns: {totalCollected, totalPending, transactionCount}
```

### notification-service (:8085)

```
-- INTERNAL (called by order-service)

POST   /api/notifications/order-placed
  Body: {orderId, customerId, customerEmail, customerName, orderItems, totalAmount, slot}

POST   /api/notifications/order-status-changed
  Body: {orderId, customerId, customerEmail, customerName, newStatus}

-- INTERNAL (called by subscription-service — Phase 2)

POST   /api/notifications/subscription-order-placed
  Body: {subscriptionId, customerId, customerEmail, orderId}

POST   /api/notifications/subscription-expiring
  Body: {subscriptionId, customerId, customerEmail, expiryDate}

-- ADMIN

GET    /api/notifications/history/{userId}
  Auth: Bearer token (ROLE_OWNER)
```

### ai-service (:8086)

```
GET    /api/ai/recommendations?customerId={id}
  Auth: Bearer token
  Action: 1) Fetch last 10 orders from order-service via Feign
          2) Fetch today's menu from menu-service via Feign
          3) Build prompt with order history + menu
          4) Call Ollama via Spring AI
          5) Return 2-3 recommended items
  Returns: {recommendations: [{itemId, name, reason}]}

POST   /api/ai/menu-search
  Auth: Bearer token
  Body: {query: "light vegetarian under 100 rupees", menuId}
  Action: Fetch menu items, pass to Spring AI with natural language query
  Returns: {matches: [{itemId, name, price, matchReason}]}
```

### subscription-service (:8087) — Phase 2

```
POST   /api/subscriptions
  Auth: Bearer token (ROLE_CUSTOMER)
  Body: {slot, daysOfWeek, deliveryAddressId, startDate, endDate}

GET    /api/subscriptions/my
  Auth: Bearer token
  Returns: customer's active subscription

PUT    /api/subscriptions/{id}/pause
PUT    /api/subscriptions/{id}/resume
DELETE /api/subscriptions/{id}

GET    /api/subscriptions/admin/summary
  Auth: Bearer token (ROLE_OWNER)
  Returns: {activeCount, projectedOrdersToday, bySlot: {LUNCH: 12, DINNER: 5}}
```

---

## 8. JWT ARCHITECTURE IN DETAIL

This is the most important cross-cutting concern. Understand it completely.

### Flow

```
1. Customer sends POST /api/users/login
2. API Gateway forwards to user-service (no auth check on /login route)
3. user-service validates credentials, generates JWT:
   - Subject: userId
   - Claims: {role: "ROLE_CUSTOMER", email: "user@example.com", name: "Ravi"}
   - Expiry: 24 hours
   - Signed with HS256 + secret key from environment variable
4. JWT returned to React frontend
5. React stores JWT in memory (NOT localStorage — XSS risk)
   Use React Context + httpOnly cookie or memory state

6. Customer sends GET /api/orders/my-orders
   Header: Authorization: Bearer <token>
7. API Gateway AuthFilter intercepts:
   - Extract token from header
   - Validate signature + expiry
   - If invalid: return 401 immediately (service never sees this request)
   - If valid: extract claims, add headers:
       X-User-Id: 42
       X-User-Role: ROLE_CUSTOMER
       X-User-Email: user@example.com
8. Gateway forwards request to order-service WITH those headers
9. order-service reads X-User-Id and X-User-Role from headers
   NO JWT validation in order-service — it trusts the Gateway
```

### Gateway Auth Filter (critical code pattern)

```java
// gateway/src/main/java/com/tiffinbox/gateway/filter/AuthFilter.java

@Component
public class AuthFilter implements GatewayFilter, Ordered {

    // Routes that do NOT require authentication
    private static final List<String> PUBLIC_ROUTES = List.of(
        "/api/users/login",
        "/api/users/register",
        "/api/menu/today",    // browsing menu doesn't require login
        "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (PUBLIC_ROUTES.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);  // skip auth
        }

        String authHeader = exchange.getRequest()
            .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-User-Id", claims.getSubject())
                .header("X-User-Role", claims.get("role", String.class))
                .header("X-User-Email", claims.get("email", String.class))
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (JwtException e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() { return -1; }  // run before all other filters
}
```

### How services use the forwarded headers

```java
// In any service controller

@GetMapping("/my-orders")
public ResponseEntity<List<OrderResponse>> getMyOrders(
    @RequestHeader("X-User-Id") Long userId,           // injected by Gateway
    @RequestHeader("X-User-Role") String role,
    @RequestParam(defaultValue = "0") int page
) {
    return ResponseEntity.ok(orderService.getOrdersByCustomer(userId, page));
}
```

---

## 9. FEIGN CLIENT DESIGN WITH RESILIENCE4J

### Example: order-service calling menu-service

```java
// order-service/src/main/java/com/tiffinbox/order/client/MenuClient.java

@FeignClient(
    name = "menu-service",            // must match spring.application.name in menu-service
    fallbackFactory = MenuClientFallbackFactory.class
)
public interface MenuClient {

    @GetMapping("/api/menu/internal/{menuId}/validate-items")
    MenuValidationResponse validateItems(
        @PathVariable Long menuId,
        @RequestBody List<OrderItemRequest> items
    );

    @GetMapping("/api/menu/internal/{menuId}/is-open")
    MenuStatusResponse isMenuOpen(@PathVariable Long menuId);
}

// Fallback factory — what happens when menu-service is down
@Component
public class MenuClientFallbackFactory
    implements FallbackFactory<MenuClient> {

    @Override
    public MenuClient create(Throwable cause) {
        return new MenuClient() {
            @Override
            public MenuValidationResponse validateItems(Long menuId,
                List<OrderItemRequest> items) {
                // Do NOT silently succeed — fail the order
                throw new ServiceUnavailableException(
                    "Menu service is currently unavailable. Please try again.");
            }

            @Override
            public MenuStatusResponse isMenuOpen(Long menuId) {
                // When in doubt, assume ordering is closed (safer for the owner)
                return new MenuStatusResponse(false, "Unavailable");
            }
        };
    }
}
```

### application.yml configuration for Feign + Resilience4j

```yaml
# In order-service application.yml
feign:
  circuitbreaker:
    enabled: true

resilience4j:
  circuitbreaker:
    instances:
      menu-service:
        slidingWindowSize: 10
        failureRateThreshold: 50        # open circuit after 50% failures
        waitDurationInOpenState: 10s    # try again after 10 seconds
        permittedNumberOfCallsInHalfOpenState: 3
      notification-service:
        slidingWindowSize: 5
        failureRateThreshold: 60
        waitDurationInOpenState: 15s
```

---

## 10. ORDER PLACEMENT FLOW (most complex flow — know every step)

This is the flow an interviewer will ask you to walk through end-to-end.

```
Customer clicks "Place Order" in React UI

Step 1: React sends POST /api/orders
        Headers: Authorization: Bearer <jwt>
        Body: {menuId, slot, deliveryAddressId, items: [{itemId:1, qty:2}, ...]}

Step 2: API Gateway
        - Validates JWT
        - Adds X-User-Id, X-User-Role headers
        - Routes to order-service:8083

Step 3: order-service OrderController.placeOrder()
        - Extracts userId from X-User-Id header

Step 4: order-service calls menu-service via Feign
        GET /api/menu/internal/{menuId}/is-open
        If false: throw OrderCutoffPassedException (400)

Step 5: order-service calls menu-service via Feign
        GET /api/menu/internal/{menuId}/validate-items
        Sends: [{itemId: 1, quantity: 2}]
        Receives: {valid: true, errors: [], totalAmount: 240.00}
        If invalid: throw InvalidMenuItemException (400)

Step 6: order-service creates Order entity
        status = PLACED, totalAmount = 240.00

Step 7: order-service creates OrderItem entities
        Stores item name and price at time of order (denormalized)

Step 8: order-service calls payment-service via Feign
        POST /api/payments/create
        Body: {orderId: 55, customerId: 42, amount: 240.00}
        Payment record created with status = PENDING

Step 9: order-service calls notification-service via Feign
        POST /api/notifications/order-placed
        Body: {orderId, customerId, customerEmail, ...}
        notification-service sends email asynchronously

Step 10: order-service returns 201 Created
         Body: {orderId: 55, status: "PLACED", totalAmount: 240.00}

Step 11: React shows order confirmation screen
```

What happens if any step fails:
- Step 4/5 fail (menu-service down): Feign fallback → 503 Service Unavailable
- Step 6/7 fail (DB error): Transaction rolls back, nothing is created
- Step 8 fails (payment-service down): Order is still created, payment status = PENDING
  (non-fatal — owner can mark as paid manually)
- Step 9 fails (notification-service down): Order is still created, email not sent
  (non-fatal — notification is a convenience, not a requirement)

This failure tolerance design is what you explain in interviews.
Steps 8 and 9 are fire-and-forget in the sense that their failure does not
roll back the order. Step 4 and 5 are blocking — order cannot proceed without them.

---

## 11. PROJECT STRUCTURE

```
tiffinbox/
│
├── eureka-server/
│   └── src/main/
│       ├── java/com/tiffinbox/eureka/EurekaServerApplication.java
│       └── resources/application.yml
│
├── api-gateway/
│   └── src/main/
│       ├── java/com/tiffinbox/gateway/
│       │   ├── ApiGatewayApplication.java
│       │   └── filter/AuthFilter.java
│       └── resources/application.yml    ← route definitions here
│
├── user-service/
│   └── src/main/java/com/tiffinbox/user/
│       ├── UserServiceApplication.java
│       ├── controller/
│       │   ├── AuthController.java
│       │   └── UserController.java
│       ├── service/
│       │   ├── UserService.java
│       │   └── JwtService.java
│       ├── repository/
│       │   ├── UserRepository.java
│       │   └── AddressRepository.java
│       ├── model/entity/
│       │   ├── User.java
│       │   └── Address.java
│       ├── model/dto/
│       │   ├── LoginRequest.java
│       │   ├── RegisterRequest.java
│       │   ├── LoginResponse.java
│       │   └── UserProfileResponse.java
│       ├── security/
│       │   └── SecurityConfig.java
│       └── exception/
│           ├── GlobalExceptionHandler.java
│           └── UserNotFoundException.java
│
├── menu-service/
│   └── src/main/java/com/tiffinbox/menu/
│       ├── MenuServiceApplication.java
│       ├── controller/
│       │   ├── MenuController.java
│       │   └── MenuInternalController.java   ← internal Feign endpoints
│       ├── service/MenuService.java
│       ├── repository/
│       │   ├── MenuRepository.java
│       │   └── MenuItemRepository.java
│       ├── model/entity/
│       │   ├── Menu.java
│       │   └── MenuItem.java
│       ├── model/dto/ ...
│       └── scheduler/MenuCutoffScheduler.java  ← @Scheduled task
│
├── order-service/
│   └── src/main/java/com/tiffinbox/order/
│       ├── OrderServiceApplication.java
│       ├── controller/
│       │   ├── OrderController.java
│       │   └── OrderInternalController.java
│       ├── service/OrderService.java
│       ├── repository/
│       │   ├── OrderRepository.java
│       │   └── OrderItemRepository.java
│       ├── model/entity/
│       │   ├── Order.java
│       │   └── OrderItem.java
│       ├── model/dto/ ...
│       ├── client/
│       │   ├── MenuClient.java
│       │   ├── MenuClientFallbackFactory.java
│       │   ├── PaymentClient.java
│       │   ├── PaymentClientFallbackFactory.java
│       │   ├── NotificationClient.java
│       │   └── NotificationClientFallbackFactory.java
│       └── exception/ ...
│
├── payment-service/
│   └── src/main/java/com/tiffinbox/payment/
│       (similar structure — simpler, no outbound Feign calls)
│
├── notification-service/
│   └── src/main/java/com/tiffinbox/notification/
│       ├── controller/NotificationController.java
│       ├── service/
│       │   ├── NotificationService.java
│       │   └── EmailService.java          ← JavaMailSender wrapper
│       ├── model/entity/Notification.java
│       └── model/dto/ ...
│
├── ai-service/
│   └── src/main/java/com/tiffinbox/ai/
│       ├── AiServiceApplication.java
│       ├── controller/AiController.java
│       ├── service/AiService.java
│       ├── client/
│       │   ├── OrderClient.java
│       │   └── MenuClient.java
│       └── config/SpringAiConfig.java
│
├── subscription-service/  ← Phase 2
│   └── src/main/java/com/tiffinbox/subscription/
│       ├── controller/SubscriptionController.java
│       ├── service/SubscriptionService.java
│       ├── repository/ ...
│       ├── model/ ...
│       ├── client/
│       │   ├── OrderClient.java
│       │   ├── MenuClient.java
│       │   └── NotificationClient.java
│       └── scheduler/
│           └── AutoOrderScheduler.java     ← @Scheduled, runs daily at 8AM
│
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   │   └── axios.js        ← axios instance with JWT interceptor
│   │   ├── context/
│   │   │   └── AuthContext.jsx
│   │   ├── pages/
│   │   │   ├── Login.jsx
│   │   │   ├── Register.jsx
│   │   │   ├── Menu.jsx        ← browse today's menu
│   │   │   ├── Cart.jsx
│   │   │   ├── OrderHistory.jsx
│   │   │   ├── OrderDetail.jsx ← track order status
│   │   │   └── owner/
│   │   │       ├── OwnerDashboard.jsx
│   │   │       ├── MenuManagement.jsx
│   │   │       ├── OrderManagement.jsx
│   │   │       └── PaymentManagement.jsx
│   │   ├── components/
│   │   │   ├── Navbar.jsx
│   │   │   ├── MenuItemCard.jsx
│   │   │   ├── OrderStatusBadge.jsx
│   │   │   └── AiRecommendations.jsx
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
│
├── docker-compose.yml
├── docker-compose.dev.yml      ← without ai-service for faster dev startup
├── .env.example
└── README.md
```

---

## 12. ENVIRONMENT VARIABLES (.env.example)

```
# JWT
JWT_SECRET=your-256-bit-secret-key-here-minimum-32-characters
JWT_EXPIRY_MS=86400000

# Eureka
EUREKA_SERVER_URL=http://eureka-server:8761/eureka/

# PostgreSQL — one block per service
USERDB_URL=jdbc:postgresql://userdb:5432/userdb
USERDB_USER=postgres
USERDB_PASS=postgres

MENUDB_URL=jdbc:postgresql://menudb:5432/menudb
MENUDB_USER=postgres
MENUDB_PASS=postgres

ORDERDB_URL=jdbc:postgresql://orderdb:5432/orderdb
ORDERDB_USER=postgres
ORDERDB_PASS=postgres

PAYMENTDB_URL=jdbc:postgresql://paymentdb:5432/paymentdb
PAYMENTDB_USER=postgres
PAYMENTDB_PASS=postgres

NOTIFDB_URL=jdbc:postgresql://notifdb:5432/notifdb
NOTIFDB_USER=postgres
NOTIFDB_PASS=postgres

# Email (Gmail SMTP)
MAIL_USERNAME=your-gmail@gmail.com
MAIL_PASSWORD=your-app-password      # NOT your real Gmail password — use App Password

# Spring AI / Ollama
OLLAMA_BASE_URL=http://ollama:11434
OLLAMA_MODEL=llama3.1:8b

# Frontend
VITE_API_BASE_URL=http://localhost:8080
```

---

## 13. DOCKER COMPOSE

```yaml
version: "3.9"

services:

  # ─── INFRASTRUCTURE ───────────────────────────────

  eureka-server:
    build: ./eureka-server
    ports:
      - "8761:8761"
    environment:
      - EUREKA_SERVER_URL=${EUREKA_SERVER_URL}
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 10s
      retries: 5

  api-gateway:
    build: ./api-gateway
    ports:
      - "8080:8080"
    environment:
      - EUREKA_SERVER_URL=${EUREKA_SERVER_URL}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      eureka-server:
        condition: service_healthy

  ollama:
    image: ollama/ollama
    ports:
      - "11434:11434"
    volumes:
      - ollama_data:/root/.ollama

  # ─── DATABASES ────────────────────────────────────

  userdb:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: userdb
      POSTGRES_USER: ${USERDB_USER}
      POSTGRES_PASSWORD: ${USERDB_PASS}
    volumes:
      - userdb_data:/var/lib/postgresql/data

  menudb:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: menudb
      POSTGRES_USER: ${MENUDB_USER}
      POSTGRES_PASSWORD: ${MENUDB_PASS}
    volumes:
      - menudb_data:/var/lib/postgresql/data

  orderdb:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: orderdb
      POSTGRES_USER: ${ORDERDB_USER}
      POSTGRES_PASSWORD: ${ORDERDB_PASS}
    volumes:
      - orderdb_data:/var/lib/postgresql/data

  paymentdb:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: paymentdb
      POSTGRES_USER: ${PAYMENTDB_USER}
      POSTGRES_PASSWORD: ${PAYMENTDB_PASS}
    volumes:
      - paymentdb_data:/var/lib/postgresql/data

  notifdb:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: notifdb
      POSTGRES_USER: ${NOTIFDB_USER}
      POSTGRES_PASSWORD: ${NOTIFDB_PASS}
    volumes:
      - notifdb_data:/var/lib/postgresql/data

  # ─── SERVICES ─────────────────────────────────────

  user-service:
    build: ./user-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=${USERDB_URL}
      - SPRING_DATASOURCE_USERNAME=${USERDB_USER}
      - SPRING_DATASOURCE_PASSWORD=${USERDB_PASS}
      - EUREKA_SERVER_URL=${EUREKA_SERVER_URL}
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      - userdb
      - eureka-server

  menu-service:
    build: ./menu-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_DATASOURCE_URL=${MENUDB_URL}
      - SPRING_DATASOURCE_USERNAME=${MENUDB_USER}
      - SPRING_DATASOURCE_PASSWORD=${MENUDB_PASS}
      - EUREKA_SERVER_URL=${EUREKA_SERVER_URL}
    depends_on:
      - menudb
      - eureka-server

  order-service:
    build: ./order-service
    ports:
      - "8083:8083"
    environment:
      - SPRING_DATASOURCE_URL=${ORDERDB_URL}
      - SPRING_DATASOURCE_USERNAME=${ORDERDB_USER}
      - SPRING_DATASOURCE_PASSWORD=${ORDERDB_PASS}
      - EUREKA_SERVER_URL=${EUREKA_SERVER_URL}
    depends_on:
      - orderdb
      - eureka-server
      - user-service
      - menu-service
      - payment-service
      - notification-service

  payment-service:
    build: ./payment-service
    ports:
      - "8084:8084"
    environment:
      - SPRING_DATASOURCE_URL=${PAYMENTDB_URL}
      - SPRING_DATASOURCE_USERNAME=${PAYMENTDB_USER}
      - SPRING_DATASOURCE_PASSWORD=${PAYMENTDB_PASS}
      - EUREKA_SERVER_URL=${EUREKA_SERVER_URL}
    depends_on:
      - paymentdb
      - eureka-server

  notification-service:
    build: ./notification-service
    ports:
      - "8085:8085"
    environment:
      - SPRING_DATASOURCE_URL=${NOTIFDB_URL}
      - SPRING_DATASOURCE_USERNAME=${NOTIFDB_USER}
      - SPRING_DATASOURCE_PASSWORD=${NOTIFDB_PASS}
      - EUREKA_SERVER_URL=${EUREKA_SERVER_URL}
      - MAIL_USERNAME=${MAIL_USERNAME}
      - MAIL_PASSWORD=${MAIL_PASSWORD}
    depends_on:
      - notifdb
      - eureka-server

  ai-service:
    build: ./ai-service
    ports:
      - "8086:8086"
    environment:
      - EUREKA_SERVER_URL=${EUREKA_SERVER_URL}
      - OLLAMA_BASE_URL=${OLLAMA_BASE_URL}
      - OLLAMA_MODEL=${OLLAMA_MODEL}
    depends_on:
      - eureka-server
      - ollama
      - order-service
      - menu-service

  frontend:
    build: ./frontend
    ports:
      - "3000:80"
    environment:
      - VITE_API_BASE_URL=http://localhost:8080
    depends_on:
      - api-gateway

volumes:
  userdb_data:
  menudb_data:
  orderdb_data:
  paymentdb_data:
  notifdb_data:
  ollama_data:
```

---

## 14. IMPLEMENTATION PHASES (WEEK BY WEEK)

### Phase 1 — Foundation (Weeks 1–2)

Week 1:
  Day 1: Create all 8 Maven projects (eureka, gateway, 6 services)
          Set up parent pom.xml with shared dependency management
          Add correct dependencies to each service's pom.xml
          Verify all projects compile

  Day 2: Build and test Eureka Server
          Build API Gateway with basic routing (no auth yet)
          Verify services register with Eureka on startup

  Day 3-4: Build user-service completely
            - User entity + Address entity
            - Register and Login endpoints
            - BCrypt password hashing
            - JWT generation (JwtService)
            - SecurityConfig (permit /register and /login, authenticate rest)
            Test with Postman: register → login → get JWT

  Day 5: Add JWT validation to API Gateway (AuthFilter)
          Test the full auth flow end-to-end through Gateway

Week 2:
  Day 1-2: Build menu-service completely
            - Menu + MenuItem entities
            - All owner endpoints (create menu, add items, activate)
            - All customer endpoints (get today's menu, check is-open)
            - Internal endpoints for Feign calls
            - @Scheduled task for auto-closing menu at cutoff time
            Test: create menu via Postman, verify cutoff logic

  Day 3-4: Build payment-service completely
            - Payment entity
            - Create payment endpoint (called by order-service)
            - Get payment status endpoint
            - Mark-as-paid endpoint for owner
            Test: all endpoints with Postman

  Day 5: Build notification-service completely
          - Notification entity + email log
          - EmailService with JavaMailSender
          - All notification trigger endpoints
          Test: send a real email via Postman call

### Phase 2 — Core Business Logic (Weeks 3–4)

Week 3:
  Day 1: Add Feign clients to order-service
          - MenuClient + MenuClientFallbackFactory
          - PaymentClient + PaymentClientFallbackFactory
          - NotificationClient + NotificationClientFallbackFactory
          Configure Resilience4j in application.yml

  Day 2-3: Build order-service OrderService.placeOrder()
            Implement the full 10-step flow from Section 10 of this document
            Add @Transactional to the order creation steps
            Test: place an order, verify email is received, payment record created

  Day 4: Build owner order management endpoints
          (view today's orders, update status, summary)

  Day 5: End-to-end test of the core flow:
          Login → Browse Menu → Place Order → Receive Email → Owner Updates Status → Customer Sees Update

Week 4:
  Day 1-2: Build ai-service
            - Add spring-ai-ollama dependency
            - MenuClient + OrderClient Feign clients
            - AiService.getRecommendations() method
            - AiService.searchMenu() method
            - Build and test prompts until output is reliable

  Day 3-5: Build React frontend
            - Set up Vite + React + Tailwind
            - Axios instance with JWT interceptor
            - AuthContext for token management
            - Login + Register pages
            - Menu browsing page
            - Order placement flow (Cart → Confirm → Success)
            - Order history + status tracking
            - Owner dashboard (orders list, status updates, payment marking)

### Phase 3 — Polish + Deploy (Week 5)

  Day 1: Write all Dockerfiles (one per service + frontend)
          Test Docker Compose: docker-compose up --build
          Fix container networking issues (common: services can't find each other —
          use service names as hostnames, not localhost)

  Day 2: Test complete stack in Docker
          Full end-to-end flow: register, login, browse menu,
          place order, receive email, owner updates status

  Day 3: Deploy to cloud
          Option A: Railway (easiest — push GitHub repo, auto-deploy)
          Option B: AWS EC2 free tier (more impressive but harder)
          Deploy Postgres databases first, then services

  Day 4: Write README.md
          - Problem statement (the WhatsApp story)
          - Architecture diagram
          - Service breakdown table
          - Setup instructions
          - Screenshots / screen recording GIF of full flow
          - Tech stack table

  Day 5: Record a 2-minute demo video
          Upload to YouTube (unlisted) and link in README
          This is what recruiter sees when they click your GitHub link

### Phase 4 — Subscription Service (Only if Phase 1-3 are complete)

Week 6-7:
  - Create subscription-service Maven project
  - Add to Docker Compose and Eureka
  - Build subscription entities + repository
  - Build customer subscription endpoints
  - Build AutoOrderScheduler (@Scheduled, runs daily at 8 AM)
  - Build Feign clients (menu-service, order-service, notification-service)
  - Build owner summary endpoint
  - Add subscription section to React frontend
  - Test end-to-end: create subscription → scheduler fires → order placed → email sent

---

## 15. CRITICAL MISTAKES TO AVOID

1. Do NOT share a database between services.
   The single most common mistake in student microservices projects.
   No cross-service JOINs. No foreign keys across service boundaries.
   Each service owns its data completely.

2. Do NOT validate JWT inside each service.
   JWT validation belongs ONLY in the API Gateway.
   Services trust X-User-Id and X-User-Role headers forwarded by Gateway.
   Duplicating JWT validation in every service defeats the purpose of a Gateway.

3. Do NOT use @Transactional across Feign calls.
   @Transactional only works within a single service's database connection.
   A transaction in order-service cannot include a call to payment-service.
   Design for partial failure (see Step 8 in the order flow — payment failure is non-fatal).

4. Do NOT hardcode service URLs.
   Never write "http://menu-service:8082" in a Feign client.
   Use the service name registered in Eureka: @FeignClient(name = "menu-service")
   Eureka resolves the actual host and port at runtime.

5. Do NOT store JWT in localStorage on the React frontend.
   localStorage is vulnerable to XSS attacks.
   Use React state (in-memory) + httpOnly cookies for production.
   For this project, in-memory state is acceptable but mention the security consideration.

6. Do NOT skip the denormalization in order_items.
   Store item_name and item_price directly in order_items.
   Never query menu-service at read time to get item names.
   Historical orders must reflect prices at time of ordering.

7. Do NOT build the frontend before the backend APIs are tested.
   Test every API endpoint with Postman before writing a single React component.
   Frontend bugs and backend bugs at the same time are impossible to debug.

8. Do NOT start all 6 services simultaneously in development.
   Start with: eureka → user-service → test login.
   Add one service at a time. Running all 6 on a student laptop needs 6+ GB RAM.
   Use docker-compose.dev.yml without ai-service and ollama during development.

---

## 16. CV BULLET POINTS (fill in actual numbers after building)

"Architected a 7-service Spring Boot microservices platform digitizing a
WhatsApp-based tiffin business, with JWT auth at API Gateway, service
discovery via Eureka, and inter-service communication via OpenFeign with
Resilience4j circuit breakers"

"Designed separate PostgreSQL databases per service, implemented denormalized
order history, and handled partial failure scenarios across order, payment,
and notification services"

"Built a subscription service with @Scheduled auto-ordering for daily meal
plans, with configurable cutoff enforcement across services"

"Added AI-powered meal recommendations and natural language menu search
using Spring AI + Ollama (Llama 3.1 8B) — fully local, zero API cost"

"Containerized full stack with Docker Compose (9 containers: 6 services +
3 infrastructure) and deployed to [Railway/AWS] — live at [URL]"

---

## 17. INTERVIEW QUESTIONS — KNOW THESE COLD

Q: Why does each service have its own database?
A: Microservices are independently deployable. If menu-service and order-service
   share a database, deploying a schema change in one breaks the other.
   Database-per-service enforces true independence. Consistency is maintained
   through API contracts, not database transactions.

Q: What happens when menu-service is down and a customer tries to place an order?
A: The Feign client for menu-service has a Resilience4j circuit breaker.
   If menu-service fails, the fallback throws a ServiceUnavailableException
   which returns 503 to the customer. The order is never created — we never
   proceed with an unvalidated order. The circuit opens after 50% failure rate,
   fast-failing requests for 10 seconds before retrying.

Q: Why is JWT validated at the Gateway and not in each service?
A: Single point of enforcement. If I validate in each service, I need to
   maintain the JWT secret in all 6 services and update all 6 when it rotates.
   Validating at the Gateway means one place to change, one place to audit.
   Services inside the cluster trust requests that reach them — if it passed
   the Gateway, it's authenticated.

Q: What does @Transactional mean and why doesn't it work across services?
A: @Transactional creates a database transaction scoped to one service's
   DataSource connection. When order-service calls payment-service via Feign,
   that's a network call — it has no transaction context from order-service.
   You cannot roll back a payment-service database record from an order-service
   transaction. This is the fundamental distributed transactions problem.
   I handle it by making non-critical calls (payment, notification) non-blocking
   — order creation succeeds even if they fail.

Q: Why did you denormalize item name and price in order_items?
A: If I stored only itemId, tomorrow's price change in menu-service would
   retroactively change historical order amounts — which is wrong.
   The order record must reflect exactly what was charged at the time of ordering.
   Since I cannot JOIN across service databases, denormalization is the correct
   and only approach.

Q: How does the subscription auto-ordering work?
A: A @Scheduled method in subscription-service runs daily at 8 AM.
   It queries for all active subscriptions where today's day of week matches
   the subscribed days. For each, it calls menu-service to verify today's menu
   is available and not yet past cutoff, then calls order-service to place the
   order programmatically on behalf of the customer. Results (success/failure)
   are logged to subscription_logs. Customers receive an email confirming
   their auto-placed order.
```

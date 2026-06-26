# TiffinBox — Build Roadmap & Learning Tracker

This is a **living document**. We execute it phase by phase. Each phase is small
(one concept), gets an explainer in `docs/`, and is not "done" until you can
defend it in the quiz.

## How each phase works
1. Claude builds one small, self-contained piece.
2. Claude writes `docs/phaseN-<topic>.md` (explainer + interview questions).
3. You read the code + explainer.
4. Claude quizzes you (easy → brutal interview follow-ups).
5. Only when you can defend it do we move to the next phase.

## Status legend
- `[ ]` not started
- `[~]` built, explainer written, awaiting your read
- `[Q]` quiz in progress
- `[x]` built + explained + quiz passed

## Baseline decisions (locked)
- **Java 21 + Spring Boot 3.x** (confirm JDK on machine before Phase 0)
- DB-per-service (logically; may run one Postgres w/ multiple DBs locally)
- JWT validated **only** at the Gateway
- **Subscription service** = our highest-value differentiator (build it well)
- **Kafka** = ONE flow only (order-placed → notification), done deeply, not sprinkled
- **Spring AI** = natural-language menu search, framed honestly (not "smart recs")
- **GitHub Actions** for CI (not Jenkins)
- Ship it: live deploy + README + 2-min demo video

---

## PHASES

### Foundation
- `[~]` **Phase 0** — Eureka server (independent project, no parent pom). → *Concept: service discovery, what registers & why* — built + boots (HTTP 200 on :8761), awaiting your read + quiz
- `[ ]` **Phase 1** — API Gateway, routing only (no auth). → *Concept: single entry point, path-based routing*

### Auth
- `[ ]` **Phase 2** — user-service: User/Address entities, register/login, BCrypt. → *Concept: JPA persistence + password hashing*
- `[ ]` **Phase 3** — JWT generation in user-service. → *Concept: what a JWT actually is — claims, signing, expiry*
- `[ ]` **Phase 4** — Gateway AuthFilter + forwarded headers (X-User-Id/Role). → *Concept: edge auth, why not per-service*

### Core domain
- `[ ]` **Phase 5** — menu-service: Menu/MenuItem entities, owner + customer endpoints. → *Concept: domain modeling, role-based endpoints*
- `[ ]` **Phase 6** — menu cutoff logic + `@Scheduled` auto-close + internal endpoints. → *Concept: scheduled tasks, internal vs public APIs*
- `[ ]` **Phase 7** — payment-service (mock). → *Concept: a simple service, payment lifecycle*
- `[ ]` **Phase 8** — notification-service + JavaMailSender (Gmail SMTP). → *Concept: side-effect service, email sending*

### Inter-service communication (the hard, high-value part)
- `[ ]` **Phase 9** — Feign clients in order-service (menu + payment). → *Concept: declarative HTTP, Eureka name resolution*
- `[ ]` **Phase 10** — Resilience4j circuit breakers + fallbacks. → *Concept: partial failure, what happens when a dep is down*
- `[ ]` **Phase 11** — order placement flow end-to-end + `@Transactional` boundaries. → *Concept: distributed txn problem, blocking vs non-fatal calls*
- `[ ]` **Phase 12** — owner order management (today's orders, status updates, summary). → *Concept: aggregation endpoints*

### Kafka (split — concept heavy)
- `[ ]` **Phase 13** — Kafka setup + produce `order-placed` event from order-service. → *Concept: producer, topic, why async over Feign here*
- `[ ]` **Phase 14** — notification-service consumes the event; idempotency + failure handling. → *Concept: consumer groups, offsets, at-least-once, duplicates*

### Subscription (highest-value differentiator)
- `[ ]` **Phase 15** — subscription-service: entities + CRUD (subscribe/pause/resume/cancel). → *Concept: business logic service*
- `[ ]` **Phase 16** — `AutoOrderScheduler` (`@Scheduled` daily) places orders for active subs. → *Concept: scheduled cross-service orchestration*

### Spring AI
- `[ ]` **Phase 17** — ai-service: Spring AI + Ollama, natural-language menu search. → *Concept: LLM integration, honest framing*

### Frontend
- `[ ]` **Phase 18** — React + Vite + Tailwind setup, Axios + JWT interceptor, AuthContext. → *Concept: token handling on the client*
- `[ ]` **Phase 19** — Customer flow: login/register, browse menu, cart, place order, history/tracking.
- `[ ]` **Phase 20** — Owner dashboard: orders, status updates, payments, menu management.

### Ship it
- `[ ]` **Phase 21** — Dockerfiles + docker-compose, full stack up locally. → *Concept: container networking, service names as hosts*
- `[ ]` **Phase 22** — GitHub Actions CI (build + test + docker build). → *Concept: CI/CD basics*
- `[ ]` **Phase 23** — Deploy (Railway or AWS) + README + 2-min demo video. → *Concept: the demo IS the project*

---

## Notes & adjustments log
*(We edit this as we go — e.g. if a quiz reveals a gap, we add a deep-dive phase here.)*
- Kafka (13–14) and JWT (3–4) are deliberately split because they're concept-heavy and the most-drilled in interviews.

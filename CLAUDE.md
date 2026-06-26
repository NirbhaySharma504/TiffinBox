# CLAUDE.md — TiffinBox project context

> **Read this first.** This file is the portable memory for the TiffinBox build. It was
> written on a Mac before migrating to Ubuntu; the prior chat history and Claude memory do
> NOT travel with the project folder, so everything needed to resume is captured here and in
> `docs/`. After reading this, also read `docs/BUILD-PLAN.md`, `docs/ROADMAP.md`, and
> `tiffin_microservices_engineering_plan.md`.

## What this project is
A Spring Boot **microservices** food-ordering platform that digitizes a real WhatsApp-based
tiffin (home-cooked meal) business. It's a **resume project** — the #1 goal is a working,
**defensible** system the user can explain in interviews. Quality + understanding over raw
feature count.

## Working agreement (how we collaborate)
- **Build first, learn after.** We build the whole system, then the user learns it
  phase-by-phase by reading the codebase, with **rigorous quizzes** (real interview
  follow-ups, no softballs). On a gap, pause for a focused deep-dive. Tracker: `docs/ROADMAP.md`.
- **Code delivery: Claude writes the files, user reads them** (in IntelliJ). Because reading
  is passive, quizzes must be hard — that's where understanding is proven.
- **Division of labor:** USER generates each Spring project manually from start.spring.io
  (Claude gives exact deps), runs Postgres, installs Docker/tools, runs services. CLAUDE
  writes/explains code, gives generation configs, creates DBs via psql, verifies each piece
  compiles + boots before moving on.
- Each service is verified (compiles + boots + registers with Eureka) before the next.

## Locked technical decisions
- **Primary dev machine: Ubuntu** (16GB RAM, Ryzen 3600x, RTX 2060 Super). The 8GB M2 Mac
  can't run the full stack comfortably; Ubuntu has RAM, native Docker, Linux/prod parity,
  and a GPU for Ollama if AI is added later.
- **Java 21** everywhere. Spring Boot 3.x does NOT support Java 26. On Ubuntu install JDK 21
  and set `JAVA_HOME` to the Linux path (e.g. `/usr/lib/jvm/java-21-openjdk-amd64`).
  Verify `java -version` shows 21 before building.
- **Spring Boot 3.5.x** (not 4.x, not SNAPSHOT/M), **Spring Cloud 2025.0.x** train.
- **Independent Maven projects** (no parent pom), one folder per service.
- **Config format: `application.properties`** (user preference — NOT yaml).
- **DB-per-service**: one local Postgres instance, one database per service.
- **JWT validated ONLY at the API Gateway**; services trust forwarded `X-User-Id` /
  `X-User-Role` / `X-User-Email` headers (no JWT validation inside services).
- **Kafka** broker runs in a **Docker** container (native Docker on Ubuntu). Used for ONE
  flow only: order-service produces `order-placed` → notification-service consumes it.
- **AI service: SKIPPED this pass** (no ai-service, no Ollama). Add later as its own task,
  framed as natural-language menu search.

## Current status
- ✅ `eureka-server` built and boots (HTTP 200 on :8761). Uses `@EnableEurekaServer`,
  `application.properties` with `register-with-eureka=false` / `fetch-registry=false`,
  port 8761. Explainer: `docs/phase0-eureka.md`.
- ⏭️ **Next: migrate to Ubuntu, then build `api-gateway`** (build order step 1 in
  `docs/BUILD-PLAN.md`).

## Resume-on-Ubuntu checklist (do these first on the new machine)
1. Install prerequisites: **JDK 21, Maven, PostgreSQL, Docker (native), Node**.
2. `java -version` → must be 21; export `JAVA_HOME` to the JDK 21 Linux path.
3. `cd TiffinBox`, run `claude`, tell it to read `CLAUDE.md` + `docs/BUILD-PLAN.md`.
4. (Optional but recommended) `git init` + push to GitHub — needed for CI/deploy anyway.
5. Continue the build at step 1 (api-gateway) of `docs/BUILD-PLAN.md`.

## Services to build (no AI this pass)
eureka-server (done) · api-gateway · user-service · menu-service · payment-service ·
notification-service · order-service · subscription-service · frontend (React)

## Key reference docs
- `docs/BUILD-PLAN.md` — the approved full build plan (order, deps per service, infra gates).
- `docs/ROADMAP.md` — living phase tracker + learning status.
- `tiffin_microservices_engineering_plan.md` — the detailed original engineering design.
- `docs/phase0-eureka.md` — first explainer (template for future explainers).

## Critical mistakes to avoid (from the engineering plan)
No shared DBs / no cross-service joins · JWT only at the gateway · no `@Transactional`
across Feign calls (design for partial failure) · no hardcoded service URLs (use Eureka
names) · denormalize item name/price in order_items · test APIs (Postman/curl) before frontend.

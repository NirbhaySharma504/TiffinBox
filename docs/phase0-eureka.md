# Phase 0 — Eureka Server (Service Discovery)

## What we built
A standalone Spring Boot app whose only job is to be a **phone book for services**.
Every other service in TiffinBox will, on startup, call this server and say "Hi, I'm
`order-service` and I live at `192.168.x.x:8083`." When `order-service` later needs to
talk to `menu-service`, it asks Eureka "where is menu-service?" instead of hardcoding an
address. This app runs on port **8761** (the conventional Eureka port).

## Why service discovery exists (the core concept)
In a microservices system, services need to call each other. The naive way is to hardcode
URLs: `http://localhost:8082/api/menu/...`. That breaks the moment:
- a service moves to a different host/port,
- you run **multiple copies** of a service for load (which one do you hardcode?),
- you deploy to the cloud where IPs are assigned dynamically and change on restart.

A **service registry** (Eureka) solves this. Services register themselves by **name** on
startup and send a heartbeat to stay registered. Callers look services up **by name** at
runtime and get back a live address. If a service dies, it stops heart-beating and Eureka
removes it, so callers stop being sent to a dead instance. This is the foundation that
lets Feign clients later say `@FeignClient(name = "menu-service")` with no URL at all.

## File-by-file walkthrough

### `pom.xml`
- `spring-boot-starter-parent` **3.5.15** — our Spring Boot 3.x baseline.
- `<java.version>21</java.version>` — pinned to the LTS.
- `spring-cloud-dependencies` **2025.0.3** — the Spring Cloud "release train" whose
  versions are tested to work with Spring Boot 3.5. We never pick Spring Cloud component
  versions by hand; this BOM (Bill of Materials) does it for us.
- `spring-cloud-starter-netflix-eureka-server` — the one dependency that brings in the
  Eureka **server**. (There's a separate `...-eureka-client` for the *other* services —
  don't confuse them.)

### `EurekaServerApplication.java`
```java
@SpringBootApplication
@EnableEurekaServer        // <-- this is the whole trick
public class EurekaServerApplication { ... }
```
`@EnableEurekaServer` flips this ordinary Spring Boot app into a running Eureka registry
(it auto-configures the registry, the heartbeat tracking, and the dashboard UI). Without
this annotation, the `eureka-server` dependency is on the classpath but does nothing.

### `application.properties`
```properties
server.port=8761                          # the conventional Eureka port everyone expects
spring.application.name=eureka-server
eureka.client.register-with-eureka=false  # don't register myself with myself
eureka.client.fetch-registry=false        # don't download a registry — I AM the registry
eureka.instance.hostname=localhost
```
The two `false` lines matter: an Eureka **server** is itself a Eureka **client** by
default (because the same library can do both), so Spring would otherwise try to make the
server register *with itself* and *fetch a registry from itself*. For a single standalone
registry that's pointless and produces noisy startup errors, so we turn both off.
(In a real high-availability setup you run multiple Eureka servers that DO register with
each other — that's a peer cluster — but we don't need that for a student project.)

## The one concept to really get
**Services find each other by name, resolved at runtime — not by hardcoded URLs.**
Eureka is the registry that makes that possible. Everything else in this project (Feign,
the Gateway's load-balanced routing, Resilience4j) builds on this one idea.

## How you run / verify it
1. Terminal: `export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-21.jdk/Contents/Home`
2. From `eureka-server/`: `./mvnw spring-boot:run`
3. Open **http://localhost:8761** — you'll see the Eureka dashboard. Under
   "Instances currently registered with Eureka" it will say **none** — correct! No other
   service exists yet. In Phase 1 the Gateway will appear here.

---

## Interview questions (try before peeking)
1. What problem does a service registry solve that hardcoding URLs does not?
2. What does `@EnableEurekaServer` actually do to a plain Spring Boot app?
3. Why do we set `register-with-eureka: false` and `fetch-registry: false` on the server?
4. How does Eureka know a service has died?
5. (Harder) A service registered, then its instance crashed. For a short window Eureka may
   still hand its address to callers. Why, and what mechanism eventually fixes it?
6. (Harder) What's the difference between the eureka **server** starter and the eureka
   **client** starter, and which services in our project use which?

---

## Answers (don't read until you've tried)
1. It decouples callers from network locations. Services can move hosts/ports, scale to
   multiple instances, or get dynamic cloud IPs, and callers still find them **by name**.
   Hardcoded URLs break on any of those changes.
2. It auto-configures and starts the Eureka registry inside this app: the in-memory
   registry of instances, heartbeat/lease tracking, the REST endpoints clients use to
   register and query, and the web dashboard. Without it, the dependency is inert.
3. Because a Eureka server is also a Eureka client by default. Left on, it would try to
   register with itself and download a registry from itself — meaningless for a single
   standalone node and it logs connection errors. We disable both.
4. Registered services send periodic **heartbeats** (renew their "lease", default every
   30s). If Eureka stops receiving heartbeats past the lease expiry, it evicts the
   instance from the registry.
5. Eureka favors **availability over strict consistency** (it's an AP system) and evicts
   only after the lease expires, so there's a propagation lag where a stale address can be
   served. Mechanisms that fix/limit it: lease expiry + eviction, client-side caching with
   refresh, and — crucially for us — **client-side resilience** (Resilience4j circuit
   breakers + Feign retries) so a call to a dead instance fails fast and falls back. (This
   is exactly why Phase 10 exists.)
6. The **server** starter (`...-eureka-server`) turns an app INTO the registry — only our
   `eureka-server` uses it. The **client** starter (`...-eureka-client`) makes an app
   register itself and look others up — every other service (gateway, user, menu, order,
   payment, notification, subscription, ai) uses that one.

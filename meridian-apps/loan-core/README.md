# loan-core

Origination, underwriting, pricing and servicing for Meridian Lending. Spring Boot, Java 17.

Layers: `controller` → `service` → `domain` / `rules` / `pricing` → `repository`. Integrations (credit bureau, Kafka)
live in `integration`. `archive/` holds retired code kept for audit and is not built.

Build: `mvn package`. Owner: lending-core.

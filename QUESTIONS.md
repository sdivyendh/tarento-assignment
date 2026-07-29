# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
The code currently uses three persistence styles: Active Record for `Store`, a Panache
repository for `Product`, and a domain port plus adapter for `Warehouse`. I would not
standardize them merely for visual consistency; the amount of abstraction should follow
the complexity of the feature. Active Record is adequate for very small CRUD-only
features, while the Warehouse rules benefit from keeping the domain independent of
Panache and JPA.

I would nevertheless refactor incrementally toward explicit repositories and application
services at transaction boundaries. In particular, REST resources should translate HTTP
requests and responses, not own persistence workflows. Repositories should expose
business-oriented operations such as finding an active warehouse, rather than leaking
generic persistence operations into use cases. JPA entities and API models should also
remain separate from domain models where their lifecycles differ.

This gives the code one clear place for transaction handling, makes business rules easier
to test without a database, and avoids coupling domain behavior to Quarkus. I would
support it with database constraints for invariants that must remain true under
concurrency, schema migrations, and integration tests against PostgreSQL. The migration
would be feature-by-feature so that abstraction work is tied to a concrete maintenance
benefit rather than becoming a large rewrite.
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
An OpenAPI-first approach creates an explicit, language-neutral contract before
implementation. It can generate server interfaces and client SDKs, produce consistent
documentation, and let CI detect breaking changes. This is particularly useful when an
API has external consumers or several teams work independently. Its costs are generator
configuration, generated-code upgrades, a slower feedback loop, and the risk of forcing
business logic into generated types. Generated validation also does not replace domain
validation.

A code-first endpoint is quick to build and keeps simple behavior close together. It is a
reasonable choice for a small internal API. However, annotations spread across handlers
can allow documentation, error formats, and actual behavior to drift, and consumers
cannot reliably develop against the contract before deployment.

For this application I would use OpenAPI as the source of truth for all public endpoints,
while generating only transport-layer interfaces and DTOs. Handwritten adapters would
map those types to application use cases; generated files would never contain business
logic. I would migrate Product and Store incrementally, introduce a shared error schema,
validate the specification in CI, and run contract tests against the implementation.
This keeps the benefits of a consistent API contract without coupling the domain to the
code generator.
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
I would prioritize by business impact and failure likelihood rather than trying to make
every layer equally exhaustive:

1. Fast unit tests would cover Warehouse creation, replacement, and archive rules,
   including boundary values, missing data, and rollback-triggering failures. These rules
   carry the greatest risk and can be tested cheaply through domain ports.
2. Focused persistence tests would verify active-record filtering, uniqueness,
   timestamps, mappings, and atomic replacement. A production-compatible PostgreSQL
   test environment should cover database-specific behavior; a lightweight database can
   still be useful for fast local feedback if its differences are understood.
3. REST tests would exercise each status code, request/response mapping, validation, and
   the generated OpenAPI contract. Store tests would specifically prove that legacy
   synchronization happens after commit and never after rollback.
4. A small number of end-to-end tests would protect the most valuable journeys, such as
   create, retrieve, replace, and archive. Broad end-to-end coverage would be avoided
   because it is slower and more fragile.

Tests should be deterministic, independent, and use builders or fixtures that make the
business condition under test obvious. They would run in CI on every change, with slower
database and end-to-end suites staged appropriately. Line and branch coverage can reveal
untested areas, but neither should be a target by itself; risk coverage, useful
assertions, and mutation testing on critical rules provide stronger evidence. Every
production defect should add a regression test, and flaky tests should be fixed or
quarantined with an owner rather than silently retried.

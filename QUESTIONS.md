# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
Yes, I would refactor some parts, but I would not rewrite the whole persistence layer.

The project currently uses Active Record for `Store`, a Panache repository for `Product`,
and ports and adapters for `Warehouse`. These approaches are suitable for different levels
of complexity. `Store` and `Product` mainly contain CRUD operations, while `Warehouse`
contains more business rules around creation, replacement, archive, location, and capacity.

I would start by adding a repository and service for `Store`. At present, `StoreResource`
handles HTTP requests and also performs database operations directly. Moving the database
work into a repository and the workflow into a service would make the code easier to test
and maintain. `Product` already has a repository, so only larger workflows would need to
move into a service as the feature grows.

I would keep the current Warehouse structure because separating its domain rules from JPA
and Quarkus is useful. I would make these changes one feature at a time to avoid a large and
risky rewrite.
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
OpenAPI-first means defining the API contract before writing the endpoint implementation.
It provides clear documentation and can generate server interfaces, models, and client
code. This is useful when other teams or external applications consume the API because
they can understand the request and response formats without reading the Java code.

The disadvantages are the extra generator configuration and generated source files. The
generated code can also have limitations. For example, `WarehouseResourceImpl` redeclares
the `POST` annotation and `201` response status because they were not applied correctly by
the generated interface. Business validation must still be written manually.

The code-first approach used by `Product` and `Store` is quicker and easier for small
endpoints. However, the implementation and API documentation can become different over
time, and response formats may become inconsistent.

For public APIs, I would choose OpenAPI as the source of truth. I would generate only the
API interfaces and request or response models, while keeping business logic in handwritten
services and domain classes. I would migrate Product and Store gradually instead of
changing every endpoint at once.
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
I would test the areas with the highest business risk first instead of trying to test every
class equally.

1. Unit tests should cover Warehouse creation, replacement, archive, location, and
   capacity rules. They should also cover the fulfilment limits because these rules decide
   whether an assignment is valid.
2. Database tests should verify repository queries, mappings, constraints, and transaction
   behavior. Replacement and archive operations are especially important because several
   database records may change together.
3. REST tests should check successful responses, validation errors, not-found responses,
   and request and response formats for every endpoint.
4. Store synchronization tests should confirm that the legacy system is called only after
   the database transaction commits and is not called after a rollback.
5. A small number of end-to-end tests should cover the most important complete workflows.

This project already includes these unit, database, transaction, and REST tests. GitHub
Actions runs them for every pull request and push to `main`, and JaCoCo enforces at least
80% line coverage. Coverage is useful for finding untested code, but the tests must also
contain meaningful checks. When a defect is found, I would add a regression test, and I
would fix unstable tests instead of simply retrying them.

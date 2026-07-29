# Case Study Scenarios to discuss

## Scenario 1: Cost Allocation and Tracking
**Situation**: The company needs to track and allocate costs accurately across different Warehouses and Stores. The costs include labor, inventory, transportation, and overhead expenses.

**Task**: Discuss the challenges in accurately tracking and allocating costs in a fulfillment environment. Think about what are important considerations for this, what are previous experiences that you have you could related to this problem and elaborate some questions and considerations

**Questions you may have and considerations:**

The first requirement is a cost model that distinguishes direct costs from shared costs.
Labor booked to a warehouse, a carrier invoice, or damaged stock may be directly
attributable. Rent, central planning, software, and shared transport need documented
allocation drivers such as occupied area, labor hours, order lines, pallet movements,
weight, distance, or delivered units. A simple driver that users trust is normally more
valuable than a theoretically precise model that cannot be explained.

The main challenges are data granularity and timing. Operational events and accounting
entries may use different identifiers, time zones, accounting periods, currencies, and
levels of aggregation. Invoices can arrive after the operational event, inventory may
move between facilities, and returns, shrinkage, depreciation, and accruals can otherwise
be counted twice or omitted. Allocation rules therefore need effective dates,
versioning, ownership, audit trails, and reconciliation back to the general ledger.

Before defining scope, I would ask:

- Which decisions must the tool support: ledger posting, management reporting, pricing,
  network design, or all of these?
- What level of detail is actionable—warehouse, store, route, order, product, or
  activity—and how quickly must data be available?
- Which systems are authoritative for labor, inventory, transport, facilities, and
  finance data? How complete and timely are their identifiers?
- How should shared costs, transfers, returns, idle capacity, and one-off costs be
  allocated, and who approves changes to those rules?
- Are there legal-entity, tax, currency, capitalization, or accounting-period
  requirements?

Initial success measures would include the percentage of spend automatically attributed,
the unreconciled amount, time to close a period, allocation-rule exceptions, and the
ability to trace a reported amount to its source and rule.

## Scenario 2: Cost Optimization Strategies
**Situation**: The company wants to identify and implement cost optimization strategies for its fulfillment operations. The goal is to reduce overall costs without compromising service quality.

**Task**: Discuss potential cost optimization strategies for fulfillment operations and expected outcomes from that. How would you identify, prioritize and implement these strategies?

**Questions you may have and considerations:**

Optimization should start with a reliable cost-to-serve baseline segmented by warehouse,
store, route, product family, and service level. This makes it possible to distinguish
structural cost from avoidable waste and prevents a local saving from increasing costs
elsewhere in the network.

Potential interventions include better labor forecasting and shift planning; slotting
fast-moving stock closer to picking and dispatch; improving inventory placement to
reduce split shipments and transfers; consolidating loads and optimizing routes;
renegotiating carrier and supplier terms; reducing packaging, damage, returns, and energy
use; and selectively automating stable, high-volume work. Capacity consolidation can
also help, but resilience, peak demand, safety, and delivery promises must remain
constraints rather than afterthoughts.

I would rank opportunities by expected net benefit, confidence in the data, effort,
reversibility, operational risk, and time to value. The highest-ranked ideas would be
piloted in a comparable facility or route, with a pre-agreed baseline and guardrail
metrics. Results should be evaluated using like-for-like volumes and seasonality before
scaling.

Questions needed before prioritization include:

- Which service levels, safety standards, workforce agreements, and resilience targets
  are non-negotiable?
- Where are the largest cost variances and bottlenecks, and are they caused by volume,
  process, network design, or poor data?
- What demand seasonality, growth, lease commitments, and investment constraints apply?
- Can the expected benefit be measured against a control or credible baseline, including
  implementation and transition costs?

Outcomes should be monitored as cost per order/unit, labor hours per order, utilization,
transport fill rate, inventory turns, damage and return rates, on-time delivery, order
accuracy, lead time, safety incidents, and customer complaints. A saving is accepted
only when the agreed service and safety guardrails remain healthy.

## Scenario 3: Integration with Financial Systems
**Situation**: The Cost Control Tool needs to integrate with existing financial systems to ensure accurate and timely cost data. The integration should support real-time data synchronization and reporting.

**Task**: Discuss the importance of integrating the Cost Control Tool with financial systems. What benefits the company would have from that and how would you ensure seamless integration and data synchronization?

**Questions you may have and considerations:**

Integration removes manual re-entry and gives operations and finance a consistent view
of actual, committed, and allocated costs. It can shorten period close, expose variance
earlier, improve forecasts, and provide traceability from an operational event to a
financial posting. “Real time” should be defined per use case: an operational alert may
need minutes, while a controlled ledger posting may be better handled in an approved
batch.

I would first define system ownership for every field and a canonical mapping for
warehouse instance, business unit, cost center, account, supplier, currency, tax,
quantity, and accounting period. Interfaces should use versioned contracts and stable
event identifiers. Consumers must be idempotent because retries and duplicate delivery
are normal. An outbox or change-data-capture pattern can publish committed changes;
durable queues, bounded retries, dead-letter handling, and replay support protect
against temporary outages. Reconciliation totals and exception queues are required even
when transport is reliable.

Security should include least-privilege service accounts, encryption, secret rotation,
data classification, segregation of duties, and an immutable audit trail. Operational
readiness needs schema monitoring, freshness and lag metrics, correlation IDs,
alerting, runbooks, ownership, and a tested recovery process. A phased migration with
parallel reconciliation is safer than a single cutover.

Before choosing the design, I would ask which ERP, planning, payroll, transport, and
warehouse systems are involved; what APIs and event facilities they support; expected
volumes and latency; period-close rules; data residency and retention requirements; and
which team owns corrections. Success measures would include synchronization latency,
delivery success, duplicate rate, reconciliation variance, exception age, data
freshness, and time spent on manual corrections.

## Scenario 4: Budgeting and Forecasting
**Situation**: The company needs to develop budgeting and forecasting capabilities for its fulfillment operations. The goal is to predict future costs and allocate resources effectively.

**Task**: Discuss the importance of budgeting and forecasting in fulfillment operations and what would you take into account designing a system to support accurate budgeting and forecasting?

**Questions you may have and considerations:**

Fulfillment costs are driven by demand, product mix, service promises, distance, labor,
and finite capacity. A forecast links those operational drivers to money, allowing the
company to plan headcount, carrier capacity, inventory, cash, and capital expenditure
before constraints become expensive. A rolling forecast is generally more useful for
operations than a fixed annual budget alone.

The model should separate variable, fixed, step, and one-off costs and retain both
quantity and rate assumptions. Relevant inputs include order and return volumes, SKU and
channel mix, promotions and seasonality, productivity, wage and carrier rates, fuel and
energy prices, inflation and foreign exchange, leases, maintenance, planned openings or
replacements, and capacity limits. Base, upside, downside, and disruption scenarios make
uncertainty visible instead of presenting one number as certain.

The system should support versioned assumptions, approvals, comments, role-based access,
effective-dated facility structures, and comparisons among budget, forecast, committed
cost, and actuals. Forecasts should be reproducible, explainable by cost driver, and
recalculated on an agreed cadence. Accuracy should be measured by horizon and segment;
bias is as important as absolute error because persistent optimism produces systematic
underfunding.

Before scoping, I would establish the planning horizon and granularity, decision cadence,
owners of each assumption, source history and its quality, treatment of new facilities
with little history, required scenarios, currencies, approval workflow, and integration
with the corporate planning process. Useful outcomes include forecast error and bias,
budget variance, forecast cycle time, assumption freshness, capacity shortfall warnings,
and the percentage of variance that can be explained by volume, rate, mix, or efficiency.

## Scenario 5: Cost Control in Warehouse Replacement
**Situation**: The company is planning to replace an existing Warehouse with a new one. The new Warehouse will reuse the Business Unit Code of the old Warehouse. The old Warehouse will be archived, but its cost history must be preserved.

**Task**: Discuss the cost control aspects of replacing a Warehouse. Why is it important to preserve cost history and how this relates to keeping the new Warehouse operation within budget?

**Questions you may have and considerations:**

The business unit code represents operational continuity, but it must not be the database
identity of a physical warehouse. The old and new warehouse need distinct immutable
instance IDs. The old record should be soft-archived with an effective timestamp, while
the new active record may reuse the business unit code. Costs must reference the
warehouse instance and service period so that replacement does not rewrite history or
misattribute late invoices.

Preserved history supports audit and accounting retention, vendor disputes, trend
analysis, and a credible comparison between the replacement business case and actual
performance. It also provides the baseline needed to determine whether the new facility
improves cost per unit rather than merely moving costs between periods or cost centers.
Historical allocation rules and organizational mappings must therefore be
effective-dated as well.

The replacement budget should separately track acquisition or construction, automation,
fit-out, migration, parallel operation, stock transfer, write-offs, training,
decommissioning, and contingency. It should then track the new recurring run rate for
labor, rent, utilities, maintenance, transport, and expected productivity ramp-up.
Approval gates can compare forecast, committed spend, cash paid, and actual cost while
protecting schedule, stock integrity, service, and safety.

Key questions include:

- What is the approved business case, contingency, funding profile, and target payback?
- When do financial responsibility and inventory ownership move to the new instance?
- How long will facilities overlap, and where are overlap and decommissioning costs
  recorded?
- Which contracts, assets, employees, open orders, accruals, and late invoices transfer?
- What capacity, stock-matching, service, regulatory, and cutover criteria must be met
  before activation?
- Who may approve budget changes, and which records are subject to retention or legal
  hold?

The archive-and-create operation should be atomic, with at most one active warehouse per
business unit code and no mutation of archived cost records. Control measures include
capital and operating variance, committed versus approved spend, cost per unit, ramp-up
productivity, transfer loss, downtime, on-time delivery, and forecast payback. These show
whether the replacement stays within budget without hiding deterioration in service.

## Instructions for Candidates
Before starting the case study, read the [BRIEFING.md](BRIEFING.md) to quickly understand the domain, entities, business rules, and other relevant details.

**Analyze the Scenarios**: Carefully analyze each scenario and consider the tasks provided. To make informed decisions about the project's scope and ensure valuable outcomes, what key information would you seek to gather before defining the boundaries of the work? Your goal is to bridge technical aspects with business value, bringing a high level discussion; no need to deep dive.

Across all scenarios, scope should be agreed only after identifying the decisions the
tool must improve, the accountable users, the authoritative systems, data quality and
latency, accounting and regulatory constraints, integration ownership, and measurable
baselines. A useful first release would cover a small number of high-value cost
categories and facilities end to end—including reconciliation and auditability—before
adding finer allocation detail or predictive sophistication. This makes business value
and data limitations visible early and creates evidence for the next investment.

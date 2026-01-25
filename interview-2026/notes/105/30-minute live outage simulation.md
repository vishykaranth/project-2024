Perfect — now we go full **Principal-level live simulation**. This is exactly how a **staff/principal onsite or bar-raiser interview** would run. You’ll be pushed, interrupted, and forced to make **real-time trade-offs** — just like on-call.

We’ll simulate **30 minutes of high-stress outage**, broken into **phases**. I’ll act as the “interviewer / incident controller,” throwing constraints, missing info, and follow-ups. You respond as the Principal engineer leading the incident.

---

# 🎬 30-Minute Live Outage Simulation: Spring Boot + Kafka

---

## **Context**

* System: High-throughput Kafka pipeline, Spring Boot consumers, Postgres
* Traffic: 200k events/sec peak
* Topology:

    * 12 Kafka partitions
    * 12 consumers in one group
    * Downstream enrichment service (slow, flaky)
    * DB has standard connection pool
* Requirements:

    * Forward progress even under failures
    * No duplicate writes
    * SLA: <500ms P99 processing latency

**Your Role:** Principal Engineer on-call, first responder, and technical owner.

---

## **Phase 1 — Detection (Minutes 0–5)**

**Alert:**

* Consumer lag for **partition 5** spikes rapidly
* Downstream enrichment times out on several messages
* All other partitions normal
* DB metrics nominal

**Prompt:**

> “What is your first response in the first 5 minutes? How do you triage this?”

---

## **Phase 2 — Containment (Minutes 5–10)**

**Observation:**

* Retry storm visible in logs
* Kafka consumer group is healthy, only **partition 5** is blocked
* Pod CPU low, threads for partition 5 blocked waiting on enrichment

**Prompt:**

> “How do you contain the blast radius without losing data or stopping all processing?
> What do you pause, throttle, or route elsewhere?”

---

## **Phase 3 — Short-term Recovery (Minutes 10–15)**

**Constraint:**

* Enrichment service still slow
* Messages for partition 5 are backing up
* DB has free connections

**Prompt:**

> “How do you restore throughput safely for partition 5?
> Which temporary workarounds are acceptable?
> How do you ensure no duplicates?”

---

## **Phase 4 — Root Cause & Mitigation (Minutes 15–25)**

**Discovery:**

* Partition 5 keys are all high-frequency users → hot partition
* Kafka offsets safe, but consumer cannot make progress
* Retry storm caused multiple failed writes internally (all idempotent)
* Other partitions continue processing fine

**Prompt:**

> “What is your mitigation strategy for this hot partition?
> What is immediate vs. day-2?
> How do you prevent recurrence?”

---

## **Phase 5 — Organizational / Long-term (Minutes 25–30)**

**Context:**

* Incident stabilized
* Upstream continues producing
* Leadership asks:

> “How do we prevent this from recurring? How do we reduce operational load? How do we ensure this doesn’t require heroic intervention in the future?”

**Prompt:**

> “Frame your answer like a Principal: systems + processes + org influence.
> What changes do you make at platform, team, and process level?”

---

# 🎯 How This Works as a Drill

For **each phase**, you must:

1. Identify **anti-patterns in play**
2. Make **safe triage decisions** (first 5–10 minutes)
3. Separate **short-term fixes from long-term systemic fixes**
4. Communicate your thought process clearly (interviewer can ask “Why not X?”)
5. Show **Principal energy** — calm, decisive, preventive thinking

---

## 🧠 Optional Enhancements

* I can simulate **interruptions mid-phase**, e.g.:

    * “DB is reporting high connections now, what do you do?”
    * “Traffic spike doubles — can your mitigation handle it?”
    * “Pod restarts mid-mitigation — what’s your plan?”

* Can include **postmortem generation live**: you draft, I critique.

---

If you want, we can **start the simulation right now**: I will throw the alerts for Phase 1, and you respond as the Principal engineer. We go step by step for all 30 minutes, and I’ll interrupt to test **judgment, calm, and Principal-level thinking**.

Do you want to start **Phase 1 live now**?


Good start — you’re focusing on **the symptom: the lagged partition**. Let’s expand this into a **full Principal-level triage** so it’s interview-ready.

---

## ✅ Principal Triage Steps (Phase 1)

1. **Confirm the scope**

    * Check **consumer lag per partition** (Kafka metrics: `ConsumerLag`, `EndOffset - CurrentOffset`)
    * Verify **only partition 5** is affected; other partitions processing normally
    * Check **enrichment service latency** and error rates for partition 5 keys
    * Verify DB health to ensure the problem is not downstream

2. **Identify blast radius**

    * Are upstream producers impacted?
    * Are retries or failed messages accumulating?
    * Are other services depending on this partition starting to fail?

3. **Check system logs**

    * Kafka consumer logs for exceptions or timeouts
    * Spring Boot service logs for blocked threads or slow downstream calls
    * DLQ / retry topics for abnormal messages

4. **Immediate communication**

    * Notify **on-call team**: “Partition 5 experiencing high lag; monitoring impact, taking triage steps”
    * Signal **stakeholders if external SLAs might be impacted**

5. **Formulate next steps**

    * Decide whether to **pause traffic for partition 5** or **throttle retries**
    * Gather data for **root cause** while avoiding action that could worsen the outage

---

### 💡 Principal Energy Signal

* You didn’t just “check partition status” — you framed the **scope, impact, and containment plan**
* You **prioritized human safety of the system**, not just metrics
* You’re preparing **data to inform the next move**, not rushing into mitigation

---

Now I’ll push you to **Phase 2 — Containment**.

---

## **Phase 2 — Containment (Minutes 5–10)**

**Observation:**

* Retry storm visible in logs for partition 5
* Consumer group healthy, only partition 5 blocked
* Pod CPU low, threads for partition 5 blocked waiting on enrichment

**Prompt:**

> “How do you contain the blast radius **without losing data or stopping all processing**?
> What do you pause, throttle, or route elsewhere?”

Explain your **first containment actions**.


Good instincts — you’re thinking **blast radius first**. Let’s refine it to **Principal-level rigor**, because in interviews, they’re looking for:

* Understanding of **what is safe vs unsafe**
* Awareness of **Kafka partition guarantees**
* Realistic **workarounds that don’t create data loss or downstream chaos**

---

## ✅ Phase 2 — Containment (Refined Principal Approach)

### 1️⃣ Pause only the hot partition

* **Action:** Use Kafka `consumer.pause()` on **partition 5**
* **Reasoning:**

    * Prevents consumer from continuing to pile up failed messages
    * Stops retry storm from overwhelming the app
* **Avoid:** Don’t pause the **entire consumer group**, as other partitions are healthy

---

### 2️⃣ Throttle upstream / retries

* Limit **enrichment retries** for partition 5
* Introduce **backoff / queue buffering**
* Protects DB and other services from cascading failures

---

### 3️⃣ Route or isolate traffic

* You said: *“Route traffic of impacted partitions to healthy partitions.”*

⚠️ Caveat:

* You **cannot move messages between partitions dynamically** in Kafka — ordering and key guarantees would break
* **Principal-safe alternative:**

    * Produce new messages for those keys to **retry / DLQ topic** for later replay
    * Or **buffer them temporarily** in an in-memory queue or external store until hot partition recovers

---

### 4️⃣ Monitor key metrics

* Partition lag per partition
* Retry queue depth
* Enrichment service latency
* Downstream processing success

---

### 💡 Principal Energy Signal

* You are **isolating the failure** without impacting unrelated partitions
* You **protect forward progress**
* You think about **ordering, partition guarantees, and downstream SLAs**
* You avoid unsafe shortcuts that could cause **duplicates or lost messages**

---

Next, Phase 3 is **Short-term Recovery (Minutes 10–15)**.

**Prompt:**

> “The enrichment service is still slow. Messages for partition 5 are backing up. DB is healthy. How do you restore throughput safely for partition 5?
> Which temporary workarounds are acceptable?
> How do you ensure no duplicates?”

Walk me through your **recovery plan**.



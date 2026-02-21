Great — we’ll run this as a **real Principal/Staff+ system design interview**.
I will **drive**, **interrupt**, and **push for depth**.

---

# 🎤 System Design Interview: Design Twitter (X)

You are the candidate.

## 🧭 Step 0 — Clarification (you lead)

**Question 1**

Design a system like Twitter.

Start by telling me:

* What are the **core use cases** you want to support?
* Who are the **actors**?
* What scale are you designing for?
  (Give numbers — DAU, tweets/sec, read/write ratio)

👉 Don’t jump to architecture yet.
I’m evaluating how you **frame the problem**.

---

## ⏱ Constraints (you should ask for these — if you don’t, I’ll deduct signal)

If you ask, I’ll answer.

Possible dimensions:

* Latency expectations
* Consistency expectations
* Tweet size / media
* Fanout model
* Geo distribution
* HA / DR requirements

---

## 🎯 Product scope (not all features are required)

You must explicitly choose.

Options include:

* Post tweet
* Follow / unfollow
* Home timeline
* User timeline
* Likes
* Retweets
* Replies
* Search
* Trending hashtags
* Notifications

Tell me:

👉 What are **in scope for V1** and what are **out of scope**?

---

## ⚠️ Principal-level signal I’m looking for

I am not evaluating whether you know:

* load balancer
* cache
* database

I AM evaluating whether you:

* Design for **evolution**
* Separate **write path vs read path**
* Think in **product trade-offs**
* Model **fanout strategies**
* Handle **celebrity problem**
* Define **SLOs**

---

# 🚫 Do NOT start drawing boxes yet.

Start with:

### 👉 Functional requirements

### 👉 Non-functional requirements

### 👉 Scale estimation

---

Your move.

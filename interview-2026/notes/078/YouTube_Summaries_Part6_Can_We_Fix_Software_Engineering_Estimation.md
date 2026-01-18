# Can We Fix Software Engineering Estimation?

## Overview

Software estimation is notoriously difficult and often inaccurate. This summary explores why estimation fails, the challenges involved, and modern approaches to improve estimation accuracy and usefulness.

## Why Estimation Fails

### 1. The Planning Fallacy

```
┌─────────────────────────────────────────────────────────┐
│         Planning Fallacy                               │
└─────────────────────────────────────────────────────────┘

Human Tendency:
    │
    ├─► Optimistic estimates
    ├─► Ignore past failures
    └─► Underestimate complexity

Reality:
└─ Most projects take longer than estimated
```

**Why This Happens:**
- Focus on best-case scenarios
- Ignore historical data
- Cognitive biases
- Pressure to provide optimistic estimates

### 2. Unknown Unknowns

```
┌─────────────────────────────────────────────────────────┐
│         Estimation Uncertainty                         │
└─────────────────────────────────────────────────────────┘

Known Knowns:
├─ Requirements we know
└─ Can estimate accurately

Known Unknowns:
├─ Requirements we know we don't know
└─ Can estimate with uncertainty

Unknown Unknowns:
├─ Things we don't know we don't know
└─ Cannot estimate (surprises)
```

**The Problem:**
- Unknown unknowns are inevitable
- They derail estimates
- Hard to account for in planning

### 3. Estimation as Commitment

```
┌─────────────────────────────────────────────────────────┘
│         Estimation vs Commitment                        │
└─────────────────────────────────────────────────────────┘

Estimation:
    │
    ├─► Best guess
    ├─► Based on assumptions
    └─► Should be a range

Reality:
    │
    ├─► Treated as commitment
    ├─► Fixed deadline
    └─► No flexibility
```

**Why This Fails:**
- Estimates become promises
- No room for uncertainty
- Pressure to meet estimates
- Quality suffers

## Modern Estimation Approaches

### 1. Relative Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Story Points (Relative)                        │
└─────────────────────────────────────────────────────────┘

Approach:
    │
    ├─► Compare stories relatively
    ├─► Use Fibonacci sequence
    └─► Focus on complexity, not time

Benefits:
├─ More accurate than hours
├─ Accounts for uncertainty
└─ Team-based estimation
```

**How It Works:**
- Compare stories to each other
- Use reference stories
- Team consensus
- Velocity-based planning

### 2. Three-Point Estimation

```
┌─────────────────────────────────────────────────────────┐
│         PERT Estimation                                │
└─────────────────────────────────────────────────────────┘

Three Estimates:
    │
    ├─► Optimistic (O)
    ├─► Most Likely (M)
    └─► Pessimistic (P)

Formula:
Expected = (O + 4M + P) / 6

Provides range, not single point
```

**Benefits:**
- Acknowledges uncertainty
- Provides range
- More realistic
- Accounts for risk

### 3. No Estimates Movement

```
┌─────────────────────────────────────────────────────────┐
│         #NoEstimates                                   │
└─────────────────────────────────────────────────────────┘

Philosophy:
    │
    ├─► Estimates are waste
    ├─► Focus on delivery
    └─► Use flow metrics

Instead of Estimates:
├─ Cycle time
├─ Throughput
├─ Lead time
└─ Work in progress
```

**Approach:**
- Stop estimating
- Measure actual performance
- Use historical data
- Forecast based on flow

### 4. Probabilistic Forecasting

```
┌─────────────────────────────────────────────────────────┐
│         Monte Carlo Simulation                         │
└─────────────────────────────────────────────────────────┘

Method:
    │
    ├─► Use historical data
    ├─► Run simulations
    └─► Generate probability distributions

Output:
└─ "80% chance of completion by date X"
```

**Benefits:**
- Based on actual data
- Provides probabilities
- More accurate
- Honest about uncertainty

## Estimation Best Practices

### 1. Use Historical Data

```
┌─────────────────────────────────────────────────────────┐
│         Learn from History                            │
└─────────────────────────────────────────────────────────┘

Track:
├─ Actual vs estimated time
├─ Estimation accuracy
├─ Common delays
└─ Patterns

Use:
└─ Improve future estimates
```

### 2. Estimate in Ranges

```
┌─────────────────────────────────────────────────────────┐
│         Range Estimation                               │
└─────────────────────────────────────────────────────────┘

Instead of:
└─ "This will take 2 weeks"

Say:
└─ "This will take 1-3 weeks (most likely 2)"
```

**Benefits:**
- Acknowledges uncertainty
- Sets expectations
- More honest
- Allows planning flexibility

### 3. Decompose Large Tasks

```
┌─────────────────────────────────────────────────────────┐
│         Task Decomposition                            │
└─────────────────────────────────────────────────────────┘

Large Task:
    │
    ├─► Hard to estimate
    └─► High uncertainty

Decompose:
    │
    ├─► Smaller tasks
    ├─► Easier to estimate
    └─► Lower uncertainty

Sum estimates:
└─ More accurate total
```

### 4. Include Buffer Time

```
┌─────────────────────────────────────────────────────────┐
│         Buffer Management                             │
└─────────────────────────────────────────────────────────┘

Estimation:
    │
    ├─► Core work estimate
    └─► Add buffer for:
        ├─ Unknown unknowns
        ├─ Dependencies
        └─ Interruptions

Buffer Size:
└─ Based on historical variance
```

## Estimation Anti-Patterns

### Anti-Pattern 1: Padding Estimates
```
Problem: Add arbitrary padding
Solution: Use data-driven buffers
```

### Anti-Pattern 2: Negotiating Estimates
```
Problem: Pressure to reduce estimates
Solution: Estimates are facts, not negotiable
```

### Anti-Pattern 3: Individual Estimates
```
Problem: One person estimates
Solution: Team-based estimation
```

### Anti-Pattern 4: Ignoring History
```
Problem: Don't learn from past
Solution: Track and use historical data
```

## Summary

**Can we fix estimation?**
- **Partially** - We can improve, not perfect
- Use better techniques
- Acknowledge uncertainty
- Focus on value over precision

**Key Improvements:**
1. **Relative estimation** - Story points over hours
2. **Range estimates** - Acknowledge uncertainty
3. **Historical data** - Learn from past
4. **Probabilistic forecasting** - Use statistics
5. **Flow metrics** - Consider #NoEstimates

**Takeaway:** Perfect estimation is impossible, but we can make it more useful by acknowledging uncertainty, using better techniques, and focusing on delivery over precision.

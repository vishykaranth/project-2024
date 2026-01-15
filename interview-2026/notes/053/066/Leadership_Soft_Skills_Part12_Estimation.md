# Estimation: Story Points, Velocity, Capacity Planning

## Overview

Estimation is a critical leadership skill that involves predicting the effort, time, and resources needed to complete work. In agile development, estimation uses story points, velocity tracking, and capacity planning to create realistic plans and manage expectations effectively.

## Estimation Framework

```
┌─────────────────────────────────────────────────────────┐
│         Estimation Process                             │
└─────────────────────────────────────────────────────────┘

Understand Work
    │
    ▼
┌─────────────────┐
│ Break Down      │  ← Decompose into smaller pieces
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Estimate        │  ← Assign story points
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Track Velocity  │  ← Measure actual performance
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Plan Capacity   │  ← Allocate work
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Review & Adjust │  ← Learn and improve
└─────────────────┘
```

## Story Points

### What are Story Points?

```
┌─────────────────────────────────────────────────────────┐
│         Story Points Explained                         │
└─────────────────────────────────────────────────────────┘

Definition:
├─ Relative measure of effort
├─ Not absolute time
└─ Team-specific scale

Why Use Story Points:
├─ Avoid time anchoring
├─ Account for uncertainty
├─ Focus on relative complexity
└─ Team discussion

What They Include:
├─ Effort required
├─ Complexity
├─ Risk/uncertainty
└─ Dependencies
```

### Story Point Scale

```
┌─────────────────────────────────────────────────────────┐
│         Common Scales                                  │
└─────────────────────────────────────────────────────────┘

Fibonacci Scale:
├─ 1, 2, 3, 5, 8, 13, 21
├─ Reflects uncertainty
└─ Most common

T-Shirt Sizing:
├─ XS, S, M, L, XL
├─ Simple, intuitive
└─ Good for beginners

Powers of 2:
├─ 1, 2, 4, 8, 16
├─ Clear progression
└─ Less granular

Linear Scale:
├─ 1, 2, 3, 4, 5
├─ Simple but less accurate
└─ Not recommended
```

### Story Point Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Estimation Process                             │
└─────────────────────────────────────────────────────────┘

1. Reference Story
   ├─ Pick a medium story (5 points)
   ├─ Team agrees on reference
   └─ Use as baseline

2. Compare Stories
   ├─ Is it bigger or smaller?
   ├─ How much bigger/smaller?
   └─ Assign points relatively

3. Team Discussion
   ├─ Share estimates
   ├─ Discuss differences
   └─ Reach consensus

4. Record Estimate
   ├─ Document points
   ├─ Note assumptions
   └─ Track for learning
```

### Planning Poker

```
┌─────────────────────────────────────────────────────────┐
│         Planning Poker Process                         │
└─────────────────────────────────────────────────────────┘

1. Product Owner presents story
   ├─ Reads story
   ├─ Answers questions
   └─ Team understands

2. Individual Estimation
   ├─ Each person estimates privately
   ├─ Selects card (1, 2, 3, 5, 8, 13...)
   └─ No discussion yet

3. Reveal Estimates
   ├─ Everyone shows card
   ├─ See range of estimates
   └─ Identify differences

4. Discussion
   ├─ High estimators explain
   ├─ Low estimators explain
   └─ Share perspectives

5. Re-estimate
   ├─ Estimate again
   ├─ Usually converge
   └─ Repeat if needed

6. Consensus
   ├─ Agree on estimate
   ├─ Or take average
   └─ Record estimate
```

## Velocity

### What is Velocity?

```
┌─────────────────────────────────────────────────────────┐
│         Velocity Definition                           │
└─────────────────────────────────────────────────────────┘

Definition:
├─ Story points completed per sprint
├─ Team's throughput measure
└─ Historical performance indicator

Purpose:
├─ Predict future capacity
├─ Plan sprints realistically
└─ Track team performance

Calculation:
├─ Sum of story points completed
├─ Per sprint
└─ Average over time
```

### Velocity Tracking

```
┌─────────────────────────────────────────────────────────┐
│         Velocity Tracking                              │
└─────────────────────────────────────────────────────────┘

Sprint 1: Completed 21 points
Sprint 2: Completed 18 points
Sprint 3: Completed 23 points
Sprint 4: Completed 20 points

Average Velocity: (21+18+23+20)/4 = 20.5 points

Planning Next Sprint:
├─ Use average velocity
├─ Plan for ~20 points
└─ Adjust based on context
```

### Velocity Trends

```
┌─────────────────────────────────────────────────────────┐
│         Velocity Analysis                             │
└─────────────────────────────────────────────────────────┘

Stable Velocity:
├─ Consistent over time
├─ Predictable
└─ Good for planning

Increasing Velocity:
├─ Team improving
├─ Process working
└─ Adjust expectations

Decreasing Velocity:
├─ Team struggling
├─ Process issues
└─ Investigate causes

Variable Velocity:
├─ High variation
├─ Unpredictable
└─ Need more data
```

## Capacity Planning

### Capacity Calculation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Planning                              │
└─────────────────────────────────────────────────────────┘

Step 1: Available Time
├─ Team members: 5
├─ Working days: 10
├─ Hours per day: 8
└─ Total: 5 × 10 × 8 = 400 hours

Step 2: Deduct Non-Work Time
├─ Meetings: 2 hours/day = 100 hours
├─ Time off: 2 days = 80 hours
└─ Other: 20 hours
Total deducted: 200 hours

Step 3: Available Capacity
├─ Net capacity: 400 - 200 = 200 hours
└─ Per person: 40 hours

Step 4: Apply Buffer
├─ Add 20% buffer for unknowns
└─ Final capacity: 160 hours
```

### Capacity vs Velocity

```
┌─────────────────────────────────────────────────────────┐
│         Capacity vs Velocity                           │
└─────────────────────────────────────────────────────────┘

Capacity:
├─ Available time
├─ Theoretical maximum
└─ Input measure

Velocity:
├─ Actual output
├─ Story points completed
└─ Output measure

Relationship:
├─ Velocity ≤ Capacity
├─ Efficiency = Velocity / Capacity
└─ Target: 70-80% efficiency
```

## Estimation Techniques

### 1. Relative Sizing

```
┌─────────────────────────────────────────────────────────┐
│         Relative Sizing                               │
└─────────────────────────────────────────────────────────┘

Approach:
├─ Compare to reference story
├─ Don't think in hours
└─ Focus on relative complexity

Example:
├─ Reference: Login feature = 5 points
├─ New story: Payment feature
├─ Comparison: 2x more complex
└─ Estimate: 8 points

Benefits:
├─ Avoids time anchoring
├─ Accounts for uncertainty
└─ Team discussion
```

### 2. Three-Point Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Three-Point Estimation                        │
└─────────────────────────────────────────────────────────┘

Estimates:
├─ Optimistic (best case)
├─ Pessimistic (worst case)
└─ Most likely (realistic)

Calculation:
Estimate = (Optimistic + 4×Most Likely + Pessimistic) / 6

Example:
├─ Optimistic: 5 points
├─ Most likely: 8 points
├─ Pessimistic: 13 points
└─ Estimate: (5 + 4×8 + 13) / 6 = 8.3 points

Use for:
├─ High uncertainty
├─ Complex stories
└─ Risk assessment
```

### 3. T-Shirt Sizing

```
┌─────────────────────────────────────────────────────────┐
│         T-Shirt Sizing                                │
└─────────────────────────────────────────────────────────┘

Scale:
├─ XS: 1 point
├─ S: 2 points
├─ M: 3 points
├─ L: 5 points
└─ XL: 8 points

Process:
├─ Quick categorization
├─ Convert to points
└─ Refine if needed

Best for:
├─ Initial estimation
├─ Large backlogs
└─ High-level planning
```

## Estimation Best Practices

### 1. Use Reference Stories

```
┌─────────────────────────────────────────────────────────┐
│         Reference Stories                             │
└─────────────────────────────────────────────────────────┘

Create Baseline:
├─ Pick 2-3 reference stories
├─ Different sizes (small, medium, large)
└─ Team agrees on points

Use for Comparison:
├─ Compare new stories
├─ "Is it bigger than X?"
└─ Maintain consistency

Update Periodically:
├─ Recalibrate as team learns
├─ Adjust reference stories
└─ Keep relevant
```

### 2. Account for Uncertainty

```
┌─────────────────────────────────────────────────────────┐
│         Handling Uncertainty                          │
└─────────────────────────────────────────────────────────┘

Unknowns:
├─ Add points for uncertainty
├─ Spike stories for research
└─ Break down large stories

Risk:
├─ Technical risk
├─ Dependency risk
└─ Complexity risk

Buffer:
├─ Add buffer for unknowns
├─ Don't over-commit
└─ Plan conservatively
```

### 3. Break Down Large Stories

```
┌─────────────────────────────────────────────────────────┐
│         Story Decomposition                           │
└─────────────────────────────────────────────────────────┘

Large Story (13+ points):
├─ Hard to estimate accurately
├─ High uncertainty
└─ Break down into smaller stories

Process:
├─ Identify sub-features
├─ Create smaller stories (3-8 points)
└─ Estimate each separately

Benefits:
├─ More accurate estimates
├─ Better planning
└─ Clearer scope
```

## Common Estimation Mistakes

### Mistake 1: Converting to Hours

```
┌─────────────────────────────────────────────────────────┐
│         Avoid Hour Conversion                         │
└─────────────────────────────────────────────────────────┘

Problem:
├─ "1 point = 4 hours"
├─ Loses relative nature
└─ Creates false precision

Solution:
├─ Keep story points abstract
├─ Focus on relative size
└─ Use velocity for planning
```

### Mistake 2: Individual Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Team Estimation                               │
└─────────────────────────────────────────────────────────┘

Problem:
├─ One person estimates
├─ Misses team perspective
└─ Less accurate

Solution:
├─ Team estimates together
├─ Share knowledge
└─ Reach consensus
```

### Mistake 3: Not Tracking

```
┌─────────────────────────────────────────────────────────┐
│         Track and Learn                               │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Don't track actuals
├─ Can't improve
└─ Repeat mistakes

Solution:
├─ Track velocity
├─ Compare estimates to actuals
└─ Learn and adjust
```

## Estimation Tools

### Planning Tools
- **Jira**: Story points, velocity charts
- **Azure DevOps**: Estimation, capacity planning
- **Trello**: Simple estimation
- **Planning Poker Apps**: Digital planning poker

### Tracking Tools
- **Velocity Charts**: Track over time
- **Burndown Charts**: Sprint progress
- **Burnup Charts**: Release progress

## Summary

Estimation:
- **Story Points**: Relative measure of effort
- **Velocity**: Story points completed per sprint
- **Capacity**: Available time for work
- **Techniques**: Planning poker, relative sizing, three-point

**Key Principles:**
- Use relative sizing
- Team estimates together
- Track velocity over time
- Account for uncertainty
- Learn and improve

**Best Practice**: Use story points for relative estimation, track velocity to predict capacity, and continuously refine estimates based on actual performance to improve accuracy over time.

# Agile Methodologies: Scrum, Kanban, Sprint Planning

## Overview

Agile Methodologies are iterative and incremental approaches to software development that emphasize flexibility, collaboration, and customer feedback. As a technical leader, understanding Scrum, Kanban, and sprint planning enables you to guide teams effectively, manage work efficiently, and deliver value continuously.

## Agile Principles

```
┌─────────────────────────────────────────────────────────┐
│         Agile Manifesto Values                         │
└─────────────────────────────────────────────────────────┘

Individuals and interactions over processes and tools
Working software over comprehensive documentation
Customer collaboration over contract negotiation
Responding to change over following a plan

While there is value in items on the right,
we value items on the left more.
```

## Scrum Framework

### Scrum Overview

```
┌─────────────────────────────────────────────────────────┐
│         Scrum Framework                                │
└─────────────────────────────────────────────────────────┘

Roles:
├─ Product Owner (prioritizes work)
├─ Scrum Master (facilitates process)
└─ Development Team (delivers work)

Artifacts:
├─ Product Backlog (all work)
├─ Sprint Backlog (sprint work)
└─ Increment (working software)

Events:
├─ Sprint Planning
├─ Daily Scrum
├─ Sprint Review
└─ Sprint Retrospective
```

### Scrum Process

```
┌─────────────────────────────────────────────────────────┐
│         Scrum Sprint Cycle                             │
└─────────────────────────────────────────────────────────┘

Sprint Planning
    │
    ▼
Sprint (1-4 weeks)
    │
    ├─► Daily Scrum (every day)
    │
    └─► Development Work
        │
        ▼
Sprint Review
    │
    ▼
Sprint Retrospective
    │
    └───► Next Sprint
```

### Sprint Planning

```
┌─────────────────────────────────────────────────────────┐
│         Sprint Planning Process                        │
└─────────────────────────────────────────────────────────┘

Part 1: What to Build
├─ Product Owner presents backlog
├─ Team selects items
└─ Define sprint goal

Part 2: How to Build
├─ Break down tasks
├─ Estimate effort
└─ Commit to work

Output:
├─ Sprint backlog
├─ Sprint goal
└─ Team commitment
```

### Daily Scrum

```
┌─────────────────────────────────────────────────────────┐
│         Daily Scrum Structure                          │
└─────────────────────────────────────────────────────────┘

Format: 15-minute standup

Each team member answers:
├─ What did I do yesterday?
├─ What will I do today?
└─ Are there any impediments?

Purpose:
├─ Synchronize work
├─ Identify blockers
└─ Plan the day

Rules:
├─ Same time, same place
├─ Keep it brief
└─ Focus on coordination
```

### Sprint Review

```
┌─────────────────────────────────────────────────────────┐
│         Sprint Review                                  │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Demonstrate completed work
├─ Gather feedback
└─ Update product backlog

Participants:
├─ Development team
├─ Product Owner
├─ Stakeholders
└─ Users (if applicable)

Agenda:
├─ Review sprint goal
├─ Demo completed features
├─ Discuss what was learned
└─ Update backlog
```

### Sprint Retrospective

```
┌─────────────────────────────────────────────────────────┐
│         Sprint Retrospective                           │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Reflect on sprint
├─ Identify improvements
└─ Plan actions

Format:
├─ What went well?
├─ What could improve?
└─ What will we do differently?

Output:
├─ Action items
├─ Process improvements
└─ Team agreements
```

## Kanban Framework

### Kanban Overview

```
┌─────────────────────────────────────────────────────────┐
│         Kanban Principles                              │
└─────────────────────────────────────────────────────────┘

Visualize Work:
├─ Kanban board
├─ Work items visible
└─ Flow visible

Limit Work in Progress:
├─ WIP limits
├─ Focus on completion
└─ Reduce multitasking

Manage Flow:
├─ Optimize flow
├─ Reduce bottlenecks
└─ Improve throughput

Continuous Improvement:
├─ Regular reviews
├─ Process refinement
└─ Team learning
```

### Kanban Board

```
┌─────────────────────────────────────────────────────────┐
│         Kanban Board Structure                         │
└─────────────────────────────────────────────────────────┘

┌──────────┬──────────┬──────────┬──────────┬──────────┐
│  Backlog │   To Do  │  In      │  Review  │   Done   │
│          │          │ Progress │          │          │
│          │          │          │          │          │
│    [3]   │   [2]    │   [3]    │   [2]    │   [5]    │
│          │          │          │          │          │
│ WIP: ∞   │ WIP: 3   │ WIP: 3   │ WIP: 2   │ WIP: ∞   │
└──────────┴──────────┴──────────┴──────────┴──────────┘
```

### Kanban Metrics

```
┌─────────────────────────────────────────────────────────┐
│         Kanban Metrics                                 │
└─────────────────────────────────────────────────────────┘

Lead Time:
├─ Time from request to delivery
├─ End-to-end time
└─ Customer perspective

Cycle Time:
├─ Time from start to completion
├─ Active work time
└─ Team perspective

Throughput:
├─ Items completed per time period
├─ Velocity measure
└─ Productivity indicator

Work in Progress (WIP):
├─ Items currently being worked on
├─ Should be limited
└─ Focus indicator
```

## Sprint Planning Deep Dive

### Planning Process

```
┌─────────────────────────────────────────────────────────┐
│         Sprint Planning Steps                          │
└─────────────────────────────────────────────────────────┘

1. Review Product Backlog
   ├─ Prioritized items
   ├─ Dependencies
   └─ Ready items

2. Select Sprint Goal
   ├─ What to achieve
   ├─ Why it matters
   └─ Success criteria

3. Select Backlog Items
   ├─ Fit sprint goal
   ├─ Team capacity
   └─ Dependencies considered

4. Break Down Items
   ├─ User stories → tasks
   ├─ Estimate tasks
   └─ Identify dependencies

5. Commit to Sprint
   ├─ Team agreement
   ├─ Realistic commitment
   └─ Sprint backlog created
```

### Capacity Planning

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Calculation                          │
└─────────────────────────────────────────────────────────┘

Team Capacity:
├─ Available team members
├─ Working days in sprint
├─ Hours per day
└─ Deduct meetings, time off

Example:
├─ 5 team members
├─ 10 working days
├─ 6 hours/day (after meetings)
└─ Capacity: 5 × 10 × 6 = 300 hours

Buffer:
├─ Add 20% buffer for unknowns
├─ Final capacity: 240 hours
└─ Plan work accordingly
```

### Story Point Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Story Points                                   │
└─────────────────────────────────────────────────────────┘

Scale: Fibonacci (1, 2, 3, 5, 8, 13, 21)

Relative Sizing:
├─ Compare to reference story
├─ Not absolute time
└─ Team-specific

Planning Poker:
├─ Each estimates independently
├─ Discuss differences
└─ Reach consensus

Benefits:
├─ Focus on relative size
├─ Avoid anchoring
└─ Team discussion
```

## Agile Best Practices

### 1. Maintain Backlog

```
┌─────────────────────────────────────────────────────────┐
│         Product Backlog Management                     │
└─────────────────────────────────────────────────────────┘

Keep It Prioritized:
├─ Most valuable items first
├─ Regular refinement
└─ Remove obsolete items

Keep It Ready:
├─ Clear acceptance criteria
├─ Estimated
└─ Dependencies identified

Keep It Visible:
├─ Accessible to all
├─ Regularly reviewed
└─ Transparent priorities
```

### 2. Focus on Value

```
┌─────────────────────────────────────────────────────────┐
│         Value Delivery                                │
└─────────────────────────────────────────────────────────┘

Prioritize by Value:
├─ Business value
├─ User value
└─ Technical value

Deliver Incrementally:
├─ Small, frequent releases
├─ Early feedback
└─ Reduce risk

Measure Value:
├─ User adoption
├─ Business metrics
└─ Feedback quality
```

### 3. Embrace Change

```
┌─────────────────────────────────────────────────────────┐
│         Adapting to Change                            │
└─────────────────────────────────────────────────────────┘

Welcome Change:
├─ Requirements change
├─ Priorities shift
└─ Learn and adapt

Respond Quickly:
├─ Short iterations
├─ Frequent feedback
└─ Quick adjustments

Balance:
├─ Change vs stability
├─ Flexibility vs focus
└─ Adapt vs plan
```

## Scrum vs Kanban

### Comparison

```
┌─────────────────────────────────────────────────────────┐
│         Scrum vs Kanban                               │
└─────────────────────────────────────────────────────────┘

Scrum:
├─ Time-boxed sprints
├─ Fixed roles
├─ Ceremonies
└─ Best for: Projects with defined goals

Kanban:
├─ Continuous flow
├─ Flexible roles
├─ No ceremonies
└─ Best for: Ongoing work, support

Hybrid:
├─ Scrumban
├─ Combine both
└─ Best of both worlds
```

## Common Challenges

### Challenge 1: Scope Creep

```
┌─────────────────────────────────────────────────────────┐
│         Managing Scope Creep                          │
└─────────────────────────────────────────────────────────┘

Problem:
├─ New work added during sprint
├─ Sprint goal changes
└─ Team overwhelmed

Solution:
├─ Protect sprint goal
├─ Add to backlog, not sprint
├─ Say "no" when needed
└─ Use change control
```

### Challenge 2: Estimation Accuracy

```
┌─────────────────────────────────────────────────────────┐
│         Improving Estimates                           │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Estimates often wrong
├─ Underestimate complexity
└─ Overcommit

Solution:
├─ Use relative sizing
├─ Track velocity
├─ Include buffer
└─ Learn from past
```

### Challenge 3: Team Velocity

```
┌─────────────────────────────────────────────────────────┐
│         Velocity Management                           │
└─────────────────────────────────────────────────────────┘

Problem:
├─ Velocity varies
├─ Hard to predict
└─ Pressure to increase

Solution:
├─ Track over time
├─ Understand variation
├─ Focus on sustainable pace
└─ Don't game velocity
```

## Summary

Agile Methodologies:
- **Scrum**: Time-boxed sprints, defined roles, ceremonies
- **Kanban**: Continuous flow, WIP limits, visualize work
- **Sprint Planning**: Select work, break down, commit
- **Principles**: Value, collaboration, responding to change

**Key Practices:**
- Maintain prioritized backlog
- Focus on delivering value
- Embrace change
- Continuous improvement
- Team collaboration

**Best Practice**: Choose the methodology (Scrum, Kanban, or hybrid) that fits your team and work type, then adapt it to your specific needs while maintaining core agile principles.

# Lesson 195 - Sacrificial Architecture

## Overview

Sacrificial Architecture is a design approach where you intentionally build a system knowing it will be replaced or significantly refactored in the future. This lesson explains when and how to use this strategy.

## What is Sacrificial Architecture?

### Definition

Sacrificial Architecture is an architectural approach where you build a system with the explicit intention of replacing or significantly refactoring it later, accepting that the initial implementation is temporary.

```
┌─────────────────────────────────────────────────────────┐
│         Sacrificial Architecture Concept              │
└─────────────────────────────────────────────────────────┘

Initial System (Sacrificial)
    │
    ├─► Learn from it
    ├─► Validate assumptions
    ├─► Understand requirements
    └─► Build knowledge
    │
    ▼
Replace/Refactor
    │
    ▼
Production System (Long-term)
```

## When to Use Sacrificial Architecture

### Scenarios

```
┌─────────────────────────────────────────────────────────┐
│         When to Use Sacrificial Architecture          │
└─────────────────────────────────────────────────────────┘

1. Unknown Requirements
   ├─ Requirements unclear
   ├─ Need to learn domain
   └─ Validate assumptions

2. New Technology
   ├─ Learning new technology
   ├─ Evaluating technology
   └─ Proof of concept

3. Time Constraints
   ├─ Need quick solution
   ├─ Learn while building
   └─ Iterate based on feedback

4. High Uncertainty
   ├─ Market uncertainty
   ├─ Technical uncertainty
   └─ Business model uncertainty
```

## Benefits

### 1. Learning

```
┌─────────────────────────────────────────────────────────┐
│         Learning Benefits                              │
└─────────────────────────────────────────────────────────┘

Learn:
├─ Domain knowledge
├─ User needs
├─ Technical constraints
├─ Performance requirements
└─ Integration challenges
```

### 2. Risk Reduction

```
┌─────────────────────────────────────────────────────────┐
│         Risk Reduction                                │
└─────────────────────────────────────────────────────────┘

Reduce:
├─ Over-engineering risk
├─ Wrong technology risk
├─ Premature optimization risk
└─ Requirements misunderstanding risk
```

### 3. Faster Time to Market

```
┌─────────────────────────────────────────────────────────┐
│         Time to Market                                 │
└─────────────────────────────────────────────────────────┘

Sacrificial Architecture:
├─ Build quickly
├─ Get to market fast
├─ Learn from usage
└─ Build better version
```

## Sacrificial Architecture Patterns

### Pattern 1: Throwaway Prototype

```
┌─────────────────────────────────────────────────────────┐
│         Throwaway Prototype                           │
└─────────────────────────────────────────────────────────┘

Prototype (Sacrificial)
    │
    ├─► Learn requirements
    ├─► Validate approach
    └─► Understand domain
    │
    ▼
Production System (New)
    └─► Built with knowledge gained
```

### Pattern 2: Evolutionary Replacement

```
┌─────────────────────────────────────────────────────────┐
│         Evolutionary Replacement                      │
└─────────────────────────────────────────────────────────┘

Version 1 (Sacrificial)
    │
    ├─► Get to market
    ├─► Learn from users
    └─► Understand scale
    │
    ▼
Version 2 (Improved)
    │
    ├─► Address learned issues
    ├─► Better architecture
    └─► Production-ready
```

### Pattern 3: Strangler Pattern

```
┌─────────────────────────────────────────────────────────┐
│         Strangler Pattern                             │
└─────────────────────────────────────────────────────────┘

Old System (Sacrificial)
    │
    ├─► Gradually replaced
    ├─► Feature by feature
    └─► Eventually retired
    │
    ▼
New System
    └─► Replaces old system incrementally
```

## Design Principles

### 1. Keep It Simple

```
┌─────────────────────────────────────────────────────────┐
│         Simplicity Principles                         │
└─────────────────────────────────────────────────────────┘

Sacrificial Architecture:
├─ Simple design
├─ Minimal features
├─ Quick to build
└─ Easy to replace
```

### 2. Focus on Learning

```
┌─────────────────────────────────────────────────────────┐
│         Learning Focus                                │
└─────────────────────────────────────────────────────────┘

Prioritize:
├─ Understanding requirements
├─ Validating assumptions
├─ Learning domain
└─ Testing approaches
```

### 3. Plan for Replacement

```
┌─────────────────────────────────────────────────────────┐
│         Replacement Planning                          │
└─────────────────────────────────────────────────────────┘

Plan:
├─ Data migration strategy
├─ Feature parity requirements
├─ Transition timeline
└─ Knowledge transfer
```

## Common Mistakes

### 1. Not Planning Replacement

```
❌ BAD: Build sacrificial system, forget to replace it
✅ GOOD: Plan replacement from the start
```

### 2. Over-Engineering

```
❌ BAD: Build production-quality sacrificial system
✅ GOOD: Keep it simple, focus on learning
```

### 3. Ignoring Lessons Learned

```
❌ BAD: Build new system without applying lessons
✅ GOOD: Document and apply lessons learned
```

## Best Practices

### 1. Set Expectations
- Clearly communicate it's temporary
- Set replacement timeline
- Document replacement plan

### 2. Learn Actively
- Document lessons learned
- Validate assumptions
- Understand requirements

### 3. Plan Migration
- Plan data migration
- Plan feature migration
- Plan team transition

### 4. Time-Box
- Set replacement deadline
- Don't let it become permanent
- Regular review of replacement plan

## Summary

**Key Points:**
- Sacrificial Architecture: Intentionally temporary system
- Use when requirements unclear, learning needed, or time-constrained
- Benefits: Learning, risk reduction, faster time to market
- Patterns: Throwaway prototype, evolutionary replacement, strangler
- Plan for replacement from the start

**When to Use:**
- Unknown requirements
- New technology
- Time constraints
- High uncertainty

**Remember**: Sacrificial architecture is a learning tool. Use it to gain knowledge, then build the real system with that knowledge!

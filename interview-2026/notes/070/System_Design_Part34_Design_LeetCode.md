# System Design Interview: Design LeetCode (Online Judge) w/ a Ex-Meta Staff Engineer

## Overview

Designing an online judge like LeetCode requires handling code submissions, execution, test cases, and real-time results. This guide covers code execution architecture, sandboxing, and scaling strategies.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Submit code solutions
├─ Execute code in sandbox
├─ Run test cases
├─ Return results (pass/fail)
├─ Leaderboards
└─ Problem management

Non-Functional:
├─ Millions of submissions
├─ < 5s execution time
├─ Secure code execution
└─ High availability
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [Submission Service]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [Execution Service]  [Test Case DB]  [Result Service]
                    │               │               │
                    ▼               ▼               ▼
            [Sandbox]        [Problem DB]    [Result Storage]
```

## 1. Code Execution Flow

```
┌─────────────────────────────────────────────────────────┐
│         Execution Flow                                 │
└─────────────────────────────────────────────────────────┘

1. User submits code
   │
   ▼
2. Validate submission
   │
   ▼
3. Queue for execution
   │
   ▼
4. Execute in sandbox
   │
   ▼
5. Run test cases
   │
   ▼
6. Collect results
   │
   ▼
7. Return to user
```

## 2. Sandboxing

```
┌─────────────────────────────────────────────────────────┐
│         Sandbox Security                               │
└─────────────────────────────────────────────────────────┘

Isolation:
├─ Docker containers
├─ Resource limits (CPU, memory, time)
├─ Network restrictions
└─ File system isolation

Security:
├─ No network access
├─ Limited system calls
├─ Time limits
└─ Memory limits
```

## 3. Test Case Management

```
Test Cases:
├─ Public test cases (visible)
├─ Private test cases (hidden)
└─ Edge cases

Execution:
├─ Run all test cases
├─ Collect results
└─ Aggregate score
```

## Summary

LeetCode Design:
- **Submission Service**: Handle code submissions
- **Execution Service**: Run code in sandbox
- **Sandbox**: Secure code execution
- **Test Cases**: Public and private tests
- **Scaling**: Queue-based execution

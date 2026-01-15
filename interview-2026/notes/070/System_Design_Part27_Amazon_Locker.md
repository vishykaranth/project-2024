# Low-Level Design Interview: Design Amazon Locker w/ a Ex-Meta Staff Engineer

## Overview

Designing Amazon Locker requires managing locker locations, package assignments, user reservations, and pickup/delivery operations. This guide covers object-oriented design, state management, and system interactions.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Find nearby lockers
├─ Reserve locker for package
├─ Assign package to locker
├─ User pickup with code
├─ Admin operations
└─ Package expiration

Non-Functional:
├─ Thread-safe operations
├─ Handle concurrent access
└─ Data consistency
```

## Class Design

```
┌─────────────────────────────────────────────────────────┐
│              Class Diagram                             │
└─────────────────────────────────────────────────────────┘

Locker
├─ lockerId
├─ location
├─ size (Small, Medium, Large)
├─ status (Available, Reserved, Occupied)
└─ methods: reserve(), assign(), release()

LockerLocation
├─ locationId
├─ address
├─ lockers: List<Locker>
└─ methods: findAvailableLockers()

Package
├─ packageId
├─ size
├─ lockerId
├─ reservationCode
├─ status
└─ expirationTime

LockerService
├─ findNearbyLockers()
├─ reserveLocker()
├─ assignPackage()
├─ pickupPackage()
└─ releaseLocker()
```

## State Management

```
┌─────────────────────────────────────────────────────────┐
│         Locker State Machine                           │
└─────────────────────────────────────────────────────────┘

AVAILABLE → RESERVED → OCCUPIED → AVAILABLE
    │          │           │
    └──────────┴───────────┘
        (Release/Expire)
```

## Summary

Amazon Locker Design:
- **Classes**: Locker, Location, Package, Service
- **State Management**: State machine for locker states
- **Concurrency**: Thread-safe operations
- **Reservation**: Time-based reservations
- **Pickup**: Code-based authentication

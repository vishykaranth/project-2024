# System Design Interview: Design Ticketmaster w/ a Ex-Meta Staff Engineer

## Overview

Designing Ticketmaster requires handling ticket sales, seat selection, inventory management, high concurrency, and preventing overselling. This guide covers ticket booking flow, concurrency control, and inventory management.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Browse events
├─ Select seats
├─ Reserve tickets
├─ Payment processing
├─ Prevent overselling
└─ Handle high traffic

Non-Functional:
├─ Millions of users
├─ Thousands of concurrent bookings
├─ < 200ms response time
└─ Zero overselling
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [Booking Service]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [Inventory Service]  [Seat Service]  [Payment Service]
                    │               │               │
                    ▼               ▼               ▼
            [Inventory DB]    [Seat Map]     [Payment Gateway]
```

## 1. Booking Flow

```
┌─────────────────────────────────────────────────────────┐
│         Ticket Booking Flow                            │
└─────────────────────────────────────────────────────────┘

1. User selects event
   │
   ▼
2. View available seats
   │
   ▼
3. Select seats
   │
   ▼
4. Reserve seats (lock)
   │
   ▼
5. Process payment
   │
   ├─ Success → Confirm booking
   └─ Failure → Release seats
```

## 2. Concurrency Control

```
┌─────────────────────────────────────────────────────────┐
│         Preventing Overselling                         │
└─────────────────────────────────────────────────────────┘

Option 1: Database Locks
├─ Pessimistic locking
├─ Lock seat during booking
└─ Serialize requests

Option 2: Optimistic Locking
├─ Version-based updates
├─ Retry on conflict
└─ Better performance

Option 3: Distributed Lock
├─ Redis-based locking
├─ Timeout-based release
└─ Handle failures
```

## 3. Inventory Management

```
Seat Inventory:
├─ Total seats per event
├─ Available seats
├─ Reserved seats (temporary)
└─ Sold seats

Reservation:
├─ Lock seat for 5 minutes
├─ Auto-release if not paid
└─ Prevent double booking
```

## Summary

Ticketmaster Design:
- **Booking Service**: Core booking logic
- **Inventory Service**: Seat management
- **Concurrency Control**: Prevent overselling
- **Reservation System**: Temporary locks
- **Payment Integration**: Secure payments

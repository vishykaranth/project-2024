# OpenAI & Meta Senior/Staff System Design Mock Interview: Design Online Auction

## Overview

Designing an online auction system requires handling real-time bidding, concurrency control, time-based events, and payment processing. This guide covers auction lifecycle, bidding system, and scaling strategies.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Create auctions
├─ Place bids
├─ Real-time bid updates
├─ Auction end handling
├─ Winner determination
└─ Payment processing

Non-Functional:
├─ Handle concurrent bids
├─ Real-time updates
├─ High availability
└─ Data consistency
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [Auction Service]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [Bidding Service]   [WebSocket]    [Payment Service]
                    │               │               │
                    ▼               ▼               ▼
            [Database]      [Message Queue]  [Payment Gateway]
```

## 1. Auction Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│         Auction States                                 │
└─────────────────────────────────────────────────────────┘

CREATED → SCHEDULED → ACTIVE → ENDING → ENDED → SETTLED
    │         │         │        │       │        │
    └─────────┴─────────┴────────┴───────┴────────┘
```

## 2. Bidding System

### Concurrency Control

```
┌─────────────────────────────────────────────────────────┐
│         Bidding Concurrency                            │
└─────────────────────────────────────────────────────────┘

Option 1: Optimistic Locking
├─ Check version before update
├─ Retry on conflict
└─ Better for low contention

Option 2: Pessimistic Locking
├─ Lock auction during bid
├─ Serialize bids
└─ Better for high contention

Option 3: Message Queue
├─ Queue all bids
├─ Process sequentially
└─ Guaranteed ordering
```

## 3. Real-Time Updates

```
┌─────────────────────────────────────────────────────────┐
│         Real-Time Bid Updates                          │
└─────────────────────────────────────────────────────────┘

1. User places bid
   │
   ▼
2. Validate bid (amount, time)
   │
   ▼
3. Update database
   │
   ▼
4. Publish to message queue
   │
   ▼
5. Broadcast to all watchers via WebSocket
```

## Summary

Online Auction Design:
- **Auction Service**: Core auction logic
- **Bidding Service**: Handle concurrent bids
- **Real-time Updates**: WebSocket for live updates
- **Concurrency**: Optimistic/pessimistic locking
- **Payment**: Integration with payment gateway

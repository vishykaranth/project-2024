# System Design Interview: Design Tinder w/ a Ex-Meta Staff Engineer

## Overview

Designing Tinder requires matching algorithms, location-based services, real-time messaging, and handling millions of users. This guide covers matching system, location services, and messaging architecture.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ User profiles
├─ Swipe (like/dislike)
├─ Matching algorithm
├─ Messaging
├─ Location-based matching
└─ Recommendations

Non-Functional:
├─ Millions of users
├─ Real-time matching
├─ < 200ms swipe response
└─ High availability
```

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [Matching Service]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [Profile Service]   [Location Service] [Messaging]
                    │               │               │
                    ▼               ▼               ▼
            [Profile DB]    [Geospatial DB]  [Message Queue]
```

## 1. Matching Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Matching Score                                  │
└─────────────────────────────────────────────────────────┘

Score = w1 × Distance
      + w2 × Age Preference
      + w3 × Interest Match
      + w4 × Activity Level
      + w5 × Mutual Connections

Rank by score, return top N candidates
```

## 2. Swipe System

```
┌─────────────────────────────────────────────────────────┐
│         Swipe Flow                                     │
└─────────────────────────────────────────────────────────┘

1. User swipes right (like)
   │
   ▼
2. Check if mutual like
   │
   ├─ Yes → Create match
   └─ No → Store like
   │
   ▼
3. Update recommendation pool
```

## 3. Messaging

```
┌─────────────────────────────────────────────────────────┐
│         Messaging Architecture                         │
└─────────────────────────────────────────────────────────┘

Only matched users can message:
├─ Check match status
├─ WebSocket for real-time
├─ Message queue for reliability
└─ Store messages in database
```

## Summary

Tinder Design:
- **Matching Service**: Algorithm-based matching
- **Location Service**: Geospatial queries
- **Profile Service**: User profiles
- **Messaging**: Real-time chat
- **Recommendations**: ML-based suggestions

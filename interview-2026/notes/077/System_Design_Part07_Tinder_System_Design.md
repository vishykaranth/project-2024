# Tinder System Design | Dating App System Design

## Overview

Tinder is a location-based dating application that matches users based on proximity and preferences. The system must handle millions of users, real-time matching, and location-based services.

## System Requirements

### Functional Requirements
- User registration and authentication
- Profile creation and management
- Location-based user discovery
- Swipe left/right (like/dislike)
- Matching algorithm
- Messaging between matches
- Push notifications

### Non-Functional Requirements
- Handle millions of users
- Low latency (< 200ms)
- High availability (99.9%)
- Real-time updates
- Scalability

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Tinder System Architecture                     │
└─────────────────────────────────────────────────────────┘

Mobile Apps              API Gateway          Microservices
    │                            │                        │
    ├─► iOS                     │                        │
    ├─► Android                 │                        │
    └─► Web                     │                        │
        │                        │                        │
        └───Requests─────────────>│                        │
            │                    │                        │
            │                    ├───User Service───────>│
            │                    ├───Match Service──────>│
            │                    ├───Location Service───>│
            │                    ├───Messaging Service──>│
            │                    └───Notification Service>│
            │                    │                        │
            │<──Response─────────│                        │
            │                    │                        │
```

## Core Components

### 1. User Service

```
┌─────────────────────────────────────────────────────────┐
│         User Service                                    │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ User registration
├─ Authentication
├─ Profile management
├─ Preferences storage
└─ User search

Database:
├─ User profiles
├─ Preferences
├─ Settings
└─ Authentication data
```

### 2. Location Service

```
┌─────────────────────────────────────────────────────────┐
│         Location Service                                │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ Store user locations
├─ Find nearby users
├─ Geospatial queries
└─ Location updates

Storage:
├─ Geospatial database (Redis Geo)
├─ Location cache
└─ Location history
```

### 3. Match Service

```
┌─────────────────────────────────────────────────────────┐
│         Match Service                                   │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ Swipe processing
├─ Match detection
├─ Match storage
└─ Recommendation engine

Algorithm:
├─ Distance-based
├─ Preference matching
├─ Activity-based
└─ ML-based recommendations
```

### 4. Messaging Service

```
┌─────────────────────────────────────────────────────────┐
│         Messaging Service                               │
└─────────────────────────────────────────────────────────┘

Responsibilities:
├─ Message storage
├─ Real-time delivery
├─ Read receipts
└─ Message history

Technology:
├─ WebSocket for real-time
├─ Message queue
└─ Database for persistence
```

## Swipe System

### Swipe Flow

```
┌─────────────────────────────────────────────────────────┐
│         Swipe Processing Flow                           │
└─────────────────────────────────────────────────────────┘

User A                    Match Service          User B
    │                            │                        │
    │───Swipe Right─────────────>│                        │
    │    (Like User B)           │                        │
    │                            │                        │
    │                            ├───Check Match─────────>│
    │                            │    (Did B like A?)     │
    │                            │                        │
    │                            │<──No Match─────────────│
    │<──No Match─────────────────│                        │
    │                            │                        │
    │                            │ (Later, B swipes A)    │
    │                            │                        │
    │                            ├───Check Match─────────>│
    │                            │                        │
    │                            │<──Match!───────────────│
    │                            │                        │
    │<──Match!───────────────────│                        │
    │                            │                        │
    │                            ├───Notify──────────────>│
    │                            │    User B               │
    │                            │                        │
```

### Swipe Storage

```
┌─────────────────────────────────────────────────────────┐
│         Swipe Data Model                                │
└─────────────────────────────────────────────────────────┘

Swipe Table:
├─ swipe_id (PK)
├─ swiper_id (FK)
├─ swiped_id (FK)
├─ action (like/pass)
├─ timestamp
└─ Index: (swiper_id, swiped_id)

Match Table:
├─ match_id (PK)
├─ user1_id (FK)
├─ user2_id (FK)
├─ matched_at
└─ Index: (user1_id, user2_id)
```

## Location-Based Discovery

### Geospatial Queries

```
┌─────────────────────────────────────────────────────────┐
│         Location-Based Discovery                        │
└─────────────────────────────────────────────────────────┘

User Location: (lat, lon)
    │
    ▼
Find Users Within Radius
    │
    ├─► Redis Geo (Fast)
    ├─► PostgreSQL PostGIS
    └─► Elasticsearch Geo
    │
    ▼
Filter by Preferences
    │
    ├─► Age range
    ├─► Gender
    └─► Distance
    │
    ▼
Return Candidate List
```

### Redis Geo Implementation

```redis
# Add user location
GEOADD users 77.5946 12.9716 user123

# Find nearby users (within 5km)
GEORADIUS users 77.5946 12.9716 5 km WITHCOORD

# Get distance between users
GEODIST users user123 user456 km
```

## Matching Algorithm

### Basic Matching

```
┌─────────────────────────────────────────────────────────┐
│         Matching Algorithm                              │
└─────────────────────────────────────────────────────────┘

1. Distance Check
   ├─ Within max distance?
   └─ Yes → Continue

2. Preference Check
   ├─ Age range match?
   ├─ Gender preference?
   └─ All match → Continue

3. Swipe History
   ├─ Already swiped?
   └─ No → Show candidate

4. Activity Score
   ├─ Recent activity?
   └─ Higher score → Priority
```

### Advanced Matching (ML)

```
┌─────────────────────────────────────────────────────────┐
│         ML-Based Matching                               │
└─────────────────────────────────────────────────────────┘

Features:
├─ User preferences
├─ Swipe patterns
├─ Message interactions
├─ Profile similarity
└─ Behavioral patterns

Model:
├─ Collaborative filtering
├─ Content-based filtering
└─ Deep learning models

Output:
└─ Match score (0-1)
```

## Real-Time Messaging

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Messaging Architecture                          │
└─────────────────────────────────────────────────────────┘

User A                    WebSocket Server        User B
    │                            │                        │
    │───Connect─────────────────>│                        │
    │                            │                        │
    │───Send Message─────────────>│                        │
    │                            │                        │
    │                            ├───Store──────────────>│
    │                            │    Database            │
    │                            │                        │
    │                            ├───Forward─────────────>│
    │                            │    (If online)         │
    │                            │                        │
    │                            │<──Delivered────────────│
    │<──Acknowledged──────────────│                        │
    │                            │                        │
```

### Message Storage

```
┌─────────────────────────────────────────────────────────┐
│         Message Data Model                              │
└─────────────────────────────────────────────────────────┘

Messages Table:
├─ message_id (PK)
├─ match_id (FK)
├─ sender_id (FK)
├─ receiver_id (FK)
├─ content
├─ timestamp
├─ read_status
└─ Index: (match_id, timestamp)
```

## Push Notifications

```
┌─────────────────────────────────────────────────────────┐
│         Notification System                             │
└─────────────────────────────────────────────────────────┘

Event                    Notification Service    Push Service
    │                            │                        │
    ├─► New Match                │                        │
    ├─► New Message              │                        │
    ├─► Super Like               │                        │
    └─► Profile View             │                        │
        │                        │                        │
        └───Trigger──────────────>│                        │
            │                    │                        │
            │                    ├───Get Device Token───>│
            │                    │                        │
            │                    ├───Send Push──────────>│
            │                    │    (FCM/APNS)          │
            │                    │                        │
```

## Database Design

### User Schema

```sql
Users:
- user_id (PK)
- email
- password_hash
- name
- age
- gender
- bio
- photos[]
- preferences
- created_at

Preferences:
- user_id (FK)
- age_min
- age_max
- max_distance
- gender_preference
```

### Swipe Schema

```sql
Swipes:
- swipe_id (PK)
- swiper_id (FK)
- swiped_id (FK)
- action (like/pass)
- timestamp
- UNIQUE(swiper_id, swiped_id)

Matches:
- match_id (PK)
- user1_id (FK)
- user2_id (FK)
- matched_at
- UNIQUE(user1_id, user2_id)
```

## Scaling Strategies

### 1. Horizontal Scaling
- Multiple service instances
- Load balancing
- Database sharding

### 2. Caching
- User profiles (Redis)
- Location data (Redis Geo)
- Match results (Redis)

### 3. CDN
- Profile images
- Static content
- Media files

## Summary

Tinder System Design:
- **Components**: User, Location, Match, Messaging services
- **Key Features**: Swipe system, location-based discovery, matching algorithm
- **Technologies**: Redis Geo, WebSocket, Push notifications
- **Scaling**: Horizontal scaling, caching, CDN

**Key Challenges:**
- Real-time location updates
- Efficient geospatial queries
- Matching algorithm performance
- Real-time messaging
- High availability

# Google Docs System Design Part 1: Operational Transformation | Differential Synchronisation

## Overview

Google Docs enables real-time collaborative editing where multiple users can edit the same document simultaneously. This requires sophisticated conflict resolution algorithms: Operational Transformation (OT) and Differential Synchronization (DS).

## The Challenge

```
┌─────────────────────────────────────────────────────────┐
│         Collaborative Editing Challenge                │
└─────────────────────────────────────────────────────────┘

User A: "Hello World"
    │
    ├─► User A types: "Hello Beautiful World"
    └─► User B types: "Hello Wonderful World"
        │
        └─► How to merge both changes?
```

## Operational Transformation (OT)

### Concept

```
┌─────────────────────────────────────────────────────────┐
│         Operational Transformation                      │
└─────────────────────────────────────────────────────────┘

Operations:
├─ Insert(position, text)
├─ Delete(position, length)
└─ Retain(length)

Transform operations to account for concurrent changes
```

### OT Example

```
┌─────────────────────────────────────────────────────────┐
│         OT Conflict Resolution                          │
└─────────────────────────────────────────────────────────┘

Initial: "Hello World"
    │
    ├─► User A: Insert(6, "Beautiful ")
    │   Result: "Hello Beautiful World"
    │
    └─► User B: Insert(6, "Wonderful ")
        Result: "Hello Wonderful World"

OT Transformation:
├─ Transform User B's operation against User A's
├─ User B: Insert(6, "Wonderful ")
└─ After transform: Insert(15, "Wonderful ")
    │
    └─► Final: "Hello Beautiful Wonderful World"
```

### OT Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         OT Algorithm Flow                               │
└─────────────────────────────────────────────────────────┘

1. User A applies operation locally
    │
    ├─► Send to server
    │
2. Server receives operation
    │
    ├─► Transform against pending operations
    │
3. Server broadcasts transformed operation
    │
4. Other users apply transformed operation
```

## Differential Synchronization (DS)

### Concept

```
┌─────────────────────────────────────────────────────────┐
│         Differential Synchronization                    │
└─────────────────────────────────────────────────────────┘

Instead of transforming operations:
├─ Send document differences (diffs)
├─ Server merges diffs
└─ Send merged diff to clients
```

### DS Example

```
┌─────────────────────────────────────────────────────────┐
│         DS Conflict Resolution                          │
└─────────────────────────────────────────────────────────┘

Initial: "Hello World"
    │
    ├─► User A: "Hello Beautiful World"
    │   Diff: Insert "Beautiful " at position 6
    │
    └─► User B: "Hello Wonderful World"
        Diff: Insert "Wonderful " at position 6

Server:
├─ Receives both diffs
├─ Merges intelligently
└─ Sends merged diff to clients
    │
    └─► Result: "Hello Beautiful Wonderful World"
```

## System Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Google Docs Architecture                        │
└─────────────────────────────────────────────────────────┘

Clients                  Server                  Storage
    │                        │                        │
    ├─► User A               │                        │
    ├─► User B               │                        │
    └─► User C               │                        │
        │                    │                        │
        └───WebSocket────────>│                        │
            Connection        │                        │
            │                │                        │
            │                ├───Operation Queue─────>│
            │                │                        │
            │                ├───Transform/Merge──────>│
            │                │                        │
            │                ├───Broadcast────────────>│
            │                │                        │
            │<──Updates───────│                        │
            │                │                        │
            │                ├───Persist──────────────>│
            │                │    Document             │
            │                │                        │
```

## Operational Transformation Details

### Operation Types

```
┌─────────────────────────────────────────────────────────┐
│         Operation Types                                 │
└─────────────────────────────────────────────────────────┘

Insert Operation:
├─ position: Where to insert
├─ text: What to insert
└─ Example: Insert(5, "Hello")

Delete Operation:
├─ position: Where to delete
├─ length: How many characters
└─ Example: Delete(5, 3)

Retain Operation:
├─ length: How many characters to keep
└─ Example: Retain(5)
```

### Transformation Rules

```
┌─────────────────────────────────────────────────────────┐
│         Transformation Rules                            │
└─────────────────────────────────────────────────────────┘

Transform(Insert, Insert):
├─ If Insert1.pos < Insert2.pos:
│   └─► Insert2.pos += Insert1.length
└─ If Insert1.pos >= Insert2.pos:
    └─► Insert1.pos += Insert2.length

Transform(Insert, Delete):
├─ If Insert.pos <= Delete.pos:
│   └─► Delete.pos += Insert.length
└─ If Insert.pos > Delete.pos:
    └─► Insert.pos -= Delete.length
```

## Conflict Resolution

```
┌─────────────────────────────────────────────────────────┐
│         Conflict Resolution Strategies                  │
└─────────────────────────────────────────────────────────┘

1. Last Writer Wins (LWW):
   └─► Simple but loses data

2. Operational Transformation:
   ├─► Preserves all changes
   └─► Complex but correct

3. Three-Way Merge:
   ├─► Compare with base version
   └─► Merge intelligently
```

## Performance Optimizations

### 1. Operation Batching

```
┌─────────────────────────────────────────────────────────┐
│         Operation Batching                              │
└─────────────────────────────────────────────────────────┘

Instead of sending each keystroke:
├─ Batch multiple operations
├─ Send as single message
└─ Reduce network overhead
```

### 2. Compression

```
┌─────────────────────────────────────────────────────────┐
│         Operation Compression                           │
└─────────────────────────────────────────────────────────┘

Compress operations:
├─ Delta compression
├─ Operation merging
└─ Reduce payload size
```

### 3. Caching

```
┌─────────────────────────────────────────────────────────┐
│         Caching Strategy                                 │
└─────────────────────────────────────────────────────────┘

Cache:
├─ Document state
├─ Operation history
└─ User cursors
```

## Summary

Google Docs Part 1:
- **Challenge**: Real-time collaborative editing
- **Solutions**: OT and DS algorithms
- **Architecture**: WebSocket-based, operation queue
- **Conflict Resolution**: Transform operations intelligently

**Key Concepts:**
- Operational Transformation (OT)
- Differential Synchronization (DS)
- Operation types (Insert, Delete, Retain)
- Transformation rules
- Conflict resolution

**Next**: Part 2 covers system components and microservices architecture.

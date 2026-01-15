# Distributed Patterns - Complete Diagrams Guide (Part 8: Idempotency Keys)

## 🔑 Idempotency Keys Pattern

---

## 1. Idempotency Overview

### Problem: Duplicate Requests
```
┌─────────────────────────────────────────────────────────────┐
│              The Problem                                    │
└─────────────────────────────────────────────────────────────┘

Client
    │
    │ Request 1: Create Payment
    │
    ▼
┌──────────┐
│ Payment  │
│ Service  │
│          │
│ Process Payment
│──────────┐
│          │
│    ⏱️  Slow response
│          │
│◄─────────┘
│ (timeout)
│
└──────────┘
    │
    │ Client retries (Request 2)
    │
    ▼
┌──────────┐
│ Payment  │
│ Service  │
│          │
│ Process Payment (again!)
│──────────┐
│          │
│◄─────────┘
│ ✅ Success
│
└──────────┘

Problem:
- Payment processed twice
- Duplicate charges
- Inconsistent state
- Data corruption
```

### Solution: Idempotency Keys
```
┌─────────────────────────────────────────────────────────────┐
│              Idempotency Keys Solution                      │
└─────────────────────────────────────────────────────────────┘

Client
    │
    │ Request 1: Create Payment
    │ Idempotency-Key: "key-123"
    │
    ▼
┌──────────┐
│ Payment  │
│ Service  │
│          │
│ Check if "key-123" processed?
│──────────┐
│          │
│    No ──► Process Payment
│          │
│    Store "key-123" → Result
│          │
│◄─────────┘
│ ✅ Success (slow)
│
└──────────┘
    │
    │ Client retries (Request 2)
    │ Idempotency-Key: "key-123"
    │
    ▼
┌──────────┐
│ Payment  │
│ Service  │
│          │
│ Check if "key-123" processed?
│──────────┐
│          │
│    Yes ──► Return cached result
│          │
│◄─────────┘
│ ✅ Success (immediate)
│ (same result as Request 1)
│
└──────────┘

Benefits:
- Safe retries
- No duplicate processing
- Consistent results
- Better user experience
```

---

## 2. Idempotency Key Flow

### Request Processing Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Idempotency Key Flow                           │
└─────────────────────────────────────────────────────────────┘

Request Arrives
    │
    │ Extract Idempotency-Key header
    │
    ▼
┌──────────────────┐
│  Check Cache/DB  │
│  for Key         │
└──────┬───────────┘
       │
   ┌───┴───┐
   │       │
   ▼       ▼
Found    Not Found
   │       │
   │       ▼
   │   ┌──────────────────┐
   │   │  Process Request  │
   │   │  (Business Logic)  │
   │   └──────┬─────────────┘
   │          │
   │          ▼
   │      ┌──────────────────┐
   │      │  Store Result    │
   │      │  with Key        │
   │      └──────┬───────────┘
   │             │
   │             ▼
   │         Return Result
   │
   ▼
┌──────────────────┐
│  Return Cached   │
│  Result          │
│  (Same as before)│
└──────────────────┘
```

---

## 3. Idempotency Key Storage

### Storage Options
```
┌─────────────────────────────────────────────────────────────┐
│              Storage Options                                 │
└─────────────────────────────────────────────────────────────┘

Option 1: In-Memory Cache (Redis)
    ┌─────────────────────────────────────┐
    │  Key: "idempotency:key-123"         │
    │  Value: {                           │
    │    "result": {...},                 │
    │    "status": 200,                   │
    │    "timestamp": "2024-01-15..."     │
    │  }                                  │
    │  TTL: 24 hours                      │
    └─────────────────────────────────────┘
    
    Pros: Fast, scalable
    Cons: Lost on restart, limited retention

Option 2: Database Table
    ┌─────────────────────────────────────┐
    │  idempotency_keys Table:            │
    │  ────────────────────────────────── │
    │  key (PK) | result | status | time │
    │  ────────┼────────┼────────┼───────│
    │  key-123  | {...}  | 200    | ...  │
    └─────────────────────────────────────┘
    
    Pros: Persistent, reliable
    Cons: Slower, needs cleanup

Option 3: Hybrid (Cache + DB)
    ┌─────────────────────────────────────┐
    │  1. Check Redis (fast)              │
    │     │                                │
    │     ├───► Found: Return            │
    │     │                                │
    │     └───► Not Found:                │
    │           │                          │
    │           2. Check Database         │
    │              │                        │
    │              ├───► Found:            │
    │              │     Cache in Redis    │
    │              │     Return            │
    │              │                        │
    │              └───► Not Found:        │
    │                    Process & Store    │
    └─────────────────────────────────────┘
    
    Pros: Fast + Reliable
    Cons: More complex
```

---

## 4. Idempotency Key Schema

### Database Schema
```
┌─────────────────────────────────────────────────────────────┐
│              Database Schema                                │
└─────────────────────────────────────────────────────────────┘

idempotency_keys Table:
    ┌─────────────────────────────────────┐
    │  Column          | Type             │
    │  ───────────────┼──────────────────│
    │  idempotency_key | VARCHAR (PK)     │
    │  request_hash    | VARCHAR          │
    │  response_body   | TEXT/JSON        │
    │  status_code     | INTEGER          │
    │  created_at      | TIMESTAMP        │
    │  expires_at      | TIMESTAMP        │
    └─────────────────────────────────────┘

Indexes:
    ┌─────────────────────────────────────┐
    │  PRIMARY KEY (idempotency_key)      │
    │  INDEX (expires_at)                 │
    └─────────────────────────────────────┘

Example Record:
    ┌─────────────────────────────────────┐
    │  idempotency_key: "key-123"        │
    │  request_hash: "sha256(...)"       │
    │  response_body: '{"paymentId":     │
    │                    "pay-456",      │
    │                    "status":       │
    │                    "success"}'     │
    │  status_code: 200                  │
    │  created_at: "2024-01-15 10:00:00" │
    │  expires_at: "2024-01-16 10:00:00" │
    └─────────────────────────────────────┘
```

---

## 5. Request Hash Validation

### Detecting Request Changes
```
┌─────────────────────────────────────────────────────────────┐
│              Request Hash Validation                        │
└─────────────────────────────────────────────────────────────┘

Scenario 1: Same Key, Same Request
    Request 1:
        Idempotency-Key: "key-123"
        Body: {"amount": 100, "orderId": "order-1"}
        Hash: sha256("key-123" + body) = "hash-abc"
    
    Request 2 (Retry):
        Idempotency-Key: "key-123"
        Body: {"amount": 100, "orderId": "order-1"}
        Hash: sha256("key-123" + body) = "hash-abc"
    
    ✅ Hashes match → Return cached result

Scenario 2: Same Key, Different Request
    Request 1:
        Idempotency-Key: "key-123"
        Body: {"amount": 100, "orderId": "order-1"}
        Hash: "hash-abc"
    
    Request 2 (Different):
        Idempotency-Key: "key-123"
        Body: {"amount": 200, "orderId": "order-1"}
        Hash: sha256("key-123" + body) = "hash-xyz"
    
    ❌ Hashes don't match → Reject request
    Error: "Idempotency key conflict"
```

### Hash Calculation
```
┌─────────────────────────────────────────────────────────────┐
│              Hash Calculation                               │
└─────────────────────────────────────────────────────────────┘

Request Hash:
    ┌─────────────────────────────────────┐
    │  Components:                       │
    │  - Idempotency-Key                  │
    │  - HTTP Method                      │
    │  - Request Path                    │
    │  - Request Body                    │
    │  - Query Parameters                 │
    │                                      │
    │  Hash = SHA256(                     │
    │    key + method + path + body + query│
    │  )                                  │
    └─────────────────────────────────────┘

Example:
    ┌─────────────────────────────────────┐
    │  Key: "key-123"                    │
    │  Method: "POST"                    │
    │  Path: "/api/payments"             │
    │  Body: '{"amount": 100}'           │
    │  Query: ""                         │
    │                                      │
    │  Hash = SHA256(                     │
    │    "key-123" +                      │
    │    "POST" +                         │
    │    "/api/payments" +                │
    │    '{"amount": 100}' +              │
    │    ""                               │
    │  )                                  │
    │  = "a1b2c3d4..."                    │
    └─────────────────────────────────────┘
```

---

## 6. Idempotency Key Implementation

### Interceptor/Filter Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Implementation Pattern                         │
└─────────────────────────────────────────────────────────────┘

Request Flow:
    ┌─────────────────────────────────────┐
    │  1. Request arrives                 │
    │     │                                │
    │     ▼                                │
    │  2. Idempotency Filter              │
    │     - Extract header                │
    │     - Calculate request hash         │
    │     │                                │
    │     ▼                                │
    │  3. Check Storage                   │
    │     - Lookup by key                 │
    │     │                                │
    │     ├───► Found:                     │
    │     │     - Compare hash             │
    │     │     - Match: Return cached      │
    │     │     - Mismatch: Error          │
    │     │                                │
    │     └───► Not Found:                 │
    │           │                          │
    │           ▼                          │
    │       4. Process Request             │
    │          │                           │
    │          ▼                           │
    │       5. Store Result                │
    │          - Key + Hash + Response    │
    │          │                           │
    │          ▼                           │
    │       6. Return Response            │
    └─────────────────────────────────────┘
```

### Code Example
```
┌─────────────────────────────────────────────────────────────┐
│              Code Example                                   │
└─────────────────────────────────────────────────────────────┘

@RestController
public class PaymentController {
    
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> createPayment(
        @RequestHeader("Idempotency-Key") String key,
        @RequestBody PaymentRequest request
    ) {
        // 1. Check idempotency
        IdempotencyRecord record = 
            idempotencyService.get(key);
        
        if (record != null) {
            // 2. Validate request hash
            String requestHash = calculateHash(
                key, request
            );
            
            if (record.getRequestHash()
                .equals(requestHash)) {
                // 3. Return cached response
                return ResponseEntity
                    .status(record.getStatusCode())
                    .body(record.getResponse());
            } else {
                // 4. Hash mismatch
                throw new IdempotencyKeyConflictException();
            }
        }
        
        // 5. Process new request
        PaymentResponse response = 
            paymentService.process(request);
        
        // 6. Store result
        idempotencyService.store(
            key,
            calculateHash(key, request),
            response,
            200
        );
        
        return ResponseEntity.ok(response);
    }
}
```

---

## 7. Idempotency Key Expiration

### Expiration Strategy
```
┌─────────────────────────────────────────────────────────────┐
│              Expiration Strategy                            │
└─────────────────────────────────────────────────────────────┘

TTL Configuration:
    ┌─────────────────────────────────────┐
    │  Payment: 24 hours                    │
    │  Order: 7 days                        │
    │  Refund: 30 days                      │
    │  General: 24 hours (default)          │
    └─────────────────────────────────────┘

Cleanup Process:
    ┌─────────────────────────────────────┐
    │  @Scheduled(cron = "0 0 2 * * *")   │
    │  public void cleanupExpiredKeys() {  │
    │    LocalDateTime cutoff =            │
    │      LocalDateTime.now()             │
    │        .minusHours(24);               │
    │                                      │
    │    idempotencyRepository             │
    │      .deleteByCreatedAtBefore(cutoff);│
    │  }                                   │
    └─────────────────────────────────────┘

Redis TTL:
    ┌─────────────────────────────────────┐
    │  SET idempotency:key-123 {...}      │
    │  EXPIRE idempotency:key-123 86400   │
    │  (24 hours = 86400 seconds)         │
    └─────────────────────────────────────┘
```

---

## 8. Real-World Example

### Payment Service with Idempotency
```
┌─────────────────────────────────────────────────────────────┐
│              Payment Service Example                       │
└─────────────────────────────────────────────────────────────┘

Client Request:
    POST /api/payments
    Headers:
        Idempotency-Key: "pay-123-456"
    Body:
        {
            "orderId": "order-789",
            "amount": 100.00,
            "cardToken": "tok_abc"
        }

Server Processing:
    1. Extract key: "pay-123-456"
    2. Calculate hash from request
    3. Check Redis:
       - Key exists? No
    4. Process payment:
       - Charge card: ✅ Success
       - Payment ID: "pay-202"
    5. Store in Redis:
       Key: "idempotency:pay-123-456"
       Value: {
           "requestHash": "abc123...",
           "response": {
               "paymentId": "pay-202",
               "status": "success"
           },
           "statusCode": 200
       }
       TTL: 24 hours
    6. Return response

Client Retry (Same Request):
    POST /api/payments
    Headers:
        Idempotency-Key: "pay-123-456"
    Body: (same as before)

Server Processing:
    1. Extract key: "pay-123-456"
    2. Calculate hash from request
    3. Check Redis:
       - Key exists? Yes
       - Hash matches? Yes
    4. Return cached response:
       {
           "paymentId": "pay-202",
           "status": "success"
       }
    (No duplicate charge!)
```

---

## Key Concepts Summary

### Idempotency Key Benefits
```
✅ Safe retries
✅ No duplicate processing
✅ Consistent results
✅ Better UX (fast retries)
✅ Prevents data corruption
```

### Idempotency Key Challenges
```
❌ Storage overhead
❌ Key generation responsibility
❌ Hash validation complexity
❌ Expiration management
❌ Storage cleanup needed
```

### Best Practices
```
1. Client generates unique keys
2. Include request hash for validation
3. Set appropriate TTL
4. Use fast storage (Redis)
5. Clean up expired keys
6. Handle hash mismatches
7. Document key format
8. Monitor key usage
```

---

## Complete Pattern Summary

### All 8 Patterns Covered:
1. ✅ Circuit Breaker: Fault tolerance, fallback mechanisms
2. ✅ Bulkhead: Resource isolation, failure containment
3. ✅ Retry Patterns: Exponential backoff, jitter, retry policies
4. ✅ Saga Pattern: Distributed transactions overview
5. ✅ Saga Choreography: Event-driven coordination
6. ✅ Saga Orchestration: Centralized coordination
7. ✅ Outbox Pattern: Reliable event publishing
8. ✅ Idempotency Keys: Safe retries, duplicate detection

All patterns include:
- Detailed diagrams
- Implementation examples
- Real-world scenarios
- Best practices
- Key concepts

**Complete guide ready for distributed systems design!** 🚀


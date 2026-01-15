# Messaging Patterns - Complete Guide (Part 5: Message Transformation)

## 🔄 Message Transformation: Enricher and Translator Patterns

---

## 1. Basic Message Transformation Concepts

### Transformation Fundamentals
```
┌─────────────────────────────────────────────────────────────┐
│              Message Transformation                          │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Original Message
         │
         ▼
    ┌──────────┐
    │Transform │
    │  Engine  │
    └────┬─────┘
         │
         │ Transform:
         │ - Format conversion
         │ - Data enrichment
         │ - Structure modification
         │ - Content translation
         │
         ▼
    ┌──────────┐
    │Transformed│
    │ Message  │
    └────┬─────┘
         │
         ▼
    Consumer receives transformed message
```

### Transformation Types
```
┌─────────────────────────────────────────────────────────────┐
│              Transformation Types                            │
└─────────────────────────────────────────────────────────────┘

1. Format Transformation:
   XML → JSON
   JSON → XML
   CSV → JSON
   Binary → Text

2. Structure Transformation:
   Flatten nested structure
   Restructure data
   Add/remove fields
   Rename fields

3. Content Enrichment:
   Add missing data
   Lookup external data
   Calculate derived fields
   Merge with reference data

4. Content Translation:
   Language translation
   Unit conversion
   Code mapping
   Data normalization
```

---

## 2. Message Translator Pattern

### Basic Translator
```
┌─────────────────────────────────────────────────────────────┐
│              Message Translator                              │
└─────────────────────────────────────────────────────────────┘

    Source System
         │
         │ Message (Format A)
         │ {
         │   "order_id": 123,
         │   "customer_name": "John",
         │   "total": 100.50
         │ }
         │
         ▼
    ┌──────────┐
    │Translator│
    └────┬─────┘
         │
         │ Transform to Format B
         │
         ▼
    Target System
         │
         │ Message (Format B)
         │ <order>
         │   <id>123</id>
         │   <customer>John</customer>
         │   <amount>100.50</amount>
         │ </order>
```

### Format Translation Examples
```
┌─────────────────────────────────────────────────────────────┐
│              Format Translation                              │
└─────────────────────────────────────────────────────────────┘

JSON to XML:
Input (JSON):
{
    "order": {
        "id": 123,
        "items": [
            {"product": "A", "qty": 2},
            {"product": "B", "qty": 1}
        ]
    }
}

Output (XML):
<order>
    <id>123</id>
    <items>
        <item>
            <product>A</product>
            <qty>2</qty>
        </item>
        <item>
            <product>B</product>
            <qty>1</qty>
        </item>
    </items>
</order>

CSV to JSON:
Input (CSV):
order_id,customer,total
123,John,100.50
124,Jane,200.75

Output (JSON):
[
    {"order_id": 123, "customer": "John", "total": 100.50},
    {"order_id": 124, "customer": "Jane", "total": 200.75}
]
```

### Structure Transformation
```
┌─────────────────────────────────────────────────────────────┐
│              Structure Transformation                        │
└─────────────────────────────────────────────────────────────┘

Input (Nested):
{
    "order": {
        "id": 123,
        "customer": {
            "name": "John",
            "email": "john@example.com"
        },
        "items": [
            {"product": "A", "price": 50}
        ]
    }
}

Output (Flattened):
{
    "order_id": 123,
    "customer_name": "John",
    "customer_email": "john@example.com",
    "item_product": "A",
    "item_price": 50
}

Transformation Rules:
- order.id → order_id
- order.customer.name → customer_name
- order.customer.email → customer_email
- order.items[0].product → item_product
- order.items[0].price → item_price
```

---

## 3. Message Enricher Pattern

### Basic Enricher
```
┌─────────────────────────────────────────────────────────────┐
│              Message Enricher                                │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Original Message
         │ {
         │   "order_id": 123,
         │   "customer_id": 456
         │ }
         │
         ▼
    ┌──────────┐
    │ Enricher │
    └────┬─────┘
         │
         │ Lookup customer data
         │ Lookup product data
         │ Calculate totals
         │
         ▼
    ┌──────────┐
    │ External │
    │  Data    │  (Database, API, Cache)
    └────┬─────┘
         │
         │ Merge data
         │
         ▼
    Enriched Message
         │
         │ {
         │   "order_id": 123,
         │   "customer_id": 456,
         │   "customer_name": "John Doe",      ← Added
         │   "customer_email": "john@...",     ← Added
         │   "customer_address": "...",        ← Added
         │   "order_total": 1500.00,           ← Calculated
         │   "order_status": "confirmed"        ← Added
         │ }
         │
         ▼
    Consumer
```

### Enrichment Sources
```
┌─────────────────────────────────────────────────────────────┐
│              Enrichment Sources                              │
└─────────────────────────────────────────────────────────────┘

1. Database Lookup:
   Message: {customer_id: 456}
   ↓
   Database Query: SELECT * FROM customers WHERE id = 456
   ↓
   Enriched: {customer_id: 456, customer_name: "John", ...}

2. External API:
   Message: {product_id: 789}
   ↓
   API Call: GET /api/products/789
   ↓
   Enriched: {product_id: 789, product_name: "Widget", ...}

3. Cache:
   Message: {order_id: 123}
   ↓
   Cache Lookup: order:123
   ↓
   Enriched: {order_id: 123, cached_data: {...}}

4. File System:
   Message: {file_id: "abc"}
   ↓
   File Read: /data/files/abc.json
   ↓
   Enriched: {file_id: "abc", file_content: {...}}
```

### Enrichment Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Enrichment Flow                                 │
└─────────────────────────────────────────────────────────────┘

Step 1: Receive message
    Message: {order_id: 123, customer_id: 456}

Step 2: Extract enrichment keys
    Keys: customer_id = 456

Step 3: Lookup enrichment data
    Database: SELECT * FROM customers WHERE id = 456
    Result: {name: "John", email: "john@...", address: "..."}

Step 4: Merge data
    Original: {order_id: 123, customer_id: 456}
    Enrichment: {customer_name: "John", customer_email: "...", ...}
    Merged: {order_id: 123, customer_id: 456, customer_name: "John", ...}

Step 5: Send enriched message
    Enriched message sent to next step
```

---

## 4. Content Enricher Variants

### Content Filter
```
┌─────────────────────────────────────────────────────────────┐
│              Content Filter                                  │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message with all fields
         │ {
         │   "order_id": 123,
         │   "customer_id": 456,
         │   "internal_notes": "...",
         │   "debug_info": "...",
         │   "sensitive_data": "..."
         │ }
         │
         ▼
    ┌──────────┐
    │  Filter  │
    └────┬─────┘
         │
         │ Remove sensitive/internal fields
         │
         ▼
    Filtered Message
         │
         │ {
         │   "order_id": 123,
         │   "customer_id": 456
         │   // internal_notes removed
         │   // debug_info removed
         │   // sensitive_data removed
         │ }
         │
         ▼
    Consumer
```

### Content-Based Router with Enrichment
```
┌─────────────────────────────────────────────────────────────┐
│              Router with Enrichment                          │
└─────────────────────────────────────────────────────────────┘

    Producer
         │
         │ Message: {order_id: 123, customer_id: 456}
         │
         ▼
    ┌──────────┐
    │ Enricher │
    └────┬─────┘
         │
         │ Enrich with customer data
         │
         ▼
    Enriched Message: {
        order_id: 123,
        customer_id: 456,
        customer_region: "US",      ← Added
        customer_tier: "premium"    ← Added
    }
         │
         ▼
    ┌──────────┐
    │  Router  │
    └────┬─────┘
         │
         │ Route based on enriched data
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Q1   Q2   Q3
   
Routing:
- customer_region == "US" → Q1
- customer_tier == "premium" → Q2
- order_id > 100 → Q3
```

### Claim Check Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Claim Check Pattern                             │
└─────────────────────────────────────────────────────────────┘

Step 1: Store large message
    Large Message (10MB)
         │
         ▼
    ┌──────────┐
    │ Storage  │  (File system, S3, etc.)
    │          │
    │ Stored with ID: "claim-12345"
    └──────────┘

Step 2: Send claim check
    Small Message
         │
         │ {
         │   "claim_id": "claim-12345",
         │   "order_id": 123,
         │   "metadata": {...}
         │ }
         │
         ▼
    Queue

Step 3: Retrieve when needed
    Consumer receives claim check
         │
         │ Extract claim_id
         │
         ▼
    ┌──────────┐
    │ Storage  │
    │          │
    │ Retrieve: "claim-12345"
    └────┬─────┘
         │
         │ Full message retrieved
         │
         ▼
    Consumer processes full message
```

---

## 5. Normalizer Pattern

### Message Normalizer
```
┌─────────────────────────────────────────────────────────────┐
│              Message Normalizer                              │
└─────────────────────────────────────────────────────────────┘

    Source 1          Source 2          Source 3
         │                 │                 │
         │ Format A        │ Format B        │ Format C
         │                 │                 │
         └─────────┬───────┴─────────────────┘
                   │
                   ▼
            ┌──────────┐
            │Normalizer│
            └────┬─────┘
                 │
                 │ Convert all to standard format
                 │
                 ▼
            Standard Format
                 │
                 ▼
            Consumer
                 
Benefits:
- Single consumer handles all formats
- Format conversion centralized
- Easy to add new sources
```

### Normalization Example
```
┌─────────────────────────────────────────────────────────────┐
│              Normalization Example                          │
└─────────────────────────────────────────────────────────────┘

Input Format 1 (XML):
<order>
    <id>123</id>
    <customer>John</customer>
    <total>100.50</total>
</order>

Input Format 2 (JSON):
{
    "order_id": 123,
    "customer_name": "John",
    "amount": 100.50
}

Input Format 3 (CSV):
order_id,customer,total
123,John,100.50

Normalized Output (Standard JSON):
{
    "orderId": 123,
    "customerName": "John",
    "totalAmount": 100.50,
    "currency": "USD",
    "timestamp": "2024-01-01T12:00:00Z"
}

Normalization Rules:
- All formats → Standard JSON
- Field names standardized (camelCase)
- Data types normalized
- Default values added
- Timestamps added
```

---

## 6. Canonical Data Model

### Canonical Model Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Canonical Data Model                            │
└─────────────────────────────────────────────────────────────┘

    System A          System B          System C
         │                 │                 │
         │ Format A        │ Format B        │ Format C
         │                 │                 │
         └─────────┬───────┴─────────────────┘
                   │
                   ▼
            ┌──────────┐
            │Translator│
            │   to     │
            │Canonical │
            └────┬─────┘
                 │
                 │ Canonical Format
                 │
                 ▼
            ┌──────────┐
            │Canonical │
            │  Model   │
            └────┬─────┘
                 │
                 │ Translate from canonical
                 │
         ┌───────┼───────┐
         │       │       │
         ▼       ▼       ▼
      System D  System E  System F
```

### Canonical Model Example
```
┌─────────────────────────────────────────────────────────────┐
│              Canonical Model Example                         │
└─────────────────────────────────────────────────────────────┘

Canonical Format (Standard):
{
    "order": {
        "id": "string",
        "customer": {
            "id": "string",
            "name": "string",
            "email": "string"
        },
        "items": [
            {
                "productId": "string",
                "quantity": "number",
                "price": "number"
            }
        ],
        "total": "number",
        "currency": "string",
        "status": "enum",
        "timestamp": "ISO8601"
    }
}

Translations:
- System A Format → Canonical → System B Format
- System B Format → Canonical → System C Format
- System C Format → Canonical → System A Format

Benefits:
- N translations instead of N²
- Single source of truth
- Easier maintenance
```

---

## 7. Real-World Examples

### Order Processing Pipeline
```
┌─────────────────────────────────────────────────────────────┐
│              Order Processing with Transformation            │
└─────────────────────────────────────────────────────────────┘

    Order Service
         │
         │ Order: {order_id: 123, customer_id: 456}
         │
         ▼
    ┌──────────┐
    │ Enricher │
    └────┬─────┘
         │
         │ Enrich with:
         │ - Customer details
         │ - Product details
         │ - Pricing information
         │
         ▼
    Enriched Order
         │
         ▼
    ┌──────────┐
    │Translator│
    └────┬─────┘
         │
         │ Transform to:
         │ - Fulfillment format
         │ - Billing format
         │ - Analytics format
         │
    ┌────┼────┐
    │    │    │
    ▼    ▼    ▼
   Fulfillment  Billing  Analytics
```

### Data Integration Hub
```
┌─────────────────────────────────────────────────────────────┐
│              Data Integration Hub                            │
└─────────────────────────────────────────────────────────────┘

    Legacy System A    Legacy System B    Modern System C
         │                   │                   │
         │ Format A          │ Format B          │ Format C
         │                   │                   │
         └───────────┬───────┴───────────────────┘
                     │
                     ▼
            ┌──────────┐
            │Normalizer│
            └────┬─────┘
                 │
                 │ Canonical Format
                 │
                 ▼
            ┌──────────┐
            │  Hub     │
            └────┬─────┘
                 │
                 │ Transform to target format
                 │
         ┌───────┼───────┐
         │       │       │
         ▼       ▼       ▼
      System D  System E  System F
```

---

## 8. Implementation Examples

### Java (Camel Enricher)
```java
from("direct:orders")
    .enrich("direct:customer-lookup", new AggregationStrategy() {
        public Exchange aggregate(Exchange original, Exchange lookup) {
            // Merge customer data into original message
            Customer customer = lookup.getIn().getBody(Customer.class);
            Order order = original.getIn().getBody(Order.class);
            order.setCustomerName(customer.getName());
            order.setCustomerEmail(customer.getEmail());
            return original;
        }
    })
    .to("queue:enriched-orders");

from("direct:customer-lookup")
    .setHeader("customerId", simple("${body.customerId}"))
    .to("jdbc:dataSource?query=SELECT * FROM customers WHERE id = :#customerId");
```

### Python (Message Transformation)
```python
def enrich_order(message):
    order = json.loads(message)
    customer_id = order['customer_id']
    
    # Lookup customer data
    customer = db.query("SELECT * FROM customers WHERE id = %s", customer_id)
    
    # Enrich message
    order['customer_name'] = customer['name']
    order['customer_email'] = customer['email']
    order['customer_address'] = customer['address']
    
    # Calculate totals
    order['order_total'] = sum(item['price'] * item['qty'] for item in order['items'])
    
    return json.dumps(order)

def translate_json_to_xml(json_message):
    data = json.loads(json_message)
    xml = dicttoxml.dicttoxml(data)
    return xml

# Usage
enriched = enrich_order(original_message)
transformed = translate_json_to_xml(enriched)
```

---

## Key Characteristics Summary

### Message Transformation
```
✅ Format Conversion: XML ↔ JSON ↔ CSV
✅ Structure Modification: Flatten, restructure
✅ Content Enrichment: Add missing data
✅ Content Filtering: Remove sensitive data
✅ Normalization: Standard format
✅ Canonical Model: Single source of truth
✅ Claim Check: Handle large messages
```

### When to Use
```
✅ System Integration: Different formats
✅ Data Enrichment: Add missing information
✅ Format Standardization: Normalize formats
✅ Legacy Integration: Convert legacy formats
✅ API Transformation: Transform API responses
✅ Data Migration: Transform during migration
✅ Protocol Conversion: Different protocols
```

### When NOT to Use
```
❌ Simple Pass-Through: No transformation needed
❌ Performance Critical: Transformation adds overhead
❌ Same Format: No conversion needed
❌ Real-Time: Transformation may add latency
```

---

**Next: Part 6 will cover Message Aggregation (Aggregator, splitter patterns).**


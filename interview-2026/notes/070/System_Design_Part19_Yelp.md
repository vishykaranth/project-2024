# Design Yelp, Meta Staff Product Architecture: Hello Interview Mock

## Overview

Designing Yelp requires handling business listings, reviews, ratings, search, recommendations, and location-based queries. This guide covers the architecture, search strategies, and recommendation systems.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Search businesses by name, category, location
├─ View business details and reviews
├─ Write and read reviews
├─ Rating system
├─ Recommendations
└─ Photos and media

Non-Functional:
├─ Millions of businesses
├─ Billions of reviews
├─ < 200ms search latency
└─ High availability
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│              System Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [API Gateway]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [Search Service]    [Business Service] [Review Service]
                    │               │               │
                    ▼               ▼               ▼
            [Elasticsearch]   [Database]      [Review Storage]
```

## 1. Data Model

```
Businesses Table:
├─ business_id (PK)
├─ name
├─ category
├─ location (lat, lng)
├─ address
├─ rating (avg)
└─ review_count

Reviews Table:
├─ review_id (PK)
├─ business_id (FK)
├─ user_id (FK)
├─ rating
├─ content
└─ created_at
```

## 2. Search Strategy

### Geospatial + Text Search

```
Query: "Italian restaurant near me"

1. Parse query
   ├─ Category: "Italian restaurant"
   └─ Location: user's location
   │
   ▼
2. Geospatial search (within radius)
   │
   ▼
3. Text search (name, category)
   │
   ▼
4. Rank by relevance + distance
   │
   ▼
5. Return top results
```

## 3. Ranking Algorithm

```
Score = w1 × Text Relevance
      + w2 × Rating
      + w3 × Review Count
      + w4 × Distance
      + w5 × Recency
```

## Summary

Yelp Design:
- **Search Service**: Elasticsearch for text + geospatial
- **Business Service**: Core business data
- **Review Service**: Reviews and ratings
- **Recommendation**: ML-based suggestions
- **Media**: Photo storage and CDN

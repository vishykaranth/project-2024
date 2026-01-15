# System Design Mock Interview: Design Yelp w/ Meta Staff Engineer

## Overview

Designing Yelp requires business search, reviews, ratings, recommendations, and location-based queries. This guide covers search architecture, review system, and recommendation engine.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Search businesses
├─ View business details
├─ Write/read reviews
├─ Rating system
├─ Recommendations
└─ Photos and media

Non-Functional:
├─ Millions of businesses
├─ Billions of reviews
├─ < 200ms search latency
└─ High availability
```

## Architecture

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

## 1. Search Strategy

```
Geospatial + Text Search:
├─ Elasticsearch for full-text
├─ Geospatial queries for location
├─ Combine results
└─ Rank by relevance + distance
```

## 2. Review System

```
Review Storage:
├─ Shard by business_id
├─ Cache recent reviews
└─ Aggregate ratings

Review Ranking:
├─ Sort by helpfulness
├─ Filter spam
└─ Show most relevant
```

## Summary

Yelp Design:
- **Search Service**: Elasticsearch + geospatial
- **Business Service**: Core business data
- **Review Service**: Reviews and ratings
- **Recommendations**: ML-based suggestions
- **Media**: Photo storage and CDN

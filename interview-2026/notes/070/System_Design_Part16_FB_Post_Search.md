# Design FB Post Search: System Design Interview breakdown w/ ex Meta Interviewer

## Overview

Designing Facebook's post search requires handling billions of posts, full-text search, ranking by relevance, and serving results in milliseconds. This guide covers indexing strategies, search architecture, and ranking algorithms.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Search posts by keywords
├─ Filter by author, date, post type
├─ Rank by relevance
├─ Support autocomplete
└─ Handle typos and synonyms

Non-Functional:
├─ Billions of posts indexed
├─ < 100ms search latency
├─ High availability
└─ Support millions of queries per second
```

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│              Search Architecture                        │
└─────────────────────────────────────────────────────────┘

[Users] → [Load Balancer] → [Search API]
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
                    ▼               ▼               ▼
        [Query Parser]      [Index Service]   [Ranking Service]
                    │               │               │
                    ▼               ▼               ▼
            [Query Terms]    [Inverted Index]  [ML Ranking]
```

## 1. Inverted Index

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              Inverted Index                            │
└─────────────────────────────────────────────────────────┘

Term: "system"
├─ Post 1: [position 5, 12, 25], score: 0.8
├─ Post 3: [position 8, 15], score: 0.6
└─ Post 7: [position 3], score: 0.4

Term: "design"
├─ Post 1: [position 6, 13], score: 0.7
├─ Post 2: [position 2, 9], score: 0.9
└─ Post 5: [position 1], score: 0.5

Query: "system design"
→ Intersection: Post 1
→ Score: 0.8 + 0.7 = 1.5
```

## 2. Search Flow

```
┌─────────────────────────────────────────────────────────┐
│         Search Query Flow                              │
└─────────────────────────────────────────────────────────┘

1. User enters query
   │
   ▼
2. Query parsing and normalization
   │
   ▼
3. Lookup in inverted index
   │
   ▼
4. Intersect post lists
   │
   ▼
5. Rank by relevance
   │
   ▼
6. Apply filters
   │
   ▼
7. Return top N results
```

## 3. Ranking Algorithm

### TF-IDF Scoring

```
TF (Term Frequency):
tf(term, post) = count(term in post) / total terms in post

IDF (Inverse Document Frequency):
idf(term) = log(total posts / posts containing term)

Score:
score(term, post) = tf(term, post) × idf(term)
```

### Additional Ranking Factors

```
Final Score = w1 × TF-IDF
           + w2 × Recency
           + w3 × Engagement
           + w4 × User Relationship
           + w5 × Post Type
```

## 4. Indexing Strategy

### Distributed Indexing

```
┌─────────────────────────────────────────────────────────┐
│         Index Sharding                                 │
└─────────────────────────────────────────────────────────┘

Shard by term hash:
├─ Shard 1: hash(term) % 4 == 0
├─ Shard 2: hash(term) % 4 == 1
├─ Shard 3: hash(term) % 4 == 2
└─ Shard 4: hash(term) % 4 == 3

Benefits:
- Distribute index load
- Parallel query processing
- Horizontal scaling
```

## 5. Autocomplete

### Trie Structure

```
┌─────────────────────────────────────────────────────────┐
│         Trie for Autocomplete                          │
└─────────────────────────────────────────────────────────┘

        Root
        /  \
       s    d
      /      \
     y        e
    /          \
   s            s
  /              \
 t (system)      i (design)
  |                \
 e (systems)        g (designing)
```

## Summary

Facebook Post Search Design:
- **Inverted Index**: Core search data structure
- **Distributed Indexing**: Shard by term hash
- **Ranking**: TF-IDF + engagement + recency
- **Autocomplete**: Trie-based suggestions
- **Caching**: Cache popular queries

**Key Components:**
- Inverted index for fast lookups
- Query parser for query processing
- Ranking service for relevance
- Index sharding for scalability
- Autocomplete service for suggestions

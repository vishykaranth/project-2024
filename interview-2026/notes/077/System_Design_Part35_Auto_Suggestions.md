# Amazon Interview Question: System Design / Architecture for Auto Suggestions | Type Ahead

## Overview

Auto-suggestion systems provide real-time search suggestions as users type. Systems like Google Autocomplete must handle millions of queries, provide instant results, and scale globally.

## System Requirements

- Real-time suggestions (< 100ms)
- Handle millions of queries
- Personalized suggestions
- Popular queries prioritized
- Prefix matching
- Multi-language support

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Auto-Suggestion Architecture                    │
└─────────────────────────────────────────────────────────┘

User Input          API Gateway          Suggestion Service
    │                        │                        │
    │───"jav"────────────────>│                        │
    │                        │                        │
    │                        ├───Query───────────────>│
    │                        │    Trie/Trie           │
    │                        │                        │
    │                        ├───Rank────────────────>│
    │                        │    By Popularity        │
    │                        │                        │
    │<──Suggestions──────────│                        │
    │    ["java", "javascript"]│                        │
    │                        │                        │
```

## Data Structures

### 1. Trie (Prefix Tree)

```
┌─────────────────────────────────────────────────────────┐
│         Trie Structure                                  │
└─────────────────────────────────────────────────────────┘

        Root
        │
        ├─► j
        │   └─► a
        │       └─► v
        │           └─► a (end, count: 1000)
        │
        └─► p
            └─► y
                └─► t
                    └─► h
                        └─► o
                            └─► n (end, count: 500)
```

### 2. Ternary Search Tree

```
┌─────────────────────────────────────────────────────────┐
│         TST Structure                                  │
└─────────────────────────────────────────────────────────┘

More memory efficient than Trie
Better for sparse data
Similar search performance
```

## Ranking Algorithm

```
┌─────────────────────────────────────────────────────────┐
│         Suggestion Ranking                              │
└─────────────────────────────────────────────────────────┘

Factors:
├─ Query frequency (popularity)
├─ User history (personalization)
├─ Recent searches
├─ Context (location, time)
└─ Click-through rate

Score = w1 * frequency + w2 * personalization + ...
```

## Implementation

### Trie-Based Approach

```java
class TrieNode {
    Map<Character, TrieNode> children;
    List<String> topSuggestions; // Top N suggestions
    int count; // Frequency
    
    TrieNode() {
        children = new HashMap<>();
        topSuggestions = new ArrayList<>();
        count = 0;
    }
}

public List<String> getSuggestions(String prefix) {
    TrieNode node = root;
    
    // Navigate to prefix
    for (char c : prefix.toCharArray()) {
        if (!node.children.containsKey(c)) {
            return Collections.emptyList();
        }
        node = node.children.get(c);
    }
    
    // Return top suggestions
    return node.topSuggestions;
}
```

## Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Level Caching                             │
└─────────────────────────────────────────────────────────┘

L1: Client Cache
├─ Cache recent queries
└─ Fast but limited

L2: CDN Cache
├─ Popular queries
└─ Geographic distribution

L3: Application Cache
├─ Frequent prefixes
└─ Redis/Memcached
```

## Summary

Auto-Suggestions System:
- **Data Structure**: Trie or TST
- **Ranking**: Frequency, personalization, context
- **Performance**: < 100ms response time
- **Caching**: Multi-level caching strategy

**Key Features:**
- Real-time suggestions
- Prefix matching
- Personalized results
- High performance
- Scalable architecture

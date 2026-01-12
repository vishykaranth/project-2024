# Hash Table Problems & Solutions - Part 5

## Advanced Problems & Design Problems

This document covers advanced hash table problems and design problems that use hash tables.

---

## Common Patterns

### Pattern 1: LRU Cache Design
**Use Case**: Cache with least recently used eviction
```python
# Pattern: Hash Map + Doubly Linked List
class Node:
    def __init__(self, key, val):
        self.key = key
        self.val = val
        self.prev = None
        self.next = None

class LRUCache:
    def __init__(self, capacity):
        self.cache = {}  # key -> Node
        self.capacity = capacity
        # Dummy head and tail
        self.head = Node(0, 0)
        self.tail = Node(0, 0)
        self.head.next = self.tail
        self.tail.prev = self.head
```

### Pattern 2: Design Data Structure
**Use Case**: Design custom data structures
```python
# Pattern: Combine multiple hash maps
class DataStructure:
    def __init__(self):
        self.map1 = {}  # Primary storage
        self.map2 = {}  # Secondary index
```

### Pattern 3: Randomized Operations
**Use Case**: Need random access with O(1) operations
```python
# Pattern: Hash map + Array
class RandomizedSet:
    def __init__(self):
        self.nums = []  # Store values
        self.indices = {}  # value -> index in nums
```

---

## Problem 1: LRU Cache

### Problem Statement:
Design a data structure that follows the constraints of a Least Recently Used (LRU) cache.

**Example:**
```
LRUCache lRUCache = new LRUCache(2);
lRUCache.put(1, 1);
lRUCache.put(2, 2);
lRUCache.get(1);    // returns 1
lRUCache.put(3, 3); // evicts key 2
lRUCache.get(2);    // returns -1
```

### Solution:
```python
class Node:
    def __init__(self, key=0, value=0):
        self.key = key
        self.value = value
        self.prev = None
        self.next = None

class LRUCache:
    def __init__(self, capacity):
        self.capacity = capacity
        self.cache = {}  # key -> Node
        
        # Dummy head and tail
        self.head = Node()
        self.tail = Node()
        self.head.next = self.tail
        self.tail.prev = self.head
    
    def _add_node(self, node):
        """Add node right after head"""
        node.prev = self.head
        node.next = self.head.next
        self.head.next.prev = node
        self.head.next = node
    
    def _remove_node(self, node):
        """Remove node from list"""
        prev = node.prev
        next_node = node.next
        prev.next = next_node
        next_node.prev = prev
    
    def _move_to_head(self, node):
        """Move node to head (most recently used)"""
        self._remove_node(node)
        self._add_node(node)
    
    def _pop_tail(self):
        """Remove tail node (least recently used)"""
        last = self.tail.prev
        self._remove_node(last)
        return last
    
    def get(self, key):
        if key not in self.cache:
            return -1
        
        node = self.cache[key]
        self._move_to_head(node)
        return node.value
    
    def put(self, key, value):
        if key in self.cache:
            node = self.cache[key]
            node.value = value
            self._move_to_head(node)
        else:
            if len(self.cache) >= self.capacity:
                tail = self._pop_tail()
                del self.cache[tail.key]
            
            new_node = Node(key, value)
            self.cache[key] = new_node
            self._add_node(new_node)

# Test
cache = LRUCache(2)
cache.put(1, 1)
cache.put(2, 2)
print(cache.get(1))  # 1
cache.put(3, 3)
print(cache.get(2))  # -1
```

---

## Problem 2: LFU Cache

### Problem Statement:
Design and implement a data structure for a Least Frequently Used (LFU) cache.

**Example:**
```
LFUCache lfu = new LFUCache(2);
lfu.put(1, 1);
lfu.put(2, 2);
lfu.get(1);      // returns 1
lfu.put(3, 3);   // evicts key 2
lfu.get(2);      // returns -1
lfu.get(3);      // returns 3
lfu.put(4, 4);   // evicts key 1
```

### Solution:
```python
from collections import defaultdict

class Node:
    def __init__(self, key=0, value=0):
        self.key = key
        self.value = value
        self.freq = 1
        self.prev = None
        self.next = None

class DoublyLinkedList:
    def __init__(self):
        self.head = Node()
        self.tail = Node()
        self.head.next = self.tail
        self.tail.prev = self.head
        self.size = 0
    
    def add_node(self, node):
        node.prev = self.head
        node.next = self.head.next
        self.head.next.prev = node
        self.head.next = node
        self.size += 1
    
    def remove_node(self, node):
        node.prev.next = node.next
        node.next.prev = node.prev
        self.size -= 1
    
    def remove_tail(self):
        if self.size > 0:
            last = self.tail.prev
            self.remove_node(last)
            return last
        return None

class LFUCache:
    def __init__(self, capacity):
        self.capacity = capacity
        self.cache = {}  # key -> Node
        self.freq_map = defaultdict(DoublyLinkedList)  # freq -> DLL
        self.min_freq = 0
    
    def _update_node(self, node):
        freq = node.freq
        self.freq_map[freq].remove_node(node)
        
        if self.freq_map[freq].size == 0:
            del self.freq_map[freq]
            if freq == self.min_freq:
                self.min_freq += 1
        
        node.freq += 1
        freq = node.freq
        self.freq_map[freq].add_node(node)
    
    def get(self, key):
        if key not in self.cache:
            return -1
        
        node = self.cache[key]
        self._update_node(node)
        return node.value
    
    def put(self, key, value):
        if self.capacity == 0:
            return
        
        if key in self.cache:
            node = self.cache[key]
            node.value = value
            self._update_node(node)
        else:
            if len(self.cache) >= self.capacity:
                # Remove LFU node
                lfu_list = self.freq_map[self.min_freq]
                lfu_node = lfu_list.remove_tail()
                del self.cache[lfu_node.key]
            
            new_node = Node(key, value)
            self.cache[key] = new_node
            self.freq_map[1].add_node(new_node)
            self.min_freq = 1

# Test
lfu = LFUCache(2)
lfu.put(1, 1)
lfu.put(2, 2)
print(lfu.get(1))  # 1
lfu.put(3, 3)
print(lfu.get(2))  # -1
```

---

## Problem 3: Insert Delete GetRandom O(1)

### Problem Statement:
Implement the `RandomizedSet` class that supports insert, remove, and getRandom operations, each in O(1) average time.

**Example:**
```
RandomizedSet randomizedSet = new RandomizedSet();
randomizedSet.insert(1);
randomizedSet.remove(2);
randomizedSet.getRandom();
```

### Solution:
```python
import random

class RandomizedSet:
    def __init__(self):
        self.nums = []  # Store values
        self.indices = {}  # value -> index in nums
    
    def insert(self, val):
        if val in self.indices:
            return False
        
        self.indices[val] = len(self.nums)
        self.nums.append(val)
        return True
    
    def remove(self, val):
        if val not in self.indices:
            return False
        
        # Move last element to position of val
        index = self.indices[val]
        last_val = self.nums[-1]
        
        self.nums[index] = last_val
        self.indices[last_val] = index
        
        # Remove last element
        self.nums.pop()
        del self.indices[val]
        return True
    
    def getRandom(self):
        return random.choice(self.nums)

# Test
rs = RandomizedSet()
rs.insert(1)
rs.insert(2)
rs.remove(1)
print(rs.getRandom())  # 2
```

---

## Problem 4: Insert Delete GetRandom O(1) - Duplicates Allowed

### Problem Statement:
Implement the `RandomizedCollection` class that supports duplicates.

**Example:**
```
RandomizedCollection collection = new RandomizedCollection();
collection.insert(1);
collection.insert(1);
collection.insert(2);
collection.getRandom();
collection.remove(1);
```

### Solution:
```python
import random
from collections import defaultdict

class RandomizedCollection:
    def __init__(self):
        self.nums = []  # Store values
        self.indices = defaultdict(set)  # value -> set of indices
    
    def insert(self, val):
        self.indices[val].add(len(self.nums))
        self.nums.append(val)
        return len(self.indices[val]) == 1
    
    def remove(self, val):
        if not self.indices[val]:
            return False
        
        # Get any index of val
        remove_idx = self.indices[val].pop()
        last_idx = len(self.nums) - 1
        last_val = self.nums[last_idx]
        
        # Move last element to remove_idx
        self.nums[remove_idx] = last_val
        
        # Update indices
        self.indices[last_val].add(remove_idx)
        self.indices[last_val].discard(last_idx)
        
        # Remove last element
        self.nums.pop()
        return True
    
    def getRandom(self):
        return random.choice(self.nums)

# Test
rc = RandomizedCollection()
rc.insert(1)
rc.insert(1)
rc.insert(2)
print(rc.getRandom())
rc.remove(1)
```

---

## Problem 5: Design HashMap

### Problem Statement:
Design a HashMap without using any built-in hash table libraries.

**Example:**
```
MyHashMap hashMap = new MyHashMap();
hashMap.put(1, 1);
hashMap.put(2, 2);
hashMap.get(1);    // returns 1
hashMap.remove(2);
hashMap.get(2);    // returns -1
```

### Solution:
```python
class MyHashMap:
    def __init__(self):
        self.size = 1000
        self.buckets = [[] for _ in range(self.size)]
    
    def _hash(self, key):
        return key % self.size
    
    def put(self, key, value):
        bucket = self.buckets[self._hash(key)]
        for i, (k, v) in enumerate(bucket):
            if k == key:
                bucket[i] = (key, value)
                return
        bucket.append((key, value))
    
    def get(self, key):
        bucket = self.buckets[self._hash(key)]
        for k, v in bucket:
            if k == key:
                return v
        return -1
    
    def remove(self, key):
        bucket = self.buckets[self._hash(key)]
        for i, (k, v) in enumerate(bucket):
            if k == key:
                bucket.pop(i)
                return

# Test
hm = MyHashMap()
hm.put(1, 1)
hm.put(2, 2)
print(hm.get(1))  # 1
hm.remove(2)
print(hm.get(2))  # -1
```

---

## Problem 6: Design HashSet

### Problem Statement:
Design a HashSet without using any built-in hash set libraries.

**Example:**
```
MyHashSet hashSet = new MyHashSet();
hashSet.add(1);
hashSet.contains(1);    // returns true
hashSet.remove(1);
hashSet.contains(1);    // returns false
```

### Solution:
```python
class MyHashSet:
    def __init__(self):
        self.size = 1000
        self.buckets = [[] for _ in range(self.size)]
    
    def _hash(self, key):
        return key % self.size
    
    def add(self, key):
        bucket = self.buckets[self._hash(key)]
        if key not in bucket:
            bucket.append(key)
    
    def remove(self, key):
        bucket = self.buckets[self._hash(key)]
        if key in bucket:
            bucket.remove(key)
    
    def contains(self, key):
        bucket = self.buckets[self._hash(key)]
        return key in bucket

# Test
hs = MyHashSet()
hs.add(1)
print(hs.contains(1))  # True
hs.remove(1)
print(hs.contains(1))  # False
```

---

## Problem 7: Design Twitter

### Problem Statement:
Design a simplified version of Twitter where users can post tweets, follow/unfollow another user, and see the 10 most recent tweets in the user's news feed.

**Example:**
```
Twitter twitter = new Twitter();
twitter.postTweet(1, 5);
twitter.getNewsFeed(1);
twitter.follow(1, 2);
twitter.postTweet(2, 6);
twitter.getNewsFeed(1);
```

### Solution:
```python
import heapq
from collections import defaultdict

class Twitter:
    def __init__(self):
        self.time = 0
        self.tweets = defaultdict(list)  # user_id -> [(time, tweet_id)]
        self.following = defaultdict(set)  # user_id -> set of followee_ids
    
    def postTweet(self, userId, tweetId):
        self.tweets[userId].append((self.time, tweetId))
        self.time -= 1  # Negative for max heap (most recent first)
    
    def getNewsFeed(self, userId):
        heap = []
        
        # Add user's own tweets
        if userId in self.tweets:
            for time, tweetId in self.tweets[userId]:
                heapq.heappush(heap, (time, tweetId))
        
        # Add tweets from followees
        for followeeId in self.following[userId]:
            if followeeId in self.tweets:
                for time, tweetId in self.tweets[followeeId]:
                    heapq.heappush(heap, (time, tweetId))
        
        # Get top 10
        result = []
        while heap and len(result) < 10:
            time, tweetId = heapq.heappop(heap)
            result.append(tweetId)
        
        return result
    
    def follow(self, followerId, followeeId):
        self.following[followerId].add(followeeId)
    
    def unfollow(self, followerId, followeeId):
        if followeeId in self.following[followerId]:
            self.following[followerId].remove(followeeId)

# Test
twitter = Twitter()
twitter.postTweet(1, 5)
print(twitter.getNewsFeed(1))  # [5]
twitter.follow(1, 2)
twitter.postTweet(2, 6)
print(twitter.getNewsFeed(1))  # [6, 5]
```

---

## Problem 8: Design Underground System

### Problem Statement:
Design an underground railway system to track customer travel times between different stations.

**Example:**
```
UndergroundSystem undergroundSystem = new UndergroundSystem();
undergroundSystem.checkIn(45, "Leyton", 3);
undergroundSystem.checkIn(32, "Paradise", 8);
undergroundSystem.checkOut(45, "Waterloo", 15);
undergroundSystem.getAverageTime("Leyton", "Waterloo"); // 12.0
```

### Solution:
```python
from collections import defaultdict

class UndergroundSystem:
    def __init__(self):
        self.check_ins = {}  # id -> (station, time)
        self.trips = defaultdict(list)  # (start, end) -> [durations]
    
    def checkIn(self, id, stationName, t):
        self.check_ins[id] = (stationName, t)
    
    def checkOut(self, id, stationName, t):
        start_station, start_time = self.check_ins[id]
        duration = t - start_time
        self.trips[(start_station, stationName)].append(duration)
        del self.check_ins[id]
    
    def getAverageTime(self, startStation, endStation):
        durations = self.trips[(startStation, endStation)]
        return sum(durations) / len(durations) if durations else 0

# Test
us = UndergroundSystem()
us.checkIn(45, "Leyton", 3)
us.checkIn(32, "Paradise", 8)
us.checkOut(45, "Waterloo", 15)
print(us.getAverageTime("Leyton", "Waterloo"))  # 12.0
```

---

## Problem 9: Design Log Storage System

### Problem Statement:
Design a log storage system to implement the following functions: `put(id, timestamp)` and `retrieve(start, end, granularity)`.

**Example:**
```
LogSystem logSystem = new LogSystem();
logSystem.put(1, "2017:01:01:23:59:59");
logSystem.put(2, "2017:01:01:22:59:59");
logSystem.put(3, "2016:01:01:00:00:00");
logSystem.retrieve("2016:01:01:01:01:01","2017:01:01:23:00:00","Year");
// returns [1,2,3]
```

### Solution:
```python
class LogSystem:
    def __init__(self):
        self.logs = []  # [(id, timestamp)]
        self.granularity_map = {
            "Year": 4,
            "Month": 7,
            "Day": 10,
            "Hour": 13,
            "Minute": 16,
            "Second": 19
        }
    
    def put(self, id, timestamp):
        self.logs.append((id, timestamp))
    
    def retrieve(self, start, end, granularity):
        idx = self.granularity_map[granularity]
        start_prefix = start[:idx]
        end_prefix = end[:idx]
        
        result = []
        for log_id, timestamp in self.logs:
            timestamp_prefix = timestamp[:idx]
            if start_prefix <= timestamp_prefix <= end_prefix:
                result.append(log_id)
        
        return result

# Test
ls = LogSystem()
ls.put(1, "2017:01:01:23:59:59")
ls.put(2, "2017:01:01:22:59:59")
ls.put(3, "2016:01:01:00:00:00")
print(ls.retrieve("2016:01:01:01:01:01", "2017:01:01:23:00:00", "Year"))
# [1, 2, 3]
```

---

## Problem 10: Design Search Autocomplete System

### Problem Statement:
Design a search autocomplete system for a search engine. Users may input a sentence (at least one word and end with '#'). For each character they type except '#', you need to return the top 3 historical hot sentences.

**Example:**
```
AutocompleteSystem(["i love you", "island","ironman", "i love leetcode"], [5,3,2,2])
Input: "i a#"
Output: []
Input: "i l#"
Output: ["i love you", "i love leetcode"]
```

### Solution:
```python
from collections import defaultdict
import heapq

class TrieNode:
    def __init__(self):
        self.children = {}
        self.sentences = defaultdict(int)  # sentence -> count

class AutocompleteSystem:
    def __init__(self, sentences, times):
        self.root = TrieNode()
        self.current_node = self.root
        self.current_sentence = ""
        
        # Build trie
        for sentence, time in zip(sentences, times):
            self._add_sentence(sentence, time)
    
    def _add_sentence(self, sentence, count):
        node = self.root
        for char in sentence:
            if char not in node.children:
                node.children[char] = TrieNode()
            node = node.children[char]
            node.sentences[sentence] += count
    
    def input(self, c):
        if c == '#':
            # Save sentence
            self._add_sentence(self.current_sentence, 1)
            self.current_sentence = ""
            self.current_node = self.root
            return []
        
        self.current_sentence += c
        
        if self.current_node and c in self.current_node.children:
            self.current_node = self.current_node.children[c]
        else:
            self.current_node = None
            return []
        
        # Get top 3
        heap = []
        for sentence, count in self.current_node.sentences.items():
            heapq.heappush(heap, (-count, sentence))
            if len(heap) > 3:
                heapq.heappop(heap)
        
        result = []
        while heap:
            result.append(heapq.heappop(heap)[1])
        
        return result[::-1]

# Test
acs = AutocompleteSystem(["i love you", "island", "ironman", "i love leetcode"], [5, 3, 2, 2])
print(acs.input('i'))  # ["i love you", "island", "i love leetcode"]
print(acs.input(' '))  # ["i love you", "i love leetcode"]
print(acs.input('a'))  # []
print(acs.input('#'))  # []
```

---

## Summary: Part 5

### Patterns Covered:
1. **LRU Cache**: Hash map + Doubly linked list
2. **LFU Cache**: Hash map + Frequency map + Doubly linked lists
3. **Randomized Operations**: Hash map + Array
4. **Custom Hash Structures**: Design hash map/set
5. **Complex Systems**: Multiple hash maps for different purposes

### Key Takeaways:
- Use hash map for O(1) lookups
- Combine with other data structures (lists, trees) for complex operations
- Maintain multiple indices for different access patterns
- Use heaps for top-k operations
- Design for specific requirements (LRU, LFU, etc.)

---

## Complete Series Summary

### All 5 Parts Covered:

**Part 1**: Basic operations, anagrams, duplicates  
**Part 2**: Frequency counting, top-k, sliding window basics  
**Part 3**: Two sum variations, pair finding, prefix sums  
**Part 4**: Sliding window, substring problems  
**Part 5**: Advanced design problems, cache systems

### Total Problems: 50+ Hash Table Problems

### Common Patterns Mastered:
1. Direct lookup
2. Frequency counting
3. Complement lookup
4. Sliding window
5. Prefix sum
6. Two-pass hash map
7. Bi-directional mapping
8. Design patterns (LRU, LFU, Randomized)

---

**Master these patterns to solve any hash table problem!** 🚀


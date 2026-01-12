# Greedy Algorithms: Problems, Solutions & Patterns - Part 3

## Part 3: Advanced Greedy Problems & Real-World Applications

This part covers advanced greedy algorithms, optimization problems, and real-world applications.

---

### 23. Minimum Cost to Connect Ropes

**Problem**: Connect ropes with minimum cost (cost = sum of lengths being connected).

**Greedy Strategy**: Always connect two shortest ropes first.

```python
import heapq

def min_cost_to_connect_ropes(ropes):
    """
    Find minimum cost to connect all ropes.
    
    Time Complexity: O(n log n)
    Space Complexity: O(n)
    """
    if len(ropes) <= 1:
        return 0
    
    # Min heap - greedy: always connect shortest ropes
    heapq.heapify(ropes)
    total_cost = 0
    
    while len(ropes) > 1:
        # Greedy: connect two shortest ropes
        first = heapq.heappop(ropes)
        second = heapq.heappop(ropes)
        
        cost = first + second
        total_cost += cost
        
        heapq.heappush(ropes, cost)
    
    return total_cost

# Example
ropes = [4, 3, 2, 6]
min_cost = min_cost_to_connect_ropes(ropes)
print(f"Minimum cost: {min_cost}")  # 29
# Connect 2+3=5 (cost 5), then 4+5=9 (cost 9), then 6+9=15 (cost 15)
# Total: 5+9+15 = 29
```

---

### 24. Maximum Number of Events That Can Be Attended

**Problem**: Attend maximum events where each event has start and end day.

**Greedy Strategy**: Sort by end day, attend events greedily on earliest available day.

```python
import heapq

def max_events(events):
    """
    Attend maximum number of events.
    
    Time Complexity: O(n log n)
    Space Complexity: O(n)
    """
    # Sort by start day
    events.sort()
    
    # Min heap for end days of events we can attend
    heap = []
    event_idx = 0
    n = len(events)
    attended = 0
    day = 1
    
    while event_idx < n or heap:
        # Add all events that start today
        while event_idx < n and events[event_idx][0] == day:
            heapq.heappush(heap, events[event_idx][1])
            event_idx += 1
        
        # Remove events that have ended
        while heap and heap[0] < day:
            heapq.heappop(heap)
        
        # Greedy: attend event that ends earliest
        if heap:
            heapq.heappop(heap)
            attended += 1
        
        day += 1
    
    return attended

# Example
events = [[1, 2], [2, 3], [3, 4], [1, 2]]
max_attended = max_events(events)
print(f"Maximum events: {max_attended}")  # 4
```

---

### 25. Remove K Digits

**Problem**: Remove k digits to form smallest possible number.

**Greedy Strategy**: Remove digits that are larger than following digit (monotonic stack).

```python
def remove_k_digits(num, k):
    """
    Remove k digits to form smallest number.
    
    Time Complexity: O(n)
    Space Complexity: O(n)
    """
    if k >= len(num):
        return "0"
    
    stack = []
    
    for digit in num:
        # Greedy: remove digit if larger than current
        while k > 0 and stack and stack[-1] > digit:
            stack.pop()
            k -= 1
        stack.append(digit)
    
    # Remove remaining k digits from end
    while k > 0:
        stack.pop()
        k -= 1
    
    # Remove leading zeros
    result = ''.join(stack).lstrip('0')
    
    return result if result else "0"

# Example
num = "1432219"
k = 3
result = remove_k_digits(num, k)
print(f"Smallest number: {result}")  # "1219"
```

---

### 26. Reorganize String

**Problem**: Rearrange string so no two same characters are adjacent.

**Greedy Strategy**: Always place most frequent character first (if possible).

```python
from collections import Counter
import heapq

def reorganize_string(s):
    """
    Reorganize string with no adjacent same characters.
    
    Time Complexity: O(n log k) where k is unique characters
    Space Complexity: O(k)
    """
    # Count frequencies
    count = Counter(s)
    
    # Max heap (negative for max heap)
    heap = [(-freq, char) for char, freq in count.items()]
    heapq.heapify(heap)
    
    result = []
    prev_char = None
    prev_freq = 0
    
    while heap:
        freq, char = heapq.heappop(heap)
        
        # Add character to result
        result.append(char)
        freq += 1  # Decrease count (negative)
        
        # Add previous character back if still has count
        if prev_freq < 0:
            heapq.heappush(heap, (prev_freq, prev_char))
        
        prev_char = char
        prev_freq = freq
        
        # If only one character left and count > 1, impossible
        if len(heap) == 0 and prev_freq < -1:
            return ""
    
    return ''.join(result)

# Example
s = "aab"
result = reorganize_string(s)
print(f"Reorganized: {result}")  # "aba"
```

---

### 27. Candy Distribution

**Problem**: Distribute candy to children based on ratings (each child gets at least 1, more if rating higher than neighbors).

**Greedy Strategy**: Two passes - left to right, then right to left.

```python
def candy(ratings):
    """
    Distribute minimum candies based on ratings.
    
    Time Complexity: O(n)
    Space Complexity: O(n)
    """
    n = len(ratings)
    candies = [1] * n
    
    # Left to right: if rating higher than left, give more candy
    for i in range(1, n):
        if ratings[i] > ratings[i - 1]:
            candies[i] = candies[i - 1] + 1
    
    # Right to left: if rating higher than right, give more candy
    for i in range(n - 2, -1, -1):
        if ratings[i] > ratings[i + 1]:
            candies[i] = max(candies[i], candies[i + 1] + 1)
    
    return sum(candies)

# Example
ratings = [1, 0, 2]
total_candies = candy(ratings)
print(f"Total candies: {total_candies}")  # 5 (2, 1, 2)
```

---

### 28. Minimum Deletions to Make Character Frequencies Unique

**Problem**: Delete minimum characters so no two characters have same frequency.

**Greedy Strategy**: Sort frequencies, reduce duplicates greedily.

```python
from collections import Counter

def min_deletions(s):
    """
    Minimum deletions to make frequencies unique.
    
    Time Complexity: O(n + k log k) where k is unique chars
    Space Complexity: O(k)
    """
    freq = Counter(s)
    frequencies = sorted(freq.values(), reverse=True)
    
    deletions = 0
    seen = set()
    
    for freq_count in frequencies:
        # Greedy: reduce frequency until unique
        while freq_count > 0 and freq_count in seen:
            freq_count -= 1
            deletions += 1
        
        if freq_count > 0:
            seen.add(freq_count)
    
    return deletions

# Example
s = "aaabbbcc"
deletions = min_deletions(s)
print(f"Minimum deletions: {deletions}")  # 2
```

---

### 29. Maximum Performance of a Team

**Problem**: Select team of engineers to maximize performance (sum of speeds × minimum efficiency).

**Greedy Strategy**: Sort by efficiency, use heap to maintain k fastest engineers.

```python
import heapq

def max_performance(n, speed, efficiency, k):
    """
    Maximum performance of team of size at most k.
    
    Time Complexity: O(n log n)
    Space Complexity: O(k)
    """
    engineers = sorted(zip(efficiency, speed), reverse=True)
    
    heap = []  # Min heap for speeds
    total_speed = 0
    max_perf = 0
    
    for eff, spd in engineers:
        # Greedy: maintain k fastest engineers
        if len(heap) >= k:
            total_speed -= heapq.heappop(heap)
        
        heapq.heappush(heap, spd)
        total_speed += spd
        
        # Current performance = total_speed * current efficiency
        max_perf = max(max_perf, total_speed * eff)
    
    return max_perf % (10**9 + 7)

# Example
n = 6
speed = [2, 10, 3, 1, 5, 8]
efficiency = [5, 4, 3, 9, 7, 2]
k = 2
result = max_performance(n, speed, efficiency, k)
print(f"Maximum performance: {result}")
```

---

### 30. Course Schedule III

**Problem**: Schedule maximum courses where each has duration and deadline.

**Greedy Strategy**: Sort by deadline, use heap to replace longest course if needed.

```python
import heapq

def schedule_course(courses):
    """
    Schedule maximum courses.
    
    Time Complexity: O(n log n)
    Space Complexity: O(n)
    """
    # Sort by deadline - greedy choice
    courses.sort(key=lambda x: x[1])
    
    heap = []  # Max heap (negative for max heap)
    current_time = 0
    
    for duration, deadline in courses:
        current_time += duration
        heapq.heappush(heap, -duration)
        
        # Greedy: if exceeds deadline, remove longest course
        if current_time > deadline:
            longest = -heapq.heappop(heap)
            current_time -= longest
    
    return len(heap)

# Example
courses = [[100, 200], [200, 1300], [1000, 1250], [2000, 3200]]
max_courses = schedule_course(courses)
print(f"Maximum courses: {max_courses}")  # 3
```

---

### 31. Minimum Number of Arrows to Burst Balloons

**Problem**: Find minimum arrows to burst all balloons (arrow can burst overlapping balloons).

**Greedy Strategy**: Sort by end coordinate, shoot arrow at end of first balloon.

```python
def find_min_arrow_shots(points):
    """
    Minimum arrows to burst all balloons.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    if not points:
        return 0
    
    # Sort by end coordinate - greedy choice
    points.sort(key=lambda x: x[1])
    
    arrows = 1
    arrow_pos = points[0][1]
    
    for start, end in points[1:]:
        # Greedy: if balloon doesn't overlap with arrow, need new arrow
        if start > arrow_pos:
            arrows += 1
            arrow_pos = end
    
    return arrows

# Example
points = [[10, 16], [2, 8], [1, 6], [7, 12]]
min_arrows = find_min_arrow_shots(points)
print(f"Minimum arrows: {min_arrows}")  # 2
```

---

### 32. Queue Reconstruction by Height

**Problem**: Reconstruct queue based on height and number of people in front.

**Greedy Strategy**: Sort by height (descending), then by k (ascending), insert at position k.

```python
def reconstruct_queue(people):
    """
    Reconstruct queue based on height and people in front.
    
    Time Complexity: O(n²)
    Space Complexity: O(n)
    """
    # Sort by height (descending), then by k (ascending)
    people.sort(key=lambda x: (-x[0], x[1]))
    
    result = []
    
    # Greedy: insert each person at position k
    for person in people:
        result.insert(person[1], person)
    
    return result

# Example
people = [[7, 0], [4, 4], [7, 1], [5, 0], [6, 1], [5, 2]]
reconstructed = reconstruct_queue(people)
print(f"Reconstructed queue: {reconstructed}")
# [[5, 0], [7, 0], [5, 2], [6, 1], [4, 4], [7, 1]]
```

---

### 33. Minimum Domino Rotations

**Problem**: Minimum rotations to make all values in top or bottom row same.

**Greedy Strategy**: Try making all top values same, then all bottom values same.

```python
def min_domino_rotations(tops, bottoms):
    """
    Minimum rotations to make all values same.
    
    Time Complexity: O(n)
    Space Complexity: O(1)
    """
    def rotations(target):
        """Calculate rotations needed for target value"""
        top_rotations = 0
        bottom_rotations = 0
        
        for i in range(len(tops)):
            if tops[i] != target and bottoms[i] != target:
                return float('inf')
            
            if tops[i] != target:
                top_rotations += 1
            if bottoms[i] != target:
                bottom_rotations += 1
        
        return min(top_rotations, bottom_rotations)
    
    # Greedy: try first value from top and bottom
    result = min(rotations(tops[0]), rotations(bottoms[0]))
    
    return result if result != float('inf') else -1

# Example
tops = [2, 1, 2, 4, 2, 2]
bottoms = [5, 2, 6, 2, 3, 2]
min_rotations = min_domino_rotations(tops, bottoms)
print(f"Minimum rotations: {min_rotations}")  # 2
```

---

### 34. Boats to Save People

**Problem**: Minimum boats to save people (each boat has weight limit, at most 2 people).

**Greedy Strategy**: Pair heaviest with lightest person.

```python
def num_rescue_boats(people, limit):
    """
    Minimum boats to save all people.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    people.sort()
    
    boats = 0
    left = 0
    right = len(people) - 1
    
    while left <= right:
        # Greedy: try to pair heaviest with lightest
        if people[left] + people[right] <= limit:
            left += 1
        right -= 1
        boats += 1
    
    return boats

# Example
people = [3, 2, 2, 1]
limit = 3
boats = num_rescue_boats(people, limit)
print(f"Minimum boats: {boats}")  # 3
```

---

### 35. Assign Cookies

**Problem**: Assign cookies to children to maximize satisfied children.

**Greedy Strategy**: Sort both arrays, assign smallest cookie to smallest child.

```python
def find_content_children(g, s):
    """
    Maximum children that can be satisfied.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    g.sort()  # Children's greed
    s.sort()  # Cookie sizes
    
    child_idx = 0
    cookie_idx = 0
    satisfied = 0
    
    while child_idx < len(g) and cookie_idx < len(s):
        # Greedy: assign smallest cookie to smallest child
        if s[cookie_idx] >= g[child_idx]:
            satisfied += 1
            child_idx += 1
        cookie_idx += 1
    
    return satisfied

# Example
g = [1, 2, 3]
s = [1, 1]
satisfied = find_content_children(g, s)
print(f"Satisfied children: {satisfied}")  # 1
```

---

### 36. Lemonade Change

**Problem**: Can make change for all customers buying lemonade ($5) with $5, $10, $20 bills.

**Greedy Strategy**: Always give change using largest bills first.

```python
def lemonade_change(bills):
    """
    Check if can make change for all customers.
    
    Time Complexity: O(n)
    Space Complexity: O(1)
    """
    five = ten = 0
    
    for bill in bills:
        if bill == 5:
            five += 1
        elif bill == 10:
            if five == 0:
                return False
            five -= 1
            ten += 1
        else:  # $20
            # Greedy: prefer giving $10 + $5 over 3 × $5
            if ten > 0 and five > 0:
                ten -= 1
                five -= 1
            elif five >= 3:
                five -= 3
            else:
                return False
    
    return True

# Example
bills = [5, 5, 5, 10, 20]
result = lemonade_change(bills)
print(f"Can make change: {result}")  # True
```

---

### 37. Video Stitching

**Problem**: Minimum clips to cover entire time range.

**Greedy Strategy**: Sort by start time, extend coverage greedily.

```python
def video_stitching(clips, time):
    """
    Minimum clips to cover time range.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    clips.sort()
    
    clips_used = 0
    current_end = 0
    i = 0
    
    while current_end < time:
        max_end = current_end
        
        # Greedy: find clip that extends coverage most
        while i < len(clips) and clips[i][0] <= current_end:
            max_end = max(max_end, clips[i][1])
            i += 1
        
        if max_end == current_end:
            return -1  # Cannot extend
        
        current_end = max_end
        clips_used += 1
    
    return clips_used

# Example
clips = [[0, 2], [4, 6], [8, 10], [1, 9], [1, 5], [5, 9]]
time = 10
min_clips = video_stitching(clips, time)
print(f"Minimum clips: {min_clips}")  # 3
```

---

### 38. Maximum Score from Removing Stones

**Problem**: Remove stones from three piles, score = sum of two smallest, maximize total score.

**Greedy Strategy**: Always remove from two largest piles.

```python
import heapq

def maximum_score(a, b, c):
    """
    Maximum score from removing stones.
    
    Time Complexity: O(n log 3) = O(1) per operation
    Space Complexity: O(1)
    """
    heap = [-a, -b, -c]  # Max heap (negative)
    heapq.heapify(heap)
    score = 0
    
    while True:
        # Get two largest
        first = -heapq.heappop(heap)
        second = -heapq.heappop(heap)
        
        if first == 0 or second == 0:
            break
        
        # Greedy: remove one from each
        score += 1
        first -= 1
        second -= 1
        
        heapq.heappush(heap, -first)
        heapq.heappush(heap, -second)
        heapq.heappush(heap, -(-heapq.heappop(heap)))  # Third pile
    
    return score

# Example
score = maximum_score(2, 4, 6)
print(f"Maximum score: {score}")  # 6
```

---

## Real-World Applications

### 1. Network Routing (Dijkstra's Algorithm)
- **Application**: Internet routing, GPS navigation
- **Why Greedy**: Always explore closest node first
- **Impact**: Powers Google Maps, routing protocols

### 2. Data Compression (Huffman Coding)
- **Application**: File compression (ZIP, JPEG)
- **Why Greedy**: Merge least frequent characters first
- **Impact**: Reduces storage and bandwidth

### 3. Task Scheduling (Activity Selection)
- **Application**: CPU scheduling, meeting room booking
- **Why Greedy**: Maximize resource utilization
- **Impact**: Efficient resource management

### 4. Minimum Spanning Tree
- **Application**: Network design, clustering
- **Why Greedy**: Connect all nodes with minimum cost
- **Impact**: Optimizes infrastructure costs

### 5. Change Making (Coin Problem)
- **Application**: Vending machines, cash registers
- **Why Greedy**: Minimize number of coins
- **Impact**: Faster transactions

---

## Common Greedy Patterns Summary

### Pattern 1: Sort and Select
- Activity Selection
- Interval Problems
- Job Sequencing

### Pattern 2: Priority Queue
- MST (Kruskal's, Prim's)
- Shortest Path (Dijkstra's)
- Task Scheduling

### Pattern 3: Two Pointers
- Merge Intervals
- Boats to Save People
- Assign Cookies

### Pattern 4: Monotonic Stack
- Remove K Digits
- Next Greater Element

### Pattern 5: Frequency-Based
- Huffman Coding
- Reorganize String
- Task Scheduler

### Pattern 6: Two-Pass Greedy
- Candy Distribution
- Gas Station

---

## When Greedy Works

### ✅ Greedy Works When:
1. **Greedy Choice Property**: Local optimum leads to global optimum
2. **Optimal Substructure**: Problem can be broken into subproblems
3. **No Reconsideration**: Past choices don't need to be reconsidered

### ❌ Greedy Fails When:
1. **Need to Reconsider**: Past choices affect future
2. **No Greedy Choice**: No clear "best" choice at each step
3. **Counter-Examples**: Greedy solution is not optimal

### Example: 0/1 Knapsack
- **Fractional Knapsack**: Greedy works (can break items)
- **0/1 Knapsack**: Greedy fails (must use DP)

---

## Summary: Complete Series

### Total Problems Covered: 38+

**Part 1 (Basic)**: 10 problems
- Activity Selection, Fractional Knapsack, Coins, Jobs, etc.

**Part 2 (Intermediate)**: 12 problems
- MST, Shortest Path, Huffman, Scheduling, etc.

**Part 3 (Advanced)**: 16 problems
- Optimization, String Problems, Real-world Applications

### Key Takeaways:
1. **Sorting is crucial**: Most greedy problems start with sorting
2. **Priority queues help**: For dynamic greedy choices
3. **Prove correctness**: Always verify greedy choice property
4. **Watch for edge cases**: Ties, empty inputs, impossible cases
5. **Real-world impact**: Greedy algorithms power many systems

---

**Master these patterns to solve greedy problems efficiently!** 🚀


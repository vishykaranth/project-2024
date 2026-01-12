# Greedy Algorithms: Problems, Solutions & Patterns - Part 1

## Overview

Greedy algorithms make locally optimal choices at each step, hoping to find a global optimum. They're used when a problem can be solved by making the best choice at each step without reconsidering previous choices.

---

## Common Greedy Patterns

### Pattern 1: Activity Selection
**Problem Type**: Scheduling problems, interval problems  
**Strategy**: Always select the activity that finishes earliest

### Pattern 2: Fractional Knapsack
**Problem Type**: Resource allocation with divisible items  
**Strategy**: Sort by value/weight ratio, take items greedily

### Pattern 3: Minimum Spanning Tree (MST)
**Problem Type**: Connect all nodes with minimum cost  
**Strategy**: Kruskal's or Prim's algorithm

### Pattern 4: Shortest Path (Dijkstra)
**Problem Type**: Find shortest path in weighted graph  
**Strategy**: Always explore the closest unvisited node

### Pattern 5: Huffman Coding
**Problem Type**: Optimal prefix-free encoding  
**Strategy**: Merge least frequent characters first

### Pattern 6: Interval Scheduling
**Problem Type**: Maximize non-overlapping intervals  
**Strategy**: Sort by end time, select greedily

### Pattern 7: Coin Change (Greedy)
**Problem Type**: Make change with minimum coins  
**Strategy**: Always use largest coin possible

### Pattern 8: Job Sequencing
**Problem Type**: Schedule jobs with deadlines  
**Strategy**: Sort by profit, schedule greedily

---

## Part 1: Basic Greedy Problems

### 1. Activity Selection Problem

**Problem**: Given N activities with start and finish times, select maximum number of activities that can be performed by a single person.

**Greedy Strategy**: Always pick the activity that finishes earliest.

```python
def activity_selection(start, finish):
    """
    Select maximum number of non-overlapping activities.
    
    Time Complexity: O(n log n) for sorting
    Space Complexity: O(1)
    """
    n = len(start)
    
    # Create list of activities with (start, finish, index)
    activities = [(start[i], finish[i], i) for i in range(n)]
    
    # Sort by finish time (greedy choice: earliest finish first)
    activities.sort(key=lambda x: x[1])
    
    selected = []
    last_finish = 0
    
    for start_time, finish_time, index in activities:
        # Greedy choice: if activity starts after last finishes
        if start_time >= last_finish:
            selected.append(index)
            last_finish = finish_time
    
    return selected

# Example
start = [1, 3, 0, 5, 8, 5]
finish = [2, 4, 6, 7, 9, 9]
result = activity_selection(start, finish)
print(f"Selected activities: {result}")  # [0, 1, 3, 4]
```

---

### 2. Fractional Knapsack Problem

**Problem**: Given items with weights and values, fill a knapsack of capacity W to maximize value. Items can be broken (fractional).

**Greedy Strategy**: Sort by value/weight ratio, take items greedily.

```python
def fractional_knapsack(weights, values, capacity):
    """
    Maximize value in knapsack with fractional items allowed.
    
    Time Complexity: O(n log n)
    Space Complexity: O(n)
    """
    n = len(weights)
    
    # Calculate value/weight ratio
    items = [(values[i] / weights[i], weights[i], values[i], i) 
             for i in range(n)]
    
    # Sort by ratio (descending) - greedy choice
    items.sort(reverse=True, key=lambda x: x[0])
    
    total_value = 0
    remaining_capacity = capacity
    selected = []
    
    for ratio, weight, value, index in items:
        if remaining_capacity >= weight:
            # Take entire item
            total_value += value
            remaining_capacity -= weight
            selected.append((index, 1.0))  # 100% of item
        else:
            # Take fraction of item
            fraction = remaining_capacity / weight
            total_value += value * fraction
            selected.append((index, fraction))
            break
    
    return total_value, selected

# Example
weights = [10, 20, 30]
values = [60, 100, 120]
capacity = 50
max_value, items = fractional_knapsack(weights, values, capacity)
print(f"Maximum value: {max_value}")  # 240.0
print(f"Items selected: {items}")
```

---

### 3. Minimum Number of Coins (Greedy)

**Problem**: Make change for amount using minimum number of coins. Assumes greedy works (e.g., standard US coins).

**Greedy Strategy**: Always use largest coin possible.

```python
def min_coins_greedy(coins, amount):
    """
    Find minimum coins to make amount (greedy approach).
    Note: Only works when coin system is "canonical".
    
    Time Complexity: O(amount)
    Space Complexity: O(1)
    """
    # Sort coins in descending order
    coins.sort(reverse=True)
    
    count = 0
    result = []
    remaining = amount
    
    for coin in coins:
        # Greedy choice: use as many of largest coin as possible
        num_coins = remaining // coin
        if num_coins > 0:
            count += num_coins
            result.append((coin, num_coins))
            remaining -= num_coins * coin
        
        if remaining == 0:
            break
    
    if remaining > 0:
        return -1, []  # Cannot make exact change
    
    return count, result

# Example (US coins - greedy works)
coins = [1, 5, 10, 25]
amount = 67
min_count, coin_breakdown = min_coins_greedy(coins, amount)
print(f"Minimum coins: {min_count}")  # 6 (25+25+10+5+1+1)
print(f"Breakdown: {coin_breakdown}")
```

---

### 4. Job Sequencing with Deadlines

**Problem**: Schedule jobs with deadlines to maximize profit. Each job takes 1 unit of time.

**Greedy Strategy**: Sort by profit (descending), schedule each job as late as possible.

```python
def job_sequencing(jobs, deadlines, profits):
    """
    Schedule jobs to maximize profit with deadlines.
    
    Time Complexity: O(n²) or O(n log n) with union-find
    Space Complexity: O(n)
    """
    n = len(jobs)
    
    # Create job list with (profit, deadline, job_id)
    job_list = [(profits[i], deadlines[i], jobs[i]) for i in range(n)]
    
    # Sort by profit (descending) - greedy choice
    job_list.sort(reverse=True, key=lambda x: x[0])
    
    # Find maximum deadline
    max_deadline = max(deadlines)
    
    # Time slot array (True = occupied)
    time_slots = [False] * (max_deadline + 1)
    scheduled_jobs = []
    total_profit = 0
    
    for profit, deadline, job_id in job_list:
        # Greedy: schedule as late as possible
        for time in range(deadline, 0, -1):
            if not time_slots[time]:
                time_slots[time] = True
                scheduled_jobs.append((job_id, time))
                total_profit += profit
                break
    
    return scheduled_jobs, total_profit

# Example
jobs = ['a', 'b', 'c', 'd', 'e']
deadlines = [2, 1, 2, 1, 3]
profits = [100, 19, 27, 25, 15]
scheduled, profit = job_sequencing(jobs, deadlines, profits)
print(f"Scheduled jobs: {scheduled}")
print(f"Total profit: {profit}")
```

---

### 5. Minimum Platforms Required

**Problem**: Find minimum number of platforms needed at a railway station given arrival and departure times.

**Greedy Strategy**: Sort all events, count platforms needed at each event.

```python
def min_platforms(arrival, departure):
    """
    Find minimum platforms needed for trains.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    n = len(arrival)
    
    # Sort arrival and departure times
    arrival.sort()
    departure.sort()
    
    platforms_needed = 0
    max_platforms = 0
    i = j = 0
    
    # Merge process (like merge sort)
    while i < n and j < n:
        # Greedy: process event that happens first
        if arrival[i] <= departure[j]:
            platforms_needed += 1
            max_platforms = max(max_platforms, platforms_needed)
            i += 1
        else:
            platforms_needed -= 1
            j += 1
    
    return max_platforms

# Example
arrival = [900, 940, 950, 1100, 1500, 1800]
departure = [910, 1200, 1120, 1130, 1900, 2000]
min_platforms = min_platforms(arrival, departure)
print(f"Minimum platforms needed: {min_platforms}")  # 3
```

---

### 6. Maximum Meetings in One Room

**Problem**: Find maximum number of meetings that can be held in one room.

**Greedy Strategy**: Sort by end time, select meetings greedily.

```python
def max_meetings(start, end):
    """
    Find maximum non-overlapping meetings.
    
    Time Complexity: O(n log n)
    Space Complexity: O(n)
    """
    n = len(start)
    
    # Create meetings with (start, end, index)
    meetings = [(start[i], end[i], i) for i in range(n)]
    
    # Sort by end time - greedy choice
    meetings.sort(key=lambda x: x[1])
    
    selected = []
    last_end = 0
    
    for start_time, end_time, index in meetings:
        if start_time > last_end:
            selected.append(index)
            last_end = end_time
    
    return selected

# Example
start = [1, 3, 0, 5, 8, 5]
end = [2, 4, 6, 7, 9, 9]
meetings = max_meetings(start, end)
print(f"Maximum meetings: {len(meetings)}")  # 4
print(f"Selected meetings: {meetings}")
```

---

### 7. Egyptian Fraction

**Problem**: Represent a fraction as sum of unique unit fractions (1/n).

**Greedy Strategy**: Always subtract largest possible unit fraction.

```python
def egyptian_fraction(numerator, denominator):
    """
    Express fraction as sum of unique unit fractions.
    
    Time Complexity: O(log n)
    Space Complexity: O(1)
    """
    result = []
    
    while numerator != 0:
        # Greedy: find largest unit fraction <= current fraction
        # Find smallest n such that 1/n <= num/den
        # i.e., n >= den/num
        unit_denominator = (denominator + numerator - 1) // numerator
        
        result.append(unit_denominator)
        
        # Subtract 1/unit_denominator from num/den
        numerator = numerator * unit_denominator - denominator
        denominator = denominator * unit_denominator
        
        # Simplify fraction
        gcd_val = gcd(numerator, denominator)
        numerator //= gcd_val
        denominator //= gcd_val
    
    return result

def gcd(a, b):
    """Calculate GCD"""
    while b:
        a, b = b, a % b
    return a

# Example
result = egyptian_fraction(6, 14)
print(f"Egyptian fraction: {result}")  # [3, 11, 231]
# 6/14 = 1/3 + 1/11 + 1/231
```

---

### 8. Maximum Product Subset

**Problem**: Find maximum product of subset of array.

**Greedy Strategy**: Count negatives, include all positives, include even number of negatives.

```python
def max_product_subset(arr):
    """
    Find maximum product of subset.
    
    Time Complexity: O(n)
    Space Complexity: O(1)
    """
    n = len(arr)
    
    if n == 1:
        return arr[0]
    
    # Count negatives, zeros, positives
    negative_count = 0
    zero_count = 0
    max_negative = float('-inf')
    product = 1
    
    for num in arr:
        if num == 0:
            zero_count += 1
            continue
        
        if num < 0:
            negative_count += 1
            max_negative = max(max_negative, num)
        
        product *= num
    
    # Greedy strategy:
    # If all zeros and one negative: return 0
    if zero_count == n:
        return 0
    
    # If odd number of negatives: exclude largest negative
    if negative_count % 2 == 1:
        if negative_count == 1 and zero_count > 0 and zero_count + negative_count == n:
            return 0
        product //= max_negative
    
    return product

# Example
arr = [-1, -1, -2, 4, 3]
result = max_product_subset(arr)
print(f"Maximum product: {result}")  # 24
```

---

### 9. Minimum Sum of Absolute Differences

**Problem**: Given two arrays, pair elements to minimize sum of absolute differences.

**Greedy Strategy**: Sort both arrays, pair corresponding elements.

```python
def min_absolute_difference_sum(arr1, arr2):
    """
    Minimize sum of absolute differences by pairing.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    n = len(arr1)
    
    # Greedy: sort both arrays, pair corresponding elements
    arr1.sort()
    arr2.sort()
    
    total_diff = 0
    for i in range(n):
        total_diff += abs(arr1[i] - arr2[i])
    
    return total_diff

# Example
arr1 = [4, 1, 8, 7]
arr2 = [2, 3, 6, 5]
min_sum = min_absolute_difference_sum(arr1, arr2)
print(f"Minimum sum: {min_sum}")  # 6
# Pairs: (1,2), (4,3), (7,5), (8,6) -> |1-2|+|4-3|+|7-5|+|8-6| = 6
```

---

### 10. Maximum Length Chain of Pairs

**Problem**: Find longest chain of pairs where (a, b) can be followed by (c, d) if b < c.

**Greedy Strategy**: Sort by second element, select greedily.

```python
def max_chain_length(pairs):
    """
    Find maximum length chain of pairs.
    
    Time Complexity: O(n log n)
    Space Complexity: O(1)
    """
    n = len(pairs)
    
    # Sort by second element (end) - greedy choice
    pairs.sort(key=lambda x: x[1])
    
    chain_length = 1
    last_end = pairs[0][1]
    
    for i in range(1, n):
        # Greedy: if current pair starts after last ends
        if pairs[i][0] > last_end:
            chain_length += 1
            last_end = pairs[i][1]
    
    return chain_length

# Example
pairs = [(5, 24), (15, 25), (27, 40), (50, 60)]
max_length = max_chain_length(pairs)
print(f"Maximum chain length: {max_length}")  # 3
```

---

## Summary: Part 1

### Problems Covered:
1. Activity Selection
2. Fractional Knapsack
3. Minimum Coins (Greedy)
4. Job Sequencing
5. Minimum Platforms
6. Maximum Meetings
7. Egyptian Fraction
8. Maximum Product Subset
9. Minimum Absolute Differences
10. Maximum Chain Length

### Common Patterns Identified:
- **Sorting**: Most greedy problems require sorting
- **Earliest Finish**: For interval/scheduling problems
- **Highest Ratio**: For resource allocation
- **Largest First**: For coin/selection problems

### Key Takeaways:
- Greedy works when local optimum leads to global optimum
- Always prove greedy choice property
- Sorting is often the first step
- Consider edge cases (ties, empty inputs)

---

**Next**: Part 2 will cover intermediate greedy problems including MST, shortest paths, and more complex scheduling problems.


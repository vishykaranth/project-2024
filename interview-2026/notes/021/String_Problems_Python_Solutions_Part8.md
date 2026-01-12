# String Problems in Python - Part 8: Advanced String Algorithms

## Overview

This part covers advanced string algorithms including KMP, Z-algorithm, Manacher's, and suffix structures.

---

## Problem 1: KMP Algorithm (Complete Implementation)

### Problem Statement
Implement KMP algorithm for pattern searching with failure function.

### Solution
```python
def kmp_search(text, pattern):
    """
    Search pattern in text using KMP algorithm.
    Time: O(n + m), Space: O(m)
    """
    def build_lps(pattern):
        """
        Build Longest Proper Prefix which is also Suffix array.
        """
        m = len(pattern)
        lps = [0] * m
        length = 0
        i = 1
        
        while i < m:
            if pattern[i] == pattern[length]:
                length += 1
                lps[i] = length
                i += 1
            else:
                if length != 0:
                    length = lps[length - 1]
                else:
                    lps[i] = 0
                    i += 1
        return lps
    
    n, m = len(text), len(pattern)
    if m == 0:
        return []
    
    lps = build_lps(pattern)
    result = []
    i = j = 0  # i for text, j for pattern
    
    while i < n:
        if text[i] == pattern[j]:
            i += 1
            j += 1
        
        if j == m:
            result.append(i - j)
            j = lps[j - 1]
        elif i < n and text[i] != pattern[j]:
            if j != 0:
                j = lps[j - 1]
            else:
                i += 1
    
    return result

# Test
print(kmp_search("ABABDABACDABABCABCABAB", "ABABCABAB"))  # [10]
print(kmp_search("AAAAABAAABA", "AAAA"))  # [0, 1]
```

### Pattern Used: KMP Algorithm

---

## Problem 2: Z-Algorithm

### Problem Statement
Implement Z-algorithm for pattern matching.

### Solution
```python
def z_algorithm(text, pattern):
    """
    Find all occurrences of pattern using Z-algorithm.
    Time: O(n + m), Space: O(n + m)
    """
    def build_z_array(s):
        """
        Build Z-array where Z[i] is length of longest substring
        starting at i which is also prefix of s.
        """
        n = len(s)
        z = [0] * n
        left = right = 0
        
        for i in range(1, n):
            if i <= right:
                z[i] = min(right - i + 1, z[i - left])
            
            while i + z[i] < n and s[z[i]] == s[i + z[i]]:
                z[i] += 1
            
            if i + z[i] - 1 > right:
                left = i
                right = i + z[i] - 1
        
        return z
    
    # Create combined string
    combined = pattern + '$' + text
    z = build_z_array(combined)
    
    # Find matches
    result = []
    pattern_len = len(pattern)
    for i in range(pattern_len + 1, len(combined)):
        if z[i] == pattern_len:
            result.append(i - pattern_len - 1)
    
    return result

# Test
print(z_algorithm("ABABDABACDABABCABCABAB", "ABABCABAB"))  # [10]
```

### Pattern Used: Z-Algorithm

---

## Problem 3: Manacher's Algorithm (Longest Palindromic Substring)

### Problem Statement
Find longest palindromic substring using Manacher's algorithm in O(n) time.

### Solution
```python
def manacher(s):
    """
    Find longest palindromic substring using Manacher's algorithm.
    Time: O(n), Space: O(n)
    """
    # Transform string: "abc" -> "^#a#b#c#$"
    transformed = '^#' + '#'.join(s) + '#$'
    n = len(transformed)
    p = [0] * n
    center = right = 0
    
    for i in range(1, n - 1):
        # Mirror position
        mirror = 2 * center - i
        
        if i < right:
            p[i] = min(right - i, p[mirror])
        
        # Expand around center
        while transformed[i + p[i] + 1] == transformed[i - p[i] - 1]:
            p[i] += 1
        
        # Update center and right boundary
        if i + p[i] > right:
            center = i
            right = i + p[i]
    
    # Find maximum
    max_len = max(p)
    max_center = p.index(max_len)
    start = (max_center - max_len) // 2
    
    return s[start:start + max_len]

# Test
print(manacher("babad"))  # "bab" or "aba"
print(manacher("cbbd"))  # "bb"
```

### Pattern Used: Manacher's Algorithm

---

## Problem 4: Rabin-Karp Algorithm

### Problem Statement
Implement Rabin-Karp algorithm for multiple pattern matching.

### Solution
```python
def rabin_karp(text, pattern):
    """
    Search pattern using Rabin-Karp rolling hash.
    Time: O(n + m) average, O(nm) worst, Space: O(1)
    """
    n, m = len(text), len(pattern)
    if m == 0 or m > n:
        return []
    
    # Hash function parameters
    base = 256
    mod = 10**9 + 7
    
    # Calculate hash of pattern and first window
    pattern_hash = 0
    window_hash = 0
    h = 1
    
    # h = base^(m-1) % mod
    for i in range(m - 1):
        h = (h * base) % mod
    
    # Calculate initial hashes
    for i in range(m):
        pattern_hash = (pattern_hash * base + ord(pattern[i])) % mod
        window_hash = (window_hash * base + ord(text[i])) % mod
    
    result = []
    
    # Slide window
    for i in range(n - m + 1):
        if pattern_hash == window_hash:
            # Verify (handle hash collision)
            if text[i:i + m] == pattern:
                result.append(i)
        
        if i < n - m:
            # Roll hash
            window_hash = (base * (window_hash - ord(text[i]) * h) + ord(text[i + m])) % mod
            window_hash = (window_hash + mod) % mod  # Handle negative
    
    return result

# Test
print(rabin_karp("ABABDABACDABABCABCABAB", "ABABCABAB"))  # [10]
```

### Pattern Used: Rolling Hash

---

## Problem 5: Suffix Array

### Problem Statement
Build suffix array for string (sorted array of all suffixes).

### Solution
```python
def build_suffix_array(s):
    """
    Build suffix array using sorting.
    Time: O(n^2 log n), Space: O(n^2)
    """
    n = len(s)
    suffixes = []
    
    for i in range(n):
        suffixes.append((s[i:], i))
    
    suffixes.sort()
    return [index for _, index in suffixes]

# Optimized using doubling method
def build_suffix_array_optimized(s):
    """
    Build suffix array using doubling method.
    Time: O(n log^2 n), Space: O(n)
    """
    n = len(s)
    suffix_arr = list(range(n))
    
    # Initial sort by first character
    suffix_arr.sort(key=lambda i: s[i])
    
    # Double the prefix length in each iteration
    k = 1
    while k < n:
        # Sort by (rank[i], rank[i+k])
        suffix_arr.sort(key=lambda i: (s[i:i+k] if i+k <= n else s[i:], 
                                      s[i+k:i+2*k] if i+2*k <= n else ""))
        k *= 2
    
    return suffix_arr

# Test
print(build_suffix_array("banana"))  # [5, 3, 1, 0, 4, 2]
```

### Pattern Used: Suffix Array Construction

---

## Problem 6: Longest Common Prefix (LCP) Array

### Problem Statement
Build LCP array from suffix array.

### Solution
```python
def build_lcp_array(s, suffix_arr):
    """
    Build LCP array from suffix array.
    Time: O(n), Space: O(n)
    """
    n = len(s)
    lcp = [0] * n
    inv_suffix = [0] * n
    
    # Build inverse suffix array
    for i in range(n):
        inv_suffix[suffix_arr[i]] = i
    
    k = 0
    for i in range(n):
        if inv_suffix[i] == n - 1:
            k = 0
            continue
        
        j = suffix_arr[inv_suffix[i] + 1]
        
        while i + k < n and j + k < n and s[i + k] == s[j + k]:
            k += 1
        
        lcp[inv_suffix[i]] = k
        if k > 0:
            k -= 1
    
    return lcp

# Test
s = "banana"
suffix_arr = build_suffix_array(s)
lcp = build_lcp_array(s, suffix_arr)
print(lcp)  # [0, 1, 3, 0, 0, 2]
```

### Pattern Used: LCP Construction

---

## Problem 7: Longest Repeated Substring

### Problem Statement
Find longest substring that appears at least twice in string.

### Solution
```python
def longest_repeated_substring(s):
    """
    Find longest repeated substring using suffix array and LCP.
    Time: O(n log n), Space: O(n)
    """
    suffix_arr = build_suffix_array(s)
    lcp = build_lcp_array(s, suffix_arr)
    
    # Find maximum in LCP array
    max_lcp = max(lcp) if lcp else 0
    max_index = lcp.index(max_lcp) if max_lcp > 0 else -1
    
    if max_index == -1:
        return ""
    
    # Extract substring
    start = suffix_arr[max_index]
    return s[start:start + max_lcp]

# Test
print(longest_repeated_substring("banana"))  # "ana"
print(longest_repeated_substring("abcpqrabpqpq"))  # "pq"
```

### Pattern Used: Suffix Array + LCP

---

## Problem 8: Count Distinct Substrings

### Problem Statement
Count number of distinct substrings in string.

### Solution
```python
def count_distinct_substrings(s):
    """
    Count distinct substrings using suffix array and LCP.
    Time: O(n^2), Space: O(n)
    """
    n = len(s)
    suffix_arr = build_suffix_array(s)
    lcp = build_lcp_array(s, suffix_arr)
    
    # Total substrings = n*(n+1)/2
    # Subtract common prefixes
    total = n * (n + 1) // 2
    common = sum(lcp)
    
    return total - common

# Alternative: Using set (simpler but slower)
def count_distinct_substrings_set(s):
    """
    Count distinct substrings using set.
    Time: O(n^3), Space: O(n^2)
    """
    distinct = set()
    n = len(s)
    
    for i in range(n):
        for j in range(i + 1, n + 1):
            distinct.add(s[i:j])
    
    return len(distinct)

# Test
print(count_distinct_substrings("abc"))  # 6
print(count_distinct_substrings("aaa"))  # 3
```

### Pattern Used: Suffix Array + LCP

---

## Problem 9: Burrows-Wheeler Transform

### Problem Statement
Implement Burrows-Wheeler Transform for string compression.

### Solution
```python
def burrows_wheeler_transform(s):
    """
    Apply Burrows-Wheeler Transform.
    Time: O(n^2 log n), Space: O(n^2)
    """
    s += '$'  # Add sentinel
    n = len(s)
    
    # Generate all rotations
    rotations = []
    for i in range(n):
        rotations.append(s[i:] + s[:i])
    
    # Sort rotations
    rotations.sort()
    
    # Extract last column
    return ''.join(rot[-1] for rot in rotations)

def inverse_burrows_wheeler(bwt):
    """
    Inverse Burrows-Wheeler Transform.
    Time: O(n^2), Space: O(n^2)
    """
    if not bwt:
        return ""
    
    # Build table column by column
    table = [""] * len(bwt)
    
    for _ in range(len(bwt)):
        # Prepend BWT column to each row
        table = sorted(bwt[i] + table[i] for i in range(len(bwt)))
    
    # Find row ending with sentinel
    for row in table:
        if row.endswith('$'):
            return row.rstrip('$')
    
    return ""

# Test
bwt = burrows_wheeler_transform("banana")
print(bwt)  # "annb$aa"
print(inverse_burrows_wheeler(bwt))  # "banana"
```

### Pattern Used: String Transformation

---

## Problem 10: Aho-Corasick Algorithm (Multiple Pattern Matching)

### Problem Statement
Find all occurrences of multiple patterns in text simultaneously.

### Solution
```python
class AhoCorasick:
    """
    Aho-Corasick algorithm for multiple pattern matching.
    """
    def __init__(self, patterns):
        self.patterns = patterns
        self.build_trie()
        self.build_failure_links()
    
    def build_trie(self):
        """
        Build trie from patterns.
        """
        self.trie = {}
        self.output = {}
        self.fail = {}
        
        for pattern in self.patterns:
            node = self.trie
            for char in pattern:
                if char not in node:
                    node[char] = {}
                node = node[char]
            node['$'] = pattern  # Mark end of pattern
    
    def build_failure_links(self):
        """
        Build failure links for efficient matching.
        """
        from collections import deque
        
        queue = deque()
        
        # Initialize failure links for depth 1
        for char, child in self.trie.items():
            if char != '$':
                self.fail[child] = self.trie
                queue.append(child)
        
        # BFS to build failure links
        while queue:
            current = queue.popleft()
            
            for char, child in current.items():
                if char == '$':
                    continue
                
                queue.append(child)
                fail_node = self.fail.get(current, self.trie)
                
                while fail_node != self.trie and char not in fail_node:
                    fail_node = self.fail.get(fail_node, self.trie)
                
                self.fail[child] = fail_node.get(char, self.trie)
    
    def search(self, text):
        """
        Search all patterns in text.
        Time: O(n + m + z) where z is number of matches
        """
        result = []
        current = self.trie
        
        for i, char in enumerate(text):
            while current != self.trie and char not in current:
                current = self.fail.get(current, self.trie)
            
            current = current.get(char, self.trie)
            
            # Check for matches
            node = current
            while node != self.trie:
                if '$' in node:
                    result.append((i - len(node['$']) + 1, node['$']))
                node = self.fail.get(node, self.trie)
        
        return result

# Test
ac = AhoCorasick(["he", "she", "his", "hers"])
matches = ac.search("ushers")
print(matches)  # [(1, 'she'), (2, 'he'), (2, 'hers')]
```

### Pattern Used: Trie + Failure Links

---

## Summary: Part 8

### Patterns Covered:
1. **KMP**: Efficient single pattern matching
2. **Z-Algorithm**: Pattern matching with Z-array
3. **Manacher's**: O(n) palindromic substring
4. **Rabin-Karp**: Rolling hash for pattern matching
5. **Suffix Array**: All suffixes sorted
6. **LCP Array**: Longest common prefixes
7. **BWT**: String transformation for compression
8. **Aho-Corasick**: Multiple pattern matching

### Key Takeaways:
- Advanced algorithms: O(n) or O(n log n) complexity
- Suffix structures: Powerful for string analysis
- Pattern matching: Multiple efficient approaches
- Compression: Transform for better compression

---

**Next**: Part 9 will cover string problems with advanced data structures.


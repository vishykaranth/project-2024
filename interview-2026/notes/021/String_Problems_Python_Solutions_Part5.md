# String Problems in Python - Part 5: String Parsing & Tokenization

## Overview

This part covers string parsing, tokenization, and extraction problems.

---

## Common Patterns

### Pattern 1: State Machine
**Use Case**: Parse strings with different states
**Time Complexity**: O(n)
**Space Complexity**: O(1)

### Pattern 2: Regular Expressions
**Use Case**: Pattern matching and extraction
**Time Complexity**: O(n)
**Space Complexity**: O(n)

### Pattern 3: Split and Process
**Use Case**: Tokenize and process
**Time Complexity**: O(n)
**Space Complexity**: O(n)

---

## Problem 1: Valid Number

### Problem Statement
Validate if string is a valid number (integer or decimal, with optional e/E).

### Solution
```python
def is_number(s):
    """
    Validate number using state machine.
    Time: O(n), Space: O(1)
    """
    s = s.strip()
    if not s:
        return False
    
    seen_digit = False
    seen_dot = False
    seen_e = False
    
    for i, char in enumerate(s):
        if char.isdigit():
            seen_digit = True
        elif char == '.':
            if seen_dot or seen_e:
                return False
            seen_dot = True
        elif char in ['e', 'E']:
            if seen_e or not seen_digit:
                return False
            seen_e = True
            seen_digit = False  # Reset for exponent part
        elif char in ['+', '-']:
            if i != 0 and s[i - 1] not in ['e', 'E']:
                return False
        else:
            return False
    
    return seen_digit

# Test
print(is_number("0"))  # True
print(is_number(" 0.1 "))  # True
print(is_number("abc"))  # False
print(is_number("1 a"))  # False
print(is_number("2e10"))  # True
```

### Pattern Used: State Machine

---

## Problem 2: Compare Version Numbers

### Problem Statement
Compare two version numbers (e.g., "1.0.1" vs "1").

### Solution
```python
def compare_version(version1, version2):
    """
    Compare version numbers.
    Time: O(n + m), Space: O(n + m)
    """
    v1_parts = list(map(int, version1.split('.')))
    v2_parts = list(map(int, version2.split('.')))
    
    # Pad shorter version with zeros
    max_len = max(len(v1_parts), len(v2_parts))
    v1_parts += [0] * (max_len - len(v1_parts))
    v2_parts += [0] * (max_len - len(v2_parts))
    
    for v1, v2 in zip(v1_parts, v2_parts):
        if v1 < v2:
            return -1
        elif v1 > v2:
            return 1
    
    return 0

# Test
print(compare_version("1.01", "1.001"))  # 0
print(compare_version("1.0", "1.0.0"))  # 0
print(compare_version("0.1", "1.1"))  # -1
```

### Pattern Used: Split and Compare

---

## Problem 3: Basic Calculator II

### Problem Statement
Evaluate expression with +, -, *, / and no parentheses.

### Solution
```python
def calculate_ii(s):
    """
    Evaluate expression with +, -, *, / using stack.
    Time: O(n), Space: O(n)
    """
    stack = []
    num = 0
    operator = '+'
    
    for i, char in enumerate(s):
        if char.isdigit():
            num = num * 10 + int(char)
        
        if (not char.isdigit() and char != ' ') or i == len(s) - 1:
            if operator == '+':
                stack.append(num)
            elif operator == '-':
                stack.append(-num)
            elif operator == '*':
                stack.append(stack.pop() * num)
            elif operator == '/':
                stack.append(int(stack.pop() / num))
            
            operator = char
            num = 0
    
    return sum(stack)

# Test
print(calculate_ii("3+2*2"))  # 7
print(calculate_ii(" 3/2 "))  # 1
print(calculate_ii(" 3+5 / 2 "))  # 5
```

### Pattern Used: Stack

---

## Problem 4: Parse Lisp Expression

### Problem Statement
Parse and evaluate Lisp-like expression (add, multiply, let).

### Solution
```python
def evaluate(expression):
    """
    Evaluate Lisp expression using recursion and stack.
    Time: O(n), Space: O(n)
    """
    def parse(s, i, variables):
        if s[i] == '(':
            i += 1
            op = parse(s, i, variables)[0]
            i += len(op) + 1
            
            if op == 'let':
                new_vars = variables.copy()
                while s[i] != ')':
                    if s[i] == '(' or s[i] == '-' or s[i].isdigit():
                        val, i = parse(s, i, new_vars)
                        return val, i + 1
                    var = parse(s, i, new_vars)[0]
                    i += len(var) + 1
                    val, i = parse(s, i, new_vars)
                    new_vars[var] = val
                    i += 1
                expr, i = parse(s, i, new_vars)
                return expr, i + 1
            else:
                a, i = parse(s, i, variables)
                b, i = parse(s, i, variables)
                if op == 'add':
                    return a + b, i + 1
                else:  # mult
                    return a * b, i + 1
        else:
            j = i
            while j < len(s) and s[j] not in ' ()':
                j += 1
            token = s[i:j]
            if token[0] == '-' or token.isdigit():
                return int(token), j
            else:
                return variables[token], j
    
    return parse(expression, 0, {})[0]

# Simplified version for basic operations
def evaluate_simple(expression):
    # This is a complex problem - simplified structure shown
    # Full implementation requires careful parsing
    pass
```

### Pattern Used: Recursive Parsing

---

## Problem 5: Remove Invalid Parentheses

### Problem Statement
Remove minimum number of parentheses to make string valid.

### Solution
```python
def remove_invalid_parentheses(s):
    """
    Remove invalid parentheses using BFS.
    Time: O(2^n), Space: O(n)
    """
    def is_valid(string):
        count = 0
        for char in string:
            if char == '(':
                count += 1
            elif char == ')':
                count -= 1
                if count < 0:
                    return False
        return count == 0
    
    if is_valid(s):
        return [s]
    
    queue = {s}
    visited = {s}
    result = []
    found = False
    
    while queue:
        next_level = set()
        for current in queue:
            for i in range(len(current)):
                if current[i] not in '()':
                    continue
                new_string = current[:i] + current[i+1:]
                if new_string not in visited:
                    visited.add(new_string)
                    if is_valid(new_string):
                        result.append(new_string)
                        found = True
                    else:
                        next_level.add(new_string)
        
        if found:
            break
        queue = next_level
    
    return result if result else [""]

# Test
print(remove_invalid_parentheses("()())()"))  # ["()()()", "(())()"]
print(remove_invalid_parentheses("(a)())()"))  # ["(a)()()", "(a())()"]
```

### Pattern Used: BFS

---

## Problem 6: Text Justification

### Problem Statement
Justify text by arranging words in lines with max width, adding spaces evenly.

### Solution
```python
def full_justify(words, max_width):
    """
    Justify text lines.
    Time: O(n), Space: O(n)
    """
    result = []
    current_line = []
    current_length = 0
    
    for word in words:
        # Check if word fits in current line
        if current_length + len(word) + len(current_line) <= max_width:
            current_line.append(word)
            current_length += len(word)
        else:
            # Justify current line
            result.append(justify_line(current_line, current_length, max_width))
            current_line = [word]
            current_length = len(word)
    
    # Last line: left-justified
    if current_line:
        last_line = ' '.join(current_line)
        last_line += ' ' * (max_width - len(last_line))
        result.append(last_line)
    
    return result

def justify_line(words, length, max_width):
    if len(words) == 1:
        return words[0] + ' ' * (max_width - length)
    
    spaces_needed = max_width - length
    gaps = len(words) - 1
    spaces_per_gap = spaces_needed // gaps
    extra_spaces = spaces_needed % gaps
    
    line = words[0]
    for i in range(1, len(words)):
        spaces = spaces_per_gap + (1 if i <= extra_spaces else 0)
        line += ' ' * spaces + words[i]
    
    return line

# Test
words = ["This", "is", "an", "example", "of", "text", "justification."]
print(full_justify(words, 16))
```

### Pattern Used: Greedy with Formatting

---

## Problem 7: Decode Ways

### Problem Statement
Count ways to decode string where A=1, B=2, ..., Z=26.

### Solution
```python
def num_decodings(s):
    """
    Count decoding ways using dynamic programming.
    Time: O(n), Space: O(1)
    """
    if not s or s[0] == '0':
        return 0
    
    n = len(s)
    prev2 = 1  # ways for i-2
    prev1 = 1  # ways for i-1
    
    for i in range(1, n):
        current = 0
        
        # Single digit
        if s[i] != '0':
            current += prev1
        
        # Two digits
        two_digit = int(s[i-1:i+1])
        if 10 <= two_digit <= 26:
            current += prev2
        
        prev2, prev1 = prev1, current
    
    return prev1

# Test
print(num_decodings("12"))  # 2 ("AB" or "L")
print(num_decodings("226"))  # 3
print(num_decodings("06"))  # 0
```

### Pattern Used: Dynamic Programming

---

## Problem 8: Scramble String

### Problem Statement
Check if string s2 is a scrambled version of s1.

### Solution
```python
def is_scramble(s1, s2):
    """
    Check if strings are scrambled using memoization.
    Time: O(n^4), Space: O(n^3)
    """
    memo = {}
    
    def dfs(s1, s2):
        if (s1, s2) in memo:
            return memo[(s1, s2)]
        
        if s1 == s2:
            return True
        
        if len(s1) != len(s2) or sorted(s1) != sorted(s2):
            return False
        
        n = len(s1)
        for i in range(1, n):
            # Try both splits
            if (dfs(s1[:i], s2[:i]) and dfs(s1[i:], s2[i:])) or \
               (dfs(s1[:i], s2[n-i:]) and dfs(s1[i:], s2[:n-i])):
                memo[(s1, s2)] = True
                return True
        
        memo[(s1, s2)] = False
        return False
    
    return dfs(s1, s2)

# Test
print(is_scramble("great", "rgeat"))  # True
print(is_scramble("abcde", "caebd"))  # False
```

### Pattern Used: Memoization/DP

---

## Problem 9: Word Break

### Problem Statement
Check if string can be segmented into words from dictionary.

### Solution
```python
def word_break(s, word_dict):
    """
    Check word break using dynamic programming.
    Time: O(n^2), Space: O(n)
    """
    word_set = set(word_dict)
    dp = [False] * (len(s) + 1)
    dp[0] = True
    
    for i in range(1, len(s) + 1):
        for j in range(i):
            if dp[j] and s[j:i] in word_set:
                dp[i] = True
                break
    
    return dp[len(s)]

# Test
print(word_break("leetcode", ["leet", "code"]))  # True
print(word_break("applepenapple", ["apple", "pen"]))  # True
print(word_break("catsandog", ["cats", "dog", "sand", "and", "cat"]))  # False
```

### Pattern Used: Dynamic Programming

---

## Problem 10: Word Break II

### Problem Statement
Return all possible sentences by adding spaces to string using dictionary words.

### Solution
```python
def word_break_ii(s, word_dict):
    """
    Find all word break combinations using backtracking with memoization.
    Time: O(2^n), Space: O(2^n)
    """
    word_set = set(word_dict)
    memo = {}
    
    def backtrack(start):
        if start in memo:
            return memo[start]
        
        if start == len(s):
            return [""]
        
        result = []
        for end in range(start + 1, len(s) + 1):
            word = s[start:end]
            if word in word_set:
                suffixes = backtrack(end)
                for suffix in suffixes:
                    if suffix:
                        result.append(word + " " + suffix)
                    else:
                        result.append(word)
        
        memo[start] = result
        return result
    
    return backtrack(0)

# Test
print(word_break_ii("catsanddog", ["cat", "cats", "and", "sand", "dog"]))
# ["cats and dog", "cat sand dog"]
```

### Pattern Used: Backtracking with Memoization

---

## Summary: Part 5

### Patterns Covered:
1. **State Machine**: Number validation, parsing
2. **Stack**: Expression evaluation
3. **Dynamic Programming**: Decoding, word break
4. **Backtracking**: Generate all solutions
5. **BFS**: Remove invalid parentheses

### Key Takeaways:
- State machine: Handle different parsing states
- Stack: Evaluate nested expressions
- DP: Count ways, check feasibility
- Backtracking: Generate all valid combinations

---

**Next**: Part 6 will cover string encoding/decoding and serialization problems.


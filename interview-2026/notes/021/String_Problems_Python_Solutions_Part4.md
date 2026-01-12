# String Problems in Python - Part 4: String Transformation & Manipulation

## Overview

This part covers string transformation, manipulation, and formatting problems.

---

## Common Patterns

### Pattern 1: Character Mapping
**Use Case**: Transform characters based on rules
**Time Complexity**: O(n)
**Space Complexity**: O(1) or O(k)

### Pattern 2: String Building
**Use Case**: Construct strings character by character
**Time Complexity**: O(n)
**Space Complexity**: O(n)

### Pattern 3: Recursive/Iterative Processing
**Use Case**: Process strings with nested structures
**Time Complexity**: Varies
**Space Complexity**: Varies

---

## Problem 1: ZigZag Conversion

### Problem Statement
Convert string into zigzag pattern and read line by line.

### Solution
```python
def convert_zigzag(s, num_rows):
    """
    Convert string to zigzag pattern.
    Time: O(n), Space: O(n)
    """
    if num_rows == 1 or num_rows >= len(s):
        return s
    
    rows = [''] * num_rows
    current_row = 0
    going_down = False
    
    for char in s:
        rows[current_row] += char
        
        # Change direction at top or bottom
        if current_row == 0 or current_row == num_rows - 1:
            going_down = not going_down
        
        current_row += 1 if going_down else -1
    
    return ''.join(rows)

# Test
print(convert_zigzag("PAYPALISHIRING", 3))  # "PAHNAPLSIIGYIR"
print(convert_zigzag("PAYPALISHIRING", 4))  # "PINALSIGYAHRPI"
```

### Pattern Used: Character Mapping with Direction

---

## Problem 2: Integer to Roman

### Problem Statement
Convert integer to Roman numeral.

### Solution
```python
def int_to_roman(num):
    """
    Convert integer to Roman numeral using greedy approach.
    Time: O(1) - fixed number of iterations, Space: O(1)
    """
    values = [1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1]
    symbols = ["M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"]
    
    result = []
    for value, symbol in zip(values, symbols):
        count = num // value
        if count:
            result.append(symbol * count)
            num %= value
    
    return ''.join(result)

# Test
print(int_to_roman(3))  # "III"
print(int_to_roman(58))  # "LVIII"
print(int_to_roman(1994))  # "MCMXCIV"
```

### Pattern Used: Greedy Algorithm

---

## Problem 3: Roman to Integer

### Problem Statement
Convert Roman numeral to integer.

### Solution
```python
def roman_to_int(s):
    """
    Convert Roman numeral to integer.
    Time: O(n), Space: O(1)
    """
    roman_map = {
        'I': 1, 'V': 5, 'X': 10, 'L': 50,
        'C': 100, 'D': 500, 'M': 1000
    }
    
    result = 0
    prev_value = 0
    
    # Process from right to left
    for char in reversed(s):
        value = roman_map[char]
        
        # If current value < previous, subtract (e.g., IV = 4)
        if value < prev_value:
            result -= value
        else:
            result += value
        
        prev_value = value
    
    return result

# Test
print(roman_to_int("III"))  # 3
print(roman_to_int("LVIII"))  # 58
print(roman_to_int("MCMXCIV"))  # 1994
```

### Pattern Used: Right-to-Left Processing

---

## Problem 4: Decode String

### Problem Statement
Decode string with pattern k[encoded_string] where encoded_string repeats k times.

### Solution
```python
def decode_string(s):
    """
    Decode nested string using stack.
    Time: O(n), Space: O(n)
    """
    stack = []
    current_string = ""
    current_num = 0
    
    for char in s:
        if char.isdigit():
            current_num = current_num * 10 + int(char)
        elif char == '[':
            # Push current state to stack
            stack.append((current_string, current_num))
            current_string = ""
            current_num = 0
        elif char == ']':
            # Pop and decode
            prev_string, num = stack.pop()
            current_string = prev_string + current_string * num
        else:
            current_string += char
    
    return current_string

# Test
print(decode_string("3[a]2[bc]"))  # "aaabcbc"
print(decode_string("3[a2[c]]"))  # "accaccacc"
print(decode_string("2[abc]3[cd]ef"))  # "abcabccdcdcdef"
```

### Pattern Used: Stack

---

## Problem 5: Basic Calculator

### Problem Statement
Evaluate expression string containing +, -, parentheses, and spaces.

### Solution
```python
def calculate(s):
    """
    Evaluate basic calculator expression using stack.
    Time: O(n), Space: O(n)
    """
    stack = []
    num = 0
    sign = 1
    result = 0
    
    for char in s:
        if char.isdigit():
            num = num * 10 + int(char)
        elif char == '+':
            result += sign * num
            num = 0
            sign = 1
        elif char == '-':
            result += sign * num
            num = 0
            sign = -1
        elif char == '(':
            # Push current result and sign
            stack.append(result)
            stack.append(sign)
            result = 0
            sign = 1
        elif char == ')':
            # Pop and combine
            result += sign * num
            num = 0
            result *= stack.pop()  # sign
            result += stack.pop()  # previous result
    
    return result + sign * num

# Test
print(calculate("1 + 1"))  # 2
print(calculate(" 2-1 + 2 "))  # 3
print(calculate("(1+(4+5+2)-3)+(6+8)"))  # 23
```

### Pattern Used: Stack

---

## Problem 6: Simplify Path

### Problem Statement
Simplify Unix-style absolute path (handle ., .., multiple slashes).

### Solution
```python
def simplify_path(path):
    """
    Simplify Unix path using stack.
    Time: O(n), Space: O(n)
    """
    stack = []
    components = path.split('/')
    
    for component in components:
        if component == '' or component == '.':
            continue
        elif component == '..':
            if stack:
                stack.pop()
        else:
            stack.append(component)
    
    return '/' + '/'.join(stack)

# Test
print(simplify_path("/home/"))  # "/home"
print(simplify_path("/../"))  # "/"
print(simplify_path("/home//foo/"))  # "/home/foo"
print(simplify_path("/a/./b/../../c/"))  # "/c"
```

### Pattern Used: Stack

---

## Problem 7: Restore IP Addresses

### Problem Statement
Restore all valid IP addresses from string of digits.

### Solution
```python
def restore_ip_addresses(s):
    """
    Restore IP addresses using backtracking.
    Time: O(1) - max 27 combinations, Space: O(1)
    """
    result = []
    
    def backtrack(s, parts, current):
        if len(parts) == 4:
            if not s:  # All digits used
                result.append('.'.join(parts))
            return
        
        # Try 1, 2, or 3 digits
        for i in range(1, min(4, len(s) + 1)):
            segment = s[:i]
            
            # Check validity
            if (segment[0] == '0' and len(segment) > 1) or int(segment) > 255:
                continue
            
            parts.append(segment)
            backtrack(s[i:], parts, current + 1)
            parts.pop()
    
    backtrack(s, [], 0)
    return result

# Test
print(restore_ip_addresses("25525511135"))  
# ["255.255.11.135", "255.255.111.35"]
print(restore_ip_addresses("0000"))  # ["0.0.0.0"]
```

### Pattern Used: Backtracking

---

## Problem 8: Letter Combinations of Phone Number

### Problem Statement
Generate all letter combinations from phone number digits.

### Solution
```python
def letter_combinations(digits):
    """
    Generate letter combinations using backtracking.
    Time: O(4^n * n), Space: O(n)
    """
    if not digits:
        return []
    
    phone_map = {
        '2': 'abc', '3': 'def', '4': 'ghi', '5': 'jkl',
        '6': 'mno', '7': 'pqrs', '8': 'tuv', '9': 'wxyz'
    }
    
    result = []
    
    def backtrack(index, current):
        if index == len(digits):
            result.append(current)
            return
        
        digit = digits[index]
        for letter in phone_map[digit]:
            backtrack(index + 1, current + letter)
    
    backtrack(0, "")
    return result

# Test
print(letter_combinations("23"))  
# ["ad", "ae", "af", "bd", "be", "bf", "cd", "ce", "cf"]
```

### Pattern Used: Backtracking

---

## Problem 9: Generate Parentheses

### Problem Statement
Generate all valid combinations of n pairs of parentheses.

### Solution
```python
def generate_parenthesis(n):
    """
    Generate valid parentheses using backtracking.
    Time: O(4^n / sqrt(n)), Space: O(n)
    """
    result = []
    
    def backtrack(current, open_count, close_count):
        # Base case: valid combination
        if len(current) == 2 * n:
            result.append(current)
            return
        
        # Add opening parenthesis if available
        if open_count < n:
            backtrack(current + '(', open_count + 1, close_count)
        
        # Add closing parenthesis if valid
        if close_count < open_count:
            backtrack(current + ')', open_count, close_count + 1)
    
    backtrack("", 0, 0)
    return result

# Test
print(generate_parenthesis(3))
# ["((()))", "(()())", "(())()", "()(())", "()()()"]
```

### Pattern Used: Backtracking

---

## Problem 10: Add Strings

### Problem Statement
Add two non-negative integers represented as strings.

### Solution
```python
def add_strings(num1, num2):
    """
    Add two number strings.
    Time: O(max(n, m)), Space: O(max(n, m))
    """
    i, j = len(num1) - 1, len(num2) - 1
    carry = 0
    result = []
    
    while i >= 0 or j >= 0 or carry:
        digit1 = int(num1[i]) if i >= 0 else 0
        digit2 = int(num2[j]) if j >= 0 else 0
        
        total = digit1 + digit2 + carry
        carry = total // 10
        result.append(str(total % 10))
        
        i -= 1
        j -= 1
    
    return ''.join(reversed(result))

# Test
print(add_strings("11", "123"))  # "134"
print(add_strings("456", "77"))  # "533"
print(add_strings("0", "0"))  # "0"
```

### Pattern Used: Two Pointers (Right to Left)

---

## Summary: Part 4

### Patterns Covered:
1. **Stack**: Nested structures, parentheses, paths
2. **Backtracking**: Generate combinations, restore addresses
3. **Greedy**: Roman numerals, optimal choices
4. **Two Pointers**: String addition, processing

### Key Takeaways:
- Stack: Essential for nested structures
- Backtracking: Generate all valid combinations
- Greedy: Optimal local choices
- Two pointers: Process from both ends

---

**Next**: Part 5 will cover string parsing and tokenization problems.


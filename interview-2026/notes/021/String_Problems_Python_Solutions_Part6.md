# String Problems in Python - Part 6: Encoding, Decoding & Serialization

## Overview

This part covers string encoding, decoding, and serialization problems.

---

## Problem 1: Encode and Decode Strings

### Problem Statement
Design algorithm to encode list of strings to single string and decode back.

### Solution
```python
def encode(strs):
    """
    Encode strings with length prefix.
    Time: O(n), Space: O(n)
    """
    encoded = []
    for s in strs:
        encoded.append(str(len(s)) + '#' + s)
    return ''.join(encoded)

def decode(s):
    """
    Decode string back to list.
    Time: O(n), Space: O(n)
    """
    result = []
    i = 0
    
    while i < len(s):
        # Find length
        j = i
        while s[j] != '#':
            j += 1
        length = int(s[i:j])
        
        # Extract string
        result.append(s[j + 1:j + 1 + length])
        i = j + 1 + length
    
    return result

# Test
strs = ["hello", "world", "test"]
encoded = encode(strs)
print(encoded)  # "5#hello5#world4#test"
print(decode(encoded))  # ["hello", "world", "test"]
```

### Pattern Used: Length Prefix Encoding

---

## Problem 2: Serialize and Deserialize Binary Tree (String Representation)

### Problem Statement
Serialize binary tree to string and deserialize back.

### Solution
```python
class TreeNode:
    def __init__(self, val=0, left=None, right=None):
        self.val = val
        self.left = left
        self.right = right

def serialize(root):
    """
    Serialize tree to string using preorder traversal.
    Time: O(n), Space: O(n)
    """
    if not root:
        return "None,"
    
    return str(root.val) + "," + serialize(root.left) + serialize(root.right)

def deserialize(data):
    """
    Deserialize string to tree.
    Time: O(n), Space: O(n)
    """
    def build_tree(nodes):
        if nodes[0] == "None":
            nodes.pop(0)
            return None
        
        node = TreeNode(int(nodes[0]))
        nodes.pop(0)
        node.left = build_tree(nodes)
        node.right = build_tree(nodes)
        return node
    
    nodes = data.split(',')
    return build_tree(nodes)
```

### Pattern Used: Preorder Traversal

---

## Problem 3: URL Encoding/Decoding

### Problem Statement
Encode string for URL (replace spaces with %20) and decode back.

### Solution
```python
def url_encode(s):
    """
    Encode string for URL.
    Time: O(n), Space: O(n)
    """
    return s.replace(' ', '%20')

def url_decode(s):
    """
    Decode URL string.
    Time: O(n), Space: O(n)
    """
    return s.replace('%20', ' ')

# More comprehensive version
def url_encode_comprehensive(s):
    import urllib.parse
    return urllib.parse.quote(s)

def url_decode_comprehensive(s):
    import urllib.parse
    return urllib.parse.unquote(s)
```

### Pattern Used: Character Replacement

---

## Problem 4: Base64 Encoding/Decoding

### Problem Statement
Encode string to Base64 and decode back.

### Solution
```python
import base64

def base64_encode(s):
    """
    Encode string to Base64.
    Time: O(n), Space: O(n)
    """
    return base64.b64encode(s.encode()).decode()

def base64_decode(s):
    """
    Decode Base64 string.
    Time: O(n), Space: O(n)
    """
    return base64.b64decode(s.encode()).decode()

# Manual implementation
def base64_encode_manual(s):
    BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    result = []
    padding = 0
    
    for i in range(0, len(s), 3):
        # Get 3 bytes
        chunk = s[i:i+3]
        if len(chunk) < 3:
            padding = 3 - len(chunk)
            chunk += '\x00' * padding
        
        # Convert to 24-bit number
        num = (ord(chunk[0]) << 16) | (ord(chunk[1]) << 8) | ord(chunk[2])
        
        # Extract 4 groups of 6 bits
        for j in range(4):
            if j < 4 - padding:
                result.append(BASE64_CHARS[(num >> (18 - 6 * j)) & 0x3F])
            else:
                result.append('=')
    
    return ''.join(result)
```

### Pattern Used: Bit Manipulation

---

## Problem 5: Run-Length Encoding

### Problem Statement
Encode string using run-length encoding (e.g., "aaabb" -> "a3b2").

### Solution
```python
def run_length_encode(s):
    """
    Encode string using run-length encoding.
    Time: O(n), Space: O(n)
    """
    if not s:
        return ""
    
    result = []
    count = 1
    current = s[0]
    
    for i in range(1, len(s)):
        if s[i] == current:
            count += 1
        else:
            result.append(current + str(count))
            current = s[i]
            count = 1
    
    result.append(current + str(count))
    return ''.join(result)

def run_length_decode(s):
    """
    Decode run-length encoded string.
    Time: O(n), Space: O(n)
    """
    result = []
    i = 0
    
    while i < len(s):
        char = s[i]
        i += 1
        
        # Extract number
        num_str = ""
        while i < len(s) and s[i].isdigit():
            num_str += s[i]
            i += 1
        
        result.append(char * int(num_str))
    
    return ''.join(result)

# Test
encoded = run_length_encode("aaabbcc")
print(encoded)  # "a3b2c2"
print(run_length_decode(encoded))  # "aaabbcc"
```

### Pattern Used: Character Counting

---

## Problem 6: Huffman Encoding (String Compression)

### Problem Statement
Encode string using Huffman coding for compression.

### Solution
```python
import heapq
from collections import Counter

class HuffmanNode:
    def __init__(self, char, freq, left=None, right=None):
        self.char = char
        self.freq = freq
        self.left = left
        self.right = right
    
    def __lt__(self, other):
        return self.freq < other.freq

def build_huffman_tree(text):
    """
    Build Huffman tree from text.
    Time: O(n log n), Space: O(n)
    """
    # Count frequencies
    freq = Counter(text)
    
    # Create priority queue
    heap = [HuffmanNode(char, freq) for char, freq in freq.items()]
    heapq.heapify(heap)
    
    # Build tree
    while len(heap) > 1:
        left = heapq.heappop(heap)
        right = heapq.heappop(heap)
        merged = HuffmanNode(None, left.freq + right.freq, left, right)
        heapq.heappush(heap, merged)
    
    return heap[0]

def build_codes(root, code="", codes={}):
    """
    Build encoding table from tree.
    Time: O(n), Space: O(n)
    """
    if root.char:
        codes[root.char] = code if code else "0"
    else:
        build_codes(root.left, code + "0", codes)
        build_codes(root.right, code + "1", codes)
    return codes

def huffman_encode(text):
    """
    Encode text using Huffman coding.
    """
    if not text:
        return "", {}
    
    root = build_huffman_tree(text)
    codes = build_codes(root)
    
    encoded = ''.join(codes[char] for char in text)
    return encoded, codes

def huffman_decode(encoded, codes):
    """
    Decode Huffman encoded string.
    """
    reverse_codes = {v: k for k, v in codes.items()}
    result = []
    current = ""
    
    for bit in encoded:
        current += bit
        if current in reverse_codes:
            result.append(reverse_codes[current])
            current = ""
    
    return ''.join(result)
```

### Pattern Used: Greedy Algorithm (Huffman)

---

## Problem 7: String Compression (LeetCode Style)

### Problem Statement
Compress string in-place: "aabcccccaaa" -> "a2b1c5a3". If compressed not shorter, return original.

### Solution
```python
def compress(chars):
    """
    Compress string in-place.
    Time: O(n), Space: O(1)
    """
    write = 0
    read = 0
    
    while read < len(chars):
        char = chars[read]
        count = 0
        
        # Count consecutive characters
        while read < len(chars) and chars[read] == char:
            count += 1
            read += 1
        
        # Write character
        chars[write] = char
        write += 1
        
        # Write count if > 1
        if count > 1:
            for digit in str(count):
                chars[write] = digit
                write += 1
    
    return write

# Test
chars = list("aabcccccaaa")
length = compress(chars)
print(chars[:length])  # ['a', '2', 'b', '1', 'c', '5', 'a', '3']
```

### Pattern Used: Two Pointers

---

## Problem 8: Encode String with Shortest Length

### Problem Statement
Encode string by collapsing repeated substrings: "ababab" -> "3[ab]".

### Solution
```python
def encode_shortest(s):
    """
    Encode string with shortest length using DP.
    Time: O(n^3), Space: O(n^2)
    """
    n = len(s)
    dp = [[""] * n for _ in range(n)]
    
    for length in range(1, n + 1):
        for i in range(n - length + 1):
            j = i + length - 1
            substr = s[i:j+1]
            
            # Try encoding as repetition
            encoded = substr
            for k in range(i, j):
                if len(dp[i][k] + dp[k+1][j]) < len(encoded):
                    encoded = dp[i][k] + dp[k+1][j]
            
            # Check if substring can be encoded as repetition
            repeated = (substr * 2).find(substr, 1)
            if repeated != -1 and repeated < len(substr):
                count = len(substr) // repeated
                pattern = substr[:repeated]
                candidate = f"{count}[{dp[i][i+repeated-1]}]"
                if len(candidate) < len(encoded):
                    encoded = candidate
            
            dp[i][j] = encoded
    
    return dp[0][n-1]

# Test
print(encode_shortest("ababab"))  # "3[ab]"
print(encode_shortest("aaa"))  # "aaa" or "3[a]"
```

### Pattern Used: Dynamic Programming

---

## Problem 9: Decode String (Nested)

### Problem Statement
Decode string with nested patterns: "3[a2[c]]" -> "accaccacc".

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
            stack.append((current_string, current_num))
            current_string = ""
            current_num = 0
        elif char == ']':
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

## Problem 10: HTML Entity Parser

### Problem Statement
Replace HTML entities in string: "&amp;" -> "&", "&quot;" -> '"', etc.

### Solution
```python
def entity_parser(text):
    """
    Parse HTML entities.
    Time: O(n), Space: O(n)
    """
    entities = {
        "&quot;": '"',
        "&apos;": "'",
        "&amp;": "&",
        "&gt;": ">",
        "&lt;": "<",
        "&frasl;": "/"
    }
    
    result = []
    i = 0
    
    while i < len(text):
        if text[i] == '&':
            # Try to match entity
            found = False
            for entity, char in entities.items():
                if text[i:i+len(entity)] == entity:
                    result.append(char)
                    i += len(entity)
                    found = True
                    break
            
            if not found:
                result.append(text[i])
                i += 1
        else:
            result.append(text[i])
            i += 1
    
    return ''.join(result)

# Test
print(entity_parser("&amp; is an HTML entity but &ambassador; is not."))
# "& is an HTML entity but &ambassador; is not."
```

### Pattern Used: Pattern Matching

---

## Summary: Part 6

### Patterns Covered:
1. **Length Prefix**: Simple encoding scheme
2. **Stack**: Nested structure decoding
3. **Bit Manipulation**: Base64 encoding
4. **Character Counting**: Run-length encoding
5. **Greedy**: Huffman encoding
6. **Dynamic Programming**: Optimal encoding

### Key Takeaways:
- Encoding: Add metadata (length, structure)
- Decoding: Use metadata to reconstruct
- Compression: Reduce size while preserving information
- Stack: Essential for nested structures

---

**Next**: Part 7 will cover string validation and formatting problems.


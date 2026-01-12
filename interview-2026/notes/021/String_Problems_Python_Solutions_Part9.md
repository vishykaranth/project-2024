# String Problems in Python - Part 9: String Problems with Data Structures

## Overview

This part covers string problems that require advanced data structures like Trie, Segment Trees, and Fenwick Trees.

---

## Problem 1: Implement Trie (Prefix Tree)

### Problem Statement
Implement Trie data structure with insert, search, and startsWith operations.

### Solution
```python
class Trie:
    """
    Trie (Prefix Tree) implementation.
    """
    def __init__(self):
        self.children = {}
        self.is_end = False
    
    def insert(self, word):
        """
        Insert word into trie.
        Time: O(m) where m is word length, Space: O(m)
        """
        node = self
        for char in word:
            if char not in node.children:
                node.children[char] = Trie()
            node = node.children[char]
        node.is_end = True
    
    def search(self, word):
        """
        Search if word exists in trie.
        Time: O(m), Space: O(1)
        """
        node = self
        for char in word:
            if char not in node.children:
                return False
            node = node.children[char]
        return node.is_end
    
    def starts_with(self, prefix):
        """
        Check if any word starts with prefix.
        Time: O(m), Space: O(1)
        """
        node = self
        for char in prefix:
            if char not in node.children:
                return False
            node = node.children[char]
        return True

# Test
trie = Trie()
trie.insert("apple")
print(trie.search("apple"))  # True
print(trie.search("app"))  # False
print(trie.starts_with("app"))  # True
```

### Pattern Used: Trie Data Structure

---

## Problem 2: Word Search II (Trie + Backtracking)

### Problem Statement
Find all words from dictionary that can be formed in 2D board.

### Solution
```python
def find_words(board, words):
    """
    Find words using Trie and backtracking.
    Time: O(m * n * 4^L), Space: O(ALPHABET_SIZE * N * M)
    """
    # Build Trie
    trie = Trie()
    for word in words:
        trie.insert(word)
    
    result = []
    m, n = len(board), len(board[0])
    
    def backtrack(i, j, node, path):
        char = board[i][j]
        if char not in node.children:
            return
        
        node = node.children[char]
        path += char
        
        if node.is_end:
            result.append(path)
            node.is_end = False  # Avoid duplicates
        
        # Mark as visited
        board[i][j] = '#'
        
        # Explore neighbors
        for di, dj in [(0, 1), (0, -1), (1, 0), (-1, 0)]:
            ni, nj = i + di, j + dj
            if 0 <= ni < m and 0 <= nj < n:
                backtrack(ni, nj, node, path)
        
        # Restore
        board[i][j] = char
    
    for i in range(m):
        for j in range(n):
            backtrack(i, j, trie, "")
    
    return result

# Test
board = [
    ['o','a','a','n'],
    ['e','t','a','e'],
    ['i','h','k','r'],
    ['i','f','l','v']
]
words = ["oath","pea","eat","rain"]
print(find_words(board, words))  # ["oath", "eat"]
```

### Pattern Used: Trie + Backtracking

---

## Problem 3: Longest Word in Dictionary

### Problem Statement
Find longest word that can be built one character at a time from other words in dictionary.

### Solution
```python
def longest_word(words):
    """
    Find longest word using Trie.
    Time: O(sum of word lengths), Space: O(sum of word lengths)
    """
    # Build Trie
    trie = Trie()
    for word in words:
        trie.insert(word)
    
    longest = ""
    
    def dfs(node, path):
        nonlocal longest
        
        if len(path) > len(longest) or (len(path) == len(longest) and path < longest):
            longest = path
        
        for char, child in node.children.items():
            if child.is_end:  # Can only extend from complete words
                dfs(child, path + char)
    
    dfs(trie, "")
    return longest

# Test
print(longest_word(["w","wo","wor","worl","world"]))  # "world"
print(longest_word(["a","banana","app","appl","ap","apply","apple"]))  # "apple"
```

### Pattern Used: Trie + DFS

---

## Problem 4: Replace Words

### Problem Statement
Replace words in sentence with shortest root from dictionary if root is prefix.

### Solution
```python
def replace_words(dictionary, sentence):
    """
    Replace words with roots using Trie.
    Time: O(n * m), Space: O(ALPHABET_SIZE * N * M)
    """
    # Build Trie
    trie = Trie()
    for root in dictionary:
        trie.insert(root)
    
    words = sentence.split()
    result = []
    
    for word in words:
        node = trie
        root = ""
        
        for char in word:
            if char not in node.children:
                break
            node = node.children[char]
            root += char
            if node.is_end:
                break
        
        result.append(root if node.is_end else word)
    
    return ' '.join(result)

# Test
print(replace_words(["cat","bat","rat"], "the cattle was rattled by the battery"))
# "the cat was rat by the bat"
```

### Pattern Used: Trie Prefix Matching

---

## Problem 5: Maximum XOR of Two Numbers in Array

### Problem Statement
Find maximum XOR of two numbers (represented as binary strings).

### Solution
```python
class TrieNode:
    def __init__(self):
        self.children = {}

class BinaryTrie:
    """
    Binary Trie for XOR operations.
    """
    def __init__(self):
        self.root = TrieNode()
    
    def insert(self, num):
        """
        Insert number as binary string.
        Time: O(32), Space: O(32)
        """
        node = self.root
        for i in range(31, -1, -1):
            bit = (num >> i) & 1
            if bit not in node.children:
                node.children[bit] = TrieNode()
            node = node.children[bit]
    
    def find_max_xor(self, num):
        """
        Find maximum XOR with given number.
        Time: O(32), Space: O(1)
        """
        node = self.root
        max_xor = 0
        
        for i in range(31, -1, -1):
            bit = (num >> i) & 1
            # Try opposite bit for maximum XOR
            opposite = 1 - bit
            
            if opposite in node.children:
                max_xor |= (1 << i)
                node = node.children[opposite]
            else:
                node = node.children[bit]
        
        return max_xor

def find_maximum_xor(nums):
    """
    Find maximum XOR of two numbers.
    Time: O(n * 32), Space: O(n * 32)
    """
    trie = BinaryTrie()
    max_xor = 0
    
    for num in nums:
        trie.insert(num)
        max_xor = max(max_xor, trie.find_max_xor(num))
    
    return max_xor

# Test
print(find_maximum_xor([3, 10, 5, 25, 2, 8]))  # 28 (5 XOR 25)
```

### Pattern Used: Binary Trie

---

## Problem 6: Palindrome Pairs

### Problem Statement
Find all pairs of words that form palindrome when concatenated.

### Solution
```python
def palindrome_pairs(words):
    """
    Find palindrome pairs using Trie.
    Time: O(n * k^2), Space: O(n * k)
    """
    def is_palindrome(s):
        return s == s[::-1]
    
    # Build Trie with words and their indices
    trie = {}
    for i, word in enumerate(words):
        node = trie
        for char in reversed(word):
            if char not in node:
                node[char] = {}
            node = node[char]
        node['$'] = i
    
    result = []
    
    for i, word in enumerate(words):
        node = trie
        # Check if remaining part of word is palindrome
        for j in range(len(word)):
            if '$' in node and node['$'] != i:
                if is_palindrome(word[j:]):
                    result.append([i, node['$']])
            
            if word[j] not in node:
                break
            node = node[word[j]]
        else:
            # Word fully matched, check for palindromes in remaining
            def find_palindromes(node, path):
                if '$' in node and node['$'] != i:
                    if is_palindrome(path):
                        result.append([i, node['$']])
                for char, child in node.items():
                    if char != '$':
                        find_palindromes(child, path + char)
            
            find_palindromes(node, "")
    
    return result

# Test
print(palindrome_pairs(["abcd","dcba","lls","s","sssll"]))
# [[0,1], [1,0], [3,2], [2,4]]
```

### Pattern Used: Trie + Palindrome Check

---

## Problem 7: Design Search Autocomplete System

### Problem Statement
Design autocomplete system that returns top 3 sentences with given prefix.

### Solution
```python
class AutocompleteSystem:
    """
    Autocomplete system using Trie.
    """
    def __init__(self, sentences, times):
        self.trie = {}
        self.current_query = ""
        
        # Build Trie with frequencies
        for sentence, time in zip(sentences, times):
            self._insert(sentence, time)
    
    def _insert(self, sentence, time):
        node = self.trie
        for char in sentence:
            if char not in node:
                node[char] = {}
            node = node[char]
        node['$'] = node.get('$', 0) + time
    
    def input(self, c):
        if c == '#':
            # Save query
            self._insert(self.current_query, 1)
            self.current_query = ""
            return []
        
        self.current_query += c
        
        # Find all sentences with prefix
        node = self.trie
        for char in self.current_query:
            if char not in node:
                return []
            node = node[char]
        
        # Collect all sentences
        sentences = []
        def dfs(n, path):
            if '$' in n:
                sentences.append((path, n['$']))
            for char, child in n.items():
                if char != '$':
                    dfs(child, path + char)
        
        dfs(node, self.current_query)
        
        # Sort by frequency (desc) then lexicographically
        sentences.sort(key=lambda x: (-x[1], x[0]))
        return [s[0] for s in sentences[:3]]

# Test
system = AutocompleteSystem(
    ["i love you", "island", "iroman", "i love leetcode"],
    [5, 3, 2, 2]
)
print(system.input('i'))  # ["i love you", "island", "i love leetcode"]
print(system.input(' '))  # ["i love you", "i love leetcode"]
```

### Pattern Used: Trie + Priority

---

## Problem 8: String Matching with Wildcards (Trie)

### Problem Statement
Match strings with wildcards using Trie.

### Solution
```python
class WildcardTrie:
    """
    Trie supporting wildcard matching.
    """
    def __init__(self):
        self.children = {}
        self.is_end = False
    
    def insert(self, word):
        node = self
        for char in word:
            if char not in node.children:
                node.children[char] = WildcardTrie()
            node = node.children[char]
        node.is_end = True
    
    def search(self, word):
        """
        Search with wildcard support.
        """
        def dfs(node, i):
            if i == len(word):
                return node.is_end
            
            char = word[i]
            if char == '.':
                # Try all children
                for child in node.children.values():
                    if dfs(child, i + 1):
                        return True
                return False
            else:
                if char not in node.children:
                    return False
                return dfs(node.children[char], i + 1)
        
        return dfs(self, 0)

# Test
trie = WildcardTrie()
trie.insert("bad")
trie.insert("dad")
trie.insert("mad")
print(trie.search("pad"))  # False
print(trie.search("bad"))  # True
print(trie.search(".ad"))  # True
print(trie.search("b.."))  # True
```

### Pattern Used: Trie + DFS with Wildcards

---

## Problem 9: Count Words with Given Prefix

### Problem Statement
Count number of words in Trie with given prefix.

### Solution
```python
class PrefixCounter:
    """
    Trie with prefix counting.
    """
    def __init__(self):
        self.children = {}
        self.count = 0
        self.is_end = False
    
    def insert(self, word):
        node = self
        for char in word:
            if char not in node.children:
                node.children[char] = PrefixCounter()
            node = node.children[char]
            node.count += 1
        node.is_end = True
    
    def count_words_with_prefix(self, prefix):
        """
        Count words with given prefix.
        Time: O(m), Space: O(1)
        """
        node = self
        for char in prefix:
            if char not in node.children:
                return 0
            node = node.children[char]
        return node.count

# Test
counter = PrefixCounter()
counter.insert("apple")
counter.insert("app")
counter.insert("application")
print(counter.count_words_with_prefix("app"))  # 3
print(counter.count_words_with_prefix("ap"))  # 3
```

### Pattern Used: Trie with Counting

---

## Problem 10: Stream of Characters (Trie)

### Problem Statement
Check if last k characters form any word in dictionary.

### Solution
```python
class StreamChecker:
    """
    Check stream of characters against dictionary.
    """
    def __init__(self, words):
        self.trie = {}
        self.stream = []
        
        # Build reversed Trie
        for word in words:
            node = self.trie
            for char in reversed(word):
                if char not in node:
                    node[char] = {}
                node = node[char]
            node['$'] = True
    
    def query(self, letter):
        """
        Check if suffix forms word.
        Time: O(m) where m is max word length, Space: O(1)
        """
        self.stream.append(letter)
        
        # Check from end backwards
        node = self.trie
        for i in range(len(self.stream) - 1, -1, -1):
            char = self.stream[i]
            if char not in node:
                return False
            node = node[char]
            if '$' in node:
                return True
        
        return False

# Test
checker = StreamChecker(["cd", "f", "kl"])
print(checker.query('a'))  # False
print(checker.query('b'))  # False
print(checker.query('c'))  # False
print(checker.query('d'))  # True (cd)
```

### Pattern Used: Reversed Trie

---

## Summary: Part 9

### Patterns Covered:
1. **Trie**: Prefix tree for efficient prefix operations
2. **Binary Trie**: For bitwise operations (XOR)
3. **Trie + Backtracking**: Word search, combinations
4. **Trie + DFS**: Traversal and matching
5. **Reversed Trie**: Suffix matching

### Key Takeaways:
- Trie: O(m) search/insert for words of length m
- Prefix operations: Natural fit for Trie
- Wildcard matching: DFS through Trie
- Stream processing: Reversed Trie for suffix checking

---

**Next**: Part 10 will cover complex string problems and combinations of multiple patterns.


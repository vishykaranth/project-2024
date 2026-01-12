# Backtracking Problems and Solutions - Part 2

## Advanced Backtracking Problems

This part covers more complex backtracking problems including constraint satisfaction, grid-based problems, and path finding.

---

## Problem 9: N-Queens

### Problem Statement:
Place n queens on an n×n chessboard such that no two queens attack each other. Return all distinct solutions.

**Example:**
```
Input: n = 4
Output: [[".Q..","...Q","Q...","..Q."], ["..Q.","Q...","...Q",".Q.."]]
```

### Solution:
```python
def solveNQueens(n):
    """
    Solve N-Queens problem using backtracking
    Time: O(n!)
    Space: O(n) for recursion stack
    """
    result = []
    board = [['.' for _ in range(n)] for _ in range(n)]
    
    def is_safe(row, col):
        """Check if placing queen at (row, col) is safe"""
        # Check column
        for i in range(row):
            if board[i][col] == 'Q':
                return False
        
        # Check upper-left diagonal
        i, j = row - 1, col - 1
        while i >= 0 and j >= 0:
            if board[i][j] == 'Q':
                return False
            i -= 1
            j -= 1
        
        # Check upper-right diagonal
        i, j = row - 1, col + 1
        while i >= 0 and j < n:
            if board[i][j] == 'Q':
                return False
            i -= 1
            j += 1
        
        return True
    
    def backtrack(row):
        # Base case: all queens placed
        if row == n:
            # Convert board to required format
            solution = [''.join(row) for row in board]
            result.append(solution)
            return
        
        # Try placing queen in each column of current row
        for col in range(n):
            if is_safe(row, col):
                board[row][col] = 'Q'
                backtrack(row + 1)
                board[row][col] = '.'  # Backtrack
    
    backtrack(0)
    return result

# Optimized version using sets for O(1) lookup
def solveNQueens_optimized(n):
    """Optimized with sets for diagonal tracking"""
    result = []
    board = [['.' for _ in range(n)] for _ in range(n)]
    
    # Track occupied columns and diagonals
    cols = set()
    diag1 = set()  # row - col (constant)
    diag2 = set()  # row + col (constant)
    
    def backtrack(row):
        if row == n:
            solution = [''.join(row) for row in board]
            result.append(solution)
            return
        
        for col in range(n):
            # Check if position is safe
            if col in cols or (row - col) in diag1 or (row + col) in diag2:
                continue
            
            # Place queen
            board[row][col] = 'Q'
            cols.add(col)
            diag1.add(row - col)
            diag2.add(row + col)
            
            backtrack(row + 1)
            
            # Backtrack
            board[row][col] = '.'
            cols.remove(col)
            diag1.remove(row - col)
            diag2.remove(row + col)
    
    backtrack(0)
    return result

# Test
print(solveNQueens(4))
# Output: [['.Q..', '...Q', 'Q...', '..Q.'], ['..Q.', 'Q...', '...Q', '.Q..']]
```

---

## Problem 10: Sudoku Solver

### Problem Statement:
Write a program to solve a Sudoku puzzle by filling the empty cells. A valid solution must satisfy all of the following rules:
1. Each row must contain digits 1-9 without repetition
2. Each column must contain digits 1-9 without repetition
3. Each 3×3 sub-box must contain digits 1-9 without repetition

**Example:**
```
Input: board = [["5","3",".",".","7",".",".",".","."],
               ["6",".",".","1","9","5",".",".","."],
               [".","9","8",".",".",".",".","6","."],
               ["8",".",".",".","6",".",".",".","3"],
               ["4",".",".","8",".","3",".",".","1"],
               ["7",".",".",".","2",".",".",".","6"],
               [".","6",".",".",".",".","2","8","."],
               [".",".",".","4","1","9",".",".","5"],
               [".",".",".",".","8",".",".","7","9"]]
```

### Solution:
```python
def solveSudoku(board):
    """
    Solve Sudoku using backtracking
    Time: O(9^m) where m is number of empty cells
    Space: O(1) excluding recursion stack
    """
    def is_valid(row, col, num):
        """Check if placing num at (row, col) is valid"""
        # Check row
        for j in range(9):
            if board[row][j] == num:
                return False
        
        # Check column
        for i in range(9):
            if board[i][col] == num:
                return False
        
        # Check 3x3 box
        box_row = (row // 3) * 3
        box_col = (col // 3) * 3
        for i in range(box_row, box_row + 3):
            for j in range(box_col, box_col + 3):
                if board[i][j] == num:
                    return False
        
        return True
    
    def backtrack():
        # Find next empty cell
        for i in range(9):
            for j in range(9):
                if board[i][j] == '.':
                    # Try each digit
                    for num in '123456789':
                        if is_valid(i, j, num):
                            board[i][j] = num
                            if backtrack():  # If solution found, return True
                                return True
                            board[i][j] = '.'  # Backtrack
                    return False  # No valid number found
        return True  # All cells filled
    
    backtrack()

# Optimized: Find next empty cell more efficiently
def solveSudoku_optimized(board):
    """Optimized version"""
    def is_valid(row, col, num):
        for j in range(9):
            if board[row][j] == num:
                return False
        for i in range(9):
            if board[i][col] == num:
                return False
        box_row, box_col = (row // 3) * 3, (col // 3) * 3
        for i in range(box_row, box_row + 3):
            for j in range(box_col, box_col + 3):
                if board[i][j] == num:
                    return False
        return True
    
    def find_empty():
        """Find next empty cell"""
        for i in range(9):
            for j in range(9):
                if board[i][j] == '.':
                    return i, j
        return None, None
    
    def backtrack():
        row, col = find_empty()
        if row is None:  # No empty cells
            return True
        
        for num in '123456789':
            if is_valid(row, col, num):
                board[row][col] = num
                if backtrack():
                    return True
                board[row][col] = '.'
        return False
    
    backtrack()

# Test
board = [["5","3",".",".","7",".",".",".","."],
         ["6",".",".","1","9","5",".",".","."],
         [".","9","8",".",".",".",".","6","."],
         ["8",".",".",".","6",".",".",".","3"],
         ["4",".",".","8",".","3",".",".","1"],
         ["7",".",".",".","2",".",".",".","6"],
         [".","6",".",".",".",".","2","8","."],
         [".",".",".","4","1","9",".",".","5"],
         [".",".",".",".","8",".",".","7","9"]]
solveSudoku(board)
print(board)
```

---

## Problem 11: Word Search

### Problem Statement:
Given an m×n grid of characters `board` and a string `word`, return `true` if `word` exists in the grid. The word can be constructed from letters of sequentially adjacent cells (horizontally or vertically neighboring).

**Example:**
```
Input: board = [["A","B","C","E"],
                ["S","F","C","S"],
                ["A","D","E","E"]], 
       word = "ABCCED"
Output: true
```

### Solution:
```python
def exist(board, word):
    """
    Search for word in grid using backtracking
    Time: O(m * n * 4^L) where L is word length
    Space: O(L) for recursion stack
    """
    m, n = len(board), len(board[0])
    directions = [(0, 1), (1, 0), (0, -1), (-1, 0)]
    
    def backtrack(row, col, index):
        # Base case: found entire word
        if index == len(word):
            return True
        
        # Base case: out of bounds or character doesn't match
        if (row < 0 or row >= m or col < 0 or col >= n or 
            board[row][col] != word[index]):
            return False
        
        # Mark cell as visited (temporarily)
        temp = board[row][col]
        board[row][col] = '#'  # Mark as visited
        
        # Try all 4 directions
        for dr, dc in directions:
            if backtrack(row + dr, col + dc, index + 1):
                return True
        
        # Backtrack: restore cell
        board[row][col] = temp
        return False
    
    # Try starting from each cell
    for i in range(m):
        for j in range(n):
            if backtrack(i, j, 0):
                return True
    
    return False

# Test
board = [["A","B","C","E"],
         ["S","F","C","S"],
         ["A","D","E","E"]]
print(exist(board, "ABCCED"))  # True
print(exist(board, "SEE"))     # True
print(exist(board, "ABCB"))    # False
```

---

## Problem 12: Word Search II

### Problem Statement:
Given an m×n board of characters and a list of strings `words`, return all words on the board.

**Example:**
```
Input: board = [["o","a","a","n"],
                ["e","t","a","e"],
                ["i","h","k","r"],
                ["i","f","l","v"]], 
       words = ["oath","pea","eat","rain"]
Output: ["eat","oath"]
```

### Solution:
```python
def findWords(board, words):
    """
    Find all words in board using Trie + Backtracking
    Time: O(m * n * 4^L * W) where W is number of words
    Space: O(ALPHABET_SIZE * N * M) for Trie
    """
    # Build Trie
    class TrieNode:
        def __init__(self):
            self.children = {}
            self.word = None  # Store word at end node
    
    root = TrieNode()
    for word in words:
        node = root
        for char in word:
            if char not in node.children:
                node.children[char] = TrieNode()
            node = node.children[char]
        node.word = word  # Mark end of word
    
    m, n = len(board), len(board[0])
    result = []
    directions = [(0, 1), (1, 0), (0, -1), (-1, 0)]
    
    def backtrack(row, col, node):
        char = board[row][col]
        curr_node = node.children[char]
        
        # Check if we found a word
        if curr_node.word:
            result.append(curr_node.word)
            curr_node.word = None  # Avoid duplicates
        
        # Mark as visited
        board[row][col] = '#'
        
        # Explore neighbors
        for dr, dc in directions:
            new_row, new_col = row + dr, col + dc
            if (0 <= new_row < m and 0 <= new_col < n and 
                board[new_row][new_col] in curr_node.children):
                backtrack(new_row, new_col, curr_node)
        
        # Backtrack
        board[row][col] = char
        
        # Optimization: remove leaf nodes
        if not curr_node.children:
            del node.children[char]
    
    # Start from each cell
    for i in range(m):
        for j in range(n):
            if board[i][j] in root.children:
                backtrack(i, j, root)
    
    return result

# Test
board = [["o","a","a","n"],
         ["e","t","a","e"],
         ["i","h","k","r"],
         ["i","f","l","v"]]
words = ["oath","pea","eat","rain"]
print(findWords(board, words))  # ['oath', 'eat']
```

---

## Problem 13: Restore IP Addresses

### Problem Statement:
Given a string s containing only digits, return all possible valid IP addresses that can be formed by inserting dots into s.

**Example:**
```
Input: s = "25525511135"
Output: ["255.255.11.135","255.255.111.35"]
```

### Solution:
```python
def restoreIpAddresses(s):
    """
    Restore IP addresses using backtracking
    Time: O(1) - constant time (max 3^4 = 81 possibilities)
    Space: O(1) excluding result
    """
    result = []
    
    def is_valid(segment):
        """Check if IP segment is valid"""
        # Length check
        if len(segment) > 3 or len(segment) == 0:
            return False
        # Leading zero check
        if len(segment) > 1 and segment[0] == '0':
            return False
        # Range check
        num = int(segment)
        return 0 <= num <= 255
    
    def backtrack(start, current):
        # Base case: 4 segments and used all characters
        if len(current) == 4:
            if start == len(s):
                result.append('.'.join(current))
            return
        
        # Try segments of length 1, 2, or 3
        for length in range(1, 4):
            if start + length > len(s):
                break
            
            segment = s[start:start + length]
            if is_valid(segment):
                current.append(segment)
                backtrack(start + length, current)
                current.pop()  # Backtrack
    
    backtrack(0, [])
    return result

# Test
print(restoreIpAddresses("25525511135"))
# Output: ['255.255.11.135', '255.255.111.35']
```

---

## Problem 14: Generate Parentheses

### Problem Statement:
Given n pairs of parentheses, write a function to generate all combinations of well-formed parentheses.

**Example:**
```
Input: n = 3
Output: ["((()))","(()())","(())()","()(())","()()()"]
```

### Solution:
```python
def generateParenthesis(n):
    """
    Generate all valid parentheses combinations
    Time: O(4^n / sqrt(n)) - Catalan number
    Space: O(n) for recursion stack
    """
    result = []
    
    def backtrack(current, open_count, close_count):
        # Base case: valid combination complete
        if len(current) == 2 * n:
            result.append(''.join(current))
            return
        
        # Add opening parenthesis if we haven't used all
        if open_count < n:
            current.append('(')
            backtrack(current, open_count + 1, close_count)
            current.pop()  # Backtrack
        
        # Add closing parenthesis if valid (more opens than closes)
        if close_count < open_count:
            current.append(')')
            backtrack(current, open_count, close_count + 1)
            current.pop()  # Backtrack
    
    backtrack([], 0, 0)
    return result

# Test
print(generateParenthesis(3))
# Output: ['((()))', '(()())', '(())()', '()(())', '()()()']
```

---

## Problem 15: Word Break II

### Problem Statement:
Given a string s and a dictionary of strings wordDict, add spaces in s to construct a sentence where each word is a valid dictionary word. Return all such possible sentences.

**Example:**
```
Input: s = "catsanddog", wordDict = ["cat","cats","and","sand","dog"]
Output: ["cats and dog","cat sand dog"]
```

### Solution:
```python
def wordBreak(s, wordDict):
    """
    Break string into words using backtracking with memoization
    Time: O(2^n) without memo, O(n^2) with memo
    Space: O(n^2) for memo
    """
    word_set = set(wordDict)
    memo = {}  # Memoization: string -> list of sentences
    
    def backtrack(start):
        # Base case: reached end of string
        if start == len(s):
            return [""]
        
        # Check memo
        if start in memo:
            return memo[start]
        
        result = []
        
        # Try each possible end position
        for end in range(start + 1, len(s) + 1):
            word = s[start:end]
            
            if word in word_set:
                # Get sentences for remaining substring
                sentences = backtrack(end)
                
                # Combine current word with sentences
                for sentence in sentences:
                    if sentence:
                        result.append(word + " " + sentence)
                    else:
                        result.append(word)
        
        memo[start] = result
        return result
    
    return backtrack(0)

# Test
print(wordBreak("catsanddog", ["cat","cats","and","sand","dog"]))
# Output: ['cat sand dog', 'cats and dog']
```

---

## Problem 16: Remove Invalid Parentheses

### Problem Statement:
Given a string s that contains parentheses and letters, remove the minimum number of invalid parentheses to make the input string valid. Return all possible results.

**Example:**
```
Input: s = "()())()"
Output: ["(())()","()()()"]
```

### Solution:
```python
def removeInvalidParentheses(s):
    """
    Remove invalid parentheses using backtracking
    Time: O(2^n) in worst case
    Space: O(n) for recursion stack
    """
    # First, find minimum removals needed
    def get_min_removals(s):
        left_removals = right_removals = 0
        for char in s:
            if char == '(':
                left_removals += 1
            elif char == ')':
                if left_removals > 0:
                    left_removals -= 1
                else:
                    right_removals += 1
        return left_removals, right_removals
    
    left_removals, right_removals = get_min_removals(s)
    result = set()
    
    def is_valid(s):
        """Check if string has valid parentheses"""
        count = 0
        for char in s:
            if char == '(':
                count += 1
            elif char == ')':
                count -= 1
                if count < 0:
                    return False
        return count == 0
    
    def backtrack(index, current, left_rem, right_rem, left_count):
        # Base case: processed entire string
        if index == len(s):
            if left_rem == 0 and right_rem == 0 and is_valid(current):
                result.add(current)
            return
        
        char = s[index]
        
        # Option 1: Remove current character (if it's a parenthesis)
        if (char == '(' and left_rem > 0) or (char == ')' and right_rem > 0):
            backtrack(index + 1, current, 
                     left_rem - (1 if char == '(' else 0),
                     right_rem - (1 if char == ')' else 0),
                     left_count)
        
        # Option 2: Keep current character
        current += char
        if char == '(':
            backtrack(index + 1, current, left_rem, right_rem, left_count + 1)
        elif char == ')':
            if left_count > 0:  # Only if we have matching opening
                backtrack(index + 1, current, left_rem, right_rem, left_count - 1)
        else:
            backtrack(index + 1, current, left_rem, right_rem, left_count)
    
    backtrack(0, "", left_removals, right_removals, 0)
    return list(result)

# Test
print(removeInvalidParentheses("()())()"))
# Output: ['(())()', '()()()']
```

---

## Common Patterns Identified (Part 2)

### Pattern 5: Constraint Satisfaction (N-Queens, Sudoku)
- **Template**: Check constraints before placing, backtrack if invalid
- **Key**: Efficient constraint checking (use sets/maps for O(1) lookup)
- **Optimization**: Prune early, use memoization

### Pattern 6: Grid/Matrix Backtracking (Word Search, Sudoku)
- **Template**: Explore neighbors, mark visited, backtrack
- **Key**: Use directions array, restore state after backtracking
- **Optimization**: Early termination when solution found

### Pattern 7: String Partitioning (IP Addresses, Word Break)
- **Template**: Try different partition points, validate segments
- **Key**: Validate each segment before recursing
- **Optimization**: Memoization for repeated subproblems

### Pattern 8: Balanced Structures (Parentheses)
- **Template**: Track opening/closing counts, maintain balance
- **Key**: Only add closing if more opens than closes
- **Optimization**: Prune invalid paths early

---

## Time Complexity Analysis (Part 2)

| Problem | Time Complexity | Space Complexity |
|---------|---------------|------------------|
| N-Queens | O(n!) | O(n) |
| Sudoku | O(9^m) | O(1) |
| Word Search | O(m * n * 4^L) | O(L) |
| Word Search II | O(m * n * 4^L * W) | O(ALPHABET * N * M) |
| Restore IP | O(1) | O(1) |
| Generate Parentheses | O(4^n / sqrt(n)) | O(n) |
| Word Break II | O(2^n) → O(n^2) with memo | O(n^2) |
| Remove Invalid Parentheses | O(2^n) | O(n) |

---

## Key Takeaways (Part 2)

1. **Constraint Checking**: Efficient constraint validation is crucial
2. **State Management**: Mark visited, restore after backtracking
3. **Memoization**: Cache results for repeated subproblems
4. **Early Pruning**: Stop exploring invalid paths immediately
5. **Grid Problems**: Use direction arrays for neighbor exploration

---

**Next**: Part 3 will cover advanced problems, optimization techniques, and pattern recognition strategies.


## Longest Substring Without Repeating Characters

To solve this problem efficiently, we use the **Sliding Window** technique with a hash map. This allows us to find the longest substring in a single pass ( time).

### The Strategy: Sliding Window

We maintain a "window" of characters defined by two pointers, `left` and `right`. As we move the `right` pointer to expand the window, we check if the current character has been seen before.

1. **Expansion**: Move the `right` pointer one character at a time.
2. **Lookup**: Use a hash map to store the **last index** where each character appeared.
3. **Contraction**: If we encounter a character that is already in our map and its index is within the current window (`index >= left`), we jump the `left` pointer to `index + 1`. This effectively "removes" the duplicate.
4. **Update**: Calculate the current window size () and update the maximum length found so far.

---

### Python Implementation

```python
class Solution:
    def lengthOfLongestSubstring(self, s: str) -> int:
        # Maps character -> its last seen index
        char_map = {}
        left = 0
        max_length = 0
        
        for right, char in enumerate(s):
            # If the character is in the map and within the current window
            if char in char_map and char_map[char] >= left:
                # Move the left pointer past the previous occurrence
                left = char_map[char] + 1
            
            # Record/Update the character's position
            char_map[char] = right
            
            # Calculate the window size and update max_length
            max_length = max(max_length, right - left + 1)
            
        return max_length

```

---

### Step-by-Step Example: `s = "abcabcbb"`

| `right` | `char` | `left` | `char_map` update | `max_length` |
| --- | --- | --- | --- | --- |
| 0 | 'a' | 0 | `{'a': 0}` | 1 |
| 1 | 'b' | 0 | `{'a': 0, 'b': 1}` | 2 |
| 2 | 'c' | 0 | `{'a': 0, 'b': 1, 'c': 2}` | 3 |
| 3 | 'a' | **1** | `{'a': 3, 'b': 1, 'c': 2}` | 3 (window "bca") |
| 4 | 'b' | **2** | `{'a': 3, 'b': 4, 'c': 2}` | 3 (window "cab") |

---

### Complexity Analysis

* **Time Complexity**: , where  is the length of the string. Each character is visited at most twice (once by the `right` pointer and once conceptually by the `left` pointer).
* **Space Complexity**: , where  is the size of the character set (alphabet/symbols). In the worst case, we store every unique character of the string in the hash map.

### Why not use a Set?

While a `set` also works for the sliding window, using a `map` to store indices is more efficient. With a `set`, you might have to increment the `left` pointer one by one until the duplicate is gone. With a `map`, you can **jump** the `left` pointer directly to the correct spot.

Would you like me to explain how this logic handles special characters like spaces and symbols?
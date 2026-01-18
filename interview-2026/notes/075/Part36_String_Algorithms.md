# Part 36: String Algorithms - Quick Revision

## String Matching

- **Naive**: O(nm) time, check pattern at each position
- **KMP (Knuth-Morris-Pratt)**: O(n + m) time, uses failure function
- **Rabin-Karp**: O(n + m) average, hash-based, rolling hash
- **Boyer-Moore**: O(nm) worst, O(n/m) best, skip characters

## String Operations

- **Substring**: Extract portion of string, O(n) time
- **Concatenation**: Combine strings, use StringBuilder for multiple
- **Pattern Matching**: Regular expressions, finite automata
- **String Comparison**: Lexicographic order, case-sensitive/insensitive

## Common Problems

- **Longest Common Substring**: Dynamic programming, O(nm) time
- **Longest Palindromic Substring**: Expand around centers, O(n²) time
- **Anagram Detection**: Sort and compare, or count characters
- **String Compression**: Run-length encoding, Huffman coding

## String Data Structures

- **Trie (Prefix Tree)**: Store strings, prefix matching, O(m) search
- **Suffix Tree**: All suffixes of string, substring search, O(m) search
- **Use Cases**: Autocomplete, spell checker, text search

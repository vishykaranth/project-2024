# Greedy Algorithms: Optimal Substructure, Greedy Choice

## Overview

Greedy algorithms make locally optimal choices at each step, hoping to find a global optimum. They work when problems have optimal substructure and greedy choice property.

## 1. Greedy Algorithm Principles

### Key Properties

```
┌─────────────────────────────────────────────────────────┐
│         Greedy Algorithm Properties                     │
└─────────────────────────────────────────────────────────┘

1. Greedy Choice Property
   - Locally optimal choice leads to globally optimal solution
   - Make best choice at each step

2. Optimal Substructure
   - Optimal solution contains optimal solutions to subproblems
   - Similar to DP but no overlapping subproblems
```

## 2. Activity Selection Problem

```java
// Select maximum activities: O(n log n) time
public int activitySelection(int[] start, int[] end) {
    int n = start.length;
    int[][] activities = new int[n][2];
    for (int i = 0; i < n; i++) {
        activities[i] = new int[]{start[i], end[i]};
    }
    
    // Sort by end time
    Arrays.sort(activities, (a, b) -> a[1] - b[1]);
    
    int count = 1;
    int lastEnd = activities[0][1];
    
    for (int i = 1; i < n; i++) {
        if (activities[i][0] >= lastEnd) {
            count++;
            lastEnd = activities[i][1];
        }
    }
    
    return count;
}
```

## 3. Fractional Knapsack

```java
// Maximum value with fractional items: O(n log n) time
public double fractionalKnapsack(int[] weights, int[] values, int W) {
    int n = weights.length;
    double[][] items = new double[n][3];
    
    for (int i = 0; i < n; i++) {
        items[i] = new double[]{weights[i], values[i], (double)values[i] / weights[i]};
    }
    
    // Sort by value/weight ratio (descending)
    Arrays.sort(items, (a, b) -> Double.compare(b[2], a[2]));
    
    double totalValue = 0;
    int remaining = W;
    
    for (double[] item : items) {
        if (remaining <= 0) break;
        double weight = item[0];
        double value = item[1];
        
        if (weight <= remaining) {
            totalValue += value;
            remaining -= weight;
        } else {
            totalValue += value * (remaining / weight);
            remaining = 0;
        }
    }
    
    return totalValue;
}
```

## 4. Huffman Coding

```java
// Build Huffman tree: O(n log n) time
class HuffmanNode {
    char data;
    int freq;
    HuffmanNode left, right;
}

public HuffmanNode buildHuffmanTree(char[] chars, int[] freq) {
    PriorityQueue<HuffmanNode> pq = new PriorityQueue<>(
        (a, b) -> a.freq - b.freq
    );
    
    for (int i = 0; i < chars.length; i++) {
        HuffmanNode node = new HuffmanNode();
        node.data = chars[i];
        node.freq = freq[i];
        pq.offer(node);
    }
    
    while (pq.size() > 1) {
        HuffmanNode left = pq.poll();
        HuffmanNode right = pq.poll();
        
        HuffmanNode merged = new HuffmanNode();
        merged.freq = left.freq + right.freq;
        merged.left = left;
        merged.right = right;
        
        pq.offer(merged);
    }
    
    return pq.poll();
}
```

## Summary

**Greedy Algorithms:**
- **Principle**: Make locally optimal choices
- **Properties**: Greedy choice + Optimal substructure
- **Use**: When greedy choice leads to optimal solution
- **Examples**: Activity selection, fractional knapsack, Huffman coding

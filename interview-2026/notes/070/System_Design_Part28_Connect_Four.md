# Low-Level Design Interview: Design Connect Four w/ a Ex-Meta Staff Engineer

## Overview

Designing Connect Four requires game state management, move validation, win detection, and multiplayer support. This guide covers game logic, board representation, and player management.

## System Requirements

```
┌─────────────────────────────────────────────────────────┐
│              Requirements                               │
└─────────────────────────────────────────────────────────┘

Functional:
├─ Create game
├─ Make moves
├─ Validate moves
├─ Detect win condition
├─ Handle turns
└─ Game state management
```

## Class Design

```
┌─────────────────────────────────────────────────────────┐
│              Class Diagram                             │
└─────────────────────────────────────────────────────────┘

Board
├─ rows: int
├─ cols: int
├─ grid: int[][]
└─ methods: 
    ├─ makeMove(col, player)
    ├─ isValidMove(col)
    ├─ checkWin(row, col)
    └─ isFull()

Game
├─ board: Board
├─ currentPlayer: Player
├─ status: GameStatus
└─ methods:
    ├─ startGame()
    ├─ makeMove(col)
    └─ getWinner()

Player
├─ playerId
├─ color (Red/Yellow)
└─ score
```

## Win Detection

```
┌─────────────────────────────────────────────────────────┐
│         Win Detection Algorithm                        │
└─────────────────────────────────────────────────────────┘

After each move, check:
├─ Horizontal: 4 in a row
├─ Vertical: 4 in a column
├─ Diagonal (\): 4 diagonally
└─ Diagonal (/): 4 diagonally

Check only around last move position
for efficiency.
```

## Summary

Connect Four Design:
- **Board**: 2D grid representation
- **Game Logic**: Move validation and win detection
- **State Management**: Game state tracking
- **Players**: Turn-based gameplay
- **Win Detection**: Check 4 directions

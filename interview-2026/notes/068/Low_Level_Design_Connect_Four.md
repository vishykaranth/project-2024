# Low-Level Design: Connect Four - In-Depth Summary

## 1. Requirements Gathering

### Functional Requirements
- Two players take turns dropping discs into a 7x6 grid
- Players alternate turns (Player 1: Red, Player 2: Yellow)
- Discs fall to the lowest available position in the selected column
- Game ends when a player connects 4 discs horizontally, vertically, or diagonally
- Game ends in a draw if the board is full with no winner
- Players can restart the game

### Non-Functional Requirements
- Efficient win detection algorithm
- Clean, maintainable code structure
- Extensible design (easy to change board size, win condition)
- Thread-safe if needed for multiplayer

### Constraints
- Board size: 7 columns × 6 rows (standard)
- Win condition: 4 in a row
- Two players only

## 2. Class Design

### 2.1 Class Diagram

```
┌─────────────────────────────────────────────────────────┐
│              Class Diagram                              │
└─────────────────────────────────────────────────────────┘

                    ┌──────────────┐
                    │    Game      │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
              ▼            ▼            ▼
      ┌───────────┐  ┌──────────┐  ┌──────────┐
      │   Board   │  │  Player  │  │  Disc    │
      └───────────┘  └──────────┘  └──────────┘
              │
              ▼
      ┌───────────┐
      │   Cell    │
      └───────────┘
```

### 2.2 Core Classes

#### Game Class
```java
public class Game {
    private Board board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private GameStatus status;
    
    public Game(String player1Name, String player2Name) {
        this.board = new Board(6, 7); // rows, cols
        this.player1 = new Player(player1Name, DiscColor.RED);
        this.player2 = new Player(player2Name, DiscColor.YELLOW);
        this.currentPlayer = player1;
        this.status = GameStatus.IN_PROGRESS;
    }
    
    public boolean makeMove(int column) {
        // Validate move
        // Place disc
        // Check win condition
        // Switch player or end game
    }
    
    public GameStatus getStatus() {
        return status;
    }
}
```

#### Board Class
```java
public class Board {
    private int rows;
    private int cols;
    private Cell[][] grid;
    
    public Board(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        initializeBoard();
    }
    
    public boolean isValidMove(int column) {
        // Check if column is valid and has space
    }
    
    public boolean placeDisc(int column, DiscColor color) {
        // Find lowest available row in column
        // Place disc
    }
    
    public boolean checkWin(int row, int col, DiscColor color) {
        // Check horizontal, vertical, diagonal wins
    }
    
    public boolean isFull() {
        // Check if board is completely filled
    }
}
```

#### Player Class
```java
public class Player {
    private String name;
    private DiscColor color;
    
    public Player(String name, DiscColor color) {
        this.name = name;
        this.color = color;
    }
    
    // Getters
}
```

#### Cell Class
```java
public class Cell {
    private DiscColor disc;
    private int row;
    private int col;
    
    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        this.disc = null;
    }
    
    public boolean isEmpty() {
        return disc == null;
    }
    
    public void setDisc(DiscColor color) {
        this.disc = color;
    }
}
```

#### Enums
```java
public enum DiscColor {
    RED, YELLOW, EMPTY
}

public enum GameStatus {
    IN_PROGRESS, PLAYER1_WON, PLAYER2_WON, DRAW
}
```

## 3. Data Structure Design

### 3.1 Board Representation

```
┌─────────────────────────────────────────────────────────┐
│         Board Data Structure                           │
└─────────────────────────────────────────────────────────┘

Option 1: 2D Array
┌─────────────────────────────┐
│  [0][0] [0][1] ... [0][6]  │
│  [1][0] [1][1] ... [1][6]  │
│  ...                        │
│  [5][0] [5][1] ... [5][6]  │
└─────────────────────────────┘

Option 2: Column-based (for easier drop)
┌──────┐
│ Col0 │ → Stack of discs
│ Col1 │ → Stack of discs
│ ...  │
│ Col6 │ → Stack of discs
└──────┘
```

### 3.2 Memory Layout

```
┌─────────────────────────────────────────────────────────┐
│         Memory Representation                           │
└─────────────────────────────────────────────────────────┘

Board (6 rows × 7 cols):
├─ Total cells: 42
├─ Each cell: DiscColor enum (1 byte)
└─ Total memory: ~42 bytes

Alternative: Bitboard
├─ Use bits to represent board state
├─ More memory efficient
└─ Faster win detection
```

## 4. Game Logic Flow

### 4.1 Game Flow Diagram

```
┌─────────────────────────────────────────────────────────┐
│              Game Flow                                  │
└─────────────────────────────────────────────────────────┘

Start Game
    │
    ▼
Initialize Board
    │
    ▼
┌───────────────────┐
│ Player 1 Turn     │
└─────────┬─────────┘
          │
          ▼
    Get Column Input
          │
          ▼
    Validate Move
          │
      ┌───┴───┐
      │       │
   Valid   Invalid
      │       │
      │       └──► Ask Again
      │
      ▼
    Place Disc
      │
      ▼
    Check Win Condition
      │
  ┌───┴───┐
  │       │
 Win    No Win
  │       │
  │       ▼
  │   Check Draw
  │       │
  │   ┌───┴───┐
  │   │       │
  │ Draw   Continue
  │   │       │
  │   │       ▼
  │   │   Switch Player
  │   │       │
  │   │       └──► Player 2 Turn
  │   │
  │   └──► End Game
  │
  └──► End Game
```

### 4.2 Move Validation

```java
public boolean isValidMove(int column) {
    // Check column bounds
    if (column < 0 || column >= cols) {
        return false;
    }
    
    // Check if column has space
    return grid[0][column].isEmpty();
}
```

### 4.3 Disc Placement

```java
public boolean placeDisc(int column, DiscColor color) {
    // Find lowest available row
    for (int row = rows - 1; row >= 0; row--) {
        if (grid[row][column].isEmpty()) {
            grid[row][column].setDisc(color);
            return true;
        }
    }
    return false; // Column full
}
```

## 5. Win Detection Algorithm

### 5.1 Win Detection Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Win Detection Directions                        │
└─────────────────────────────────────────────────────────┘

After placing disc at (row, col):

1. Horizontal Check: → ←
   Check left and right from (row, col)

2. Vertical Check: ↑ ↓
   Check up and down from (row, col)

3. Diagonal (\): ↗ ↙
   Check diagonal from top-left to bottom-right

4. Diagonal (/): ↖ ↘
   Check diagonal from top-right to bottom-left
```

### 5.2 Win Detection Implementation

```java
public boolean checkWin(int row, int col, DiscColor color) {
    return checkHorizontal(row, col, color) ||
           checkVertical(row, col, color) ||
           checkDiagonal1(row, col, color) ||  // \
           checkDiagonal2(row, col, color);     // /
}

private boolean checkHorizontal(int row, int col, DiscColor color) {
    int count = 1; // Current disc
    
    // Check left
    for (int c = col - 1; c >= 0 && grid[row][c].getDisc() == color; c--) {
        count++;
    }
    
    // Check right
    for (int c = col + 1; c < cols && grid[row][c].getDisc() == color; c++) {
        count++;
    }
    
    return count >= 4;
}

private boolean checkVertical(int row, int col, DiscColor color) {
    int count = 1;
    
    // Check up (only need to check down since disc falls)
    for (int r = row + 1; r < rows && grid[r][col].getDisc() == color; r++) {
        count++;
    }
    
    return count >= 4;
}

private boolean checkDiagonal1(int row, int col, DiscColor color) {
    int count = 1;
    
    // Check top-left
    for (int r = row - 1, c = col - 1; 
         r >= 0 && c >= 0 && grid[r][c].getDisc() == color; 
         r--, c--) {
        count++;
    }
    
    // Check bottom-right
    for (int r = row + 1, c = col + 1; 
         r < rows && c < cols && grid[r][c].getDisc() == color; 
         r++, c++) {
        count++;
    }
    
    return count >= 4;
}

private boolean checkDiagonal2(int row, int col, DiscColor color) {
    int count = 1;
    
    // Check top-right
    for (int r = row - 1, c = col + 1; 
         r >= 0 && c < cols && grid[r][c].getDisc() == color; 
         r--, c++) {
        count++;
    }
    
    // Check bottom-left
    for (int r = row + 1, c = col - 1; 
         r < rows && c >= 0 && grid[r][c].getDisc() == color; 
         r++, c--) {
        count++;
    }
    
    return count >= 4;
}
```

### 5.3 Optimized Win Detection

```
┌─────────────────────────────────────────────────────────┐
│         Optimized Win Detection                        │
└─────────────────────────────────────────────────────────┘

Instead of checking all directions from every position:

1. Only check from last placed disc
2. Use direction vectors
3. Count consecutive discs in each direction
4. Early termination if count >= 4

Time Complexity: O(1) - constant time
Space Complexity: O(1) - no extra space
```

## 6. Design Patterns

### 6.1 Strategy Pattern (Win Detection)

```java
interface WinStrategy {
    boolean checkWin(Board board, int row, int col, DiscColor color);
}

class HorizontalWinStrategy implements WinStrategy { }
class VerticalWinStrategy implements WinStrategy { }
class DiagonalWinStrategy implements WinStrategy { }

class WinChecker {
    private List<WinStrategy> strategies;
    
    public boolean checkWin(Board board, int row, int col, DiscColor color) {
        return strategies.stream()
            .anyMatch(strategy -> strategy.checkWin(board, row, col, color));
    }
}
```

### 6.2 State Pattern (Game State)

```java
interface GameState {
    void handleMove(Game game, int column);
}

class InProgressState implements GameState {
    public void handleMove(Game game, int column) {
        // Process move
        // Check win
        // Update state
    }
}

class GameOverState implements GameState {
    public void handleMove(Game game, int column) {
        // Reject moves
        throw new IllegalStateException("Game is over");
    }
}
```

### 6.3 Observer Pattern (Game Events)

```java
interface GameObserver {
    void onMoveMade(int row, int col, DiscColor color);
    void onGameWon(Player winner);
    void onGameDraw();
}

class Game {
    private List<GameObserver> observers;
    
    public void notifyMoveMade(int row, int col, DiscColor color) {
        observers.forEach(obs -> obs.onMoveMade(row, col, color));
    }
}
```

## 7. Complete Implementation Structure

### 7.1 Package Structure

```
┌─────────────────────────────────────────────────────────┐
│         Package Structure                               │
└─────────────────────────────────────────────────────────┘

com.connectfour
├─ model/
│  ├─ Game.java
│  ├─ Board.java
│  ├─ Player.java
│  ├─ Cell.java
│  ├─ DiscColor.java
│  └─ GameStatus.java
├─ strategy/
│  ├─ WinStrategy.java
│  ├─ HorizontalWinStrategy.java
│  ├─ VerticalWinStrategy.java
│  └─ DiagonalWinStrategy.java
├─ service/
│  ├─ GameService.java
│  └─ WinChecker.java
└─ controller/
   └─ GameController.java
```

### 7.2 Complete Game Class

```java
public class Game {
    private Board board;
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private GameStatus status;
    private WinChecker winChecker;
    
    public Game(String player1Name, String player2Name) {
        this.board = new Board(6, 7);
        this.player1 = new Player(player1Name, DiscColor.RED);
        this.player2 = new Player(player2Name, DiscColor.YELLOW);
        this.currentPlayer = player1;
        this.status = GameStatus.IN_PROGRESS;
        this.winChecker = new WinChecker();
    }
    
    public boolean makeMove(int column) {
        if (status != GameStatus.IN_PROGRESS) {
            return false;
        }
        
        if (!board.isValidMove(column)) {
            return false;
        }
        
        int row = board.placeDisc(column, currentPlayer.getColor());
        
        if (winChecker.checkWin(board, row, column, currentPlayer.getColor())) {
            status = (currentPlayer == player1) ? 
                     GameStatus.PLAYER1_WON : GameStatus.PLAYER2_WON;
            return true;
        }
        
        if (board.isFull()) {
            status = GameStatus.DRAW;
            return true;
        }
        
        switchPlayer();
        return true;
    }
    
    private void switchPlayer() {
        currentPlayer = (currentPlayer == player1) ? player2 : player1;
    }
    
    public void reset() {
        board = new Board(6, 7);
        currentPlayer = player1;
        status = GameStatus.IN_PROGRESS;
    }
}
```

## 8. Advanced Features

### 8.1 Undo/Redo Functionality

```java
public class Game {
    private Stack<Move> moveHistory;
    private Stack<Move> undoneMoves;
    
    public boolean makeMove(int column) {
        Move move = new Move(column, currentPlayer.getColor());
        // Execute move
        moveHistory.push(move);
        undoneMoves.clear();
    }
    
    public void undo() {
        if (moveHistory.isEmpty()) return;
        
        Move lastMove = moveHistory.pop();
        board.removeDisc(lastMove.getColumn());
        undoneMoves.push(lastMove);
        switchPlayer();
    }
    
    public void redo() {
        if (undoneMoves.isEmpty()) return;
        
        Move move = undoneMoves.pop();
        makeMove(move.getColumn());
    }
}
```

### 8.2 AI Player (Minimax Algorithm)

```java
public class AIPlayer extends Player {
    private int difficulty; // Depth for minimax
    
    public int getBestMove(Board board) {
        return minimax(board, difficulty, true, 
                      Integer.MIN_VALUE, Integer.MAX_VALUE).column;
    }
    
    private MoveResult minimax(Board board, int depth, 
                              boolean maximizing, int alpha, int beta) {
        // Minimax with alpha-beta pruning
        // Evaluate board state
        // Return best move
    }
}
```

### 8.3 Game History & Replay

```java
public class GameHistory {
    private List<Move> moves;
    private GameStatus finalStatus;
    
    public void replay() {
        Board board = new Board(6, 7);
        for (Move move : moves) {
            board.placeDisc(move.getColumn(), move.getColor());
            // Display board state
        }
    }
}
```

## 9. Testing Strategy

### 9.1 Unit Tests

```java
@Test
public void testValidMove() {
    Board board = new Board(6, 7);
    assertTrue(board.isValidMove(0));
    assertFalse(board.isValidMove(7)); // Out of bounds
}

@Test
public void testWinDetection() {
    Board board = new Board(6, 7);
    // Place 4 discs horizontally
    board.placeDisc(0, DiscColor.RED);
    board.placeDisc(1, DiscColor.RED);
    board.placeDisc(2, DiscColor.RED);
    board.placeDisc(3, DiscColor.RED);
    
    assertTrue(board.checkWin(5, 3, DiscColor.RED));
}

@Test
public void testDrawCondition() {
    Board board = new Board(6, 7);
    // Fill board without winner
    // ...
    assertTrue(board.isFull());
}
```

### 9.2 Integration Tests

```java
@Test
public void testCompleteGame() {
    Game game = new Game("Alice", "Bob");
    
    // Simulate game moves
    game.makeMove(0); // Player 1
    game.makeMove(1); // Player 2
    // ... more moves
    
    assertNotEquals(GameStatus.IN_PROGRESS, game.getStatus());
}
```

## 10. Performance Considerations

### 10.1 Time Complexity

```
┌─────────────────────────────────────────────────────────┐
│         Operation Complexity                          │
└─────────────────────────────────────────────────────────┘

Operation              Time Complexity
─────────────────────────────────────
Place Disc            O(rows) = O(6) = O(1)
Check Win             O(1) - constant directions
Validate Move         O(1)
Check Draw            O(cols) = O(7) = O(1)
Get Board State       O(rows × cols) = O(42) = O(1)
```

### 10.2 Space Complexity

```
┌─────────────────────────────────────────────────────────┐
│         Space Complexity                               │
└─────────────────────────────────────────────────────────┘

Component              Space
─────────────────────────────
Board                  O(rows × cols) = O(42)
Game State            O(1)
Move History          O(moves) - if implemented
```

## 11. Extensibility

### 11.1 Configurable Board Size

```java
public class Board {
    private int rows;
    private int cols;
    private int winCondition; // 4 by default
    
    public Board(int rows, int cols, int winCondition) {
        this.rows = rows;
        this.cols = cols;
        this.winCondition = winCondition;
    }
}
```

### 11.2 Multiple Players

```java
public class Game {
    private List<Player> players;
    private int currentPlayerIndex;
    
    private void switchPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
    }
}
```

## 12. Summary

**Key Design Decisions:**
1. **2D Array for Board**: Simple and intuitive representation
2. **OOP Design**: Clear separation of concerns (Game, Board, Player, Cell)
3. **Efficient Win Detection**: Only check from last placed disc
4. **Strategy Pattern**: Extensible win detection strategies
5. **State Pattern**: Manage game state transitions
6. **Observer Pattern**: Decouple game logic from UI

**Design Principles:**
- **Single Responsibility**: Each class has one responsibility
- **Open/Closed**: Easy to extend (new win strategies, board sizes)
- **Dependency Inversion**: Depend on abstractions (WinStrategy interface)

**Trade-offs:**
- **Simplicity vs Flexibility**: Simple 2D array vs more complex data structures
- **Performance vs Readability**: Optimized win detection vs clear code
- **Features vs Complexity**: Basic game vs advanced features (AI, undo)

This design provides a clean, maintainable, and extensible solution for Connect Four that can be easily tested and enhanced with additional features.

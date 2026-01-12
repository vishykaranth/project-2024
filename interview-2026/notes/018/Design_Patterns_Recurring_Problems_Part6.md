# Design Patterns: Solving Recurring Problems - Part 6

## Behavioral Patterns (Part 2)

This document covers Iterator, Mediator, and Memento patterns.

---

## 1. Iterator Pattern

### Recurring Problem:
**"How do I provide a way to access elements of an aggregate object sequentially without exposing its underlying representation?"**

### Common Scenarios:
- Traversing collections (lists, trees, graphs)
- Hiding collection implementation details
- Providing uniform access to different collections
- Supporting multiple traversal algorithms
- Lazy evaluation of collections

### Problem Without Pattern:
```java
// Problem: Exposed internal structure, different access for each collection
public class ArrayList {
    private Object[] elements;
    
    public Object get(int index) {
        return elements[index];
    }
    
    public int size() {
        return elements.length;
    }
}

public class HashSet {
    private Node[] buckets;
    
    // Problem: Different access pattern - no index!
    public boolean contains(Object obj) {
        // Hash-based access
    }
}

// Problem: Client needs to know internal structure
// Different code for ArrayList vs HashSet
for (int i = 0; i < list.size(); i++) {
    Object obj = list.get(i); // Works for ArrayList
}

// But doesn't work for HashSet - no index!
```

### Solution with Iterator:
```java
// Solution: Uniform interface for traversing collections
public interface Iterator<T> {
    boolean hasNext();
    T next();
    void remove(); // Optional
}

public class ArrayList<T> {
    private T[] elements;
    
    public Iterator<T> iterator() {
        return new ArrayListIterator();
    }
    
    private class ArrayListIterator implements Iterator<T> {
        private int index = 0;
        
        @Override
        public boolean hasNext() {
            return index < elements.length;
        }
        
        @Override
        public T next() {
            return elements[index++];
        }
        
        @Override
        public void remove() {
            // Remove logic
        }
    }
}

public class HashSet<T> {
    private Node<T>[] buckets;
    
    public Iterator<T> iterator() {
        return new HashSetIterator();
    }
    
    private class HashSetIterator implements Iterator<T> {
        private int bucketIndex = 0;
        private Node<T> currentNode = null;
        
        @Override
        public boolean hasNext() {
            // Complex logic to find next element
            return findNext() != null;
        }
        
        @Override
        public T next() {
            currentNode = findNext();
            return currentNode.value;
        }
        
        private Node<T> findNext() {
            // Traverse buckets and nodes
            return null; // Simplified
        }
    }
}

// Usage: Same code for all collections!
Iterator<String> it1 = arrayList.iterator();
while (it1.hasNext()) {
    String item = it1.next();
}

Iterator<String> it2 = hashSet.iterator();
while (it2.hasNext()) {
    String item = it2.next(); // Same code!
}
```

### Problems Solved:
- ✅ **Uniformity**: Same interface for all collections
- ✅ **Encapsulation**: Hides collection implementation
- ✅ **Flexibility**: Multiple traversal algorithms
- ✅ **Separation**: Separates traversal from collection

### Real-World Example:
```java
// Java Collections Framework uses Iterator
List<String> list = Arrays.asList("A", "B", "C");
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    System.out.println(it.next());
}

// Works with any Collection
Set<String> set = new HashSet<>();
Iterator<String> it2 = set.iterator();
while (it2.hasNext()) {
    System.out.println(it2.next());
}
```

---

## 2. Mediator Pattern

### Recurring Problem:
**"How do I define how a set of objects interact, promoting loose coupling by keeping objects from referring to each other explicitly?"**

### Common Scenarios:
- GUI components (buttons, text fields, dialogs)
- Chat applications (users communicating through mediator)
- Air traffic control (airplanes communicating through tower)
- Event buses
- Message queues

### Problem Without Pattern:
```java
// Problem: Tight coupling - components know about each other
public class Button {
    private TextField textField;
    private Checkbox checkbox;
    private Dialog dialog;
    
    public void click() {
        if (checkbox.isChecked()) {
            textField.setEnabled(true);
            dialog.show();
        } else {
            textField.setEnabled(false);
            dialog.hide();
        }
        // Problem: Button knows about all other components!
        // Adding new component = modify Button class
    }
}

public class TextField {
    private Button button;
    private Checkbox checkbox;
    // Problem: TextField also knows about others!
}

// Problem: N×N dependencies (every component knows about every other)
```

### Solution with Mediator:
```java
// Solution: Components communicate through mediator
public interface Mediator {
    void notify(Component sender, String event);
}

public class DialogMediator implements Mediator {
    private Button button;
    private TextField textField;
    private Checkbox checkbox;
    private Dialog dialog;
    
    public DialogMediator(Button button, TextField textField, 
                          Checkbox checkbox, Dialog dialog) {
        this.button = button;
        this.textField = textField;
        this.checkbox = checkbox;
        this.dialog = dialog;
        
        // Set mediator for components
        button.setMediator(this);
        textField.setMediator(this);
        checkbox.setMediator(this);
    }
    
    @Override
    public void notify(Component sender, String event) {
        if (sender == button && event.equals("click")) {
            if (checkbox.isChecked()) {
                textField.setEnabled(true);
                dialog.show();
            } else {
                textField.setEnabled(false);
                dialog.hide();
            }
        } else if (sender == checkbox && event.equals("check")) {
            // Handle checkbox change
        }
        // All coordination logic in one place!
    }
}

public abstract class Component {
    protected Mediator mediator;
    
    public void setMediator(Mediator mediator) {
        this.mediator = mediator;
    }
    
    public void changed() {
        mediator.notify(this, "change");
    }
}

public class Button extends Component {
    public void click() {
        mediator.notify(this, "click");
        // Button doesn't know about other components!
    }
}

public class TextField extends Component {
    public void setEnabled(boolean enabled) {
        // Implementation
    }
}

// Usage: Components are decoupled
Button button = new Button();
TextField textField = new TextField();
Checkbox checkbox = new Checkbox();
Dialog dialog = new Dialog();

Mediator mediator = new DialogMediator(button, textField, checkbox, dialog);
// All coordination through mediator
```

### Problems Solved:
- ✅ **Decoupling**: Components don't know about each other
- ✅ **Centralization**: All interaction logic in mediator
- ✅ **Maintainability**: Easy to modify interactions
- ✅ **Reusability**: Components can be reused independently

### Real-World Example:
```java
// Chat Application Mediator
public interface ChatMediator {
    void sendMessage(String message, User user);
    void addUser(User user);
}

public class ChatRoom implements ChatMediator {
    private List<User> users = new ArrayList<>();
    
    @Override
    public void sendMessage(String message, User user) {
        for (User u : users) {
            if (u != user) {
                u.receive(message);
            }
        }
    }
    
    @Override
    public void addUser(User user) {
        users.add(user);
    }
}

public class User {
    private String name;
    private ChatMediator mediator;
    
    public User(String name, ChatMediator mediator) {
        this.name = name;
        this.mediator = mediator;
    }
    
    public void send(String message) {
        mediator.sendMessage(message, this);
    }
    
    public void receive(String message) {
        System.out.println(name + " received: " + message);
    }
}

// Usage: Users communicate through mediator
ChatMediator chatRoom = new ChatRoom();
User alice = new User("Alice", chatRoom);
User bob = new User("Bob", chatRoom);
chatRoom.addUser(alice);
chatRoom.addUser(bob);

alice.send("Hello!"); // Bob receives through mediator
```

---

## 3. Memento Pattern

### Recurring Problem:
**"How do I capture and externalize an object's internal state so it can be restored later, without violating encapsulation?"**

### Common Scenarios:
- Undo/redo functionality
- Save/load game state
- Database transactions (rollback)
- Configuration snapshots
- Version control systems

### Problem Without Pattern:
```java
// Problem: Exposing internal state breaks encapsulation
public class TextEditor {
    private String text;
    private int cursorPosition;
    private String font;
    
    // Problem: Exposing all fields breaks encapsulation
    public String getText() { return text; }
    public int getCursorPosition() { return cursorPosition; }
    public String getFont() { return font; }
    
    public void setText(String text) { this.text = text; }
    public void setCursorPosition(int pos) { this.cursorPosition = pos; }
    public void setFont(String font) { this.font = font; }
}

// Problem: External code can modify state directly
// No way to restore previous state safely
```

### Solution with Memento:
```java
// Solution: Encapsulate state in memento, restore without exposing internals
public class Memento {
    private final String text;
    private final int cursorPosition;
    private final String font;
    
    public Memento(String text, int cursorPosition, String font) {
        this.text = text;
        this.cursorPosition = cursorPosition;
        this.font = font;
    }
    
    // Package-private getters - only originator can access
    String getText() { return text; }
    int getCursorPosition() { return cursorPosition; }
    String getFont() { return font; }
}

public class TextEditor {
    private String text;
    private int cursorPosition;
    private String font;
    
    public Memento save() {
        return new Memento(text, cursorPosition, font);
    }
    
    public void restore(Memento memento) {
        this.text = memento.getText();
        this.cursorPosition = memento.getCursorPosition();
        this.font = memento.getFont();
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    // Other methods...
}

public class Caretaker {
    private Stack<Memento> history = new Stack<>();
    private TextEditor editor;
    
    public Caretaker(TextEditor editor) {
        this.editor = editor;
    }
    
    public void save() {
        history.push(editor.save());
    }
    
    public void undo() {
        if (!history.isEmpty()) {
            Memento memento = history.pop();
            editor.restore(memento);
        }
    }
}

// Usage: Save and restore state without exposing internals
TextEditor editor = new TextEditor();
Caretaker caretaker = new Caretaker(editor);

editor.setText("Hello");
caretaker.save();

editor.setText("World");
caretaker.undo(); // Restores "Hello"
```

### Problems Solved:
- ✅ **Encapsulation**: State is encapsulated in memento
- ✅ **Undo/Redo**: Easy to implement
- ✅ **Snapshots**: Can save state at any point
- ✅ **Isolation**: Caretaker doesn't know internal structure

### Real-World Example:
```java
// Game State Memento
public class GameMemento {
    private final int level;
    private final int score;
    private final String playerName;
    
    GameMemento(int level, int score, String playerName) {
        this.level = level;
        this.score = score;
        this.playerName = playerName;
    }
    
    int getLevel() { return level; }
    int getScore() { return score; }
    String getPlayerName() { return playerName; }
}

public class Game {
    private int level;
    private int score;
    private String playerName;
    
    public GameMemento save() {
        return new GameMemento(level, score, playerName);
    }
    
    public void load(GameMemento memento) {
        this.level = memento.getLevel();
        this.score = memento.getScore();
        this.playerName = memento.getPlayerName();
    }
}
```

---

## Summary: Part 6

### Patterns Covered:
1. **Iterator**: Provides uniform way to traverse collections
2. **Mediator**: Centralizes communication between components
3. **Memento**: Captures and restores object state

### Key Benefits:
- ✅ **Uniformity**: Iterator provides consistent traversal
- ✅ **Decoupling**: Mediator reduces component dependencies
- ✅ **State Management**: Memento enables undo/redo functionality
- ✅ **Encapsulation**: All patterns maintain proper encapsulation

### When to Use:
- **Iterator**: When you need to traverse collections uniformly
- **Mediator**: When you have many components that need to communicate
- **Memento**: When you need to save and restore object state

---

**Next**: Part 7 will cover Observer, State, and Strategy patterns.


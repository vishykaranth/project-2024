# Design Patterns: Solving Recurring Problems - Part 4

## Structural Patterns (Part 2)

This document covers the remaining Structural Design Patterns.

---

## 1. Decorator Pattern

### Recurring Problem:
**"How do I add behavior to objects dynamically without affecting other objects of the same class?"**

### Common Scenarios:
- Adding features to UI components (scrollbars, borders)
- Adding functionality to streams (buffering, compression, encryption)
- Adding responsibilities to services (logging, caching, validation)
- Adding features to coffee orders (milk, sugar, whipped cream)
- Adding capabilities to HTTP requests (authentication, compression)

### Problem Without Pattern:
```java
// Problem: Class explosion for combinations
public class SimpleCoffee { }
public class CoffeeWithMilk { }
public class CoffeeWithSugar { }
public class CoffeeWithMilkAndSugar { }
public class CoffeeWithWhippedCream { }
public class CoffeeWithMilkAndWhippedCream { }
// ... many more combinations!

// Problem: Adding new feature = exponential class growth
// Can't combine features dynamically
```

### Solution with Decorator:
```java
// Solution: Wrap objects to add behavior dynamically
public interface Coffee {
    String getDescription();
    double getCost();
}

public class SimpleCoffee implements Coffee {
    @Override
    public String getDescription() {
        return "Simple Coffee";
    }
    
    @Override
    public double getCost() {
        return 2.0;
    }
}

public abstract class CoffeeDecorator implements Coffee {
    protected Coffee coffee;
    
    public CoffeeDecorator(Coffee coffee) {
        this.coffee = coffee;
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription();
    }
    
    @Override
    public double getCost() {
        return coffee.getCost();
    }
}

public class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Milk";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.5;
    }
}

public class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Sugar";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.2;
    }
}

public class WhippedCreamDecorator extends CoffeeDecorator {
    public WhippedCreamDecorator(Coffee coffee) {
        super(coffee);
    }
    
    @Override
    public String getDescription() {
        return coffee.getDescription() + ", Whipped Cream";
    }
    
    @Override
    public double getCost() {
        return coffee.getCost() + 0.7;
    }
}

// Usage: Combine features dynamically
Coffee coffee = new SimpleCoffee();
coffee = new MilkDecorator(coffee);
coffee = new SugarDecorator(coffee);
coffee = new WhippedCreamDecorator(coffee);

System.out.println(coffee.getDescription()); 
// "Simple Coffee, Milk, Sugar, Whipped Cream"
System.out.println(coffee.getCost()); // 3.4
```

### Problems Solved:
- ✅ **Flexibility**: Add features dynamically at runtime
- ✅ **Extensibility**: Easy to add new decorators
- ✅ **Single Responsibility**: Each decorator adds one feature
- ✅ **Open/Closed**: Extend functionality without modifying existing code

### Real-World Example:
```java
// Stream Decorators (Java I/O uses this pattern)
InputStream fileStream = new FileInputStream("file.txt");
InputStream bufferedStream = new BufferedInputStream(fileStream);
InputStream gzipStream = new GZIPInputStream(bufferedStream);
// Each decorator adds functionality: file → buffered → compressed
```

---

## 2. Facade Pattern

### Recurring Problem:
**"How do I provide a simple interface to a complex subsystem?"**

### Common Scenarios:
- Simplifying complex API interactions
- Hiding complexity of framework usage
- Providing simple interface to legacy systems
- Simplifying database operations
- Creating unified interface for multiple services

### Problem Without Pattern:
```java
// Problem: Complex subsystem with many classes
public class CPU {
    public void freeze() { }
    public void jump(long position) { }
    public void execute() { }
}

public class Memory {
    public void load(long position, byte[] data) { }
}

public class HardDrive {
    public byte[] read(long lba, int size) { }
}

// Problem: Client needs to know all these classes and their interactions
public class Computer {
    private CPU cpu;
    private Memory memory;
    private HardDrive hardDrive;
    
    public void startComputer() {
        // Complex sequence
        cpu.freeze();
        memory.load(0, hardDrive.read(0, 1024));
        cpu.jump(0);
        cpu.execute();
        // Client needs to know this complex sequence!
    }
}
```

### Solution with Facade:
```java
// Solution: Simple interface hiding complexity
public class ComputerFacade {
    private CPU cpu;
    private Memory memory;
    private HardDrive hardDrive;
    
    public ComputerFacade() {
        this.cpu = new CPU();
        this.memory = new Memory();
        this.hardDrive = new HardDrive();
    }
    
    public void start() {
        // Hide complex sequence behind simple method
        cpu.freeze();
        memory.load(0, hardDrive.read(0, 1024));
        cpu.jump(0);
        cpu.execute();
    }
    
    public void shutdown() {
        // Another complex sequence hidden
        cpu.freeze();
        // ... shutdown logic
    }
}

// Usage: Simple interface
ComputerFacade computer = new ComputerFacade();
computer.start(); // Simple! Client doesn't need to know internals
computer.shutdown();
```

### Problems Solved:
- ✅ **Simplicity**: Provides easy-to-use interface
- ✅ **Decoupling**: Client doesn't depend on subsystem classes
- ✅ **Maintainability**: Changes to subsystem don't affect client
- ✅ **Abstraction**: Hides implementation details

### Real-World Example:
```java
// Database Facade
public class DatabaseFacade {
    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    
    public List<User> getUsers() {
        // Hide complex database operations
        connection = DriverManager.getConnection(url);
        statement = connection.createStatement();
        resultSet = statement.executeQuery("SELECT * FROM users");
        
        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            users.add(new User(resultSet.getString("name")));
        }
        
        resultSet.close();
        statement.close();
        connection.close();
        
        return users;
    }
}

// Usage: Simple database access
DatabaseFacade db = new DatabaseFacade();
List<User> users = db.getUsers(); // Simple!
```

---

## 3. Flyweight Pattern

### Recurring Problem:
**"How do I support large numbers of fine-grained objects efficiently by sharing common state?"**

### Common Scenarios:
- Text editors (character objects)
- Game development (tree, grass, rock objects)
- Rendering systems (glyphs, icons)
- Document formatting (formatting objects)
- Network systems (connection objects)

### Problem Without Pattern:
```java
// Problem: Too many similar objects consuming memory
public class Tree {
    private int x, y; // Extrinsic state (varies)
    private String type; // Intrinsic state (shared)
    private String color; // Intrinsic state (shared)
    private byte[] texture; // Intrinsic state (shared) - large!
    
    public Tree(int x, int y, String type, String color, byte[] texture) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.color = color;
        this.texture = texture; // Large memory footprint
    }
}

// Problem: Creating 1000 trees = 1000 texture copies!
List<Tree> forest = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    forest.add(new Tree(i, i, "Oak", "Green", largeTexture)); 
    // Each tree has its own copy of texture - wasteful!
}
```

### Solution with Flyweight:
```java
// Solution: Share intrinsic state, store extrinsic state separately
public class TreeType {
    private String name; // Intrinsic (shared)
    private String color; // Intrinsic (shared)
    private byte[] texture; // Intrinsic (shared)
    
    public TreeType(String name, String color, byte[] texture) {
        this.name = name;
        this.color = color;
        this.texture = texture; // Shared across all trees of this type
    }
    
    public void render(int x, int y) {
        // Render using shared texture
        System.out.println("Rendering " + name + " at (" + x + "," + y + ")");
    }
}

public class TreeTypeFactory {
    private static Map<String, TreeType> treeTypes = new HashMap<>();
    
    public static TreeType getTreeType(String name, String color, byte[] texture) {
        String key = name + "_" + color;
        TreeType type = treeTypes.get(key);
        
        if (type == null) {
            type = new TreeType(name, color, texture);
            treeTypes.put(key, type);
        }
        
        return type; // Return shared instance
    }
}

public class Tree {
    private int x, y; // Extrinsic state (unique per tree)
    private TreeType type; // Reference to shared intrinsic state
    
    public Tree(int x, int y, TreeType type) {
        this.x = x;
        this.y = y;
        this.type = type; // Shared reference, not copy!
    }
    
    public void render() {
        type.render(x, y);
    }
}

// Usage: Share intrinsic state
TreeType oakType = TreeTypeFactory.getTreeType("Oak", "Green", largeTexture);
// Only one texture copy for all oak trees!

List<Tree> forest = new ArrayList<>();
for (int i = 0; i < 1000; i++) {
    forest.add(new Tree(i, i, oakType)); 
    // All trees share the same texture - memory efficient!
}
```

### Problems Solved:
- ✅ **Memory efficiency**: Shares common state across objects
- ✅ **Performance**: Reduces object creation overhead
- ✅ **Scalability**: Supports large numbers of objects
- ✅ **Separation**: Distinguishes intrinsic vs extrinsic state

### Real-World Example:
```java
// Character Flyweight (Text Editor)
public class Character {
    private char value; // Intrinsic (shared)
    private Font font; // Intrinsic (shared)
    private int position; // Extrinsic (unique)
    
    public Character(char value, Font font, int position) {
        this.value = value;
        this.font = font; // Shared across characters with same font
        this.position = position;
    }
}

// Usage: Share font objects across characters
Font arial = new Font("Arial", 12);
Character c1 = new Character('A', arial, 0);
Character c2 = new Character('B', arial, 1);
// Both share the same font object
```

---

## Summary: Part 4

### Patterns Covered:
1. **Decorator**: Adds behavior to objects dynamically
2. **Facade**: Provides simple interface to complex subsystem
3. **Flyweight**: Shares common state to support many objects efficiently

### Key Benefits:
- ✅ **Flexibility**: Decorator adds features at runtime
- ✅ **Simplicity**: Facade hides complexity
- ✅ **Efficiency**: Flyweight reduces memory usage
- ✅ **Maintainability**: All patterns improve code organization

### When to Use:
- **Decorator**: When you need to add features dynamically
- **Facade**: When you want to simplify complex subsystem
- **Flyweight**: When you have many similar objects with shared state

---

**Next**: Part 5 will cover Proxy pattern and introduce Behavioral Patterns.


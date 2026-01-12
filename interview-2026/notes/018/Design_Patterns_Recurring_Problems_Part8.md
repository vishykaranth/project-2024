# Design Patterns: Solving Recurring Problems - Part 8

## Behavioral Patterns (Part 4)

This document covers Template Method, Visitor, Interpreter, and Null Object patterns.

---

## 1. Template Method Pattern

### Recurring Problem:
**"How do I define the skeleton of an algorithm in a method, deferring some steps to subclasses, allowing subclasses to redefine certain steps without changing the algorithm's structure?"**

### Common Scenarios:
- Framework hooks (Spring, JUnit)
- Data processing pipelines
- Build processes
- Algorithm frameworks
- Code generation

### Problem Without Pattern:
```java
// Problem: Code duplication, algorithm scattered
public class CoffeeMaker {
    public void makeCoffee() {
        boilWater();
        addCoffee();
        pourInCup();
        addSugar(); // Specific to coffee
    }
}

public class TeaMaker {
    public void makeTea() {
        boilWater();
        addTea(); // Different from coffee
        pourInCup();
        addLemon(); // Different from coffee
    }
}

// Problem: boilWater() and pourInCup() duplicated
// Algorithm structure is not enforced
```

### Solution with Template Method:
```java
// Solution: Define algorithm skeleton, let subclasses fill steps
public abstract class BeverageMaker {
    // Template method - defines algorithm structure
    public final void makeBeverage() {
        boilWater();
        addIngredient(); // Abstract - subclasses implement
        pourInCup();
        addCondiments(); // Abstract - subclasses implement
    }
    
    // Common steps
    private void boilWater() {
        System.out.println("Boiling water");
    }
    
    private void pourInCup() {
        System.out.println("Pouring into cup");
    }
    
    // Steps that vary - subclasses must implement
    protected abstract void addIngredient();
    protected abstract void addCondiments();
}

public class CoffeeMaker extends BeverageMaker {
    @Override
    protected void addIngredient() {
        System.out.println("Adding coffee");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("Adding sugar");
    }
}

public class TeaMaker extends BeverageMaker {
    @Override
    protected void addIngredient() {
        System.out.println("Adding tea");
    }
    
    @Override
    protected void addCondiments() {
        System.out.println("Adding lemon");
    }
}

// Usage: Algorithm structure enforced, steps customized
BeverageMaker coffee = new CoffeeMaker();
coffee.makeBeverage(); // Follows template

BeverageMaker tea = new TeaMaker();
tea.makeBeverage(); // Same structure, different steps
```

### Problems Solved:
- ✅ **Code Reuse**: Common algorithm steps in base class
- ✅ **Structure**: Algorithm structure is enforced
- ✅ **Flexibility**: Subclasses customize specific steps
- ✅ **Consistency**: All subclasses follow same algorithm

### Real-World Example:
```java
// JUnit Test Template
public abstract class TestCase {
    public final void runTest() {
        setUp(); // Template method hook
        testMethod(); // Abstract - subclasses implement
        tearDown(); // Template method hook
    }
    
    protected void setUp() { }
    protected abstract void testMethod();
    protected void tearDown() { }
}

public class UserTest extends TestCase {
    @Override
    protected void setUp() {
        // Initialize test data
    }
    
    @Override
    protected void testMethod() {
        // Test logic
    }
    
    @Override
    protected void tearDown() {
        // Cleanup
    }
}
```

---

## 2. Visitor Pattern

### Recurring Problem:
**"How do I define a new operation on a group of objects without changing the classes of the objects on which it operates?"**

### Common Scenarios:
- Compiler AST traversal
- Document structure operations
- File system operations
- XML/JSON tree processing
- Adding operations to existing class hierarchy

### Problem Without Pattern:
```java
// Problem: Adding new operation requires modifying all classes
public abstract class Element {
    // Problem: Adding new operation = modify this and all subclasses
}

public class Paragraph extends Element {
    public void highlight() {
        // Highlight logic
    }
    
    public void spellCheck() {
        // Spell check logic
    }
    // Problem: Each operation method in each class
}

public class Heading extends Element {
    public void highlight() {
        // Highlight logic
    }
    
    public void spellCheck() {
        // Spell check logic
    }
    // Problem: Code duplication, hard to add new operations
}

// Problem: Adding new operation (e.g., export) = modify all classes!
```

### Solution with Visitor:
```java
// Solution: Separate operations from object structure
public interface Element {
    void accept(Visitor visitor);
}

public interface Visitor {
    void visitParagraph(Paragraph paragraph);
    void visitHeading(Heading heading);
}

public class Paragraph implements Element {
    private String text;
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitParagraph(this);
    }
    
    public String getText() {
        return text;
    }
}

public class Heading implements Element {
    private String text;
    
    @Override
    public void accept(Visitor visitor) {
        visitor.visitHeading(this);
    }
    
    public String getText() {
        return text;
    }
}

public class HighlightVisitor implements Visitor {
    @Override
    public void visitParagraph(Paragraph paragraph) {
        System.out.println("Highlighting paragraph: " + paragraph.getText());
    }
    
    @Override
    public void visitHeading(Heading heading) {
        System.out.println("Highlighting heading: " + heading.getText());
    }
}

public class SpellCheckVisitor implements Visitor {
    @Override
    public void visitParagraph(Paragraph paragraph) {
        System.out.println("Spell checking paragraph: " + paragraph.getText());
    }
    
    @Override
    public void visitHeading(Heading heading) {
        System.out.println("Spell checking heading: " + heading.getText());
    }
}

// New operation - no need to modify Element classes!
public class ExportVisitor implements Visitor {
    @Override
    public void visitParagraph(Paragraph paragraph) {
        System.out.println("Exporting paragraph");
    }
    
    @Override
    public void visitHeading(Heading heading) {
        System.out.println("Exporting heading");
    }
}

// Usage: Add new operations without modifying element classes
Element paragraph = new Paragraph();
Element heading = new Heading();

Visitor highlight = new HighlightVisitor();
paragraph.accept(highlight);
heading.accept(highlight);

Visitor export = new ExportVisitor(); // New operation!
paragraph.accept(export);
heading.accept(export);
```

### Problems Solved:
- ✅ **Extensibility**: Add new operations without modifying classes
- ✅ **Separation**: Operations separated from object structure
- ✅ **Single Responsibility**: Each visitor does one operation
- ✅ **Open/Closed**: Open for new operations, closed for modifications

### Real-World Example:
```java
// Compiler AST Visitor
public interface ASTNode {
    void accept(ASTVisitor visitor);
}

public interface ASTVisitor {
    void visitVariable(VariableNode node);
    void visitFunction(FunctionNode node);
    void visitExpression(ExpressionNode node);
}

public class CodeGeneratorVisitor implements ASTVisitor {
    @Override
    public void visitVariable(VariableNode node) {
        // Generate code for variable
    }
    
    @Override
    public void visitFunction(FunctionNode node) {
        // Generate code for function
    }
    
    @Override
    public void visitExpression(ExpressionNode node) {
        // Generate code for expression
    }
}
```

---

## 3. Interpreter Pattern

### Recurring Problem:
**"How do I define a representation for a language's grammar and an interpreter that uses this representation to interpret sentences in the language?"**

### Common Scenarios:
- Regular expressions
- SQL parsers
- Mathematical expression evaluators
- Domain-specific languages (DSL)
- Query languages
- Configuration file parsers

### Problem Without Pattern:
```java
// Problem: Hard-coded parsing logic, not extensible
public class ExpressionEvaluator {
    public int evaluate(String expression) {
        // Problem: Complex parsing logic
        // "1 + 2 * 3" - how to parse?
        // Not extensible for new operators
        return 0;
    }
}
```

### Solution with Interpreter:
```java
// Solution: Represent grammar as class hierarchy
public interface Expression {
    int interpret();
}

public class Number implements Expression {
    private int value;
    
    public Number(int value) {
        this.value = value;
    }
    
    @Override
    public int interpret() {
        return value;
    }
}

public class Add implements Expression {
    private Expression left;
    private Expression right;
    
    public Add(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public int interpret() {
        return left.interpret() + right.interpret();
    }
}

public class Multiply implements Expression {
    private Expression left;
    private Expression right;
    
    public Multiply(Expression left, Expression right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public int interpret() {
        return left.interpret() * right.interpret();
    }
}

// Usage: Build expression tree
// Expression: 1 + 2 * 3
Expression expression = new Add(
    new Number(1),
    new Multiply(new Number(2), new Number(3))
);

int result = expression.interpret(); // 7
```

### Problems Solved:
- ✅ **Grammar Representation**: Grammar represented as class hierarchy
- ✅ **Extensibility**: Easy to add new grammar rules
- ✅ **Flexibility**: Can build complex expressions
- ✅ **Separation**: Parsing separated from interpretation

### Real-World Example:
```java
// SQL WHERE Clause Interpreter
public interface SQLExpression {
    boolean evaluate(Map<String, Object> row);
}

public class EqualsExpression implements SQLExpression {
    private String column;
    private Object value;
    
    public EqualsExpression(String column, Object value) {
        this.column = column;
        this.value = value;
    }
    
    @Override
    public boolean evaluate(Map<String, Object> row) {
        return row.get(column).equals(value);
    }
}

public class AndExpression implements SQLExpression {
    private SQLExpression left;
    private SQLExpression right;
    
    public AndExpression(SQLExpression left, SQLExpression right) {
        this.left = left;
        this.right = right;
    }
    
    @Override
    public boolean evaluate(Map<String, Object> row) {
        return left.evaluate(row) && right.evaluate(row);
    }
}
```

---

## 4. Null Object Pattern

### Recurring Problem:
**"How do I avoid null references by providing a default object that does nothing instead of returning null?"**

### Common Scenarios:
- Optional dependencies
- Default implementations
- Stub objects for testing
- Missing data handling
- Optional features

### Problem Without Pattern:
```java
// Problem: Null checks everywhere
public class UserService {
    public Logger getLogger() {
        // Problem: Returns null if logger not configured
        return logger; // Could be null
    }
}

// Problem: Null checks everywhere
UserService service = new UserService();
Logger logger = service.getLogger();
if (logger != null) { // Null check required!
    logger.log("Message");
}
// Problem: Easy to forget null check = NullPointerException
```

### Solution with Null Object:
```java
// Solution: Provide default "do nothing" object
public interface Logger {
    void log(String message);
}

public class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println(message);
    }
}

public class NullLogger implements Logger {
    @Override
    public void log(String message) {
        // Do nothing - null object
    }
}

public class UserService {
    private Logger logger;
    
    public UserService(Logger logger) {
        // Never null - use NullLogger if not provided
        this.logger = logger != null ? logger : new NullLogger();
    }
    
    public void doSomething() {
        logger.log("Doing something"); // No null check needed!
    }
}

// Usage: No null checks required
UserService service1 = new UserService(new ConsoleLogger());
service1.doSomething(); // Logs

UserService service2 = new UserService(null); // Uses NullLogger
service2.doSomething(); // Does nothing, no exception!
```

### Problems Solved:
- ✅ **No Null Checks**: Eliminates null pointer exceptions
- ✅ **Default Behavior**: Provides safe default
- ✅ **Simplicity**: Code is cleaner without null checks
- ✅ **Polymorphism**: Null object follows same interface

### Real-World Example:
```java
// Optional Cache
public interface Cache {
    void put(String key, Object value);
    Object get(String key);
}

public class RealCache implements Cache {
    private Map<String, Object> map = new HashMap<>();
    
    @Override
    public void put(String key, Object value) {
        map.put(key, value);
    }
    
    @Override
    public Object get(String key) {
        return map.get(key);
    }
}

public class NullCache implements Cache {
    @Override
    public void put(String key, Object value) {
        // Do nothing
    }
    
    @Override
    public Object get(String key) {
        return null; // Or throw exception
    }
}

// Usage: Cache is optional
Cache cache = isCacheEnabled() ? new RealCache() : new NullCache();
cache.put("key", "value"); // Works whether cache is enabled or not
```

---

## Summary: Part 8

### Patterns Covered:
1. **Template Method**: Defines algorithm skeleton with customizable steps
2. **Visitor**: Adds operations to object structure without modifying classes
3. **Interpreter**: Represents grammar and interprets expressions
4. **Null Object**: Provides default object instead of null

### Key Benefits:
- ✅ **Structure**: Template Method enforces algorithm structure
- ✅ **Extensibility**: Visitor adds operations without modifying classes
- ✅ **Grammar**: Interpreter represents language grammar
- ✅ **Safety**: Null Object eliminates null pointer exceptions

### When to Use:
- **Template Method**: When you have algorithm with common and varying steps
- **Visitor**: When you need to add operations to existing class hierarchy
- **Interpreter**: When you need to interpret a language or grammar
- **Null Object**: When you want to avoid null references

---

**Next**: Part 9 will cover Concurrency Patterns, and Part 10 will cover Architectural Patterns.


# Design Patterns: Solving Recurring Problems - Part 7

## Behavioral Patterns (Part 3)

This document covers Observer, State, and Strategy patterns.

---

## 1. Observer Pattern

### Recurring Problem:
**"How do I define a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically?"**

### Common Scenarios:
- Model-View architecture (MVC)
- Event handling systems
- Publish-subscribe systems
- Stock market updates
- Weather station notifications
- GUI event listeners

### Problem Without Pattern:
```java
// Problem: Tight coupling, subject needs to know all observers
public class Stock {
    private double price;
    private List<Investor> investors = new ArrayList<>();
    
    public void setPrice(double price) {
        this.price = price;
        // Problem: Subject directly calls observers
        for (Investor investor : investors) {
            investor.update(price); // Tight coupling!
        }
    }
    
    public void addInvestor(Investor investor) {
        investors.add(investor);
    }
}

// Problem: Adding new observer type requires modifying Subject
// Can't easily add/remove observers at runtime
```

### Solution with Observer:
```java
// Solution: Loose coupling through observer interface
public interface Observer {
    void update(double price);
}

public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void notifyObservers();
}

public class Stock implements Subject {
    private double price;
    private List<Observer> observers = new ArrayList<>();
    
    public void setPrice(double price) {
        this.price = price;
        notifyObservers(); // Notify all observers
    }
    
    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }
    
    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    
    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(price);
        }
    }
}

public class Investor implements Observer {
    private String name;
    
    public Investor(String name) {
        this.name = name;
    }
    
    @Override
    public void update(double price) {
        System.out.println(name + " notified: Price = " + price);
    }
}

public class Trader implements Observer {
    @Override
    public void update(double price) {
        if (price > 100) {
            System.out.println("Trader: Selling at " + price);
        }
    }
}

// Usage: Easy to add/remove observers
Stock stock = new Stock();
Observer investor1 = new Investor("Alice");
Observer investor2 = new Investor("Bob");
Observer trader = new Trader();

stock.attach(investor1);
stock.attach(investor2);
stock.attach(trader);

stock.setPrice(105.0); // All observers notified automatically
stock.detach(investor1); // Easy to remove
```

### Problems Solved:
- ✅ **Loose Coupling**: Subject doesn't know concrete observer types
- ✅ **Dynamic**: Add/remove observers at runtime
- ✅ **Broadcast**: One change notifies all observers
- ✅ **Open/Closed**: Easy to add new observer types

### Real-World Example:
```java
// Java's Observer (deprecated but illustrates pattern)
import java.util.Observable;
import java.util.Observer;

public class WeatherStation extends Observable {
    private float temperature;
    
    public void setTemperature(float temperature) {
        this.temperature = temperature;
        setChanged();
        notifyObservers(temperature);
    }
}

public class Display implements Observer {
    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof WeatherStation) {
            float temp = (Float) arg;
            System.out.println("Temperature: " + temp);
        }
    }
}
```

---

## 2. State Pattern

### Recurring Problem:
**"How do I allow an object to alter its behavior when its internal state changes, making it appear as if the object changed its class?"**

### Common Scenarios:
- State machines (vending machines, ATMs)
- Game character states (idle, running, jumping)
- Document states (draft, review, published)
- Order states (pending, shipped, delivered)
- Connection states (connected, disconnected, connecting)

### Problem Without Pattern:
```java
// Problem: Many if-else statements, hard to maintain
public class VendingMachine {
    private String state; // "idle", "hasMoney", "sold", "soldOut"
    
    public void insertCoin() {
        if (state.equals("idle")) {
            state = "hasMoney";
        } else if (state.equals("hasMoney")) {
            // Already has money
        } else if (state.equals("sold")) {
            // Can't insert coin
        }
        // Problem: Complex nested if-else for each action
    }
    
    public void selectProduct() {
        if (state.equals("hasMoney")) {
            state = "sold";
            dispenseProduct();
        } else if (state.equals("idle")) {
            // Need money first
        }
        // More if-else...
    }
    
    // Problem: Adding new state = modify all methods
    // Hard to understand state transitions
}
```

### Solution with State:
```java
// Solution: Each state is a separate class
public interface VendingMachineState {
    void insertCoin();
    void selectProduct();
    void dispense();
}

public class IdleState implements VendingMachineState {
    private VendingMachine machine;
    
    public IdleState(VendingMachine machine) {
        this.machine = machine;
    }
    
    @Override
    public void insertCoin() {
        System.out.println("Coin inserted");
        machine.setState(machine.getHasMoneyState());
    }
    
    @Override
    public void selectProduct() {
        System.out.println("Please insert coin first");
    }
    
    @Override
    public void dispense() {
        System.out.println("Please insert coin and select product");
    }
}

public class HasMoneyState implements VendingMachineState {
    private VendingMachine machine;
    
    public HasMoneyState(VendingMachine machine) {
        this.machine = machine;
    }
    
    @Override
    public void insertCoin() {
        System.out.println("Already has money");
    }
    
    @Override
    public void selectProduct() {
        System.out.println("Product selected");
        machine.setState(machine.getSoldState());
    }
    
    @Override
    public void dispense() {
        System.out.println("Please select product first");
    }
}

public class SoldState implements VendingMachineState {
    private VendingMachine machine;
    
    public SoldState(VendingMachine machine) {
        this.machine = machine;
    }
    
    @Override
    public void insertCoin() {
        System.out.println("Please wait, product dispensing");
    }
    
    @Override
    public void selectProduct() {
        System.out.println("Product already selected");
    }
    
    @Override
    public void dispense() {
        System.out.println("Dispensing product");
        machine.setState(machine.getIdleState());
    }
}

public class VendingMachine {
    private VendingMachineState idleState;
    private VendingMachineState hasMoneyState;
    private VendingMachineState soldState;
    private VendingMachineState currentState;
    
    public VendingMachine() {
        idleState = new IdleState(this);
        hasMoneyState = new HasMoneyState(this);
        soldState = new SoldState(this);
        currentState = idleState;
    }
    
    public void setState(VendingMachineState state) {
        this.currentState = state;
    }
    
    public void insertCoin() {
        currentState.insertCoin();
    }
    
    public void selectProduct() {
        currentState.selectProduct();
    }
    
    public void dispense() {
        currentState.dispense();
    }
    
    // Getters for states
    public VendingMachineState getIdleState() { return idleState; }
    public VendingMachineState getHasMoneyState() { return hasMoneyState; }
    public VendingMachineState getSoldState() { return soldState; }
}

// Usage: State transitions handled by state objects
VendingMachine machine = new VendingMachine();
machine.insertCoin(); // Changes to HasMoneyState
machine.selectProduct(); // Changes to SoldState
machine.dispense(); // Changes to IdleState
```

### Problems Solved:
- ✅ **Clarity**: Each state is a separate class
- ✅ **Maintainability**: Easy to add new states
- ✅ **No Conditionals**: Eliminates if-else chains
- ✅ **State Transitions**: Clear and explicit

### Real-World Example:
```java
// TCP Connection States
public interface TCPState {
    void open();
    void close();
    void acknowledge();
}

public class TCPListen implements TCPState {
    @Override
    public void open() {
        // Transition to established
    }
    
    @Override
    public void close() {
        // Transition to closed
    }
    
    @Override
    public void acknowledge() {
        // Not applicable in listen state
    }
}
```

---

## 3. Strategy Pattern

### Recurring Problem:
**"How do I define a family of algorithms, encapsulate each one, and make them interchangeable, allowing the algorithm to vary independently from clients that use it?"**

### Common Scenarios:
- Sorting algorithms (quick sort, merge sort, bubble sort)
- Payment methods (credit card, PayPal, cryptocurrency)
- Compression algorithms (ZIP, RAR, 7Z)
- Navigation strategies (driving, walking, public transport)
- Validation strategies (email, phone, credit card)

### Problem Without Pattern:
```java
// Problem: Hard-coded algorithm, can't switch at runtime
public class PaymentProcessor {
    public void processPayment(double amount, String method) {
        if (method.equals("credit")) {
            // Credit card processing
            System.out.println("Processing credit card payment");
        } else if (method.equals("paypal")) {
            // PayPal processing
            System.out.println("Processing PayPal payment");
        } else if (method.equals("crypto")) {
            // Cryptocurrency processing
            System.out.println("Processing cryptocurrency payment");
        }
        // Problem: Adding new method = modify this class
        // Can't switch algorithms at runtime
    }
}
```

### Solution with Strategy:
```java
// Solution: Encapsulate algorithms in strategy classes
public interface PaymentStrategy {
    void pay(double amount);
}

public class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    private String cvv;
    
    public CreditCardPayment(String cardNumber, String cvv) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paying " + amount + " using credit card");
        // Credit card processing logic
    }
}

public class PayPalPayment implements PaymentStrategy {
    private String email;
    
    public PayPalPayment(String email) {
        this.email = email;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paying " + amount + " using PayPal: " + email);
        // PayPal processing logic
    }
}

public class CryptocurrencyPayment implements PaymentStrategy {
    private String walletAddress;
    
    public CryptocurrencyPayment(String walletAddress) {
        this.walletAddress = walletAddress;
    }
    
    @Override
    public void pay(double amount) {
        System.out.println("Paying " + amount + " using cryptocurrency");
        // Cryptocurrency processing logic
    }
}

public class PaymentProcessor {
    private PaymentStrategy strategy;
    
    public PaymentProcessor(PaymentStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void setStrategy(PaymentStrategy strategy) {
        this.strategy = strategy; // Can change at runtime!
    }
    
    public void processPayment(double amount) {
        strategy.pay(amount);
    }
}

// Usage: Switch strategies at runtime
PaymentStrategy creditCard = new CreditCardPayment("1234", "123");
PaymentProcessor processor = new PaymentProcessor(creditCard);
processor.processPayment(100.0);

// Switch to PayPal
processor.setStrategy(new PayPalPayment("user@example.com"));
processor.processPayment(200.0);
```

### Problems Solved:
- ✅ **Flexibility**: Switch algorithms at runtime
- ✅ **Extensibility**: Easy to add new strategies
- ✅ **Separation**: Algorithm logic separated from client
- ✅ **Open/Closed**: Open for extension, closed for modification

### Real-World Example:
```java
// Sorting Strategy
public interface SortStrategy {
    void sort(int[] array);
}

public class QuickSort implements SortStrategy {
    @Override
    public void sort(int[] array) {
        // Quick sort implementation
    }
}

public class MergeSort implements SortStrategy {
    @Override
    public void sort(int[] array) {
        // Merge sort implementation
    }
}

public class Sorter {
    private SortStrategy strategy;
    
    public Sorter(SortStrategy strategy) {
        this.strategy = strategy;
    }
    
    public void sort(int[] array) {
        strategy.sort(array);
    }
}

// Usage: Choose sorting algorithm
Sorter sorter = new Sorter(new QuickSort());
sorter.sort(array);

// Switch to merge sort
sorter = new Sorter(new MergeSort());
sorter.sort(array);
```

---

## Summary: Part 7

### Patterns Covered:
1. **Observer**: One-to-many dependency for state changes
2. **State**: Object behavior changes with internal state
3. **Strategy**: Encapsulates algorithms and makes them interchangeable

### Key Benefits:
- ✅ **Decoupling**: Observer and Strategy reduce coupling
- ✅ **Flexibility**: All patterns allow runtime changes
- ✅ **Maintainability**: State pattern eliminates conditionals
- ✅ **Extensibility**: Easy to add new observers, states, or strategies

### When to Use:
- **Observer**: When you need to notify multiple objects of state changes
- **State**: When object behavior depends on its state
- **Strategy**: When you have multiple ways to perform a task

---

**Next**: Part 8 will cover Template Method, Visitor, and Interpreter patterns.


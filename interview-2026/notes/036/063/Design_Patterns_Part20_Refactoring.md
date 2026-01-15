# Refactoring: Improving Code Structure, Maintaining Behavior

## Overview

Refactoring is the process of restructuring existing code without changing its external behavior. The goal is to improve code readability, maintainability, and extensibility while preserving functionality. Refactoring should be done continuously as part of the development process.

## Refactoring Principles

```
┌─────────────────────────────────────────────────────────┐
│         Refactoring Principles                          │
└─────────────────────────────────────────────────────────┘

1. Preserve Behavior
   └─ Code should work the same after refactoring
   └─ No functional changes

2. Small Steps
   └─ Make small, incremental changes
   └─ Test after each step

3. Continuous Process
   └─ Refactor as you code
   └─ Don't accumulate technical debt

4. Test Coverage
   └─ Have tests before refactoring
   └─ Tests ensure behavior is preserved
```

## Common Refactoring Techniques

### 1. Extract Method

**Purpose**: Break long method into smaller, named methods

```java
// Before
public void printOwing(Invoice invoice) {
    printBanner();
    
    // Calculate outstanding
    double outstanding = 0.0;
    for (Order order : invoice.getOrders()) {
        outstanding += order.getAmount();
    }
    
    // Print details
    System.out.println("name: " + invoice.getCustomer());
    System.out.println("amount: " + outstanding);
}

// After
public void printOwing(Invoice invoice) {
    printBanner();
    double outstanding = calculateOutstanding(invoice);
    printDetails(invoice, outstanding);
}

private double calculateOutstanding(Invoice invoice) {
    double outstanding = 0.0;
    for (Order order : invoice.getOrders()) {
        outstanding += order.getAmount();
    }
    return outstanding;
}

private void printDetails(Invoice invoice, double outstanding) {
    System.out.println("name: " + invoice.getCustomer());
    System.out.println("amount: " + outstanding);
}
```

### 2. Extract Variable

**Purpose**: Make complex expressions clearer

```java
// Before
if ((platform.toUpperCase().indexOf("MAC") > -1) &&
    (browser.toUpperCase().indexOf("IE") > -1) &&
    wasInitialized() && resize > 0) {
    // Do something
}

// After
boolean isMac = platform.toUpperCase().indexOf("MAC") > -1;
boolean isIE = browser.toUpperCase().indexOf("IE") > -1;
boolean wasResized = resize > 0;

if (isMac && isIE && wasInitialized() && wasResized) {
    // Do something
}
```

### 3. Rename Variable/Method

**Purpose**: Use meaningful names

```java
// Before
public double calc(double a, double b) {
    double c = a * b;
    return c * 0.1;
}

// After
public double calculateDiscount(double price, int quantity) {
    double subtotal = price * quantity;
    return subtotal * DISCOUNT_RATE;
}
```

### 4. Extract Class

**Purpose**: Split large class into smaller, focused classes

```java
// Before: Large class
public class Person {
    private String name;
    private String officeAreaCode;
    private String officeNumber;
    
    public String getTelephoneNumber() {
        return "(" + officeAreaCode + ") " + officeNumber;
    }
    // ... many other methods
}

// After: Extract TelephoneNumber class
public class Person {
    private String name;
    private TelephoneNumber officePhone;
    
    public String getTelephoneNumber() {
        return officePhone.toString();
    }
}

public class TelephoneNumber {
    private String areaCode;
    private String number;
    
    @Override
    public String toString() {
        return "(" + areaCode + ") " + number;
    }
}
```

### 5. Move Method

**Purpose**: Move method to more appropriate class

```java
// Before: Method in wrong class
public class Account {
    private AccountType type;
    
    public double overdraftCharge() {
        if (type.isPremium()) {
            double result = 10;
            if (daysOverdrawn > 7) {
                result += (daysOverdrawn - 7) * 0.85;
            }
            return result;
        } else {
            return daysOverdrawn * 1.75;
        }
    }
}

// After: Move to AccountType
public class Account {
    private AccountType type;
    
    public double overdraftCharge() {
        return type.overdraftCharge(daysOverdrawn);
    }
}

public class AccountType {
    public double overdraftCharge(int daysOverdrawn) {
        if (isPremium()) {
            double result = 10;
            if (daysOverdrawn > 7) {
                result += (daysOverdrawn - 7) * 0.85;
            }
            return result;
        } else {
            return daysOverdrawn * 1.75;
        }
    }
}
```

### 6. Replace Conditional with Polymorphism

**Purpose**: Eliminate switch/if-else chains

```java
// Before: Switch statement
public double getSpeed() {
    switch (type) {
        case EUROPEAN:
            return getBaseSpeed();
        case AFRICAN:
            return getBaseSpeed() - getLoadFactor() * numberOfCoconuts;
        case NORWEGIAN_BLUE:
            return (isNailed) ? 0 : getBaseSpeed(voltage);
        default:
            return 0;
    }
}

// After: Polymorphism
public abstract class Bird {
    public abstract double getSpeed();
}

public class EuropeanBird extends Bird {
    public double getSpeed() {
        return getBaseSpeed();
    }
}

public class AfricanBird extends Bird {
    public double getSpeed() {
        return getBaseSpeed() - getLoadFactor() * numberOfCoconuts;
    }
}

public class NorwegianBlueBird extends Bird {
    public double getSpeed() {
        return (isNailed) ? 0 : getBaseSpeed(voltage);
    }
}
```

### 7. Replace Magic Number with Constant

**Purpose**: Make numbers meaningful

```java
// Before: Magic numbers
if (user.getAge() > 18) { }
if (retryCount < 3) { }
double discount = price * 0.1;

// After: Named constants
private static final int MINIMUM_AGE = 18;
private static final int MAX_RETRY_ATTEMPTS = 3;
private static final double DISCOUNT_RATE = 0.1;

if (user.getAge() > MINIMUM_AGE) { }
if (retryCount < MAX_RETRY_ATTEMPTS) { }
double discount = price * DISCOUNT_RATE;
```

### 8. Replace Parameter with Object

**Purpose**: Reduce parameter count

```java
// Before: Too many parameters
public void createUser(String firstName, String lastName, String email, 
                      String phone, String address) {
    // ...
}

// After: Use object
public void createUser(CreateUserRequest request) {
    // ...
}

public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
}
```

### 9. Remove Dead Code

**Purpose**: Eliminate unused code

```java
// Before: Dead code
public class UserService {
    public void createUser() {
        // Used
    }
    
    public void deleteUser() {
        // Used
    }
    
    public void updateUser() {
        // Never called - dead code
    }
    
    private void helperMethod() {
        // Never called - dead code
    }
}

// After: Removed unused code
public class UserService {
    public void createUser() { }
    public void deleteUser() { }
    // Removed unused methods
}
```

### 10. Consolidate Duplicate Code

**Purpose**: Eliminate duplication

```java
// Before: Duplicate code
public void processOrder1(Order order) {
    if (order.getTotal().compareTo(BigDecimal.ZERO) > 0) {
        // Process
    }
}

public void processOrder2(Order order) {
    if (order.getTotal().compareTo(BigDecimal.ZERO) > 0) {
        // Process (duplicate)
    }
}

// After: Extract common code
public void processOrder(Order order) {
    if (isValidOrder(order)) {
        // Process
    }
}

private boolean isValidOrder(Order order) {
    return order.getTotal().compareTo(BigDecimal.ZERO) > 0;
}
```

## Refactoring Workflow

```
┌─────────────────────────────────────────────────────────┐
│         Refactoring Workflow                            │
└─────────────────────────────────────────────────────────┘

1. Identify Smell
   └─ Recognize code smell
   └─ Understand the problem

2. Write Tests
   └─ Ensure tests cover behavior
   └─ Tests should pass before refactoring

3. Make Small Change
   └─ Apply one refactoring technique
   └─ Keep change small

4. Run Tests
   └─ Verify behavior is preserved
   └─ All tests should pass

5. Commit
   └─ Commit the refactoring
   └─ Clear commit message

6. Repeat
   └─ Continue with next refactoring
   └─ Iterate until smell is gone
```

## When to Refactor

### Good Times to Refactor

```
┌─────────────────────────────────────────────────────────┐
│         When to Refactor                                │
└─────────────────────────────────────────────────────────┘

1. Before Adding Feature
   └─ Clean up code first
   └─ Makes adding feature easier

2. After Adding Feature
   └─ Clean up after implementation
   └─ Remove duplication introduced

3. When Fixing Bug
   └─ Improve code while fixing
   └─ Prevent similar bugs

4. During Code Review
   └─ Address review comments
   └─ Improve code quality

5. Regularly
   └─ Continuous refactoring
   └─ Prevent technical debt
```

### Bad Times to Refactor

- ❌ Close to release deadline
- ❌ When code is working and stable
- ❌ Without test coverage
- ❌ When refactoring is too large

## Refactoring Best Practices

### 1. Have Tests First

```java
// Write tests before refactoring
@Test
public void testCalculateTotal() {
    Order order = new Order();
    order.addItem(new Item("Book", 10.0));
    order.addItem(new Item("Pen", 2.0));
    
    double total = orderService.calculateTotal(order);
    
    assertEquals(12.0, total, 0.01);
}
```

### 2. Make Small Steps

```java
// Step 1: Extract method
// Step 2: Rename variable
// Step 3: Extract class
// Don't do everything at once
```

### 3. Use IDE Refactoring Tools

- Extract Method
- Rename
- Extract Class
- Move Method
- Inline Variable

### 4. Commit Frequently

```bash
# Commit after each refactoring step
git commit -m "Extract calculateTotal method"
git commit -m "Rename variable for clarity"
git commit -m "Extract OrderCalculator class"
```

## Summary

Refactoring:
- **Purpose**: Improve code structure without changing behavior
- **Key Principle**: Preserve functionality while improving design
- **Approach**: Small, incremental steps with tests
- **Timing**: Continuously, as part of development

**Key Techniques:**
- ✅ Extract Method/Class
- ✅ Rename for clarity
- ✅ Replace conditional with polymorphism
- ✅ Remove duplication
- ✅ Eliminate dead code

**Remember**: Refactoring is not a one-time activity - it's a continuous process that keeps code maintainable and extensible!

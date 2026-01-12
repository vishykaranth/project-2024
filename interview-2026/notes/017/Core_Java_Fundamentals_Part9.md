# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 9: Encapsulation

---

## Table of Contents

1. [Introduction to Encapsulation](#1-introduction-to-encapsulation)
2. [Access Modifiers Revisited](#2-access-modifiers-revisited)
3. [Getters and Setters](#3-getters-and-setters)
4. [Data Hiding](#4-data-hiding)
5. [Bean Pattern](#5-bean-pattern)
6. [Package Structure](#6-package-structure)
7. [Encapsulation Best Practices](#7-encapsulation-best-practices)
8. [Complete Example](#8-complete-example)

---

## 1. Introduction to Encapsulation

### 1.1 What is Encapsulation?

**Encapsulation** is the bundling of data (fields) and methods that operate on that data within a single unit (class). It also involves hiding internal implementation details.

**Key Principles**:
- **Data Hiding**: Hide internal data from outside access
- **Controlled Access**: Provide controlled access through methods
- **Implementation Hiding**: Hide how data is stored/manipulated

### 1.2 Benefits of Encapsulation

- **Security**: Prevents unauthorized access
- **Flexibility**: Can change implementation without affecting users
- **Maintainability**: Easier to maintain and debug
- **Validation**: Can validate data before setting
- **Data Integrity**: Ensures data consistency

### 1.3 Encapsulation Example

```java
// Bad: No encapsulation
class Student {
    public String name;  // Direct access
    public int age;      // Direct access
}

Student student = new Student();
student.age = -5;  // Invalid age, but no validation

// Good: With encapsulation
class Student {
    private String name;  // Hidden
    private int age;      // Hidden
    
    // Controlled access
    public void setAge(int age) {
        if (age > 0 && age < 150) {
            this.age = age;
        } else {
            throw new IllegalArgumentException("Invalid age");
        }
    }
    
    public int getAge() {
        return age;
    }
}
```

---

## 2. Access Modifiers Revisited

### 2.1 Access Modifier Levels

| Modifier | Class | Package | Subclass | World |
|----------|-------|---------|----------|-------|
| `private` | ✅ | ❌ | ❌ | ❌ |
| `package-private` | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

### 2.2 Private Access

```java
class BankAccount {
    private double balance;  // Hidden from outside
    
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }
    
    public double getBalance() {
        return balance;
    }
}

BankAccount account = new BankAccount();
// account.balance = 1000;  // ERROR: Cannot access private field
account.deposit(1000);  // OK: Through public method
```

### 2.3 Protected Access

```java
class Animal {
    protected String name;  // Accessible in package and subclasses
    
    protected void makeSound() {
        System.out.println("Animal sound");
    }
}

// In same package or subclass
class Dog extends Animal {
    public void display() {
        name = "Buddy";  // OK: protected accessible
        makeSound();     // OK: protected accessible
    }
}
```

### 2.4 Package-Private Access

```java
class Student {
    String name;  // Package-private (no modifier)
    
    void displayInfo() {  // Package-private method
        System.out.println(name);
    }
}

// Accessible only within same package
```

---

## 3. Getters and Setters

### 3.1 What are Getters and Setters?

**Getters** (accessors) are methods that retrieve the value of a field.
**Setters** (mutators) are methods that set the value of a field.

### 3.2 Basic Getter and Setter

```java
class Student {
    private String name;
    private int age;
    
    // Getter for name
    public String getName() {
        return name;
    }
    
    // Setter for name
    public void setName(String name) {
        this.name = name;
    }
    
    // Getter for age
    public int getAge() {
        return age;
    }
    
    // Setter for age
    public void setAge(int age) {
        this.age = age;
    }
}

// Usage
Student student = new Student();
student.setName("John");
student.setAge(20);
System.out.println(student.getName());  // John
System.out.println(student.getAge());   // 20
```

### 3.3 Getter and Setter with Validation

```java
class Student {
    private String name;
    private int age;
    private String email;
    
    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }
    
    public void setAge(int age) {
        if (age >= 0 && age <= 150) {
            this.age = age;
        } else {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }
    }
    
    public void setEmail(String email) {
        if (email != null && email.contains("@")) {
            this.email = email;
        } else {
            throw new IllegalArgumentException("Invalid email");
        }
    }
    
    public String getName() {
        return name;
    }
    
    public int getAge() {
        return age;
    }
    
    public String getEmail() {
        return email;
    }
}
```

### 3.4 Read-Only Fields

```java
class Student {
    private final String studentId;  // Final: cannot be changed
    private String name;
    
    public Student(String studentId, String name) {
        this.studentId = studentId;
        this.name = name;
    }
    
    // Only getter, no setter (read-only)
    public String getStudentId() {
        return studentId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
}
```

### 3.5 Write-Only Fields

```java
class BankAccount {
    private String password;
    
    // Only setter, no getter (write-only for security)
    public void setPassword(String password) {
        if (password != null && password.length() >= 8) {
            this.password = hashPassword(password);
        } else {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }
    
    public boolean verifyPassword(String password) {
        return this.password.equals(hashPassword(password));
    }
    
    private String hashPassword(String password) {
        // Password hashing logic
        return password;  // Simplified
    }
}
```

---

## 4. Data Hiding

### 4.1 Why Hide Data?

**Benefits**:
- **Control Access**: Control how data is accessed/modified
- **Validation**: Validate data before setting
- **Flexibility**: Change internal representation without affecting users
- **Security**: Prevent unauthorized access

### 4.2 Example: Bank Account

```java
class BankAccount {
    private double balance;  // Hidden
    
    public BankAccount(double initialBalance) {
        if (initialBalance >= 0) {
            this.balance = initialBalance;
        } else {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
    }
    
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        } else {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
    }
    
    public void withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
        } else {
            throw new IllegalArgumentException("Invalid withdrawal amount");
        }
    }
    
    public double getBalance() {
        return balance;
    }
    
    // No setter for balance - can only be modified through deposit/withdraw
}
```

### 4.3 Example: Temperature Converter

```java
class Temperature {
    private double celsius;  // Internal storage in Celsius
    
    public Temperature(double celsius) {
        this.celsius = celsius;
    }
    
    // Getters for different units
    public double getCelsius() {
        return celsius;
    }
    
    public double getFahrenheit() {
        return (celsius * 9/5) + 32;
    }
    
    public double getKelvin() {
        return celsius + 273.15;
    }
    
    // Setters for different units
    public void setCelsius(double celsius) {
        this.celsius = celsius;
    }
    
    public void setFahrenheit(double fahrenheit) {
        this.celsius = (fahrenheit - 32) * 5/9;
    }
    
    public void setKelvin(double kelvin) {
        this.celsius = kelvin - 273.15;
    }
}

// Internal representation (Celsius) is hidden
// Users can work with any temperature unit
```

---

## 5. Bean Pattern

### 5.1 What is a JavaBean?

A **JavaBean** is a class that follows specific conventions:
- Private fields
- Public no-arg constructor
- Public getters and setters
- Implements Serializable (optional)

### 5.2 JavaBean Example

```java
import java.io.Serializable;

public class Student implements Serializable {
    // Private fields
    private String name;
    private int age;
    private String email;
    
    // Public no-arg constructor
    public Student() {
    }
    
    // Parameterized constructor (optional)
    public Student(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public int getAge() {
        return age;
    }
    
    public String getEmail() {
        return email;
    }
    
    // Setters
    public void setName(String name) {
        this.name = name;
    }
    
    public void setAge(int age) {
        this.age = age;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
```

### 5.3 Boolean Getter Convention

```java
class Student {
    private boolean active;
    
    // For boolean, use 'is' prefix instead of 'get'
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
```

---

## 6. Package Structure

### 6.1 What is a Package?

A **package** is a namespace that organizes classes and interfaces. It helps avoid naming conflicts and provides access control.

### 6.2 Package Declaration

```java
// File: com/example/Student.java
package com.example;

public class Student {
    // Class code
}
```

### 6.3 Package Structure

```
com/
  example/
    Student.java
    Teacher.java
    util/
      StringUtils.java
      DateUtils.java
```

### 6.4 Importing Packages

```java
// Import specific class
import java.util.ArrayList;
import java.util.List;

// Import all classes from package
import java.util.*;

// Static import
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

// Usage
List<String> list = new ArrayList<>();
double area = PI * 5 * 5;
double root = sqrt(16);
```

### 6.5 Package Access

```java
// Package-private class (no public modifier)
class Student {
    String name;  // Package-private field
    
    void displayInfo() {  // Package-private method
        System.out.println(name);
    }
}

// Accessible only within same package
```

---

## 7. Encapsulation Best Practices

### 7.1 Always Use Private Fields

```java
// Bad
class Student {
    public String name;  // Direct access
    public int age;
}

// Good
class Student {
    private String name;  // Encapsulated
    private int age;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

### 7.2 Validate in Setters

```java
class Student {
    private int age;
    
    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Invalid age");
        }
        this.age = age;
    }
}
```

### 7.3 Use Final for Immutable Fields

```java
class Student {
    private final String studentId;  // Cannot be changed
    
    public Student(String studentId) {
        this.studentId = studentId;
    }
    
    public String getStudentId() {
        return studentId;  // No setter needed
    }
}
```

### 7.4 Minimize Public Methods

```java
class Student {
    private String name;
    private int age;
    
    // Public interface (minimal)
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    // Private helper methods
    private void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
    }
}
```

### 7.5 Return Defensive Copies

```java
import java.util.ArrayList;
import java.util.List;

class Student {
    private List<String> courses;
    
    public Student() {
        this.courses = new ArrayList<>();
    }
    
    // Return defensive copy
    public List<String> getCourses() {
        return new ArrayList<>(courses);  // Return copy, not original
    }
    
    // Or return unmodifiable list
    public List<String> getCoursesUnmodifiable() {
        return List.copyOf(courses);
    }
}
```

---

## 8. Complete Example

### 8.1 Fully Encapsulated Class

```java
public class BankAccount {
    // Private fields
    private String accountNumber;
    private String accountHolder;
    private double balance;
    private final double MIN_BALANCE = 100.0;
    
    // Constructor
    public BankAccount(String accountNumber, String accountHolder, double initialBalance) {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty");
        }
        if (accountHolder == null || accountHolder.isEmpty()) {
            throw new IllegalArgumentException("Account holder cannot be empty");
        }
        if (initialBalance < MIN_BALANCE) {
            throw new IllegalArgumentException("Initial balance must be at least " + MIN_BALANCE);
        }
        
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = initialBalance;
    }
    
    // Getters
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public String getAccountHolder() {
        return accountHolder;
    }
    
    public double getBalance() {
        return balance;
    }
    
    // No setter for accountNumber (immutable)
    // No setter for balance (controlled through deposit/withdraw)
    
    // Business methods
    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }
        balance += amount;
    }
    
    public void withdraw(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }
        if (balance - amount < MIN_BALANCE) {
            throw new IllegalArgumentException("Insufficient balance. Minimum balance required: " + MIN_BALANCE);
        }
        balance -= amount;
    }
    
    public void transfer(BankAccount toAccount, double amount) {
        if (toAccount == null) {
            throw new IllegalArgumentException("Target account cannot be null");
        }
        withdraw(amount);
        toAccount.deposit(amount);
    }
    
    @Override
    public String toString() {
        return "BankAccount{" +
                "accountNumber='" + accountNumber + '\'' +
                ", accountHolder='" + accountHolder + '\'' +
                ", balance=" + balance +
                '}';
    }
}
```

### 8.2 Usage Example

```java
public class BankAccountDemo {
    public static void main(String[] args) {
        // Create accounts
        BankAccount account1 = new BankAccount("ACC001", "John Doe", 1000.0);
        BankAccount account2 = new BankAccount("ACC002", "Jane Smith", 500.0);
        
        // Display accounts
        System.out.println(account1);
        System.out.println(account2);
        
        // Deposit
        account1.deposit(500.0);
        System.out.println("After deposit: " + account1.getBalance());
        
        // Withdraw
        account1.withdraw(200.0);
        System.out.println("After withdrawal: " + account1.getBalance());
        
        // Transfer
        account1.transfer(account2, 100.0);
        System.out.println("Account1 balance: " + account1.getBalance());
        System.out.println("Account2 balance: " + account2.getBalance());
        
        // Cannot access private fields directly
        // account1.balance = 10000;  // ERROR: balance is private
    }
}
```

---

## Summary: Part 9

### Key Concepts Covered

1. **Encapsulation**: Bundling data and methods, hiding implementation
2. **Access Modifiers**: private, protected, package-private, public
3. **Getters and Setters**: Controlled access to fields
4. **Data Hiding**: Hide internal representation
5. **JavaBean Pattern**: Standard conventions for classes
6. **Package Structure**: Organizing classes in packages
7. **Best Practices**: Validation, defensive copies, minimal public interface

### Important Points

- Always use private fields
- Provide getters/setters for controlled access
- Validate data in setters
- Use final for immutable fields
- Return defensive copies for mutable objects
- Minimize public interface
- Hide implementation details

### Next Steps

**Part 10** will cover:
- Advanced OOP Concepts
- Combining All OOP Principles
- Design Patterns Basics
- SOLID Principles Introduction
- Complete OOP Example
- Best Practices Summary

---

**Master encapsulation to build secure and maintainable Java applications!**


# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 4: Methods and Method Overloading

---

## Table of Contents

1. [Introduction to Methods](#1-introduction-to-methods)
2. [Method Declaration and Syntax](#2-method-declaration-and-syntax)
3. [Method Parameters](#3-method-parameters)
4. [Return Types](#4-return-types)
5. [Method Overloading](#5-method-overloading)
6. [Variable Arguments (Varargs)](#6-variable-arguments-varargs)
7. [Recursion](#7-recursion)
8. [Method Best Practices](#8-method-best-practices)

---

## 1. Introduction to Methods

### 1.1 What is a Method?

A **method** is a block of code that performs a specific task. It's a way to organize code into reusable units.

**Benefits**:
- **Code Reusability**: Write once, use many times
- **Modularity**: Break complex problems into smaller parts
- **Maintainability**: Easier to update and debug
- **Abstraction**: Hide implementation details

### 1.2 Method vs Function

In Java, we use the term **method** (not function) because methods belong to classes. Functions are standalone, methods are part of classes.

---

## 2. Method Declaration and Syntax

### 2.1 Method Syntax

```java
[access-modifier] [static] [final] return-type method-name([parameters]) {
    // Method body
    [return statement;]
}
```

### 2.2 Method Components

**1. Access Modifier**: public, private, protected, package-private
**2. Static Keyword**: Optional, makes method class-level
**3. Return Type**: Data type of value returned (void if nothing)
**4. Method Name**: Identifier for the method
**5. Parameters**: Input values (optional)
**6. Method Body**: Code that executes

### 2.3 Simple Method Examples

```java
// Method with no parameters and no return value
public void greet() {
    System.out.println("Hello, World!");
}

// Method with parameters and no return value
public void greet(String name) {
    System.out.println("Hello, " + name + "!");
}

// Method with parameters and return value
public int add(int a, int b) {
    return a + b;
}

// Static method
public static void printMessage() {
    System.out.println("This is a static method");
}
```

---

## 3. Method Parameters

### 3.1 Parameters vs Arguments

```java
// Parameters: Variables in method declaration
public void method(int param1, String param2) {
    // param1 and param2 are parameters
}

// Arguments: Values passed when calling method
method(10, "Hello");  // 10 and "Hello" are arguments
```

### 3.2 Passing Primitive Types

**Pass by Value** (Primitives):
```java
public void modifyValue(int x) {
    x = 100;  // Changes local copy, not original
}

int num = 10;
modifyValue(num);
System.out.println(num);  // Still 10 (unchanged)
```

### 3.3 Passing Reference Types

**Pass by Reference Value** (Objects):
```java
public void modifyArray(int[] arr) {
    arr[0] = 100;  // Modifies original array
}

int[] numbers = {1, 2, 3};
modifyArray(numbers);
System.out.println(numbers[0]);  // 100 (changed)
```

### 3.4 Multiple Parameters

```java
public int calculate(int a, int b, int c) {
    return a + b + c;
}

int result = calculate(10, 20, 30);  // 60
```

### 3.5 Parameter Types

```java
// Primitive parameters
public void method(int x, double y, boolean flag) { }

// Object parameters
public void method(String name, Date date, List<String> list) { }

// Array parameters
public void method(int[] numbers, String[] names) { }
```

---

## 4. Return Types

### 4.1 Void Return Type

```java
// Method that doesn't return anything
public void printMessage() {
    System.out.println("Hello");
}

// Can use return to exit early
public void checkAge(int age) {
    if (age < 0) {
        return;  // Exit method early
    }
    System.out.println("Age: " + age);
}
```

### 4.2 Primitive Return Types

```java
// Return int
public int getSum(int a, int b) {
    return a + b;
}

// Return double
public double getAverage(int a, int b) {
    return (a + b) / 2.0;
}

// Return boolean
public boolean isEven(int number) {
    return number % 2 == 0;
}

// Return char
public char getFirstChar(String str) {
    return str.charAt(0);
}
```

### 4.3 Object Return Types

```java
// Return String
public String getFullName(String firstName, String lastName) {
    return firstName + " " + lastName;
}

// Return custom object
public Student createStudent(String name, int age) {
    return new Student(name, age);
}

// Return array
public int[] getNumbers() {
    return new int[]{1, 2, 3, 4, 5};
}

// Return collection
public List<String> getNames() {
    return Arrays.asList("Alice", "Bob", "Charlie");
}
```

### 4.4 Return Statement Rules

```java
// Must return value matching return type
public int getValue() {
    return 10;  // OK
    // return "Hello";  // ERROR: String cannot be converted to int
}

// void methods can use return to exit
public void method() {
    if (condition) {
        return;  // OK: exit early
    }
    // return 10;  // ERROR: void cannot return value
}

// All code paths must return (for non-void)
public int getValue(boolean flag) {
    if (flag) {
        return 10;
    }
    return 20;  // Must return in all paths
}
```

---

## 5. Method Overloading

### 5.1 What is Method Overloading?

**Method overloading** allows multiple methods with the same name but different parameters.

**Rules**:
- Methods must have same name
- Parameters must differ in:
  - Number of parameters
  - Type of parameters
  - Order of parameters
- Return type doesn't matter for overloading

### 5.2 Overloading by Number of Parameters

```java
public class Calculator {
    // Add two numbers
    public int add(int a, int b) {
        return a + b;
    }
    
    // Add three numbers
    public int add(int a, int b, int c) {
        return a + b + c;
    }
    
    // Add four numbers
    public int add(int a, int b, int c, int d) {
        return a + b + c + d;
    }
}

// Usage
Calculator calc = new Calculator();
calc.add(10, 20);           // Calls first method
calc.add(10, 20, 30);       // Calls second method
calc.add(10, 20, 30, 40);   // Calls third method
```

### 5.3 Overloading by Parameter Types

```java
public class Printer {
    // Print int
    public void print(int value) {
        System.out.println("Integer: " + value);
    }
    
    // Print double
    public void print(double value) {
        System.out.println("Double: " + value);
    }
    
    // Print String
    public void print(String value) {
        System.out.println("String: " + value);
    }
    
    // Print boolean
    public void print(boolean value) {
        System.out.println("Boolean: " + value);
    }
}

// Usage
Printer printer = new Printer();
printer.print(10);        // Calls print(int)
printer.print(10.5);      // Calls print(double)
printer.print("Hello");   // Calls print(String)
printer.print(true);      // Calls print(boolean)
```

### 5.4 Overloading by Parameter Order

```java
public class Formatter {
    public void format(String name, int age) {
        System.out.println("Name: " + name + ", Age: " + age);
    }
    
    public void format(int age, String name) {
        System.out.println("Age: " + age + ", Name: " + name);
    }
}

// Usage
Formatter formatter = new Formatter();
formatter.format("John", 25);  // Calls first method
formatter.format(25, "John");  // Calls second method
```

### 5.5 Type Promotion in Overloading

```java
public class OverloadDemo {
    public void method(byte b) {
        System.out.println("byte");
    }
    
    public void method(short s) {
        System.out.println("short");
    }
    
    public void method(int i) {
        System.out.println("int");
    }
    
    public void method(long l) {
        System.out.println("long");
    }
}

// Usage
OverloadDemo demo = new OverloadDemo();
demo.method(10);      // Calls method(int) - exact match
demo.method((byte)10); // Calls method(byte) - explicit cast
// If method(int) doesn't exist, 10 would be promoted to int
```

### 5.6 Ambiguous Overloading

```java
// ERROR: Ambiguous method call
public void method(int a, long b) { }
public void method(long a, int b) { }

// Calling method(10, 20) is ambiguous
// Both methods could match (with promotion)
```

---

## 6. Variable Arguments (Varargs)

### 6.1 What are Varargs?

**Varargs** (variable arguments) allow methods to accept variable number of arguments.

**Syntax**: `type... parameterName`

### 6.2 Varargs Examples

```java
// Method with varargs
public int sum(int... numbers) {
    int total = 0;
    for (int num : numbers) {
        total += num;
    }
    return total;
}

// Usage
sum(10);              // 10
sum(10, 20);          // 30
sum(10, 20, 30);      // 60
sum(10, 20, 30, 40);  // 100
```

### 6.3 Varargs Rules

```java
// Varargs must be last parameter
public void method(String name, int... numbers) { }  // OK
// public void method(int... numbers, String name) { }  // ERROR

// Only one varargs parameter allowed
// public void method(int... a, String... b) { }  // ERROR

// Varargs can be empty
public void print(String... messages) {
    for (String msg : messages) {
        System.out.println(msg);
    }
}

print();  // OK: no arguments
print("Hello");  // OK: one argument
print("Hello", "World");  // OK: multiple arguments
```

### 6.4 Varargs vs Array

```java
// Varargs (more convenient)
public void method(int... numbers) {
    // numbers is treated as int[]
}

// Array (explicit)
public void method(int[] numbers) {
    // Same functionality
}

// Varargs is syntactic sugar for array
// Both are equivalent internally
```

---

## 7. Recursion

### 7.1 What is Recursion?

**Recursion** is when a method calls itself. It's useful for solving problems that can be broken down into smaller, similar subproblems.

**Components**:
- **Base Case**: Condition that stops recursion
- **Recursive Case**: Method calls itself with modified parameters

### 7.2 Factorial Example

```java
// Factorial: n! = n * (n-1) * (n-2) * ... * 1
public int factorial(int n) {
    // Base case
    if (n <= 1) {
        return 1;
    }
    // Recursive case
    return n * factorial(n - 1);
}

// Execution trace for factorial(5):
// factorial(5) = 5 * factorial(4)
// factorial(4) = 4 * factorial(3)
// factorial(3) = 3 * factorial(2)
// factorial(2) = 2 * factorial(1)
// factorial(1) = 1 (base case)
// Result: 5 * 4 * 3 * 2 * 1 = 120
```

### 7.3 Fibonacci Example

```java
// Fibonacci: F(n) = F(n-1) + F(n-2)
// F(0) = 0, F(1) = 1
public int fibonacci(int n) {
    // Base cases
    if (n == 0) {
        return 0;
    }
    if (n == 1) {
        return 1;
    }
    // Recursive case
    return fibonacci(n - 1) + fibonacci(n - 2);
}

// Fibonacci sequence: 0, 1, 1, 2, 3, 5, 8, 13, 21, ...
```

### 7.4 Power Calculation

```java
// Calculate x^n recursively
public double power(double x, int n) {
    // Base case
    if (n == 0) {
        return 1;
    }
    if (n == 1) {
        return x;
    }
    // Recursive case
    if (n > 0) {
        return x * power(x, n - 1);
    } else {
        return 1 / power(x, -n);
    }
}
```

### 7.5 Recursion vs Iteration

```java
// Recursive approach
public int sumRecursive(int n) {
    if (n <= 0) {
        return 0;
    }
    return n + sumRecursive(n - 1);
}

// Iterative approach
public int sumIterative(int n) {
    int sum = 0;
    for (int i = 1; i <= n; i++) {
        sum += i;
    }
    return sum;
}

// Recursion: More elegant, but uses more memory (stack)
// Iteration: More efficient, uses less memory
```

### 7.6 Common Recursion Pitfalls

```java
// ERROR: Missing base case (infinite recursion)
public void infiniteRecursion() {
    infiniteRecursion();  // Never stops!
}

// ERROR: Base case never reached
public int badRecursion(int n) {
    if (n < 0) {  // Base case, but...
        return 0;
    }
    return badRecursion(n + 1);  // n increases, never reaches base case
}

// CORRECT: Proper base case
public int goodRecursion(int n) {
    if (n <= 0) {  // Base case
        return 0;
    }
    return n + goodRecursion(n - 1);  // n decreases toward base case
}
```

---

## 8. Method Best Practices

### 8.1 Method Naming

```java
// Use verb-noun pattern
public void calculateTotal() { }
public String getUserName() { }
public boolean isValid() { }
public void setAge(int age) { }

// Be descriptive
public void process() { }  // Too vague
public void processPayment() { }  // Better

// Follow camelCase
public void getUserName() { }  // Good
public void GetUserName() { }  // Bad
public void get_user_name() { }  // Bad
```

### 8.2 Method Length

```java
// Keep methods short and focused
// Good: Single responsibility
public double calculateTax(double amount) {
    double rate = 0.08;
    return amount * rate;
}

// Bad: Too many responsibilities
public void doEverything() {
    // 100+ lines doing multiple things
    // Should be broken into smaller methods
}
```

### 8.3 Parameter Count

```java
// Good: Few parameters
public void createUser(String name, String email) { }

// Bad: Too many parameters
public void createUser(String name, String email, String phone, 
                      String address, int age, String gender) { }

// Better: Use object
public void createUser(User user) { }
```

### 8.4 Return Early

```java
// Good: Early returns
public boolean isValid(String input) {
    if (input == null) {
        return false;
    }
    if (input.isEmpty()) {
        return false;
    }
    return input.length() > 5;
}

// Bad: Nested if-else
public boolean isValid(String input) {
    if (input != null) {
        if (!input.isEmpty()) {
            if (input.length() > 5) {
                return true;
            }
        }
    }
    return false;
}
```

### 8.5 Documentation

```java
/**
 * Calculates the area of a rectangle.
 * 
 * @param length The length of the rectangle
 * @param width The width of the rectangle
 * @return The area of the rectangle
 * @throws IllegalArgumentException if length or width is negative
 */
public double calculateArea(double length, double width) {
    if (length < 0 || width < 0) {
        throw new IllegalArgumentException("Dimensions must be positive");
    }
    return length * width;
}
```

---

## Summary: Part 4

### Key Concepts Covered

1. **Methods**: Reusable code blocks
2. **Parameters**: Input values to methods
3. **Return Types**: Values returned from methods
4. **Method Overloading**: Same name, different parameters
5. **Varargs**: Variable number of arguments
6. **Recursion**: Methods calling themselves

### Important Points

- Methods provide code reusability and modularity
- Parameters are passed by value (primitives) or reference (objects)
- Method overloading requires different parameter signatures
- Varargs must be the last parameter
- Recursion needs a base case to avoid infinite loops
- Keep methods short, focused, and well-documented

### Next Steps

**Part 5** will cover:
- Classes and Objects
- Object Creation
- Constructors
- this Keyword
- Instance vs Class Members

---

**Master methods to build modular and reusable Java code!**


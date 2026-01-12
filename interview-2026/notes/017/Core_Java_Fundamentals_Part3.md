# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 3: Control Flow Statements

---

## Table of Contents

1. [Introduction to Control Flow](#1-introduction-to-control-flow)
2. [If-Else Statements](#2-if-else-statements)
3. [Switch Statements](#3-switch-statements)
4. [For Loop](#4-for-loop)
5. [While Loop](#5-while-loop)
6. [Do-While Loop](#6-do-while-loop)
7. [Break and Continue](#7-break-and-continue)
8. [Nested Control Structures](#8-nested-control-structures)

---

## 1. Introduction to Control Flow

### 1.1 What is Control Flow?

**Control flow** determines the order in which statements are executed in a program. It allows programs to make decisions, repeat operations, and handle different scenarios.

### 1.2 Types of Control Flow

1. **Conditional Statements**: if-else, switch
2. **Looping Statements**: for, while, do-while, for-each
3. **Jump Statements**: break, continue, return

---

## 2. If-Else Statements

### 2.1 Simple If Statement

```java
if (condition) {
    // Code executes if condition is true
    statement;
}

// Example
int age = 20;
if (age >= 18) {
    System.out.println("You are an adult");
}
```

### 2.2 If-Else Statement

```java
if (condition) {
    // Code if condition is true
} else {
    // Code if condition is false
}

// Example
int age = 15;
if (age >= 18) {
    System.out.println("You are an adult");
} else {
    System.out.println("You are a minor");
}
```

### 2.3 If-Else-If Ladder

```java
if (condition1) {
    // Code for condition1
} else if (condition2) {
    // Code for condition2
} else if (condition3) {
    // Code for condition3
} else {
    // Code if none of conditions are true
}

// Example: Grade calculation
int score = 85;
if (score >= 90) {
    System.out.println("Grade: A");
} else if (score >= 80) {
    System.out.println("Grade: B");
} else if (score >= 70) {
    System.out.println("Grade: C");
} else if (score >= 60) {
    System.out.println("Grade: D");
} else {
    System.out.println("Grade: F");
}
```

### 2.4 Nested If Statements

```java
if (condition1) {
    if (condition2) {
        // Code if both conditions are true
    }
}

// Example: Login validation
String username = "admin";
String password = "secret";

if (username != null && !username.isEmpty()) {
    if (password != null && password.length() >= 6) {
        System.out.println("Login successful");
    } else {
        System.out.println("Invalid password");
    }
} else {
    System.out.println("Invalid username");
}
```

### 2.5 Ternary Operator (Alternative to If-Else)

```java
// Syntax: condition ? valueIfTrue : valueIfFalse

int age = 20;
String status = (age >= 18) ? "Adult" : "Minor";

int a = 10, b = 20;
int max = (a > b) ? a : b;

// Nested ternary
int score = 85;
String grade = (score >= 90) ? "A" : 
               (score >= 80) ? "B" : 
               (score >= 70) ? "C" : "F";
```

---

## 3. Switch Statements

### 3.1 Basic Switch Statement

```java
switch (expression) {
    case value1:
        // Code for value1
        break;
    case value2:
        // Code for value2
        break;
    default:
        // Code if no case matches
}

// Example: Day of week
int day = 3;
switch (day) {
    case 1:
        System.out.println("Monday");
        break;
    case 2:
        System.out.println("Tuesday");
        break;
    case 3:
        System.out.println("Wednesday");
        break;
    case 4:
        System.out.println("Thursday");
        break;
    case 5:
        System.out.println("Friday");
        break;
    default:
        System.out.println("Weekend");
}
```

### 3.2 Switch Without Break (Fall-Through)

```java
// Multiple cases can execute same code
int month = 2;
switch (month) {
    case 1:
    case 3:
    case 5:
    case 7:
    case 8:
    case 10:
    case 12:
        System.out.println("31 days");
        break;
    case 4:
    case 6:
    case 9:
    case 11:
        System.out.println("30 days");
        break;
    case 2:
        System.out.println("28 or 29 days");
        break;
}
```

### 3.3 Switch with Strings (Java 7+)

```java
String day = "Monday";
switch (day) {
    case "Monday":
        System.out.println("Start of work week");
        break;
    case "Friday":
        System.out.println("End of work week");
        break;
    default:
        System.out.println("Mid week");
}
```

### 3.4 Switch Expressions (Java 14+)

```java
// Switch as expression (returns value)
int day = 3;
String dayName = switch (day) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 3 -> "Wednesday";
    case 4 -> "Thursday";
    case 5 -> "Friday";
    default -> "Weekend";
};

// Multiple statements
int result = switch (value) {
    case 1 -> {
        System.out.println("Case 1");
        yield 10;  // Return value
    }
    case 2 -> {
        System.out.println("Case 2");
        yield 20;
    }
    default -> 0;
};
```

### 3.5 Switch with Enums

```java
enum Status {
    ACTIVE, INACTIVE, PENDING
}

Status status = Status.ACTIVE;
switch (status) {
    case ACTIVE:
        System.out.println("User is active");
        break;
    case INACTIVE:
        System.out.println("User is inactive");
        break;
    case PENDING:
        System.out.println("User is pending");
        break;
}
```

---

## 4. For Loop

### 4.1 Traditional For Loop

```java
for (initialization; condition; increment/decrement) {
    // Code to execute
}

// Example: Print numbers 1 to 10
for (int i = 1; i <= 10; i++) {
    System.out.println(i);
}

// Example: Countdown
for (int i = 10; i >= 1; i--) {
    System.out.println(i);
}
```

### 4.2 For Loop Variations

**Multiple Variables**:
```java
for (int i = 0, j = 10; i < j; i++, j--) {
    System.out.println("i: " + i + ", j: " + j);
}
```

**Empty Sections**:
```java
int i = 0;
for (; i < 10; ) {
    System.out.println(i);
    i++;
}
```

**Infinite Loop**:
```java
for (;;) {
    // Infinite loop (use break to exit)
    if (condition) {
        break;
    }
}
```

### 4.3 Enhanced For Loop (For-Each)

```java
// Syntax: for (type variable : collection)

// Array iteration
int[] numbers = {1, 2, 3, 4, 5};
for (int num : numbers) {
    System.out.println(num);
}

// String array
String[] names = {"Alice", "Bob", "Charlie"};
for (String name : names) {
    System.out.println(name);
}

// Collection iteration
List<String> list = Arrays.asList("A", "B", "C");
for (String item : list) {
    System.out.println(item);
}
```

### 4.4 Nested For Loops

```java
// Print multiplication table
for (int i = 1; i <= 10; i++) {
    for (int j = 1; j <= 10; j++) {
        System.out.print(i * j + "\t");
    }
    System.out.println();
}

// Pattern printing
for (int i = 1; i <= 5; i++) {
    for (int j = 1; j <= i; j++) {
        System.out.print("*");
    }
    System.out.println();
}
// Output:
// *
// **
// ***
// ****
// *****
```

---

## 5. While Loop

### 5.1 Basic While Loop

```java
while (condition) {
    // Code to execute while condition is true
}

// Example: Print numbers 1 to 10
int i = 1;
while (i <= 10) {
    System.out.println(i);
    i++;
}

// Example: User input validation
Scanner scanner = new Scanner(System.in);
int number;
while (true) {
    System.out.print("Enter a positive number: ");
    number = scanner.nextInt();
    if (number > 0) {
        break;
    }
    System.out.println("Invalid input. Try again.");
}
```

### 5.2 While Loop Use Cases

**Reading Until Condition**:
```java
Scanner scanner = new Scanner(System.in);
String input;
while (!(input = scanner.nextLine()).equals("quit")) {
    System.out.println("You entered: " + input);
}
```

**Processing Until Empty**:
```java
List<String> items = new ArrayList<>();
String item;
while (!(item = scanner.nextLine()).isEmpty()) {
    items.add(item);
}
```

---

## 6. Do-While Loop

### 6.1 Basic Do-While Loop

```java
do {
    // Code executes at least once
} while (condition);

// Example: Menu system
Scanner scanner = new Scanner(System.in);
int choice;
do {
    System.out.println("1. Option 1");
    System.out.println("2. Option 2");
    System.out.println("3. Exit");
    System.out.print("Enter choice: ");
    choice = scanner.nextInt();
    
    // Process choice
    switch (choice) {
        case 1:
            System.out.println("Option 1 selected");
            break;
        case 2:
            System.out.println("Option 2 selected");
            break;
    }
} while (choice != 3);
```

### 6.2 Do-While vs While

```java
// While: Condition checked first (may not execute)
int i = 10;
while (i < 5) {
    System.out.println(i);  // Never executes
    i++;
}

// Do-While: Executes at least once
int j = 10;
do {
    System.out.println(j);  // Executes once (prints 10)
    j++;
} while (j < 5);
```

---

## 7. Break and Continue

### 7.1 Break Statement

**Break in Loops**:
```java
// Exit loop immediately
for (int i = 1; i <= 10; i++) {
    if (i == 5) {
        break;  // Exit loop when i is 5
    }
    System.out.println(i);
}
// Output: 1, 2, 3, 4

// Break in nested loops (breaks inner loop only)
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (j == 2) {
            break;  // Breaks inner loop only
        }
        System.out.println("i: " + i + ", j: " + j);
    }
}
```

**Labeled Break**:
```java
// Break outer loop using label
outer: for (int i = 1; i <= 3; i++) {
    inner: for (int j = 1; j <= 3; j++) {
        if (i == 2 && j == 2) {
            break outer;  // Breaks outer loop
        }
        System.out.println("i: " + i + ", j: " + j);
    }
}
```

**Break in Switch**:
```java
int day = 1;
switch (day) {
    case 1:
        System.out.println("Monday");
        break;  // Prevents fall-through
    case 2:
        System.out.println("Tuesday");
        break;
}
```

### 7.2 Continue Statement

**Continue in Loops**:
```java
// Skip current iteration
for (int i = 1; i <= 10; i++) {
    if (i % 2 == 0) {
        continue;  // Skip even numbers
    }
    System.out.println(i);
}
// Output: 1, 3, 5, 7, 9

// Continue in nested loops
for (int i = 1; i <= 3; i++) {
    for (int j = 1; j <= 3; j++) {
        if (j == 2) {
            continue;  // Skip inner loop iteration
        }
        System.out.println("i: " + i + ", j: " + j);
    }
}
```

**Labeled Continue**:
```java
outer: for (int i = 1; i <= 3; i++) {
    inner: for (int j = 1; j <= 3; j++) {
        if (i == 2 && j == 2) {
            continue outer;  // Continue outer loop
        }
        System.out.println("i: " + i + ", j: " + j);
    }
}
```

---

## 8. Nested Control Structures

### 8.1 Nested If-Else

```java
int age = 25;
boolean hasLicense = true;

if (age >= 18) {
    if (hasLicense) {
        System.out.println("Can drive");
    } else {
        System.out.println("Need license");
    }
} else {
    System.out.println("Too young to drive");
}
```

### 8.2 Loop with Conditional

```java
// Find even numbers in array
int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
for (int num : numbers) {
    if (num % 2 == 0) {
        System.out.println(num + " is even");
    }
}
```

### 8.3 Nested Loops with Conditions

```java
// Print prime numbers
for (int i = 2; i <= 100; i++) {
    boolean isPrime = true;
    for (int j = 2; j <= Math.sqrt(i); j++) {
        if (i % j == 0) {
            isPrime = false;
            break;
        }
    }
    if (isPrime) {
        System.out.println(i + " is prime");
    }
}
```

### 8.4 Complex Control Flow Example

```java
// Student grade system
Scanner scanner = new Scanner(System.in);
int studentCount = 0;
int passCount = 0;
int failCount = 0;

while (true) {
    System.out.print("Enter student score (or -1 to exit): ");
    int score = scanner.nextInt();
    
    if (score == -1) {
        break;  // Exit loop
    }
    
    if (score < 0 || score > 100) {
        System.out.println("Invalid score. Enter 0-100.");
        continue;  // Skip to next iteration
    }
    
    studentCount++;
    
    String grade;
    if (score >= 90) {
        grade = "A";
    } else if (score >= 80) {
        grade = "B";
    } else if (score >= 70) {
        grade = "C";
    } else if (score >= 60) {
        grade = "D";
    } else {
        grade = "F";
    }
    
    System.out.println("Grade: " + grade);
    
    if (score >= 60) {
        passCount++;
    } else {
        failCount++;
    }
}

System.out.println("Total students: " + studentCount);
System.out.println("Passed: " + passCount);
System.out.println("Failed: " + failCount);
```

---

## Summary: Part 3

### Key Concepts Covered

1. **If-Else**: Conditional execution based on conditions
2. **Switch**: Multi-way branching based on value
3. **For Loop**: Iteration with initialization, condition, increment
4. **While Loop**: Iteration while condition is true
5. **Do-While Loop**: Iteration that executes at least once
6. **Break**: Exit loops or switch statements
7. **Continue**: Skip current iteration
8. **Nested Structures**: Combining control flow statements

### Control Flow Decision Guide

**Use If-Else When**:
- Conditions are boolean expressions
- Multiple conditions need checking
- Range-based decisions

**Use Switch When**:
- Comparing single variable to multiple values
- Exact value matching
- Multiple cases with same action

**Use For Loop When**:
- Known number of iterations
- Iterating over arrays/collections
- Counter-based iteration

**Use While Loop When**:
- Unknown number of iterations
- Condition-based iteration
- Reading until sentinel value

**Use Do-While When**:
- Need to execute at least once
- Menu systems
- Input validation

### Next Steps

**Part 4** will cover:
- Methods and Method Declaration
- Method Parameters and Return Types
- Method Overloading
- Variable Arguments (Varargs)
- Recursion

---

**Master control flow to build dynamic and interactive Java programs!**


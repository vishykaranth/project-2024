# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 2: Data Types, Variables, and Operators

---

## Table of Contents

1. [Data Types in Java](#1-data-types-in-java)
2. [Primitive Data Types](#2-primitive-data-types)
3. [Reference Data Types](#3-reference-data-types)
4. [Variables](#4-variables)
5. [Constants](#5-constants)
6. [Operators](#6-operators)
7. [Type Conversion and Casting](#7-type-conversion-and-casting)

---

## 1. Data Types in Java

### 1.1 Two Categories of Data Types

**1. Primitive Data Types**
- Built-in types
- Store actual values
- 8 primitive types in Java

**2. Reference Data Types**
- Objects, arrays, classes
- Store references to memory locations

### 1.2 Memory Storage

```java
// Primitive: Stores actual value
int x = 10;  // x directly contains 10

// Reference: Stores address/reference
String str = "Hello";  // str contains reference to "Hello" object
```

---

## 2. Primitive Data Types

### 2.1 The 8 Primitive Types

| Type | Size | Range | Default Value | Example |
|------|------|-------|---------------|---------|
| `byte` | 1 byte | -128 to 127 | 0 | `byte b = 100;` |
| `short` | 2 bytes | -32,768 to 32,767 | 0 | `short s = 1000;` |
| `int` | 4 bytes | -2³¹ to 2³¹-1 | 0 | `int i = 100000;` |
| `long` | 8 bytes | -2⁶³ to 2⁶³-1 | 0L | `long l = 1000000L;` |
| `float` | 4 bytes | ±3.4E+38 | 0.0f | `float f = 3.14f;` |
| `double` | 8 bytes | ±1.7E+308 | 0.0d | `double d = 3.14159;` |
| `char` | 2 bytes | 0 to 65,535 | '\u0000' | `char c = 'A';` |
| `boolean` | 1 bit | true/false | false | `boolean flag = true;` |

### 2.2 Integer Types

**byte**
```java
byte age = 25;
byte temperature = -10;
// Range: -128 to 127
// Use case: Memory-efficient storage of small numbers
```

**short**
```java
short year = 2024;
short count = 1000;
// Range: -32,768 to 32,767
// Use case: Memory-efficient storage of medium numbers
```

**int (Most Common)**
```java
int population = 1000000;
int score = 95;
// Range: -2,147,483,648 to 2,147,483,647
// Use case: Default choice for integers
```

**long**
```java
long worldPopulation = 8000000000L;  // Note: 'L' suffix
long distance = 150000000L;
// Range: Very large numbers
// Use case: Large integer values
```

### 2.3 Floating-Point Types

**float**
```java
float price = 19.99f;  // Note: 'f' suffix
float temperature = 98.6f;
float pi = 3.14159f;
// Precision: ~7 decimal digits
// Use case: Memory-efficient floating-point numbers
```

**double (Most Common)**
```java
double pi = 3.141592653589793;
double salary = 75000.50;
double distance = 123.456789;
// Precision: ~15-17 decimal digits
// Use case: Default choice for floating-point numbers
```

### 2.4 Character Type

**char**
```java
char grade = 'A';
char symbol = '@';
char unicode = '\u0041';  // Unicode for 'A'

// Character operations
char ch = 'A';
int ascii = ch;  // 65
char next = (char)(ch + 1);  // 'B'
```

### 2.5 Boolean Type

**boolean**
```java
boolean isActive = true;
boolean isComplete = false;
boolean isValid = (age >= 18);

// Only two values: true or false
// Cannot convert to/from integers
```

---

## 3. Reference Data Types

### 3.1 What are Reference Types?

**Reference types** store references (addresses) to objects in memory, not the actual values.

```java
// Primitive: Actual value stored
int x = 10;
int y = x;  // y gets copy of value (10)
x = 20;     // x is now 20, y is still 10

// Reference: Reference stored
String str1 = "Hello";
String str2 = str1;  // str2 references same object
str1 = "World";      // str1 now references "World", str2 still references "Hello"
```

### 3.2 Common Reference Types

**String**
```java
String name = "John";
String message = new String("Hello");
String fullName = firstName + " " + lastName;
```

**Arrays**
```java
int[] numbers = {1, 2, 3, 4, 5};
String[] names = new String[10];
```

**Classes**
```java
Scanner scanner = new Scanner(System.in);
Date today = new Date();
```

**Interfaces**
```java
List<String> list = new ArrayList<>();
Map<String, Integer> map = new HashMap<>();
```

### 3.3 Default Values

**Primitive Types**:
```java
int x;           // 0
double d;        // 0.0
boolean flag;    // false
char c;          // '\u0000'
```

**Reference Types**:
```java
String str;      // null
int[] arr;       // null
Object obj;      // null
```

---

## 4. Variables

### 4.1 Variable Declaration

**Syntax**:
```java
dataType variableName;
dataType variableName = value;
```

**Examples**:
```java
// Declaration
int age;
String name;

// Declaration and initialization
int age = 25;
String name = "John";
double salary = 50000.50;
boolean isActive = true;
```

### 4.2 Variable Types

**1. Instance Variables (Non-Static Fields)**
```java
public class Student {
    String name;        // Instance variable
    int age;            // Instance variable
    
    // Each object has its own copy
}
```

**2. Class Variables (Static Fields)**
```java
public class Counter {
    static int count = 0;  // Class variable (shared by all instances)
    
    // All objects share the same variable
}
```

**3. Local Variables**
```java
public void method() {
    int localVar = 10;  // Local variable (only accessible in this method)
    
    if (true) {
        int blockVar = 20;  // Block variable (only in this block)
    }
    // blockVar is not accessible here
}
```

**4. Parameters**
```java
public void method(int param) {  // Parameter
    // param is accessible in this method
}
```

### 4.3 Variable Naming Rules

**Rules**:
1. Must start with letter, underscore (_), or dollar sign ($)
2. Can contain letters, digits, _, $
3. Cannot be a Java keyword
4. Case-sensitive
5. No length limit

**Valid Names**:
```java
int age;
String firstName;
double _salary;
boolean $isActive;
int studentCount;
String user_name;
```

**Invalid Names**:
```java
int 2age;        // Cannot start with digit
String first-name; // Cannot use hyphen
double class;     // 'class' is a keyword
int public;       // 'public' is a keyword
```

### 4.4 Variable Scope

**Local Scope**:
```java
public void method() {
    int x = 10;  // Local to method
    
    if (true) {
        int y = 20;  // Local to if block
        // x is accessible here
    }
    // y is NOT accessible here
}
```

**Instance Scope**:
```java
public class MyClass {
    int instanceVar = 10;  // Accessible in all methods of this class
    
    public void method1() {
        instanceVar = 20;  // Can access
    }
    
    public void method2() {
        instanceVar = 30;  // Can access
    }
}
```

**Class Scope**:
```java
public class MyClass {
    static int classVar = 10;  // Shared by all instances
    
    public void method() {
        classVar = 20;  // Can access
    }
}
```

---

## 5. Constants

### 5.1 Using final Keyword

**Syntax**:
```java
final dataType CONSTANT_NAME = value;
```

**Examples**:
```java
final int MAX_SIZE = 100;
final double PI = 3.14159;
final String COMPANY_NAME = "MyCompany";
final boolean DEBUG_MODE = false;
```

### 5.2 Constant Best Practices

**Naming Convention**: UPPER_SNAKE_CASE
```java
final int MAX_RETRY_COUNT = 3;
final double TAX_RATE = 0.08;
final String DEFAULT_LANGUAGE = "en";
final long MAX_FILE_SIZE = 10485760L;  // 10 MB in bytes
```

**Class Constants**:
```java
public class Constants {
    public static final int MAX_SIZE = 100;
    public static final String DEFAULT_NAME = "Unknown";
    public static final double PI = 3.14159;
    
    // Usage: Constants.MAX_SIZE
}
```

**Enum Constants**:
```java
public enum Status {
    ACTIVE, INACTIVE, PENDING
}

// Usage
Status currentStatus = Status.ACTIVE;
```

---

## 6. Operators

### 6.1 Arithmetic Operators

```java
int a = 10;
int b = 3;

int sum = a + b;        // 13 (Addition)
int diff = a - b;       // 7 (Subtraction)
int product = a * b;    // 30 (Multiplication)
int quotient = a / b;   // 3 (Division - integer)
int remainder = a % b;  // 1 (Modulus - remainder)

// Increment and Decrement
int x = 5;
x++;        // Post-increment: x is now 6
++x;        // Pre-increment: x is now 7
x--;        // Post-decrement: x is now 6
--x;        // Pre-decrement: x is now 5

// Difference between pre and post
int a = 5;
int b = a++;  // b = 5, a = 6 (post-increment)
int c = ++a;  // c = 7, a = 7 (pre-increment)
```

### 6.2 Relational Operators

```java
int a = 10;
int b = 5;

boolean result;

result = a == b;  // false (Equal to)
result = a != b;  // true (Not equal to)
result = a > b;   // true (Greater than)
result = a < b;   // false (Less than)
result = a >= b;  // true (Greater than or equal)
result = a <= b;  // false (Less than or equal)
```

### 6.3 Logical Operators

```java
boolean p = true;
boolean q = false;

boolean result;

result = p && q;  // false (Logical AND)
result = p || q;  // true (Logical OR)
result = !p;      // false (Logical NOT)

// Short-circuit evaluation
int x = 5;
if (x > 0 && x < 10) {  // Second condition not evaluated if first is false
    // ...
}

if (x < 0 || x > 10) {  // Second condition not evaluated if first is true
    // ...
}
```

### 6.4 Assignment Operators

```java
int x = 10;

x += 5;   // x = x + 5;  (x is now 15)
x -= 3;   // x = x - 3;  (x is now 12)
x *= 2;   // x = x * 2;  (x is now 24)
x /= 4;   // x = x / 4;  (x is now 6)
x %= 5;   // x = x % 5;  (x is now 1)
```

### 6.5 Bitwise Operators

```java
int a = 5;   // 0101 in binary
int b = 3;   // 0011 in binary

int result;

result = a & b;   // 0001 (Bitwise AND) = 1
result = a | b;   // 0111 (Bitwise OR) = 7
result = a ^ b;   // 0110 (Bitwise XOR) = 6
result = ~a;      // 1010 (Bitwise NOT) = -6
result = a << 1;  // 1010 (Left shift) = 10
result = a >> 1;  // 0010 (Right shift) = 2
result = a >>> 1; // 0010 (Unsigned right shift) = 2
```

### 6.6 Ternary Operator

```java
// Syntax: condition ? valueIfTrue : valueIfFalse

int age = 20;
String status = (age >= 18) ? "Adult" : "Minor";
// status = "Adult"

int max = (a > b) ? a : b;
// max = larger of a and b

String result = (score >= 60) ? "Pass" : "Fail";
```

### 6.7 Instanceof Operator

```java
// Checks if object is instance of a class

String str = "Hello";
boolean isString = str instanceof String;  // true

Object obj = new String("Test");
boolean isString2 = obj instanceof String;  // true

Integer num = 10;
boolean isString3 = num instanceof String;  // false
```

### 6.8 Operator Precedence

**Order of Operations** (Highest to Lowest):
1. Postfix: `++`, `--`
2. Unary: `+`, `-`, `!`, `~`, `++`, `--`
3. Multiplicative: `*`, `/`, `%`
4. Additive: `+`, `-`
5. Shift: `<<`, `>>`, `>>>`
6. Relational: `<`, `>`, `<=`, `>=`, `instanceof`
7. Equality: `==`, `!=`
8. Bitwise AND: `&`
9. Bitwise XOR: `^`
10. Bitwise OR: `|`
11. Logical AND: `&&`
12. Logical OR: `||`
13. Ternary: `?:`
14. Assignment: `=`, `+=`, `-=`, etc.

**Examples**:
```java
int result = 2 + 3 * 4;        // 14 (multiplication first)
int result2 = (2 + 3) * 4;     // 20 (parentheses first)
boolean flag = a > 0 && b < 10; // && has lower precedence than >
```

---

## 7. Type Conversion and Casting

### 7.1 Implicit Type Conversion (Widening)

**Automatic conversion** from smaller to larger types:

```java
// byte → short → int → long → float → double
byte b = 10;
short s = b;      // byte to short
int i = s;        // short to int
long l = i;       // int to long
float f = l;      // long to float
double d = f;     // float to double

// char → int
char c = 'A';
int ascii = c;    // 65

// int to float/double
int x = 10;
float y = x;      // 10.0
double z = x;     // 10.0
```

### 7.2 Explicit Type Conversion (Narrowing/Casting)

**Manual conversion** from larger to smaller types:

```java
// double → float → long → int → short → byte
double d = 10.5;
float f = (float) d;    // Explicit cast
long l = (long) f;      // 10
int i = (int) l;        // 10
short s = (short) i;    // 10
byte b = (byte) s;      // 10

// int to char
int ascii = 65;
char c = (char) ascii;  // 'A'

// Loss of precision
double pi = 3.14159;
int whole = (int) pi;   // 3 (decimal part lost)
```

### 7.3 String Conversion

**Primitive to String**:
```java
int num = 100;
String str1 = String.valueOf(num);     // "100"
String str2 = Integer.toString(num);   // "100"
String str3 = "" + num;                // "100" (concatenation)

double d = 3.14;
String str4 = String.valueOf(d);       // "3.14"
```

**String to Primitive**:
```java
String str = "100";
int num1 = Integer.parseInt(str);      // 100
int num2 = Integer.valueOf(str);       // 100 (returns Integer object)

String str2 = "3.14";
double d1 = Double.parseDouble(str2);  // 3.14
double d2 = Double.valueOf(str2);      // 3.14 (returns Double object)

String str3 = "true";
boolean flag = Boolean.parseBoolean(str3);  // true
```

### 7.4 Type Promotion in Expressions

```java
// In expressions, smaller types are promoted to larger types
byte b = 10;
int i = 20;
long result = b + i;  // b is promoted to int, then to long

float f = 3.14f;
double d = 2.5;
double result2 = f + d;  // f is promoted to double

// Result type is the largest type in expression
int x = 10;
long y = 20L;
float z = 3.14f;
double result3 = x + y + z;  // All promoted to double
```

### 7.5 Common Casting Scenarios

**1. Division Result**:
```java
int a = 10;
int b = 3;
int result = a / b;           // 3 (integer division)
double result2 = (double) a / b;  // 3.333... (floating-point division)
```

**2. Method Overloading**:
```java
public void method(int x) { }
public void method(double x) { }

method(5);      // Calls method(int)
method(5.0);    // Calls method(double)
method((double) 5);  // Calls method(double)
```

**3. Array Access**:
```java
Object[] arr = {1, 2, 3};
int first = (int) arr[0];  // Cast Object to int
```

---

## Summary: Part 2

### Key Concepts Covered

1. **Data Types**: Primitive (8 types) and Reference types
2. **Primitive Types**: byte, short, int, long, float, double, char, boolean
3. **Variables**: Instance, class, local, parameters
4. **Constants**: Using final keyword, naming conventions
5. **Operators**: Arithmetic, relational, logical, assignment, bitwise, ternary
6. **Type Conversion**: Implicit (widening) and explicit (narrowing/casting)

### Important Points

- **Primitive types** store actual values
- **Reference types** store references to objects
- **Variables** must be declared before use
- **Constants** use `final` keyword and UPPER_SNAKE_CASE
- **Type casting** required for narrowing conversions
- **Operator precedence** determines evaluation order

### Next Steps

**Part 3** will cover:
- Control Flow Statements
- If-Else Statements
- Switch Statements
- Loops (for, while, do-while)
- Break and Continue
- Nested Control Structures

---

**Master data types and operators to build strong Java fundamentals!**


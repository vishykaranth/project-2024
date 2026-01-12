# Core Java Fundamentals and Object-Oriented Principles: Complete Tutorial

## Part 1: Introduction to Java and Basic Concepts

---

## Table of Contents

1. [What is Java?](#1-what-is-java)
2. [Java Platform and Architecture](#2-java-platform-and-architecture)
3. [Setting Up Java Development Environment](#3-setting-up-java-development-environment)
4. [Your First Java Program](#4-your-first-java-program)
5. [Java Program Structure](#5-java-program-structure)
6. [Compilation and Execution](#6-compilation-and-execution)
7. [Java Naming Conventions](#7-java-naming-conventions)
8. [Comments in Java](#8-comments-in-java)

---

## 1. What is Java?

### 1.1 Introduction

**Java** is a high-level, object-oriented programming language developed by Sun Microsystems (now owned by Oracle) in 1995. It was designed to be platform-independent, meaning Java programs can run on any device that has a Java Virtual Machine (JVM).

### 1.2 Key Features of Java

**1. Platform Independent (Write Once, Run Anywhere)**
```java
/**
 * Java code compiled on Windows can run on Linux, Mac, or any platform
 * with JVM installed
 */
```

**2. Object-Oriented**
- Everything in Java is an object
- Supports classes, objects, inheritance, polymorphism, encapsulation, abstraction

**3. Simple**
- Easy to learn and use
- Removed complex features like pointers, operator overloading

**4. Secure**
- No explicit pointers
- Bytecode verification
- Sandbox execution model

**5. Robust**
- Strong memory management
- Exception handling
- Type checking

**6. Multithreaded**
- Built-in support for multithreading
- Concurrent execution of multiple tasks

**7. High Performance**
- Just-In-Time (JIT) compilation
- Optimized bytecode execution

### 1.3 Java Editions

- **Java SE (Standard Edition)**: Core Java platform
- **Java EE (Enterprise Edition)**: Enterprise applications
- **Java ME (Micro Edition)**: Mobile and embedded devices

---

## 2. Java Platform and Architecture

### 2.1 Java Architecture

```
┌─────────────────────────────────────┐
│      Java Application               │
│  (Source Code: .java files)         │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      Java Compiler (javac)          │
│  Compiles .java → .class (bytecode) │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      Java Virtual Machine (JVM)      │
│  - Class Loader                      │
│  - Bytecode Verifier                │
│  - Interpreter/JIT Compiler          │
│  - Runtime Data Areas                │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      Operating System                │
│  (Windows, Linux, Mac, etc.)         │
└─────────────────────────────────────┘
```

### 2.2 Components of Java Platform

**1. JDK (Java Development Kit)**
- Contains JRE + development tools
- Includes: javac (compiler), java (runtime), javadoc, jar, etc.

**2. JRE (Java Runtime Environment)**
- Contains JVM + libraries
- Needed to run Java programs

**3. JVM (Java Virtual Machine)**
- Executes bytecode
- Platform-specific implementation

### 2.3 How Java Works

**Step 1: Write Source Code**
```java
// HelloWorld.java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

**Step 2: Compile**
```bash
javac HelloWorld.java
# Creates HelloWorld.class (bytecode)
```

**Step 3: Execute**
```bash
java HelloWorld
# JVM interprets bytecode and executes
```

---

## 3. Setting Up Java Development Environment

### 3.1 Installing JDK

**Windows**:
1. Download JDK from Oracle or OpenJDK
2. Run installer
3. Set JAVA_HOME environment variable
4. Add to PATH: `%JAVA_HOME%\bin`

**Linux/Mac**:
```bash
# Using package manager
sudo apt-get install openjdk-17-jdk  # Ubuntu/Debian
brew install openjdk@17              # Mac

# Verify installation
java -version
javac -version
```

### 3.2 Setting Up IDE

**Option 1: IntelliJ IDEA**
- Download from jetbrains.com
- Community Edition is free
- Full-featured IDE

**Option 2: Eclipse**
- Download from eclipse.org
- Free and open-source
- Extensive plugin ecosystem

**Option 3: VS Code**
- Install Java Extension Pack
- Lightweight and extensible

### 3.3 Verify Installation

```bash
# Check Java version
java -version
# Output: java version "17.0.1" ...

# Check compiler version
javac -version
# Output: javac 17.0.1

# Check environment variables
echo $JAVA_HOME  # Linux/Mac
echo %JAVA_HOME% # Windows
```

---

## 4. Your First Java Program

### 4.1 Hello World Program

```java
/**
 * HelloWorld.java
 * This is a simple Java program that prints "Hello, World!"
 */
public class HelloWorld {
    /**
     * Main method - entry point of the program
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

### 4.2 Understanding the Program

**1. Class Declaration**
```java
public class HelloWorld
```
- `public`: Access modifier (class is accessible from anywhere)
- `class`: Keyword to define a class
- `HelloWorld`: Name of the class (must match filename)

**2. Main Method**
```java
public static void main(String[] args)
```
- `public`: Accessible from anywhere
- `static`: Belongs to class, not instance (can be called without creating object)
- `void`: Returns nothing
- `main`: Method name (entry point)
- `String[] args`: Command-line arguments

**3. Print Statement**
```java
System.out.println("Hello, World!");
```
- `System`: Class in java.lang package
- `out`: Static PrintStream object
- `println`: Method to print line with newline

### 4.3 Compile and Run

```bash
# Compile
javac HelloWorld.java

# Run
java HelloWorld

# Output
Hello, World!
```

---

## 5. Java Program Structure

### 5.1 Basic Structure

```java
// Package declaration (optional)
package com.example;

// Import statements (optional)
import java.util.Scanner;
import java.util.*;

// Class declaration
public class MyClass {
    
    // Class variables (fields)
    private int number;
    private String name;
    
    // Constructor
    public MyClass() {
        // Initialization code
    }
    
    // Methods
    public void myMethod() {
        // Method body
    }
    
    // Main method (entry point)
    public static void main(String[] args) {
        // Program execution starts here
    }
}
```

### 5.2 Package Declaration

```java
package com.example.myapp;

/**
 * Package:
 * - Organizes classes
 * - Prevents naming conflicts
 * - Follows reverse domain naming
 * - com.example.myapp = com/example/myapp/ directory structure
 */
```

### 5.3 Import Statements

```java
// Import specific class
import java.util.ArrayList;

// Import all classes from package
import java.util.*;

// Static import (for static methods)
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

// Usage
double radius = 5.0;
double area = PI * radius * radius;
double root = sqrt(16);
```

---

## 6. Compilation and Execution

### 6.1 Compilation Process

```bash
# Compile single file
javac HelloWorld.java

# Compile multiple files
javac *.java

# Compile with classpath
javac -cp "lib/*" MyClass.java

# Compile with output directory
javac -d bin src/*.java
```

### 6.2 Execution Process

```bash
# Run compiled class
java HelloWorld

# Run with classpath
java -cp "lib/*:." MyClass

# Run with JVM options
java -Xmx512m -Xms256m MyClass

# Run with system properties
java -Dproperty=value MyClass
```

### 6.3 Common Compilation Errors

**Error 1: Class Not Found**
```bash
# Error: Could not find or load main class HelloWorld
# Solution: Check class name matches filename
```

**Error 2: Package Declaration Mismatch**
```java
// File: com/example/MyClass.java
package com.example;  // Must match directory structure
```

**Error 3: Missing Main Method**
```java
// Error: Main method not found
// Solution: Ensure public static void main(String[] args) exists
```

---

## 7. Java Naming Conventions

### 7.1 Class Names

```java
// PascalCase (First letter of each word capitalized)
public class StudentRecord { }
public class BankAccount { }
public class UserService { }

// Good examples:
MyClass
StudentManagementSystem
PaymentProcessor

// Bad examples:
myClass          // Should be PascalCase
student_record   // Should use camelCase, not snake_case
```

### 7.2 Method and Variable Names

```java
// camelCase (First word lowercase, subsequent words capitalized)
public void calculateTotal() { }
private int studentCount;
String firstName;

// Good examples:
getUserName()
calculateAverage()
isValid()
studentName
totalAmount

// Bad examples:
GetUserName()    // Should start with lowercase
calculate_total() // Should use camelCase
```

### 7.3 Constants

```java
// UPPER_SNAKE_CASE (All uppercase with underscores)
public static final int MAX_SIZE = 100;
public static final String DEFAULT_NAME = "Unknown";
public static final double PI = 3.14159;

// Good examples:
MAX_RETRY_COUNT
DEFAULT_TIMEOUT
API_BASE_URL

// Bad examples:
maxSize        // Should be uppercase
defaultName    // Should be uppercase with underscores
```

### 7.4 Package Names

```java
// lowercase (all lowercase, no underscores)
package com.example.myapp;
package org.apache.commons;
package java.util;

// Good examples:
com.company.project
org.apache.tomcat
java.lang

// Bad examples:
com.Company.Project  // Should be lowercase
com.company_my_app   // Should not use underscores
```

### 7.5 Complete Naming Convention Summary

| Element | Convention | Example |
|---------|-----------|---------|
| Class | PascalCase | `StudentRecord` |
| Interface | PascalCase | `Runnable`, `Comparable` |
| Method | camelCase | `calculateTotal()` |
| Variable | camelCase | `studentName` |
| Constant | UPPER_SNAKE_CASE | `MAX_SIZE` |
| Package | lowercase | `com.example` |
| Enum | PascalCase | `Color`, `Status` |

---

## 8. Comments in Java

### 8.1 Single-Line Comments

```java
// This is a single-line comment
int x = 10; // Comment after code
// int y = 20; // Commented-out code
```

### 8.2 Multi-Line Comments

```java
/*
 * This is a multi-line comment
 * It can span multiple lines
 * Useful for longer explanations
 */

/* This is also valid */
```

### 8.3 JavaDoc Comments

```java
/**
 * This is a JavaDoc comment
 * Used to generate API documentation
 * 
 * @param name The name of the user
 * @param age The age of the user
 * @return A formatted string with user information
 * @throws IllegalArgumentException if name is null or empty
 * @since 1.0
 * @author John Doe
 * @see User
 */
public String formatUser(String name, int age) {
    if (name == null || name.isEmpty()) {
        throw new IllegalArgumentException("Name cannot be null or empty");
    }
    return "Name: " + name + ", Age: " + age;
}
```

### 8.4 JavaDoc Tags

```java
/**
 * Common JavaDoc tags:
 * 
 * @param parameterName Description
 * @return Description of return value
 * @throws ExceptionType Description
 * @since Version number
 * @author Author name
 * @version Version number
 * @see Reference to other class/method
 * @deprecated Explanation
 */
```

### 8.5 Generating JavaDoc

```bash
# Generate JavaDoc documentation
javadoc -d docs -author -version MyClass.java

# Generate for entire package
javadoc -d docs com.example.*

# Generate with HTML output
javadoc -d docs -html MyClass.java
```

---

## Summary: Part 1

### Key Concepts Covered

1. **Java Introduction**: Platform-independent, object-oriented language
2. **Java Architecture**: JDK, JRE, JVM components
3. **Setup**: Installing JDK and IDE
4. **First Program**: Hello World example
5. **Program Structure**: Package, imports, class, methods
6. **Compilation**: javac command
7. **Execution**: java command
8. **Naming Conventions**: Class, method, variable, constant naming
9. **Comments**: Single-line, multi-line, JavaDoc

### Next Steps

**Part 2** will cover:
- Data Types (Primitive and Reference)
- Variables and Constants
- Operators (Arithmetic, Relational, Logical)
- Type Conversion and Casting

---

**Master these fundamentals to build a strong Java foundation!**


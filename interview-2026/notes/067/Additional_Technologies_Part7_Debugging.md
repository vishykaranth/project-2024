# Debugging: Breakpoints, Step Debugging, Remote Debugging

## Overview

Debugging is the process of finding and fixing bugs in software. Modern IDEs provide powerful debugging tools including breakpoints, step debugging, and remote debugging capabilities.

## Debugging Process

```
┌─────────────────────────────────────────────────────────┐
│              Debugging Workflow                         │
└─────────────────────────────────────────────────────────┘

1. Reproduce Bug
    │
    ▼
2. Set Breakpoints
    │
    ▼
3. Start Debugger
    │
    ▼
4. Step Through Code
    │
    ▼
5. Inspect Variables
    │
    ▼
6. Identify Issue
    │
    ▼
7. Fix Bug
    │
    ▼
8. Verify Fix
```

## 1. Breakpoints

### What are Breakpoints?

Breakpoints are markers in code where execution pauses, allowing you to inspect program state.

### Types of Breakpoints

#### 1. Line Breakpoint
```java
public void processUser(String username) {
    System.out.println("Processing: " + username);  // ← Line breakpoint
    User user = findUser(username);
    return user;
}
```

**Usage:**
- Click in gutter (left margin)
- Execution pauses at this line
- Most common type

#### 2. Conditional Breakpoint
```java
public void processUsers(List<User> users) {
    for (User user : users) {
        processUser(user);  // ← Conditional: user.getAge() > 18
    }
}
```

**Usage:**
- Set condition: `user.getAge() > 18`
- Only pauses when condition is true
- Useful for loops

#### 3. Exception Breakpoint
```java
// Pauses when exception is thrown
// Configure: Exception type, caught/uncaught
```

**Usage:**
- Pause on NullPointerException
- Pause on any exception
- Useful for finding error sources

#### 4. Method Breakpoint
```java
public User findUser(String username) {  // ← Method breakpoint
    // Pauses when method is entered/exited
}
```

**Usage:**
- Pause on method entry
- Pause on method exit
- Useful for tracking method calls

#### 5. Field Watchpoint
```java
public class User {
    private String name;  // ← Field watchpoint
    // Pauses when field is accessed/modified
}
```

**Usage:**
- Pause on field read
- Pause on field write
- Useful for tracking field changes

### Breakpoint States

```
┌─────────────────────────────────────────────────────────┐
│              Breakpoint States                         │
└─────────────────────────────────────────────────────────┘

Enabled:    ●  (Active, will pause)
Disabled:   ○  (Inactive, won't pause)
Invalid:    ✗  (Error, needs fixing)
```

## 2. Step Debugging

### Step Commands

```
┌─────────────────────────────────────────────────────────┐
│              Step Debugging Commands                   │
└─────────────────────────────────────────────────────────┘

Step Over (F8):
├─ Execute current line
├─ Don't enter method calls
└─ Move to next line

Step Into (F7):
├─ Execute current line
├─ Enter method calls
└─ Go inside methods

Step Out (Shift+F8):
├─ Execute remaining method
├─ Return to caller
└─ Skip to method end

Resume (F9):
├─ Continue execution
├─ Run to next breakpoint
└─ Or to end if no breakpoints

Run to Cursor (Alt+F9):
├─ Continue to cursor position
└─ Useful for skipping code
```

### Step Debugging Example

```java
public void processOrder(Order order) {
    System.out.println("Processing order");  // ← Breakpoint here
    validateOrder(order);                     // Step Over: skip method
    calculateTotal(order);                    // Step Into: enter method
    applyDiscount(order);                     // Step Over: skip method
    saveOrder(order);                         // Step Into: enter method
}

public void calculateTotal(Order order) {
    double total = 0.0;                       // ← Step Into brings you here
    for (Item item : order.getItems()) {
        total += item.getPrice();
    }
    order.setTotal(total);                    // Step Out: return to caller
}
```

### Debugging Views

```
┌─────────────────────────────────────────────────────────┐
│              Debugging Views                            │
└─────────────────────────────────────────────────────────┘

Variables View:
├─ Local variables
├─ Method parameters
└─ Instance variables

Call Stack:
├─ Current method
├─ Caller methods
└─ Execution path

Breakpoints View:
├─ All breakpoints
├─ Enable/disable
└─ Configure

Console:
├─ Program output
├─ Error messages
└─ Debug messages
```

## 3. Variable Inspection

### Inspecting Variables

```java
public void processUser(User user) {
    String name = user.getName();        // ← Inspect: name
    int age = user.getAge();             // ← Inspect: age
    List<String> roles = user.getRoles(); // ← Inspect: roles
}
```

**Inspection Methods:**
- Hover over variable
- Variables view
- Watch expressions
- Evaluate expression

### Watch Expressions

```java
// Watch expressions evaluate in real-time
user.getAge() > 18
user.getName().length()
order.getTotal() * 0.1
```

**Usage:**
- Monitor expressions
- Evaluate conditions
- Track calculations

### Evaluate Expression

```java
// During debugging, evaluate any expression
user.getName().toUpperCase()
Math.max(user.getAge(), 18)
order.getItems().size()
```

## 4. Remote Debugging

### What is Remote Debugging?

Remote debugging allows you to debug applications running on remote servers or different machines.

### Remote Debugging Setup

#### Java Remote Debugging

**1. Start Application with Debug Options**

```bash
# Java application
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar myapp.jar

# Spring Boot
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 \
     -jar spring-boot-app.jar

# Docker
docker run -p 5005:5005 \
  -e JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" \
  myapp
```

**2. Configure IDE for Remote Debugging**

**IntelliJ IDEA:**
```
Run → Edit Configurations
→ + → Remote JVM Debug
→ Host: localhost
→ Port: 5005
→ Debug mode: Attach
```

**Eclipse:**
```
Run → Debug Configurations
→ Remote Java Application
→ New
→ Host: localhost
→ Port: 5005
```

**VS Code:**
```json
// launch.json
{
    "type": "java",
    "name": "Attach to Remote",
    "request": "attach",
    "hostName": "localhost",
    "port": 5005
}
```

### Remote Debugging Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Remote Debugging Architecture                  │
└─────────────────────────────────────────────────────────┘

IDE (Debugger)
    │
    │ JDWP Protocol
    │
    ▼
Remote JVM
    │
    ├─ Application Code
    ├─ Debug Agent
    └─ Debug Socket (5005)
```

### Remote Debugging Use Cases

1. **Production Debugging**
   - Debug production issues
   - Attach to running application
   - Investigate problems

2. **Docker Containers**
   - Debug containerized apps
   - Connect to container
   - Debug microservices

3. **Remote Servers**
   - Debug on remote machines
   - Connect over network
   - Debug cloud applications

## 5. Debugging Techniques

### 1. Logging Points

```java
// Instead of breakpoint, log and continue
System.out.println("User: " + user.getName());  // Log point
```

**Usage:**
- Log without stopping
- Track execution flow
- Less intrusive than breakpoints

### 2. Conditional Breakpoints

```java
for (User user : users) {
    processUser(user);  // Breakpoint: user.getAge() > 65
}
```

**Usage:**
- Break only when condition met
- Useful for loops
- Filter specific cases

### 3. Exception Breakpoints

```
Configure:
- Exception type: NullPointerException
- Caught/Uncaught: Both
- Suspend: All threads
```

**Usage:**
- Catch exceptions immediately
- Find exception sources
- Debug error handling

### 4. Multi-threaded Debugging

```
Thread View:
├─ Thread 1 (main)
├─ Thread 2 (worker-1)
└─ Thread 3 (worker-2)

Switch between threads
Inspect thread-specific variables
```

## 6. Debugging Best Practices

### 1. Start with Reproducible Cases
- Reproduce bug consistently
- Understand steps to trigger
- Isolate the problem

### 2. Use Appropriate Breakpoints
- Don't set too many
- Use conditional breakpoints
- Remove when done

### 3. Inspect State Systematically
- Check variables in order
- Verify assumptions
- Track data flow

### 4. Use Step Debugging Effectively
- Step Over for known code
- Step Into for unknown code
- Step Out to skip methods

### 5. Document Findings
- Note variable values
- Record execution path
- Document fixes

## 7. Common Debugging Scenarios

### Scenario 1: Null Pointer Exception

```java
public User findUser(String username) {
    User user = userRepository.findByUsername(username);  // ← Breakpoint
    return user.getName();  // NPE if user is null
}
```

**Debugging:**
1. Set breakpoint before NPE
2. Inspect `user` variable
3. Check if null
4. Find why null

### Scenario 2: Infinite Loop

```java
while (condition) {  // ← Breakpoint
    processItem(item);  // Condition never changes
}
```

**Debugging:**
1. Set breakpoint in loop
2. Inspect condition
3. Check why condition doesn't change
4. Step through to find issue

### Scenario 3: Wrong Calculation

```java
public double calculateTotal(List<Item> items) {
    double total = 0.0;  // ← Breakpoint
    for (Item item : items) {
        total += item.getPrice();  // ← Watch: total
    }
    return total;
}
```

**Debugging:**
1. Set breakpoint at start
2. Watch `total` variable
3. Step through loop
4. Verify calculations

## Summary

Debugging:
- **Breakpoints**: Pause execution at specific points
- **Step Debugging**: Control execution flow
- **Remote Debugging**: Debug remote applications
- **Variable Inspection**: Examine program state

**Key Techniques:**
- Line, conditional, exception breakpoints
- Step Over, Into, Out commands
- Variable inspection and watch expressions
- Remote debugging setup

**Best Practices:**
- Reproduce bugs consistently
- Use appropriate breakpoints
- Inspect state systematically
- Document findings
- Remove breakpoints when done

**Remember**: Effective debugging requires understanding the code flow and systematically investigating the problem!

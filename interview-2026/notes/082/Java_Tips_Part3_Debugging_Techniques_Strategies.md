# Java Coding & Debugging Tips - Part 3: Debugging Techniques & Strategies

## Overview

Practical debugging tips and strategies to quickly identify and fix issues in Java applications. These techniques will make you a more effective debugger.

---

## IDE Debugging

### 1. Master Breakpoint Types

**What it does:** Different breakpoints for different scenarios

**Line Breakpoint:**
- Click in gutter to set
- Stops execution at that line
- Most common type

**Conditional Breakpoint:**
- Right-click breakpoint → Edit Breakpoint
- Set condition: `user.getId() > 100`
- Only stops when condition is true

**Logpoint:**
- Right-click breakpoint → More → Log evaluated expression
- Logs without stopping: `User: {user.getName()}`
- No code changes needed

**Exception Breakpoint:**
- Run → View Breakpoints → Add → Java Exception Breakpoint
- Stops when exception is thrown
- Useful for catching unexpected exceptions

**Method Breakpoint:**
- Click on method name
- Stops when method is entered/exited
- Useful for tracking method calls

---

### 2. Use Evaluate Expression

**What it does:** Test code without modifying it

**Usage:**
1. Set breakpoint
2. When paused, open Evaluate Expression (Alt+F8)
3. Type expression: `user.getName().toUpperCase()`
4. See result immediately

**Use cases:**
- Test method calls
- Check variable values
- Call methods on objects
- Test conditions

---

### 3. Use Watches

**What it does:** Monitor variables/expressions continuously

**Usage:**
1. Right-click variable → Add to Watches
2. Or create custom watch: `user.getEmail().contains("@")`
3. Watches update as you step through code

**Benefits:**
- Track multiple variables
- Monitor complex expressions
- See changes in real-time

---

### 4. Use Step Filters

**What it does:** Skip library code while debugging

**Settings → Build, Execution, Deployment → Debugger → Stepping:**

**Add filters:**
```
java.*
javax.*
sun.*
com.sun.*
org.springframework.*
```

**Benefits:**
- Focus on your code
- Faster debugging
- Less noise

---

### 5. Use Return Statement

**What it does:** Force method to return early

**Usage:**
1. Set breakpoint in method
2. When paused, use Evaluate Expression
3. Type: `return newValue;`
4. Continue execution

**Use cases:**
- Test different return values
- Skip problematic code
- Test error handling

---

### 6. Use Drop Frame

**What it does:** Go back to previous method call

**Usage:**
1. While debugging, use Drop Frame button
2. Returns to caller
3. Can re-execute method

**Use cases:**
- Re-test method with different input
- Go back to see what was passed
- Replay execution

---

## Remote Debugging

### 7. Debug Local Application

**What it does:** Attach debugger to running application

**Start application with debug:**
```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar
```

**Or with Maven:**
```bash
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

**IntelliJ setup:**
1. Run → Edit Configurations
2. Add → Remote JVM Debug
3. Port: 5005
4. Click Debug

---

### 8. Debug Docker Container

**What it does:** Debug application running in Docker

**Dockerfile:**
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080 5005
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]
```

**docker-compose.yml:**
```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
      - "5005:5005"
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

**Connect from IDE:**
- Same as remote debugging
- Port forward: `docker port <container> 5005`

---

### 9. Debug Kubernetes Pod

**What it does:** Debug application in Kubernetes

**Port forward:**
```bash
kubectl port-forward pod/user-service-xxx 8080:8080 5005:5005
```

**Or port forward service:**
```bash
kubectl port-forward service/user-service 8080:8080 5005:5005
```

**Connect from IDE:**
- Remote JVM Debug
- Host: localhost
- Port: 5005

**Alternative - Debug container:**
```bash
kubectl exec -it <pod-name> -- /bin/sh
# Then attach debugger
```

---

## Logging for Debugging

### 10. Use Structured Logging

**What it does:** Better log analysis and searching

**With SLF4J:**
```java
@Slf4j
public class UserService {
    
    public User createUser(User user) {
        log.info("Creating user: name={}, email={}", 
            user.getName(), user.getEmail());
        
        try {
            User created = userRepository.save(user);
            log.info("User created: id={}, name={}", 
                created.getId(), created.getName());
            return created;
        } catch (Exception e) {
            log.error("Failed to create user: name={}, email={}", 
                user.getName(), user.getEmail(), e);
            throw e;
        }
    }
}
```

**Benefits:**
- Searchable logs
- Better log aggregation
- Structured data
- Easy filtering

---

### 11. Use Log Levels Strategically

**What it does:** Control log verbosity

**Levels:**
```java
log.trace("Very detailed information");  // Development only
log.debug("Debug information");          // Development/debugging
log.info("General information");        // Production
log.warn("Warning messages");           // Issues that don't stop execution
log.error("Error messages");            // Errors that need attention
```

**Configuration (logback.xml):**
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.example" level="DEBUG"/>
    <logger name="org.springframework" level="INFO"/>
    <logger name="org.hibernate" level="WARN"/>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

---

### 12. Use MDC for Context

**What it does:** Add context to all logs in request

**Usage:**
```java
@RestController
public class UserController {
    
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody UserDTO userDTO) {
        // Add to MDC
        MDC.put("userId", userDTO.getId().toString());
        MDC.put("requestId", UUID.randomUUID().toString());
        
        try {
            User user = userService.createUser(userDTO);
            return ResponseEntity.ok(user);
        } finally {
            // Clean up
            MDC.clear();
        }
    }
}
```

**Logback pattern:**
```xml
<pattern>%d [%X{userId}] [%X{requestId}] %-5level %logger - %msg%n</pattern>
```

**Benefits:**
- Track requests across services
- Filter logs by context
- Better debugging

---

## Command Line Debugging

### 13. Use jps to Find Java Processes

**What it does:** List all Java processes

**Usage:**
```bash
# List processes
jps

# List with main class
jps -l

# List with arguments
jps -v

# List with full command
jps -lmv
```

**Output:**
```
12345 com.example.Application
67890 org.springframework.boot.loader.JarLauncher
```

---

### 14. Use jstack for Thread Dumps

**What it does:** Capture thread state for debugging

**Usage:**
```bash
# Get thread dump
jstack <pid> > threaddump.txt

# Get thread dump with locks
jstack -l <pid> > threaddump.txt

# Get thread dump of all threads
jstack -m <pid> > threaddump.txt
```

**Analyze thread dumps:**
- Look for deadlocks
- Find blocked threads
- Check thread states
- Use tools: fastThread.io, Thread Dump Analyzer

**Common issues:**
- Deadlocks: Look for "Found deadlock"
- Blocked threads: Look for "BLOCKED" state
- High CPU: Look for threads in "RUNNABLE" state

---

### 15. Use jmap for Memory Analysis

**What it does:** Analyze heap memory

**Usage:**
```bash
# Generate heap dump
jmap -dump:format=b,file=heap.hprof <pid>

# Get heap summary
jmap -heap <pid>

# Get histogram of objects
jmap -histo <pid>

# Get histogram of live objects only
jmap -histo:live <pid>
```

**Analyze with:**
- Eclipse MAT (Memory Analyzer Tool)
- VisualVM
- jhat (built-in, basic)

**Common analysis:**
- Find memory leaks
- Identify large objects
- Check object counts

---

### 16. Use jstat for GC Monitoring

**What it does:** Monitor garbage collection

**Usage:**
```bash
# GC statistics every 1 second, 10 times
jstat -gc <pid> 1000 10

# GC capacity
jstat -gccapacity <pid>

# GC utilization
jstat -gcutil <pid> 1000
```

**Output columns:**
- S0C, S1C: Survivor space capacity
- S0U, S1U: Survivor space used
- EC, EU: Eden space capacity/used
- OC, OU: Old space capacity/used
- YGC, YGCT: Young GC count/time
- FGC, FGCT: Full GC count/time

---

### 17. Use jcmd for JVM Commands

**What it does:** Execute diagnostic commands

**Usage:**
```bash
# List available commands
jcmd <pid> help

# Generate thread dump
jcmd <pid> Thread.print

# Generate heap dump
jcmd <pid> GC.run_finalization
jcmd <pid> VM.native_memory summary

# Get system properties
jcmd <pid> VM.system_properties

# Get VM flags
jcmd <pid> VM.flags
```

**Benefits:**
- Single tool for multiple operations
- More features than individual tools
- Better integration

---

## Application Debugging

### 18. Use Spring Boot Actuator for Debugging

**What it does:** Expose application internals

**Add dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

**Configuration:**
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,beans,configprops,loggers
  endpoint:
    health:
      show-details: always
```

**Endpoints:**
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment variables
- `/actuator/beans` - Spring beans
- `/actuator/configprops` - Configuration properties
- `/actuator/loggers` - Logger configuration

---

### 19. Use Spring Boot DevTools

**What it does:** Automatic restart and live reload

**Add dependency:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

**Features:**
- Automatic restart on code changes
- Live reload (browser refresh)
- Property defaults for development
- Remote debugging support

**Configuration:**
```yaml
spring:
  devtools:
    restart:
      enabled: true
    livereload:
      enabled: true
```

---

### 20. Use Conditional Logging

**What it does:** Log only when needed

**Usage:**
```java
if (log.isDebugEnabled()) {
    log.debug("Expensive operation: {}", expensiveOperation());
}

// Or use SLF4J which does this automatically
log.debug("Message: {}", expensiveOperation()); // Only evaluated if DEBUG enabled
```

**Benefits:**
- Avoid expensive string operations
- Better performance
- Conditional execution

---

## Debugging Strategies

### 21. Use Binary Search for Finding Bugs

**What it does:** Systematically narrow down problem area

**Process:**
1. Identify symptom
2. Find middle point in code path
3. Add logging/breakpoint
4. Determine if bug is before or after
5. Repeat with smaller area

**Example:**
```
Symptom: User creation fails
1. Check controller → OK
2. Check service → OK  
3. Check repository → ERROR found
4. Narrow to repository method
5. Find exact line
```

---

### 22. Use Rubber Duck Debugging

**What it does:** Explain problem to find solution

**Process:**
1. Explain problem out loud (or to rubber duck)
2. Walk through code step by step
3. Often find issue while explaining

**Benefits:**
- Forces clear thinking
- Reveals assumptions
- Often finds solution quickly

---

### 23. Use Git Bisect for Finding When Bug Was Introduced

**What it does:** Find commit that introduced bug

**Usage:**
```bash
# Start bisect
git bisect start

# Mark current commit as bad
git bisect bad

# Mark known good commit
git bisect good <commit-hash>

# Test and mark good/bad
git bisect good  # or git bisect bad

# Continue until found
git bisect reset  # When done
```

**Benefits:**
- Find exact commit
- Understand what changed
- Faster than manual search

---

### 24. Use Test Cases to Reproduce Bugs

**What it does:** Create test that reproduces bug

**Process:**
1. Write test that fails (reproduces bug)
2. Fix bug
3. Test passes
4. Keep test as regression test

**Example:**
```java
@Test
void shouldHandleNullUser() {
    // This test reproduces the bug
    assertThrows(NullPointerException.class, () -> {
        userService.processUser(null);
    });
}

// After fix
@Test
void shouldHandleNullUser() {
    assertThrows(IllegalArgumentException.class, () -> {
        userService.processUser(null);
    });
}
```

---

### 25. Use Debugging Checklist

**What it does:** Systematic approach to debugging

**Checklist:**
- [ ] Reproduce the issue
- [ ] Check logs for errors
- [ ] Verify input data
- [ ] Check database state
- [ ] Verify configuration
- [ ] Check dependencies
- [ ] Review recent changes
- [ ] Check environment differences
- [ ] Use debugger
- [ ] Add logging
- [ ] Simplify to isolate
- [ ] Test hypothesis

---

## Summary

These 25 tips cover comprehensive debugging strategies:

1. **IDE Debugging:** Breakpoints, watches, step filters
2. **Remote Debugging:** Local, Docker, Kubernetes
3. **Logging:** Structured logging, MDC, log levels
4. **Command Line:** jps, jstack, jmap, jstat, jcmd
5. **Spring Tools:** Actuator, DevTools
6. **Strategies:** Binary search, rubber duck, git bisect

**Next Steps:**
- Master IDE debugging features
- Set up remote debugging
- Improve logging strategy
- Learn command-line tools
- Develop systematic debugging approach

---

*Continue to Part 4: Performance & Optimization*

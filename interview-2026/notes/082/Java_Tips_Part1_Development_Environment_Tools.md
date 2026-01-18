# Java Coding & Debugging Tips - Part 1: Development Environment & Tools

## Overview

Practical, actionable tips to improve your Java development environment and tooling setup. These tips can be implemented immediately to boost productivity.

---

## Java Version Management

### 1. Use SDKMAN! for Java Version Management

**What it does:** Manages multiple Java versions easily

**Installation:**
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"
```

**Usage:**
```bash
# List available Java versions
sdk list java

# Install specific version
sdk install java 17.0.8-tem
sdk install java 11.0.20-tem
sdk install java 21.0.1-tem

# Switch versions
sdk use java 17.0.8-tem

# Set default version
sdk default java 17.0.8-tem

# Check current version
java -version
```

**Benefits:**
- Easy switching between Java versions
- No manual PATH management
- Works with multiple projects requiring different versions
- Also manages Maven, Gradle, and other Java tools

---

### 2. Use jEnv for Java Version Management (Alternative)

**What it does:** Lightweight alternative to SDKMAN!

**Installation:**
```bash
# macOS
brew install jenv

# Linux
git clone https://github.com/jenv/jenv.git ~/.jenv
```

**Usage:**
```bash
# Add Java versions
jenv add /Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home
jenv add /Library/Java/JavaVirtualMachines/jdk-11.jdk/Contents/Home

# Set local version for project
jenv local 17.0

# Set global version
jenv global 17.0

# List versions
jenv versions
```

---

## Build Tools Optimization

### 3. Use Maven Wrapper for Consistent Builds

**What it does:** Ensures everyone uses the same Maven version

**Setup:**
```bash
# Generate wrapper
mvn wrapper:wrapper -DmavenVersion=3.9.4

# This creates:
# - .mvn/wrapper/maven-wrapper.jar
# - .mvn/wrapper/maven-wrapper.properties
# - mvnw (Unix)
# - mvnw.cmd (Windows)
```

**Usage:**
```bash
# Instead of: mvn clean install
./mvnw clean install

# Windows
mvnw.cmd clean install
```

**Benefits:**
- No need to install Maven globally
- Same Maven version for all developers
- Works in CI/CD pipelines
- Version controlled in project

---

### 4. Use Gradle Wrapper for Consistent Builds

**What it does:** Ensures everyone uses the same Gradle version

**Setup:**
```bash
# Generate wrapper
gradle wrapper --gradle-version 8.3

# Or in existing project
./gradlew wrapper --gradle-version 8.3
```

**Usage:**
```bash
# Instead of: gradle build
./gradlew build

# Windows
gradlew.bat build
```

**Update wrapper:**
```bash
./gradlew wrapper --gradle-version 8.4
```

---

### 5. Optimize Maven Settings for Speed

**What it does:** Speeds up Maven builds significantly

**Edit ~/.m2/settings.xml:**
```xml
<settings>
  <!-- Use local repository in home directory -->
  <localRepository>${user.home}/.m2/repository</localRepository>
  
  <!-- Parallel builds -->
  <profiles>
    <profile>
      <id>parallel-build</id>
      <properties>
        <maven.compiler.fork>true</maven.compiler.fork>
        <maven.compiler.executable>javac</maven.compiler.executable>
      </properties>
    </profile>
  </profiles>
  
  <!-- Active profiles -->
  <activeProfiles>
    <activeProfile>parallel-build</activeProfile>
  </activeProfiles>
</settings>
```

**Build with parallel execution:**
```bash
mvn clean install -T 4  # Use 4 threads
mvn clean install -T 1C # Use 1 thread per CPU core
```

**Benefits:**
- Faster builds
- Better CPU utilization
- Reduced build time

---

### 6. Optimize Gradle for Speed

**What it does:** Makes Gradle builds faster

**Edit ~/.gradle/gradle.properties:**
```properties
# Enable daemon (runs in background)
org.gradle.daemon=true

# Parallel builds
org.gradle.parallel=true

# Build cache
org.gradle.caching=true

# Configure on demand
org.gradle.configureondemand=true

# JVM memory settings
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError

# Enable file system watching (Gradle 7.0+)
org.gradle.vfs.watch=true
```

**Build with parallel execution:**
```bash
./gradlew build --parallel --max-workers=4
```

---

## IDE Configuration

### 7. Configure IntelliJ IDEA Memory Settings

**What it does:** Prevents IDE slowdowns and crashes

**Help → Edit Custom VM Options:**
```properties
-Xms2g
-Xmx4g
-XX:ReservedCodeCacheSize=512m
-XX:+UseG1GC
-XX:SoftRefLRUPolicyMSPerMB=50
-ea
```

**Benefits:**
- Faster IDE performance
- Less freezing
- Better for large projects
- Handles multiple projects better

---

### 8. Use IntelliJ IDEA Live Templates

**What it does:** Speeds up code writing with shortcuts

**Settings → Editor → Live Templates:**

**Create template for logger:**
```
Abbreviation: log
Template text:
private static final Logger log = LoggerFactory.getLogger($CLASS_NAME$.class);
```

**Create template for test method:**
```
Abbreviation: testm
Template text:
@Test
void $METHOD_NAME$() {
    // Given
    $END$
    
    // When
    
    // Then
    
}
```

**Usage:** Type abbreviation and press Tab

---

### 9. Configure IntelliJ Code Style

**What it does:** Consistent code formatting across team

**Settings → Editor → Code Style → Java:**

**Import Google Java Style:**
1. Download from: https://github.com/google/styleguide
2. Settings → Editor → Code Style → Scheme → Import Scheme
3. Select `intellij-java-google-style.xml`

**Or use EditorConfig:**
```ini
# .editorconfig
root = true

[*.java]
indent_style = space
indent_size = 2
continuation_indent_size = 4
```

---

### 10. Use IntelliJ Structural Search & Replace

**What it does:** Find and replace code patterns

**Edit → Find → Search Structurally:**

**Example: Find all System.out.println:**
```
Template: System.out.println($EXPR$);
```

**Replace with logger:**
```
Template: log.info("$EXPR$");
```

**Benefits:**
- Refactor code patterns
- Find code smells
- Consistent replacements

---

## Command Line Tools

### 11. Use jq for JSON Processing

**What it does:** Parse and manipulate JSON from command line

**Installation:**
```bash
# macOS
brew install jq

# Linux
sudo apt-get install jq
```

**Usage:**
```bash
# Pretty print JSON
cat response.json | jq .

# Extract field
cat response.json | jq '.user.name'

# Filter array
cat users.json | jq '.[] | select(.age > 18)'

# Transform data
cat data.json | jq '.users[] | {name, email}'
```

**Use cases:**
- API response inspection
- Log analysis
- Configuration file manipulation
- Testing

---

### 12. Use HTTPie for API Testing

**What it does:** Better alternative to curl for API testing

**Installation:**
```bash
# macOS
brew install httpie

# Linux
sudo apt-get install httpie
```

**Usage:**
```bash
# GET request
http GET http://localhost:8080/api/users

# POST request
http POST http://localhost:8080/api/users name=John email=john@example.com

# With headers
http GET http://localhost:8080/api/users Authorization:"Bearer token123"

# Save response
http GET http://localhost:8080/api/users > response.json

# Pretty print JSON response automatically
```

**Benefits:**
- Better syntax than curl
- Automatic JSON formatting
- Easier to read
- Better for REST APIs

---

### 13. Use jps to List Java Processes

**What it does:** Quick way to see running Java processes

**Usage:**
```bash
# List all Java processes
jps

# List with main class names
jps -l

# List with JVM arguments
jps -v

# List with full command line
jps -lmv
```

**Output:**
```
12345 MyApplication
67890 com.example.Service
```

**Use cases:**
- Find process IDs
- Check if application is running
- Debug multiple JVM instances

---

### 14. Use jstack for Thread Dumps

**What it does:** Capture thread dumps for debugging

**Usage:**
```bash
# Get thread dump
jstack <pid> > threaddump.txt

# Get thread dump with locks
jstack -l <pid> > threaddump.txt

# Find PID first
jps -l
jstack <pid>
```

**Analyze thread dumps:**
- Look for deadlocks
- Identify blocked threads
- Check thread states
- Use tools like fastThread.io

---

### 15. Use jmap for Heap Dumps

**What it does:** Capture heap dumps for memory analysis

**Usage:**
```bash
# Generate heap dump
jmap -dump:format=b,file=heap.hprof <pid>

# Get heap summary
jmap -heap <pid>

# Get histogram of objects
jmap -histo <pid>
```

**Analyze with:**
- Eclipse MAT (Memory Analyzer Tool)
- VisualVM
- jhat (built-in)

---

## Version Control

### 16. Use .gitignore for Java Projects

**What it does:** Prevents committing unnecessary files

**Create .gitignore:**
```gitignore
# Compiled class files
*.class

# Log files
*.log

# Package files
*.jar
*.war
*.ear
*.nar

# Maven
target/
pom.xml.tag
pom.xml.releaseBackup
.mvn/timing.properties

# Gradle
.gradle
build/
!gradle/wrapper/gradle-wrapper.jar

# IDE
.idea/
*.iml
*.iws
*.ipr
.vscode/
.classpath
.project
.settings/

# OS
.DS_Store
Thumbs.db

# Application specific
application-local.yml
.env
```

---

### 17. Use Git Hooks for Code Quality

**What it does:** Automatically check code before commit

**Create .git/hooks/pre-commit:**
```bash
#!/bin/sh

# Run tests
mvn test
if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

# Check code style
mvn checkstyle:check
if [ $? -ne 0 ]; then
    echo "Code style check failed. Commit aborted."
    exit 1
fi

# Format code
mvn fmt:format
git add .

exit 0
```

**Make executable:**
```bash
chmod +x .git/hooks/pre-commit
```

---

## Dependency Management

### 18. Use Maven Dependency Plugin

**What it does:** Analyze and manage dependencies

**Usage:**
```bash
# Show dependency tree
mvn dependency:tree

# Show dependency tree for specific artifact
mvn dependency:tree -Dincludes=org.springframework:spring-core

# Analyze dependencies
mvn dependency:analyze

# Copy dependencies to directory
mvn dependency:copy-dependencies

# List all dependencies
mvn dependency:list
```

**Find unused dependencies:**
```bash
mvn dependency:analyze
```

---

### 19. Use Gradle Dependency Insights

**What it does:** Understand dependency resolution

**Usage:**
```bash
# Show dependency tree
./gradlew dependencies

# Show dependency tree for specific configuration
./gradlew dependencies --configuration runtimeClasspath

# Show why a dependency is included
./gradlew dependencyInsight --dependency spring-core

# Show dependency updates
./gradlew dependencyUpdates
```

---

### 20. Use Dependabot or Renovate

**What it does:** Automatically update dependencies

**GitHub Dependabot (.github/dependabot.yml):**
```yaml
version: 2
updates:
  - package-ecosystem: "maven"
    directory: "/"
    schedule:
      interval: "weekly"
    open-pull-requests-limit: 10
```

**Benefits:**
- Automatic security updates
- Stay current with dependencies
- Reduce manual work
- Security vulnerability alerts

---

## Environment Setup

### 21. Use direnv for Environment Variables

**What it does:** Automatically load environment variables per project

**Installation:**
```bash
# macOS
brew install direnv

# Linux
sudo apt-get install direnv
```

**Setup:**
```bash
# Add to ~/.zshrc or ~/.bashrc
eval "$(direnv hook zsh)"
```

**Create .envrc in project:**
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/mydb
export REDIS_HOST=localhost
export SPRING_PROFILES_ACTIVE=dev
```

**Usage:**
```bash
# Allow direnv to load
direnv allow

# Variables automatically loaded when entering directory
```

---

### 22. Use asdf for Tool Version Management

**What it does:** Manage versions of multiple tools

**Installation:**
```bash
git clone https://github.com/asdf-vm/asdf.git ~/.asdf --branch v0.13.1
```

**Usage:**
```bash
# Install Java plugin
asdf plugin add java

# Install Java version
asdf install java temurin-17.0.8+10

# Set local version
asdf local java temurin-17.0.8+10

# Set global version
asdf global java temurin-17.0.8+10
```

**Supports:** Java, Maven, Gradle, Node.js, Python, and more

---

## Quick Reference

### Essential Commands

```bash
# Java version
java -version
javac -version

# Maven
mvn --version
./mvnw clean install

# Gradle
./gradlew --version
./gradlew build

# Process management
jps -l
jstack <pid>
jmap -heap <pid>

# API testing
http GET http://localhost:8080/api/users

# JSON processing
cat file.json | jq .
```

### Essential Files

```
.gitignore          # Git ignore rules
.editorconfig       # Code style
.envrc              # Environment variables (direnv)
.mvn/wrapper/       # Maven wrapper
gradle/wrapper/     # Gradle wrapper
.gradle.properties  # Gradle settings
```

---

## Summary

These 22 tips focus on setting up an optimal development environment:

1. **Version Management:** SDKMAN!, jEnv, asdf
2. **Build Tools:** Maven/Gradle wrappers, optimization
3. **IDE:** Memory settings, templates, code style
4. **CLI Tools:** jq, HTTPie, jps, jstack, jmap
5. **Version Control:** .gitignore, git hooks
6. **Dependencies:** Analysis, updates
7. **Environment:** direnv, environment variables

**Next Steps:**
- Implement 2-3 tips immediately
- Gradually adopt others
- Customize for your workflow
- Share with your team

---

*Continue to Part 2: Coding Best Practices & Patterns*

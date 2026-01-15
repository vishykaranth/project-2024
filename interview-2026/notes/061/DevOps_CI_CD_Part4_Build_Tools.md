# Build Tools: Maven, Gradle, Dependency Management

## Overview

Build tools automate the process of compiling source code, managing dependencies, running tests, and packaging applications. They provide standardized build processes, dependency resolution, and project structure management.

## Build Tool Ecosystem

```
┌─────────────────────────────────────────────────────────┐
│              Build Tools Landscape                      │
└─────────────────────────────────────────────────────────┘

Java:
  ├─► Maven (XML-based, declarative)
  ├─► Gradle (Groovy/Kotlin DSL, flexible)
  └─► Ant (Legacy, procedural)

JavaScript:
  ├─► npm (Node Package Manager)
  ├─► yarn (Fast, reliable)
  └─► pnpm (Efficient disk usage)

Python:
  ├─► pip (Package installer)
  ├─► Poetry (Dependency management)
  └─► setuptools (Build tool)

.NET:
  ├─► MSBuild (Microsoft build)
  └─► dotnet CLI (Cross-platform)
```

## 1. Maven

### Maven Overview

Maven is a build automation and project management tool primarily used for Java projects. It uses a Project Object Model (POM) file to manage project configuration, dependencies, and build lifecycle.

### Maven Project Structure

```
┌─────────────────────────────────────────────────────────┐
│              Maven Standard Directory Layout            │
└─────────────────────────────────────────────────────────┘

project-root/
├── pom.xml                 ← Project configuration
├── src/
│   ├── main/
│   │   ├── java/          ← Source code
│   │   └── resources/     ← Resources (config files)
│   └── test/
│       ├── java/          ← Test code
│       └── resources/     ← Test resources
└── target/                 ← Build output (generated)
    ├── classes/
    ├── test-classes/
    └── *.jar
```

### Maven POM (pom.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Project Coordinates -->
    <groupId>com.example</groupId>
    <artifactId>my-app</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    
    <!-- Dependencies -->
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>5.3.21</version>
        </dependency>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.13.2</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <!-- Build Configuration -->
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.11.0</version>
                <configuration>
                    <source>17</source>
                    <target>17</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Maven Build Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│              Maven Build Lifecycle                     │
└─────────────────────────────────────────────────────────┘

Default Lifecycle Phases:

1. validate      → Validate project
2. compile       → Compile source code
3. test          → Run unit tests
4. package       → Package into JAR/WAR
5. verify        → Run integration tests
6. install       → Install to local repository
7. deploy        → Deploy to remote repository

Common Commands:
  mvn clean          → Clean target directory
  mvn compile        → Compile code
  mvn test           → Run tests
  mvn package        → Create JAR/WAR
  mvn install        → Install to local repo
  mvn deploy         → Deploy to remote repo
```

### Maven Dependency Management

```
┌─────────────────────────────────────────────────────────┐
│         Maven Dependency Resolution                    │
└─────────────────────────────────────────────────────────┘

Dependency Declaration:
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>5.3.21</version>
  </dependency>

Dependency Resolution:
  1. Check local repository (~/.m2/repository)
  2. Check remote repositories (Maven Central)
  3. Download if not found
  4. Resolve transitive dependencies
  5. Build dependency tree
```

### Maven Dependency Scopes

| Scope | Description | Example |
|-------|-------------|---------|
| **compile** | Default, available in all classpaths | Spring Core |
| **provided** | Provided by JDK/container | Servlet API |
| **runtime** | Needed at runtime only | JDBC Driver |
| **test** | Only for testing | JUnit |
| **system** | System path, not from repo | Local JAR |
| **import** | Import dependency management | BOM files |

## 2. Gradle

### Gradle Overview

Gradle is a build automation tool that combines the best features of Ant and Maven. It uses a Groovy or Kotlin DSL for build scripts, providing flexibility and powerful features.

### Gradle Project Structure

```
┌─────────────────────────────────────────────────────────┐
│              Gradle Project Structure                   │
└─────────────────────────────────────────────────────────┘

project-root/
├── build.gradle          ← Build script (Groovy/Kotlin)
├── settings.gradle       ← Project settings
├── gradle.properties     ← Gradle properties
├── src/
│   ├── main/
│   │   ├── java/
│   │   └── resources/
│   └── test/
│       ├── java/
│       └── resources/
└── build/                ← Build output
```

### Gradle Build Script (build.gradle)

```groovy
plugins {
    id 'java'
    id 'application'
}

group = 'com.example'
version = '1.0.0'
sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    // Implementation dependencies
    implementation 'org.springframework:spring-core:5.3.21'
    implementation 'com.google.guava:guava:31.1-jre'
    
    // Test dependencies
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'org.mockito:mockito-core:4.6.1'
}

application {
    mainClass = 'com.example.Main'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### Gradle Build Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│              Gradle Build Lifecycle                    │
└─────────────────────────────────────────────────────────┘

1. Initialization
   └─► Load settings.gradle, identify projects

2. Configuration
   └─► Execute build.gradle, create task graph

3. Execution
   └─► Execute tasks in dependency order

Common Tasks:
  gradle build      → Build project
  gradle test       → Run tests
  gradle clean       → Clean build directory
  gradle run         → Run application
  gradle jar         → Create JAR
```

### Gradle vs Maven

| Feature | Maven | Gradle |
|--------|-------|--------|
| **Configuration** | XML (pom.xml) | Groovy/Kotlin DSL |
| **Flexibility** | Less flexible | Highly flexible |
| **Performance** | Slower | Faster (incremental builds) |
| **Learning Curve** | Easier | Steeper |
| **Dependency Management** | Good | Excellent |
| **Plugin Ecosystem** | Large | Growing |
| **Build Speed** | Moderate | Fast |

## Dependency Management

### Dependency Resolution

```
┌─────────────────────────────────────────────────────────┐
│         Dependency Resolution Process                   │
└─────────────────────────────────────────────────────────┘

1. Declare Dependency
   └─► In pom.xml or build.gradle

2. Resolve from Repository
   ├─► Local repository (cache)
   ├─► Remote repository (Maven Central)
   └─► Corporate repository (Nexus, Artifactory)

3. Download Dependencies
   └─► Download JARs and metadata

4. Resolve Transitive Dependencies
   └─► Dependencies of dependencies

5. Build Dependency Tree
   └─► Complete dependency graph

6. Resolve Conflicts
   └─► Handle version conflicts
```

### Dependency Conflict Resolution

```
┌─────────────────────────────────────────────────────────┐
│         Dependency Conflict Resolution                 │
└─────────────────────────────────────────────────────────┘

Scenario:
  Project depends on:
    - Library A v1.0 (requires Library C v1.0)
    - Library B v2.0 (requires Library C v2.0)

Resolution Strategies:
  1. Nearest wins (Maven default)
     └─► Use version closest in dependency tree
  
  2. Latest version (Gradle default)
     └─► Use highest version
  
  3. Explicit override
     └─► Manually specify version
```

### Dependency Management Best Practices

1. **Use Version Ranges Carefully**
   ```xml
   <!-- BAD: Too broad -->
   <version>[1.0,)</version>
   
   <!-- GOOD: Specific version -->
   <version>1.2.3</version>
   ```

2. **Manage Transitive Dependencies**
   ```xml
   <!-- Exclude unwanted transitive dependency -->
   <dependency>
       <groupId>com.example</groupId>
       <artifactId>library</artifactId>
       <exclusions>
           <exclusion>
               <groupId>org.unwanted</groupId>
               <artifactId>dependency</artifactId>
           </exclusion>
       </exclusions>
   </dependency>
   ```

3. **Use Dependency Management (BOM)**
   ```xml
   <!-- Spring Boot BOM -->
   <dependencyManagement>
       <dependencies>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-dependencies</artifactId>
               <version>2.7.0</version>
               <type>pom</type>
               <scope>import</scope>
           </dependency>
       </dependencies>
   </dependencyManagement>
   ```

4. **Keep Dependencies Updated**
   - Regular security updates
   - Monitor for vulnerabilities
   - Test updates before applying

## Build Tool Integration with CI/CD

### Maven in CI/CD

```yaml
# GitHub Actions
- name: Build with Maven
  run: mvn clean package

- name: Run Tests
  run: mvn test

- name: Upload Artifacts
  uses: actions/upload-artifact@v3
  with:
    name: jar
    path: target/*.jar
```

### Gradle in CI/CD

```yaml
# GitHub Actions
- name: Build with Gradle
  run: ./gradlew build

- name: Run Tests
  run: ./gradlew test

- name: Upload Artifacts
  uses: actions/upload-artifact@v3
  with:
    name: jar
    path: build/libs/*.jar
```

## Build Optimization

### 1. Caching

```
Maven:
  - Cache ~/.m2/repository
  - Cache target/ directory

Gradle:
  - Cache ~/.gradle/caches
  - Enable build cache
  - Use Gradle daemon
```

### 2. Parallel Execution

```
Maven:
  mvn -T 4 clean install  # 4 threads

Gradle:
  ./gradlew build --parallel
  ./gradlew build --max-workers=4
```

### 3. Incremental Builds

```
Gradle:
  - Automatic incremental builds
  - Only rebuild changed modules
  - Faster subsequent builds
```

## Summary

Build Tools:
- **Maven**: XML-based, declarative, widely used
- **Gradle**: DSL-based, flexible, fast
- **Dependency Management**: Automatic resolution, conflict handling

**Key Features:**
- Standardized project structure
- Dependency management
- Build lifecycle
- Plugin ecosystem
- CI/CD integration

**Best Practices:**
- Use specific versions
- Manage transitive dependencies
- Keep dependencies updated
- Optimize builds with caching
- Use parallel execution

**Remember**: Choose the right tool for your project needs and team expertise!

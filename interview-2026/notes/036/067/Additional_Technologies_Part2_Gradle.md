# Gradle: Build Scripts, Dependency Management, Plugins

## Overview

Gradle is a modern build automation tool that combines the flexibility of Ant with the convention-over-configuration of Maven. It uses Groovy or Kotlin DSL for build scripts and provides powerful dependency management and plugin system.

## Gradle Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Gradle Architecture                        │
└─────────────────────────────────────────────────────────┘

Developer
    │
    ▼
┌─────────────────┐
│ build.gradle    │  ← Build script (Groovy/Kotlin DSL)
│ settings.gradle │  ← Project settings
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Gradle Daemon   │  ← Background process (fast builds)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Tasks           │  ← Build tasks (compile, test, etc.)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Dependency      │  ← Resolves dependencies
│ Resolution      │
└─────────────────┘
```

## 1. Build Scripts

### build.gradle Structure

```groovy
// build.gradle (Groovy DSL)

// Plugins
plugins {
    id 'java'
    id 'application'
    id 'org.springframework.boot' version '2.7.0'
}

// Project Information
group = 'com.example'
version = '1.0.0'
sourceCompatibility = '17'

// Repositories
repositories {
    mavenCentral()
    maven {
        url 'https://repo.example.com'
    }
}

// Dependencies
dependencies {
    implementation 'org.springframework:spring-core:5.3.21'
    testImplementation 'junit:junit:4.13.2'
}

// Application Configuration
application {
    mainClass = 'com.example.Main'
}

// Tasks
tasks.named('test') {
    useJUnitPlatform()
}
```

### Kotlin DSL (build.gradle.kts)

```kotlin
// build.gradle.kts (Kotlin DSL)

plugins {
    java
    application
    id("org.springframework.boot") version "2.7.0"
}

group = "com.example"
version = "1.0.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework:spring-core:5.3.21")
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.example.Main")
}

tasks.test {
    useJUnitPlatform()
}
```

### settings.gradle

```groovy
// settings.gradle

rootProject.name = 'my-project'

// Multi-module projects
include 'module-api'
include 'module-service'
include 'module-web'

// Project structure
project(':module-api').projectDir = file('api')
project(':module-service').projectDir = file('service')
```

## 2. Gradle Tasks

### Task Definition

```groovy
// Simple task
task hello {
    doLast {
        println 'Hello, Gradle!'
    }
}

// Task with configuration
task compile(type: JavaCompile) {
    source = fileTree('src/main/java')
    destinationDir = file('build/classes')
    classpath = configurations.compile
}

// Task dependencies
task build {
    dependsOn compile
    doLast {
        println 'Building...'
    }
}
```

### Built-in Tasks

```
┌─────────────────────────────────────────────────────────┐
│              Gradle Built-in Tasks                      │
└─────────────────────────────────────────────────────────┘

Java Plugin Tasks:
├─ compileJava          ← Compile main sources
├─ compileTestJava      ← Compile test sources
├─ processResources     ← Process resources
├─ classes              ← Assemble classes
├─ jar                  ← Create JAR
├─ test                 ← Run tests
├─ javadoc              ← Generate Javadoc
└─ clean                ← Clean build directory

Application Plugin Tasks:
├─ run                  ← Run application
├─ distZip              ← Create distribution ZIP
└─ installDist          ← Install distribution
```

### Task Execution

```bash
# List all tasks
gradle tasks

# Execute task
gradle compileJava
gradle test
gradle build

# Execute multiple tasks
gradle clean build

# Skip task
gradle build -x test

# Run with info
gradle build --info
gradle build --debug
```

### Task Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│              Task Execution Lifecycle                    │
└─────────────────────────────────────────────────────────┘

Task Execution
    │
    ▼
Configuration Phase
    ├─ Evaluate build scripts
    ├─ Configure tasks
    └─ Build task graph
    │
    ▼
Execution Phase
    ├─ Execute doFirst actions
    ├─ Execute task actions
    └─ Execute doLast actions
```

## 3. Dependency Management

### Dependency Configurations

```groovy
dependencies {
    // Compile-time dependencies
    implementation 'org.springframework:spring-core:5.3.21'
    
    // Compile-time only (not at runtime)
    compileOnly 'javax.servlet:servlet-api:2.5'
    
    // Runtime dependencies
    runtimeOnly 'mysql:mysql-connector-java:8.0.33'
    
    // Test dependencies
    testImplementation 'junit:junit:4.13.2'
    testRuntimeOnly 'org.hamcrest:hamcrest:2.2'
    
    // Annotation processors
    annotationProcessor 'org.projectlombok:lombok:1.18.24'
}
```

### Dependency Scopes

| Configuration | Description | Included in |
|---------------|-------------|-------------|
| **implementation** | Main dependencies | Compile, runtime |
| **compileOnly** | Compile-time only | Compile only |
| **runtimeOnly** | Runtime only | Runtime only |
| **testImplementation** | Test dependencies | Test compile, test runtime |
| **testCompileOnly** | Test compile only | Test compile only |
| **testRuntimeOnly** | Test runtime only | Test runtime only |

### Dependency Declarations

```groovy
dependencies {
    // String notation
    implementation 'org.springframework:spring-core:5.3.21'
    
    // Map notation
    implementation group: 'org.springframework', 
                    name: 'spring-core', 
                    version: '5.3.21'
    
    // Exclude transitive dependency
    implementation('org.springframework:spring-core:5.3.21') {
        exclude group: 'commons-logging', module: 'commons-logging'
    }
    
    // Version catalog (Gradle 7+)
    implementation libs.spring.core
}
```

### Version Catalog

```toml
# gradle/libs.versions.toml

[versions]
spring = "5.3.21"
junit = "4.13.2"

[libraries]
spring-core = { module = "org.springframework:spring-core", version.ref = "spring" }
junit = { module = "junit:junit", version.ref = "junit" }

[bundles]
spring = ["spring-core", "spring-context", "spring-beans"]

[plugins]
spring-boot = { id = "org.springframework.boot", version = "2.7.0" }
```

**Usage:**
```groovy
dependencies {
    implementation libs.spring.core
    implementation libs.bundles.spring
}

plugins {
    alias(libs.plugins.spring.boot)
}
```

### Dependency Resolution

```
┌─────────────────────────────────────────────────────────┐
│         Dependency Resolution Flow                      │
└─────────────────────────────────────────────────────────┘

Gradle needs dependency
    │
    ▼
Check Local Cache
    │
    ├─ Found → Use it
    │
    └─ Not Found
        │
        ▼
    Check Repositories
        │
        ├─ Found → Download & Cache
        │
        └─ Not Found → Build Failure
```

## 4. Gradle Plugins

### Plugin Types

```
┌─────────────────────────────────────────────────────────┐
│              Gradle Plugin Types                        │
└─────────────────────────────────────────────────────────┘

1. Core Plugins (Built-in)
   ├─ java
   ├─ application
   ├─ war
   └─ groovy

2. Community Plugins
   ├─ Spring Boot
   ├─ Kotlin
   └─ Docker

3. Custom Plugins
   └─ Project-specific
```

### Applying Plugins

```groovy
// Method 1: plugins block (recommended)
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.7.0'
}

// Method 2: apply plugin
apply plugin: 'java'
apply plugin: 'application'

// Method 3: Legacy apply
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.springframework.boot:spring-boot-gradle-plugin:2.7.0'
    }
}
apply plugin: 'org.springframework.boot'
```

### Common Plugins

#### Java Plugin
```groovy
plugins {
    id 'java'
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
```

#### Application Plugin
```groovy
plugins {
    id 'application'
}

application {
    mainClass = 'com.example.Main'
}
```

#### Spring Boot Plugin
```groovy
plugins {
    id 'org.springframework.boot' version '2.7.0'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
}
```

#### Kotlin Plugin
```groovy
plugins {
    id 'org.jetbrains.kotlin.jvm' version '1.8.0'
}
```

## 5. Multi-Project Builds

### Project Structure

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Project Structure                        │
└─────────────────────────────────────────────────────────┘

root-project/
├─ settings.gradle        ← Project settings
├─ build.gradle           ← Root build script
├─ api/
│  └─ build.gradle        ← API module
├─ service/
│  └─ build.gradle        ← Service module
└─ web/
   └─ build.gradle        ← Web module
```

### Root build.gradle

```groovy
// Root build.gradle

// Common configuration for all subprojects
subprojects {
    apply plugin: 'java'
    
    repositories {
        mavenCentral()
    }
    
    dependencies {
        testImplementation 'junit:junit:4.13.2'
    }
}

// Root-specific configuration
allprojects {
    group = 'com.example'
    version = '1.0.0'
}
```

### settings.gradle

```groovy
// settings.gradle

rootProject.name = 'my-project'

include 'api'
include 'service'
include 'web'

// Custom project paths
project(':api').projectDir = file('modules/api')
```

### Module build.gradle

```groovy
// api/build.gradle

dependencies {
    // Module-specific dependencies
}

// service/build.gradle

dependencies {
    implementation project(':api')  // Depend on api module
}

// web/build.gradle

dependencies {
    implementation project(':service')  // Depend on service module
}
```

## 6. Gradle Wrapper

### What is Gradle Wrapper?

Gradle Wrapper ensures everyone uses the same Gradle version.

### Wrapper Files

```
┌─────────────────────────────────────────────────────────┐
│              Gradle Wrapper Files                      │
└─────────────────────────────────────────────────────────┘

project/
├─ gradlew                ← Unix wrapper script
├─ gradlew.bat            ← Windows wrapper script
└─ gradle/
   └─ wrapper/
      ├─ gradle-wrapper.jar
      └─ gradle-wrapper.properties
```

### Generate Wrapper

```bash
# Generate wrapper
gradle wrapper --gradle-version 8.0

# Use wrapper
./gradlew build          # Unix/Mac
gradlew.bat build        # Windows
```

### gradle-wrapper.properties

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.0-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

## 7. Build Performance

### Gradle Daemon

```
┌─────────────────────────────────────────────────────────┐
│              Gradle Daemon                              │
└─────────────────────────────────────────────────────────┘

First Build
    │
    ▼
Start Daemon
    │
    ▼
Cache JVM
    │
    ▼
Subsequent Builds
    │
    ▼
Reuse Daemon (Fast!)
```

**Enable Daemon:**
```groovy
// gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
```

### Build Caching

```groovy
// gradle.properties
org.gradle.caching=true

// Enable for specific task
tasks.named('compileJava') {
    outputs.cacheIf { true }
}
```

### Parallel Execution

```groovy
// gradle.properties
org.gradle.parallel=true
org.gradle.workers.max=4
```

## Common Gradle Commands

### Build Commands

```bash
# Build
./gradlew build

# Clean build
./gradlew clean build

# Run tests
./gradlew test

# Skip tests
./gradlew build -x test

# Run application
./gradlew run

# List tasks
./gradlew tasks

# Dependency tree
./gradlew dependencies

# Build scan
./gradlew build --scan
```

### Task Commands

```bash
# Execute specific task
./gradlew compileJava
./gradlew test
./gradlew jar

# Task with options
./gradlew build --info
./gradlew build --debug
./gradlew build --stacktrace
```

## Gradle vs Maven

| Feature | Gradle | Maven |
|---------|--------|-------|
| **Build Script** | Groovy/Kotlin DSL | XML (POM) |
| **Performance** | Faster (daemon, caching) | Slower |
| **Flexibility** | High | Medium |
| **Learning Curve** | Steeper | Gentler |
| **Plugin Ecosystem** | Growing | Mature |
| **Multi-Module** | Excellent | Good |

## Best Practices

### 1. Use Version Catalog
```toml
# Centralize versions
[versions]
spring = "5.3.21"
```

### 2. Use Gradle Wrapper
```bash
# Always use wrapper
./gradlew build
```

### 3. Enable Caching
```properties
org.gradle.caching=true
```

### 4. Use Appropriate Scopes
```groovy
implementation 'lib'      // Not compile
testImplementation 'lib'   // Not testCompile
```

### 5. Organize Dependencies
```groovy
dependencies {
    // Group by purpose
    // Core dependencies
    implementation '...'
    
    // Testing
    testImplementation '...'
}
```

## Summary

Gradle:
- **Build Scripts**: Groovy/Kotlin DSL (flexible, powerful)
- **Tasks**: Build actions (compile, test, package)
- **Dependencies**: Flexible dependency management
- **Plugins**: Extensible plugin system
- **Performance**: Fast with daemon and caching

**Key Features:**
- DSL-based configuration
- Incremental builds
- Dependency resolution
- Multi-project support
- Build caching

**Best Practices:**
- Use Gradle Wrapper
- Enable build caching
- Use version catalogs
- Organize dependencies
- Leverage daemon for performance

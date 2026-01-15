# Maven: POM, Lifecycle, Plugins, Multi-Module Projects

## Overview

Apache Maven is a build automation and project management tool primarily used for Java projects. It uses a Project Object Model (POM) to manage project dependencies, build lifecycle, and plugins.

## Maven Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Maven Architecture                         │
└─────────────────────────────────────────────────────────┘

Developer
    │
    ▼
┌─────────────────┐
│ pom.xml         │  ← Project configuration
│ (POM File)      │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Maven Core      │  ← Build engine
│ (Lifecycle)     │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Plugins         │  ← Build tasks
│ (Compile, Test) │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Local Repository│  ← ~/.m2/repository
│ (Dependencies)  │
└─────────────────┘
```

## 1. POM (Project Object Model)

### What is POM?

POM is an XML file (`pom.xml`) that contains project configuration, dependencies, and build settings.

### POM Structure

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    
    <!-- Project Coordinates -->
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>my-project</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <!-- Project Information -->
    <name>My Project</name>
    <description>Project description</description>
    <url>https://example.com</url>
    
    <!-- Properties -->
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

### POM Elements Explained

#### Project Coordinates
```
┌─────────────────────────────────────────────────────────┐
│              Project Coordinates                        │
└─────────────────────────────────────────────────────────┘

groupId:    com.example        ← Organization/Company
artifactId: my-project         ← Project name
version:    1.0.0             ← Version
packaging:  jar                ← Output type (jar, war, pom)
```

**Example:**
- **groupId**: `com.example` (reverse domain)
- **artifactId**: `my-project` (project name)
- **version**: `1.0.0` (semantic versioning)
- **packaging**: `jar` (jar, war, pom, ear)

#### Dependencies Section

```xml
<dependencies>
    <!-- Direct Dependency -->
    <dependency>
        <groupId>org.springframework</groupId>
        <artifactId>spring-core</artifactId>
        <version>5.3.21</version>
        <scope>compile</scope>  <!-- Default scope -->
    </dependency>
    
    <!-- Test Dependency -->
    <dependency>
        <groupId>junit</groupId>
        <artifactId>junit</artifactId>
        <version>4.13.2</version>
        <scope>test</scope>  <!-- Only for tests -->
    </dependency>
</dependencies>
```

**Dependency Scopes:**

| Scope | Description | Included in Classpath |
|-------|-------------|----------------------|
| **compile** | Default scope | Compile, test, runtime |
| **provided** | Provided by container | Compile, test only |
| **runtime** | Not needed for compilation | Test, runtime only |
| **test** | Only for testing | Test only |
| **system** | System dependency | Similar to provided |

## 2. Maven Lifecycle

### Build Lifecycles

Maven has three built-in lifecycles:

```
┌─────────────────────────────────────────────────────────┐
│              Maven Build Lifecycles                    │
└─────────────────────────────────────────────────────────┘

1. default (build)
   ├─ validate
   ├─ initialize
   ├─ generate-sources
   ├─ process-sources
   ├─ generate-resources
   ├─ process-resources
   ├─ compile
   ├─ process-classes
   ├─ generate-test-sources
   ├─ process-test-sources
   ├─ generate-test-resources
   ├─ process-test-resources
   ├─ test-compile
   ├─ test
   ├─ prepare-package
   ├─ package
   ├─ pre-integration-test
   ├─ integration-test
   ├─ post-integration-test
   ├─ verify
   ├─ install
   └─ deploy

2. clean
   ├─ pre-clean
   ├─ clean
   └─ post-clean

3. site
   ├─ pre-site
   ├─ site
   ├─ post-site
   └─ site-deploy
```

### Default Lifecycle Phases

```
┌─────────────────────────────────────────────────────────┐
│         Default Lifecycle Phases (Key)                  │
└─────────────────────────────────────────────────────────┘

validate
    │
    ▼
compile          ← Compiles source code
    │
    ▼
test             ← Runs unit tests
    │
    ▼
package          ← Creates JAR/WAR
    │
    ▼
verify           ← Runs integration tests
    │
    ▼
install          ← Installs to local repo
    │
    ▼
deploy           ← Deploys to remote repo
```

### Lifecycle Execution

```bash
# Execute specific phase (runs all phases up to it)
mvn compile      # Runs: validate → compile
mvn test         # Runs: validate → compile → test
mvn package      # Runs: validate → compile → test → package
mvn install      # Runs: validate → compile → test → package → install
```

## 3. Maven Plugins

### What are Plugins?

Plugins are Maven extensions that provide goals (tasks) to execute during the build lifecycle.

### Plugin Structure

```xml
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
```

### Essential Maven Plugins

#### 1. Compiler Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <source>17</source>
        <target>17</target>
        <encoding>UTF-8</encoding>
    </configuration>
</plugin>
```

#### 2. Surefire Plugin (Testing)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
        </includes>
    </configuration>
</plugin>
```

#### 3. JAR Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>3.3.0</version>
    <configuration>
        <archive>
            <manifest>
                <mainClass>com.example.Main</mainClass>
            </manifest>
        </archive>
    </configuration>
</plugin>
```

#### 4. Shade Plugin (Fat JAR)
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.4.1</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Plugin Goals

```
┌─────────────────────────────────────────────────────────┐
│              Plugin Goals                               │
└─────────────────────────────────────────────────────────┘

Plugin: maven-compiler-plugin
├─ compiler:compile      ← Compile main sources
└─ compiler:testCompile  ← Compile test sources

Plugin: maven-surefire-plugin
└─ surefire:test        ← Run unit tests

Plugin: maven-jar-plugin
└─ jar:jar              ← Create JAR file
```

**Execute Goals:**
```bash
mvn compiler:compile           # Run specific goal
mvn compiler:compile test      # Run goal then phase
mvn clean package              # Run phases
```

## 4. Multi-Module Projects

### What are Multi-Module Projects?

Multi-module projects allow you to manage multiple related projects as a single unit.

### Multi-Module Structure

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Module Project Structure                  │
└─────────────────────────────────────────────────────────┘

parent-project/
├─ pom.xml              ← Parent POM (packaging: pom)
├─ module-api/
│  └─ pom.xml          ← API module
├─ module-service/
│  └─ pom.xml          ← Service module
└─ module-web/
   └─ pom.xml          ← Web module
```

### Parent POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>parent-project</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>  <!-- Parent is POM type -->
    
    <modules>
        <module>module-api</module>
        <module>module-service</module>
        <module>module-web</module>
    </modules>
    
    <!-- Common properties -->
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <spring.version>5.3.21</spring.version>
    </properties>
    
    <!-- Common dependencies -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework</groupId>
                <artifactId>spring-core</artifactId>
                <version>${spring.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
    
    <!-- Common plugins -->
    <build>
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.11.0</version>
                </plugin>
            </plugins>
        </pluginManagement>
    </build>
</project>
```

### Child Module POM

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project>
    <modelVersion>4.0.0</modelVersion>
    
    <!-- Reference parent -->
    <parent>
        <groupId>com.example</groupId>
        <artifactId>parent-project</artifactId>
        <version>1.0.0</version>
    </parent>
    
    <!-- Module coordinates -->
    <artifactId>module-api</artifactId>
    <packaging>jar</packaging>
    
    <!-- Dependencies (version from parent) -->
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <!-- Version inherited from parent -->
        </dependency>
    </dependencies>
</project>
```

### Multi-Module Build Flow

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Module Build Execution                    │
└─────────────────────────────────────────────────────────┘

mvn clean install (in parent)
    │
    ▼
┌─────────────────┐
│ Build module-api│  ← Builds first (no dependencies)
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Build module-   │  ← Depends on module-api
│ service         │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Build module-web│  ← Depends on module-service
└─────────────────┘
```

### Module Dependencies

```xml
<!-- module-service/pom.xml -->
<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>module-api</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>

<!-- module-web/pom.xml -->
<dependencies>
    <dependency>
        <groupId>com.example</groupId>
        <artifactId>module-service</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## Maven Repository Structure

### Repository Types

```
┌─────────────────────────────────────────────────────────┐
│              Maven Repositories                        │
└─────────────────────────────────────────────────────────┘

Local Repository
    │
    ▼
~/.m2/repository/
    │
    └─ com/example/my-project/1.0.0/
       └─ my-project-1.0.0.jar

Remote Repositories
    │
    ├─ Central Repository (Maven Central)
    ├─ Company Repository (Nexus, Artifactory)
    └─ Public Repositories (JCenter, etc.)
```

### Dependency Resolution

```
┌─────────────────────────────────────────────────────────┐
│         Dependency Resolution Flow                      │
└─────────────────────────────────────────────────────────┘

Maven needs dependency
    │
    ▼
Check Local Repository
    │
    ├─ Found → Use it
    │
    └─ Not Found
        │
        ▼
    Check Remote Repositories
        │
        ├─ Found → Download to Local
        │
        └─ Not Found → Build Failure
```

## Common Maven Commands

### Build Commands

```bash
# Clean build
mvn clean                    # Remove target directory
mvn clean compile            # Clean then compile
mvn clean package            # Clean then package
mvn clean install            # Clean then install

# Build phases
mvn validate                 # Validate project
mvn compile                  # Compile source code
mvn test                     # Run tests
mvn package                  # Create JAR/WAR
mvn install                  # Install to local repo
mvn deploy                   # Deploy to remote repo

# Skip tests
mvn package -DskipTests     # Skip tests
mvn package -Dmaven.test.skip=true  # Skip test compilation

# Profiles
mvn package -Pprod          # Use production profile
mvn package -Pdev,test      # Use multiple profiles
```

### Dependency Commands

```bash
# Dependency management
mvn dependency:tree         # Show dependency tree
mvn dependency:analyze      # Analyze dependencies
mvn dependency:resolve      # Resolve dependencies
mvn dependency:sources      # Download sources
mvn dependency:copy-dependencies  # Copy to directory
```

## Maven Best Practices

### 1. Use Properties
```xml
<properties>
    <java.version>17</java.version>
    <spring.version>5.3.21</spring.version>
</properties>

<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-core</artifactId>
    <version>${spring.version}</version>
</dependency>
```

### 2. Use Dependency Management
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>5.3.21</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 3. Use Plugin Management
```xml
<pluginManagement>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
        </plugin>
    </plugins>
</pluginManagement>
```

### 4. Organize Dependencies
- Group related dependencies
- Use comments for clarity
- Keep versions in properties

### 5. Use Profiles
```xml
<profiles>
    <profile>
        <id>dev</id>
        <properties>
            <env>development</env>
        </properties>
    </profile>
    <profile>
        <id>prod</id>
        <properties>
            <env>production</env>
        </properties>
    </profile>
</profiles>
```

## Summary

Maven:
- **POM**: XML-based project configuration
- **Lifecycle**: Phases from validate to deploy
- **Plugins**: Extend functionality with goals
- **Multi-Module**: Manage related projects together

**Key Concepts:**
- Project coordinates (groupId, artifactId, version)
- Build lifecycles (default, clean, site)
- Plugin goals and phases
- Dependency management
- Repository structure

**Best Practices:**
- Use properties for versions
- Use dependency/plugin management
- Organize multi-module projects
- Keep POM files clean and documented

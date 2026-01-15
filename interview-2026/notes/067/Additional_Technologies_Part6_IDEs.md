# IDEs: IntelliJ IDEA, Eclipse, VS Code

## Overview

Integrated Development Environments (IDEs) provide comprehensive tools for software development. This guide covers three popular IDEs: IntelliJ IDEA (Java-focused), Eclipse (multi-language), and VS Code (lightweight, extensible).

## IDE Comparison

```
┌─────────────────────────────────────────────────────────┐
│              IDE Comparison                            │
└─────────────────────────────────────────────────────────┘

IntelliJ IDEA:
├─ Focus: Java, Kotlin, Enterprise
├─ Type: Full-featured IDE
├─ Performance: Excellent
└─ Cost: Free (Community) / Paid (Ultimate)

Eclipse:
├─ Focus: Java, Multi-language
├─ Type: Full-featured IDE
├─ Performance: Good
└─ Cost: Free (Open Source)

VS Code:
├─ Focus: All languages (extensible)
├─ Type: Lightweight editor
├─ Performance: Excellent
└─ Cost: Free (Open Source)
```

## 1. IntelliJ IDEA

### Overview

IntelliJ IDEA is a powerful IDE developed by JetBrains, primarily for Java development but supports many languages.

### Key Features

#### 1. Smart Code Completion
```
┌─────────────────────────────────────────────────────────┐
│         IntelliJ Code Completion                        │
└─────────────────────────────────────────────────────────┘

Type: user.
    │
    ▼
IntelliJ suggests:
├─ getName()
├─ getEmail()
├─ getAge()
└─ ... (context-aware)
```

#### 2. Refactoring Tools
- **Rename**: Safe renaming across project
- **Extract Method**: Extract code to method
- **Inline**: Inline variables/methods
- **Move**: Move classes/packages
- **Change Signature**: Modify method signatures

#### 3. Code Analysis
- Real-time error detection
- Code inspections
- Quick fixes
- Code smells detection

#### 4. Built-in Tools
- **Version Control**: Git, SVN, Mercurial
- **Build Tools**: Maven, Gradle
- **Database Tools**: Database connections
- **Terminal**: Integrated terminal
- **Debugger**: Advanced debugging

### IntelliJ IDEA Editions

#### Community Edition (Free)
- Java, Kotlin support
- Basic refactoring
- Version control
- Maven, Gradle
- Limited plugins

#### Ultimate Edition (Paid)
- All languages
- Advanced refactoring
- Database tools
- Spring framework support
- Web development
- All plugins

### IntelliJ IDEA Workflow

```
┌─────────────────────────────────────────────────────────┐
│         IntelliJ Development Workflow                   │
└─────────────────────────────────────────────────────────┘

1. Create/Open Project
    │
    ▼
2. Configure SDK
    │
    ▼
3. Add Dependencies (Maven/Gradle)
    │
    ▼
4. Write Code
    ├─ Code completion
    ├─ Error detection
    └─ Quick fixes
    │
    ▼
5. Run/Debug
    │
    ▼
6. Refactor
    │
    ▼
7. Commit/Push (Git)
```

### IntelliJ IDEA Shortcuts

```
┌─────────────────────────────────────────────────────────┐
│         Essential IntelliJ Shortcuts                    │
└─────────────────────────────────────────────────────────┘

Code Navigation:
├─ Ctrl+N: Go to class
├─ Ctrl+Shift+N: Go to file
├─ Ctrl+B: Go to declaration
└─ Alt+F7: Find usages

Code Editing:
├─ Ctrl+Space: Code completion
├─ Ctrl+Alt+L: Reformat code
├─ Ctrl+Alt+O: Optimize imports
└─ Ctrl+D: Duplicate line

Refactoring:
├─ Shift+F6: Rename
├─ Ctrl+Alt+M: Extract method
├─ Ctrl+Alt+V: Extract variable
└─ F6: Move

Run/Debug:
├─ Shift+F10: Run
├─ Shift+F9: Debug
└─ Ctrl+F9: Build
```

## 2. Eclipse

### Overview

Eclipse is an open-source IDE primarily for Java development, with extensive plugin ecosystem.

### Key Features

#### 1. Workspace Concept
```
┌─────────────────────────────────────────────────────────┐
│         Eclipse Workspace                               │
└─────────────────────────────────────────────────────────┘

Workspace
├─ Project 1
├─ Project 2
└─ Project 3

Each workspace has:
├─ Settings
├─ Preferences
└─ Plugins
```

#### 2. Perspectives
- **Java Perspective**: Java development
- **Debug Perspective**: Debugging
- **Git Perspective**: Version control
- **Package Explorer**: Project navigation

#### 3. Plugins Ecosystem
- **Maven Integration**: m2e
- **Spring Tools**: Spring IDE
- **Web Tools**: WTP
- **Mylyn**: Task management

#### 4. Code Generation
- Generate getters/setters
- Generate constructors
- Generate toString, equals, hashCode
- Generate Javadoc

### Eclipse Workflow

```
┌─────────────────────────────────────────────────────────┐
│         Eclipse Development Workflow                    │
└─────────────────────────────────────────────────────────┘

1. Create Workspace
    │
    ▼
2. Create/Import Project
    │
    ▼
3. Configure Build Path
    │
    ▼
4. Write Code
    ├─ Code completion
    ├─ Error detection
    └─ Quick fixes
    │
    ▼
5. Run/Debug
    │
    ▼
6. Refactor
    │
    ▼
7. Commit/Push (EGit)
```

### Eclipse Shortcuts

```
┌─────────────────────────────────────────────────────────┐
│         Essential Eclipse Shortcuts                     │
└─────────────────────────────────────────────────────────┘

Code Navigation:
├─ Ctrl+Shift+T: Open type
├─ Ctrl+Shift+R: Open resource
├─ F3: Go to declaration
└─ Ctrl+Shift+G: Find references

Code Editing:
├─ Ctrl+Space: Content assist
├─ Ctrl+Shift+F: Format
├─ Ctrl+Shift+O: Organize imports
└─ Ctrl+Alt+Down: Duplicate line

Refactoring:
├─ Alt+Shift+R: Rename
├─ Alt+Shift+M: Extract method
├─ Alt+Shift+L: Extract local variable
└─ Alt+Shift+V: Move

Run/Debug:
├─ Ctrl+F11: Run
├─ F11: Debug
└─ Ctrl+B: Build
```

## 3. VS Code

### Overview

Visual Studio Code is a lightweight, extensible code editor that supports all programming languages through extensions.

### Key Features

#### 1. Extensions
```
┌─────────────────────────────────────────────────────────┐
│         VS Code Extension Ecosystem                     │
└─────────────────────────────────────────────────────────┘

Language Support:
├─ Java Extension Pack
├─ Python Extension
├─ JavaScript/TypeScript
└─ Go, Rust, C++, etc.

Development Tools:
├─ GitLens
├─ Docker
├─ Kubernetes
└─ Remote Development
```

#### 2. Integrated Terminal
- Multiple terminals
- Split terminals
- Terminal profiles
- Integrated shell

#### 3. IntelliSense
- Code completion
- Parameter hints
- Quick info
- Auto imports

#### 4. Debugging
- Multi-language debugging
- Breakpoints
- Watch expressions
- Call stack

### VS Code Workflow

```
┌─────────────────────────────────────────────────────────┐
│         VS Code Development Workflow                    │
└─────────────────────────────────────────────────────────┘

1. Open Folder
    │
    ▼
2. Install Extensions
    │
    ▼
3. Configure Settings
    │
    ▼
4. Write Code
    ├─ IntelliSense
    ├─ Error detection
    └─ Quick fixes
    │
    ▼
5. Run/Debug
    │
    ▼
6. Git Operations
    │
    ▼
7. Extensions for specific tasks
```

### VS Code Shortcuts

```
┌─────────────────────────────────────────────────────────┐
│         Essential VS Code Shortcuts                     │
└─────────────────────────────────────────────────────────┘

Navigation:
├─ Ctrl+P: Quick open
├─ Ctrl+Shift+P: Command palette
├─ Ctrl+B: Toggle sidebar
└─ Ctrl+`: Toggle terminal

Editing:
├─ Ctrl+Space: Trigger suggestion
├─ Shift+Alt+F: Format document
├─ Ctrl+Shift+P: Organize imports
└─ Alt+Up/Down: Move line

Multi-cursor:
├─ Alt+Click: Add cursor
├─ Ctrl+Alt+Up/Down: Add cursor above/below
└─ Ctrl+D: Select next occurrence

Run/Debug:
├─ F5: Start debugging
├─ F9: Toggle breakpoint
└─ Ctrl+Shift+B: Build
```

## 4. IDE Selection Guide

### For Java Development

```
┌─────────────────────────────────────────────────────────┐
│         Java IDE Selection                             │
└─────────────────────────────────────────────────────────┘

Enterprise Java:
├─ IntelliJ IDEA Ultimate  ← Best choice
└─ Eclipse                  ← Good alternative

Spring Boot:
├─ IntelliJ IDEA Ultimate  ← Excellent Spring support
└─ Eclipse + Spring Tools  ← Good alternative

Android:
├─ Android Studio (IntelliJ)  ← Official
└─ IntelliJ IDEA Ultimate      ← Alternative

Lightweight:
└─ VS Code + Java Extension Pack
```

### For Multi-Language Development

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Language IDE Selection                    │
└─────────────────────────────────────────────────────────┘

All Languages:
└─ VS Code  ← Best choice (extensible)

Java + Web:
├─ IntelliJ IDEA Ultimate
└─ Eclipse

Python + Data Science:
├─ VS Code
└─ PyCharm (IntelliJ)
```

### Comparison Matrix

| Feature | IntelliJ IDEA | Eclipse | VS Code |
|---------|---------------|---------|---------|
| **Java Support** | Excellent | Excellent | Good (with extensions) |
| **Performance** | Excellent | Good | Excellent |
| **Refactoring** | Excellent | Good | Good |
| **Plugins** | Many | Many | Extensive |
| **Cost** | Free/Paid | Free | Free |
| **Learning Curve** | Medium | Medium | Low |
| **Resource Usage** | High | Medium | Low |

## 5. IDE Best Practices

### 1. Keyboard Shortcuts
- Learn essential shortcuts
- Customize for your workflow
- Use keyboard over mouse

### 2. Code Templates
- Create custom templates
- Use live templates
- Speed up coding

### 3. Code Formatting
- Configure formatter
- Use consistent style
- Auto-format on save

### 4. Version Control Integration
- Use built-in Git tools
- Configure Git settings
- Use visual diff tools

### 5. Debugging
- Master debugger
- Use breakpoints effectively
- Watch expressions
- Evaluate expressions

### 6. Plugins/Extensions
- Install essential ones only
- Keep updated
- Remove unused

## 6. IDE Configuration

### IntelliJ IDEA Settings

```java
// Code Style
Settings → Editor → Code Style → Java

// Keymaps
Settings → Keymap

// Plugins
Settings → Plugins

// Build Tools
Settings → Build, Execution, Deployment → Build Tools
```

### Eclipse Preferences

```
Window → Preferences
├─ Java → Code Style
├─ General → Keys
├─ Install/Update → Available Software Sites
└─ Team → Git
```

### VS Code Settings

```json
// settings.json
{
    "editor.formatOnSave": true,
    "editor.codeActionsOnSave": {
        "source.organizeImports": true
    },
    "java.compile.nullAnalysis.mode": "automatic"
}
```

## Summary

IDEs:
- **IntelliJ IDEA**: Best for Java/Enterprise development
- **Eclipse**: Good Java IDE with extensive plugins
- **VS Code**: Lightweight, extensible for all languages

**Key Features:**
- Code completion and IntelliSense
- Refactoring tools
- Debugging capabilities
- Version control integration
- Plugin/extension ecosystem

**Best Practices:**
- Learn keyboard shortcuts
- Use code templates
- Configure code formatting
- Master debugging tools
- Install essential plugins only

**Remember**: Choose the IDE that fits your workflow and project requirements!

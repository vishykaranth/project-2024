# Command Pattern: Request Encapsulation, Undo/Redo

## Overview

The Command pattern encapsulates a request as an object, thereby allowing you to parameterize clients with different requests, queue operations, log requests, and support undoable operations. It decouples the object that invokes the operation from the one that performs it.

## Command Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Command Pattern Structure                  │
└─────────────────────────────────────────────────────────┘

        Invoker
    ┌──────────────────────┐
    │ - command: Command   │
    │ + executeCommand()   │
    └──────────┬───────────┘
               │
               │ calls
               ▼
        Command (Interface)
    ┌──────────────────────┐
    │ + execute()          │
    │ + undo()             │
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │           │           │
    ▼           ▼           ▼
ConcreteCommand1  ConcreteCommand2  ConcreteCommand3
    │               │               │
    │ uses          │ uses          │ uses
    ▼               ▼               ▼
Receiver          Receiver        Receiver
```

## Basic Command Example

### Light Control System

```java
// Receiver - knows how to perform operations
public class Light {
    private boolean isOn = false;
    
    public void turnOn() {
        isOn = true;
        System.out.println("Light is ON");
    }
    
    public void turnOff() {
        isOn = false;
        System.out.println("Light is OFF");
    }
    
    public boolean isOn() {
        return isOn;
    }
}

// Command interface
public interface Command {
    void execute();
    void undo();
}

// Concrete Commands
public class LightOnCommand implements Command {
    private Light light;
    
    public LightOnCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        light.turnOn();
    }
    
    @Override
    public void undo() {
        light.turnOff();
    }
}

public class LightOffCommand implements Command {
    private Light light;
    
    public LightOffCommand(Light light) {
        this.light = light;
    }
    
    @Override
    public void execute() {
        light.turnOff();
    }
    
    @Override
    public void undo() {
        light.turnOn();
    }
}

// Invoker
public class RemoteControl {
    private Command command;
    private Command lastCommand;  // For undo
    
    public void setCommand(Command command) {
        this.command = command;
    }
    
    public void pressButton() {
        command.execute();
        lastCommand = command;
    }
    
    public void pressUndo() {
        if (lastCommand != null) {
            lastCommand.undo();
        }
    }
}

// Usage
Light light = new Light();
Command lightOn = new LightOnCommand(light);
Command lightOff = new LightOffCommand(light);

RemoteControl remote = new RemoteControl();
remote.setCommand(lightOn);
remote.pressButton();  // Light turns on

remote.setCommand(lightOff);
remote.pressButton();  // Light turns off

remote.pressUndo();  // Light turns on again
```

## Advanced Command Example: Undo/Redo System

```java
// Command with state for undo
public interface Command {
    void execute();
    void undo();
    String getDescription();
}

// Command History
public class CommandHistory {
    private Stack<Command> history = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();
    
    public void execute(Command command) {
        command.execute();
        history.push(command);
        redoStack.clear();  // Clear redo when new command executed
    }
    
    public void undo() {
        if (!history.isEmpty()) {
            Command command = history.pop();
            command.undo();
            redoStack.push(command);
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            Command command = redoStack.pop();
            command.execute();
            history.push(command);
        }
    }
    
    public boolean canUndo() {
        return !history.isEmpty();
    }
    
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}

// Text Editor Example
public class TextEditor {
    private StringBuilder text = new StringBuilder();
    
    public void insert(String text, int position) {
        this.text.insert(position, text);
    }
    
    public void delete(int start, int end) {
        this.text.delete(start, end);
    }
    
    public String getText() {
        return text.toString();
    }
}

// Commands
public class InsertCommand implements Command {
    private TextEditor editor;
    private String text;
    private int position;
    
    public InsertCommand(TextEditor editor, String text, int position) {
        this.editor = editor;
        this.text = text;
        this.position = position;
    }
    
    @Override
    public void execute() {
        editor.insert(text, position);
    }
    
    @Override
    public void undo() {
        editor.delete(position, position + text.length());
    }
    
    @Override
    public String getDescription() {
        return "Insert '" + text + "' at position " + position;
    }
}

public class DeleteCommand implements Command {
    private TextEditor editor;
    private int start;
    private int end;
    private String deletedText;
    
    public DeleteCommand(TextEditor editor, int start, int end) {
        this.editor = editor;
        this.start = start;
        this.end = end;
    }
    
    @Override
    public void execute() {
        deletedText = editor.getText().substring(start, end);
        editor.delete(start, end);
    }
    
    @Override
    public void undo() {
        editor.insert(deletedText, start);
    }
    
    @Override
    public String getDescription() {
        return "Delete from " + start + " to " + end;
    }
}

// Usage
TextEditor editor = new TextEditor();
CommandHistory history = new CommandHistory();

history.execute(new InsertCommand(editor, "Hello", 0));
history.execute(new InsertCommand(editor, " World", 5));
// Text: "Hello World"

history.undo();  // Text: "Hello"
history.undo();  // Text: ""
history.redo();  // Text: "Hello"
history.redo();  // Text: "Hello World"
```

## Real-World Examples

### 1. Transaction System

```java
// Command for database transactions
public interface TransactionCommand {
    void execute();
    void rollback();
}

public class TransferCommand implements TransactionCommand {
    private Account fromAccount;
    private Account toAccount;
    private double amount;
    private boolean executed = false;
    
    public TransferCommand(Account from, Account to, double amount) {
        this.fromAccount = from;
        this.toAccount = to;
        this.amount = amount;
    }
    
    @Override
    public void execute() {
        if (fromAccount.getBalance() >= amount) {
            fromAccount.withdraw(amount);
            toAccount.deposit(amount);
            executed = true;
        } else {
            throw new IllegalStateException("Insufficient funds");
        }
    }
    
    @Override
    public void rollback() {
        if (executed) {
            toAccount.withdraw(amount);
            fromAccount.deposit(amount);
            executed = false;
        }
    }
}

// Transaction Manager
public class TransactionManager {
    private List<TransactionCommand> commands = new ArrayList<>();
    
    public void addCommand(TransactionCommand command) {
        commands.add(command);
    }
    
    public void executeAll() {
        List<TransactionCommand> executed = new ArrayList<>();
        try {
            for (TransactionCommand command : commands) {
                command.execute();
                executed.add(command);
            }
        } catch (Exception e) {
            // Rollback all executed commands
            for (int i = executed.size() - 1; i >= 0; i--) {
                executed.get(i).rollback();
            }
            throw e;
        }
    }
}
```

### 2. Macro Commands

```java
// Macro command - executes multiple commands
public class MacroCommand implements Command {
    private List<Command> commands = new ArrayList<>();
    
    public void addCommand(Command command) {
        commands.add(command);
    }
    
    @Override
    public void execute() {
        for (Command command : commands) {
            command.execute();
        }
    }
    
    @Override
    public void undo() {
        // Undo in reverse order
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }
    
    @Override
    public String getDescription() {
        return "Macro: " + commands.size() + " commands";
    }
}

// Usage
MacroCommand macro = new MacroCommand();
macro.addCommand(new LightOnCommand(light1));
macro.addCommand(new LightOnCommand(light2));
macro.addCommand(new FanOnCommand(fan));

macro.execute();  // Executes all commands
macro.undo();     // Undoes all commands
```

### 3. Job Queue

```java
// Command queue for background processing
public class JobQueue {
    private Queue<Command> queue = new LinkedList<>();
    private boolean running = false;
    
    public void addJob(Command command) {
        queue.offer(command);
    }
    
    public void start() {
        running = true;
        new Thread(() -> {
            while (running) {
                Command command = queue.poll();
                if (command != null) {
                    try {
                        command.execute();
                    } catch (Exception e) {
                        // Handle error
                    }
                }
            }
        }).start();
    }
    
    public void stop() {
        running = false;
    }
}
```

## Command Pattern Flow

```
┌─────────────────────────────────────────────────────────┐
│              Command Pattern Flow                       │
└─────────────────────────────────────────────────────────┘

Client
  │
  │ creates command
  ▼
Command Object
  │
  │ passed to invoker
  ▼
Invoker
  │
  │ calls execute()
  ▼
Command.execute()
  │
  │ calls receiver method
  ▼
Receiver
  │
  │ performs operation
  ▼
Result
```

## Command Pattern Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Command Pattern Benefits                    │
└─────────────────────────────────────────────────────────┘

1. Decoupling
   └─ Invoker doesn't know receiver
   └─ Commands can be parameterized

2. Undo/Redo Support
   └─ Commands can store state
   └─ Easy to implement undo

3. Queuing and Logging
   └─ Commands can be queued
   └─ Easy to log operations

4. Macro Commands
   └─ Combine multiple commands
   └─ Batch operations
```

## Best Practices

### 1. Store State for Undo

```java
public class CommandWithState implements Command {
    private Receiver receiver;
    private Object previousState;  // Store for undo
    
    @Override
    public void execute() {
        previousState = receiver.getState();  // Save state
        receiver.performOperation();
    }
    
    @Override
    public void undo() {
        receiver.setState(previousState);  // Restore state
    }
}
```

### 2. Use Command History

```java
public class CommandManager {
    private Stack<Command> history = new Stack<>();
    
    public void execute(Command command) {
        command.execute();
        history.push(command);
    }
    
    public void undo() {
        if (!history.isEmpty()) {
            history.pop().undo();
        }
    }
}
```

### 3. Make Commands Immutable When Possible

```java
// Immutable command - safer for queuing
public final class ImmutableCommand implements Command {
    private final String action;
    private final Object data;
    
    public ImmutableCommand(String action, Object data) {
        this.action = action;
        this.data = data;
    }
    
    // No setters - immutable
}
```

## Summary

Command Pattern:
- **Purpose**: Encapsulate requests as objects
- **Key Feature**: Supports undo/redo, queuing, logging
- **Use Cases**: Undo systems, job queues, transaction systems, macros
- **Benefits**: Decoupling, undo support, queuing, logging

**Key Takeaways:**
- ✅ Encapsulate requests as objects
- ✅ Support undo/redo operations
- ✅ Enable command queuing and logging
- ✅ Decouple invoker from receiver
- ✅ Enable macro commands

**Remember**: Command pattern is perfect for implementing undo/redo functionality and decoupling request senders from receivers!

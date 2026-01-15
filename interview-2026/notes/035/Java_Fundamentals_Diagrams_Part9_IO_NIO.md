# Java Language Fundamentals - Complete Diagrams Guide (Part 9: I/O & NIO)

## 📁 I/O & NIO

---

## 1. Java I/O Overview

### I/O Stream Types
```
┌─────────────────────────────────────────────────────────────┐
│              I/O Stream Hierarchy                           │
└─────────────────────────────────────────────────────────────┘

                InputStream/OutputStream
                        │
        ┌───────────────┼───────────────┐
        │               │               │
    FileInputStream  ByteArrayInputStream  ObjectInputStream
    FileOutputStream ByteArrayOutputStream ObjectOutputStream
        │               │               │
        │               │               │
    Reader/Writer    BufferedInputStream  DataInputStream
        │               │               │
    FileReader      BufferedOutputStream DataOutputStream
    FileWriter
    BufferedReader
    BufferedWriter
```

---

## 2. File I/O

### Reading Files
```java
// Method 1: FileReader
try (FileReader reader = new FileReader("file.txt")) {
    int character;
    while ((character = reader.read()) != -1) {
        System.out.print((char) character);
    }
}

// Method 2: BufferedReader (more efficient)
try (BufferedReader reader = new BufferedReader(
        new FileReader("file.txt"))) {
    String line;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
}

// Method 3: Files.readAllLines (Java 7+)
List<String> lines = Files.readAllLines(Paths.get("file.txt"));

// Method 4: Files.lines (Java 8+)
Files.lines(Paths.get("file.txt"))
    .forEach(System.out::println);
```

### Writing Files
```java
// Method 1: FileWriter
try (FileWriter writer = new FileWriter("file.txt")) {
    writer.write("Hello World");
}

// Method 2: BufferedWriter
try (BufferedWriter writer = new BufferedWriter(
        new FileWriter("file.txt"))) {
    writer.write("Line 1");
    writer.newLine();
    writer.write("Line 2");
}

// Method 3: Files.write (Java 7+)
Files.write(Paths.get("file.txt"), 
    "Hello World".getBytes());

// Method 4: Files.write with lines
List<String> lines = Arrays.asList("Line 1", "Line 2");
Files.write(Paths.get("file.txt"), lines);
```

---

## 3. NIO.2 (Java 7+)

### NIO.2 Path API
```
┌─────────────────────────────────────────────────────────────┐
│              NIO.2 Path API                                │
└─────────────────────────────────────────────────────────────┘

    Path
    ┌──────────────┐
    │  - get()     │
    │  - of()      │
    │  - resolve() │
    │  - normalize()│
    │  - toAbsolutePath()│
    └──────────────┘
           │
    ┌──────┴───────┐
    │              │
Files          Paths
┌──────┐      ┌──────┐
│read()│      │get() │
│write()│     │      │
│copy() │     │      │
│move() │     │      │
│delete()│    │      │
└──────┘      └──────┘
```

### Path Operations
```java
// Creating Path
Path path1 = Paths.get("file.txt");
Path path2 = Paths.get("/home/user", "file.txt");
Path path3 = Path.of("file.txt");  // Java 11+

// Path operations
Path absolute = path1.toAbsolutePath();
Path normalized = path1.normalize();
Path resolved = path1.resolve("subdir/file.txt");
Path parent = path1.getParent();
String fileName = path1.getFileName().toString();

// File operations with Files class
// Read
byte[] bytes = Files.readAllBytes(path1);
List<String> lines = Files.readAllLines(path1);

// Write
Files.write(path1, bytes);
Files.write(path1, lines);

// Copy
Files.copy(source, destination);

// Move
Files.move(source, destination);

// Delete
Files.delete(path1);
Files.deleteIfExists(path1);

// Check
boolean exists = Files.exists(path1);
boolean isDirectory = Files.isDirectory(path1);
boolean isRegularFile = Files.isRegularFile(path1);
```

---

## 4. NIO Channels

### Channel Types
```
┌─────────────────────────────────────────────────────────────┐
│              NIO Channels                                   │
└─────────────────────────────────────────────────────────────┘

            Channel
               │
    ┌──────────┼──────────┐
    │          │          │
FileChannel  SocketChannel  DatagramChannel
    │          │          │
    │          │          │
ReadableByteChannel  WritableByteChannel
    │          │
    └──────────┘
    ByteChannel
```

### Channel Examples
```java
// FileChannel - Reading
try (FileChannel channel = FileChannel.open(
        Paths.get("file.txt"), StandardOpenOption.READ)) {
    ByteBuffer buffer = ByteBuffer.allocate(1024);
    while (channel.read(buffer) > 0) {
        buffer.flip();  // Prepare for reading
        while (buffer.hasRemaining()) {
            System.out.print((char) buffer.get());
        }
        buffer.clear();  // Prepare for next read
    }
}

// FileChannel - Writing
try (FileChannel channel = FileChannel.open(
        Paths.get("file.txt"), 
        StandardOpenOption.WRITE, 
        StandardOpenOption.CREATE)) {
    ByteBuffer buffer = ByteBuffer.wrap("Hello".getBytes());
    channel.write(buffer);
}

// FileChannel - Copy
try (FileChannel source = FileChannel.open(sourcePath);
     FileChannel dest = FileChannel.open(destPath,
         StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
    dest.transferFrom(source, 0, source.size());
}
```

---

## 5. NIO Buffers

### Buffer Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Buffer Structure                               │
└─────────────────────────────────────────────────────────────┘

    ByteBuffer
    ┌──────────────────────┐
    │  [0][1][2][3][4][5]  │
    │  ↑              ↑    │
    │  position      limit │
    │                      │
    │  capacity             │
    └──────────────────────┘

States:
    - position: Next read/write position
    - limit: First position that cannot be read/written
    - capacity: Maximum number of elements
```

### Buffer Operations
```java
// Creating buffer
ByteBuffer buffer = ByteBuffer.allocate(1024);
ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);

// Writing to buffer
buffer.put((byte) 'H');
buffer.put((byte) 'e');
buffer.put((byte) 'l');
buffer.put((byte) 'l');
buffer.put((byte) 'o');

// Reading from buffer
buffer.flip();  // Switch to read mode
while (buffer.hasRemaining()) {
    byte b = buffer.get();
    System.out.print((char) b);
}

// Buffer operations
buffer.clear();      // Clear for writing
buffer.flip();       // Switch to reading
buffer.rewind();     // Reset position to 0
buffer.mark();       // Mark current position
buffer.reset();      // Reset to marked position
buffer.compact();    // Compact remaining data
```

---

## 6. NIO Selectors

### Selector Concept
```
┌─────────────────────────────────────────────────────────────┐
│              Selector (Non-blocking I/O)                    │
└─────────────────────────────────────────────────────────────┘

    Selector
    ┌──────────────┐
    │              │
    │  Channel 1   │───► OP_READ
    │  Channel 2   │───► OP_WRITE
    │  Channel 3   │───► OP_ACCEPT
    │  Channel 4   │───► OP_CONNECT
    └──────────────┘
           │
           │ select()
           │
    ┌──────┴───────┐
    │              │
SelectedKeys    Ready Channels
```

### Selector Example
```java
// Server with Selector
Selector selector = Selector.open();
ServerSocketChannel serverChannel = ServerSocketChannel.open();
serverChannel.configureBlocking(false);
serverChannel.bind(new InetSocketAddress(8080));
serverChannel.register(selector, SelectionKey.OP_ACCEPT);

while (true) {
    int readyChannels = selector.select();
    if (readyChannels == 0) continue;
    
    Set<SelectionKey> selectedKeys = selector.selectedKeys();
    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
    
    while (keyIterator.hasNext()) {
        SelectionKey key = keyIterator.next();
        
        if (key.isAcceptable()) {
            // Accept connection
            ServerSocketChannel server = (ServerSocketChannel) key.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
        } else if (key.isReadable()) {
            // Read data
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            channel.read(buffer);
            // Process data
        } else if (key.isWritable()) {
            // Write data
            SocketChannel channel = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.wrap("Response".getBytes());
            channel.write(buffer);
        }
        
        keyIterator.remove();
    }
}
```

---

## 7. Asynchronous I/O

### AsynchronousFileChannel
```java
// Asynchronous file operations
AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
    Paths.get("file.txt"), StandardOpenOption.READ);

ByteBuffer buffer = ByteBuffer.allocate(1024);

// Read asynchronously
Future<Integer> operation = fileChannel.read(buffer, 0);

// Do other work
// ...

// Get result
Integer bytesRead = operation.get();
buffer.flip();
// Process buffer

// With CompletionHandler
fileChannel.read(buffer, 0, null, 
    new CompletionHandler<Integer, Void>() {
        @Override
        public void completed(Integer result, Void attachment) {
            // Handle completion
        }
        
        @Override
        public void failed(Throwable exc, Void attachment) {
            // Handle failure
        }
    });
```

---

## Key Concepts Summary

### I/O Summary
```
Traditional I/O:
- Stream-based
- Blocking
- InputStream/OutputStream
- Reader/Writer

NIO.2:
- Path API (Java 7+)
- Files utility class
- Channels and Buffers
- Non-blocking I/O
- Selectors

Key Classes:
- Path, Paths, Files
- FileChannel
- ByteBuffer
- Selector
- AsynchronousFileChannel
```

---

**Next: Part 10 will cover Serialization.**


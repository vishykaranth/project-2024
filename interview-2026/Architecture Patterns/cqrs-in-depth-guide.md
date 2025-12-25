# CQRS In-Depth Guide: Command Query Responsibility Segregation

## Table of Contents
1. [CQRS Overview](#cqrs-overview)
2. [Core Concepts](#core-concepts)
3. [Command Side (Write)](#command-side-write)
4. [Query Side (Read)](#query-side-read)
5. [Read Models & Projections](#read-models--projections)
6. [Event Sourcing Integration](#event-sourcing-integration)
7. [Consistency Models](#consistency-models)
8. [Implementation Patterns](#implementation-patterns)
9. [Best Practices](#best-practices)
10. [Common Challenges](#common-challenges)
11. [Interview Questions & Answers](#interview-questions--answers)

---

## CQRS Overview

### What is CQRS?

**CQRS (Command Query Responsibility Segregation)** is:
- **Separation of Concerns**: Separate read and write models
- **Optimized Models**: Different models for reads and writes
- **Independent Scaling**: Scale read and write sides independently
- **Performance**: Optimize each side for its purpose
- **Flexibility**: Different storage, caching, and query strategies

### Key Principles

1. **Command Side**: Handles writes (commands)
2. **Query Side**: Handles reads (queries)
3. **Separate Models**: Different models for reads and writes
4. **Independent Scaling**: Scale each side independently
5. **Eventual Consistency**: Accept eventual consistency between sides

### Benefits

**Advantages:**
1. **Performance**: Optimize each side independently
2. **Scalability**: Scale read and write sides separately
3. **Flexibility**: Different storage and query strategies
4. **Complexity Management**: Handle complex read/write requirements
5. **Security**: Different security models for reads and writes
6. **Optimization**: Optimize queries without affecting writes

### Drawbacks

**Challenges:**
1. **Complexity**: More complex than traditional CRUD
2. **Eventual Consistency**: May require eventual consistency
3. **Synchronization**: Need to keep read models in sync
4. **Learning Curve**: Team needs to understand the pattern
5. **Overhead**: Additional infrastructure and code

### When to Use CQRS

**Use CQRS When:**
1. **High Read/Write Ratio**: Many more reads than writes
2. **Complex Queries**: Complex read requirements
3. **Performance Issues**: Read/write performance bottlenecks
4. **Different Models**: Read and write models differ significantly
5. **Scalability Needs**: Need to scale reads and writes independently
6. **Event Sourcing**: Using event sourcing

**Don't Use CQRS When:**
1. **Simple CRUD**: Simple create/read/update/delete
2. **Low Complexity**: Simple read/write requirements
3. **Strong Consistency**: Need immediate consistency
4. **Small Team**: Limited expertise
5. **Simple Domain**: Simple business logic

---

## Core Concepts

### Commands

**Command Definition:**

```java
public interface Command {
    String getCommandId();
    Instant getTimestamp();
}

public class CreateOrderCommand implements Command {
    private String commandId;
    private Instant timestamp;
    private String customerId;
    private List<OrderItem> items;
    private Address shippingAddress;
    
    // Getters and setters
}

public class AddItemToOrderCommand implements Command {
    private String commandId;
    private Instant timestamp;
    private String orderId;
    private String productId;
    private int quantity;
    private BigDecimal price;
    
    // Getters and setters
}

public class ShipOrderCommand implements Command {
    private String commandId;
    private Instant timestamp;
    private String orderId;
    private String trackingNumber;
    
    // Getters and setters
}
```

### Queries

**Query Definition:**

```java
public interface Query<T> {
    String getQueryId();
    Instant getTimestamp();
}

public class GetOrderQuery implements Query<OrderView> {
    private String queryId;
    private Instant timestamp;
    private String orderId;
    
    // Getters and setters
}

public class GetOrdersByCustomerQuery implements Query<List<OrderView>> {
    private String queryId;
    private Instant timestamp;
    private String customerId;
    private int page;
    private int size;
    
    // Getters and setters
}

public class GetOrderStatisticsQuery implements Query<OrderStatistics> {
    private String queryId;
    private Instant timestamp;
    private String customerId;
    private LocalDate fromDate;
    private LocalDate toDate;
    
    // Getters and setters
}
```

### Command Handler

**Command Handler Implementation:**

```java
@Component
public class OrderCommandHandler {
    
    private final OrderRepository orderRepository;
    private final EventPublisher eventPublisher;
    
    public OrderCommandHandler(OrderRepository orderRepository, EventPublisher eventPublisher) {
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @CommandHandler
    public void handle(CreateOrderCommand command) {
        // Validate command
        validateCommand(command);
        
        // Create aggregate
        Order order = new Order(
            UUID.randomUUID().toString(),
            command.getCustomerId(),
            command.getItems(),
            command.getShippingAddress()
        );
        
        // Save aggregate
        orderRepository.save(order);
        
        // Publish events
        List<DomainEvent> events = order.getUncommittedEvents();
        eventPublisher.publish(events);
        order.markEventsAsCommitted();
    }
    
    @CommandHandler
    public void handle(AddItemToOrderCommand command) {
        // Load aggregate
        Order order = orderRepository.findById(command.getOrderId());
        if (order == null) {
            throw new OrderNotFoundException(command.getOrderId());
        }
        
        // Execute command
        order.addItem(command.getProductId(), command.getQuantity(), command.getPrice());
        
        // Save aggregate
        orderRepository.save(order);
        
        // Publish events
        List<DomainEvent> events = order.getUncommittedEvents();
        eventPublisher.publish(events);
        order.markEventsAsCommitted();
    }
    
    @CommandHandler
    public void handle(ShipOrderCommand command) {
        // Load aggregate
        Order order = orderRepository.findById(command.getOrderId());
        if (order == null) {
            throw new OrderNotFoundException(command.getOrderId());
        }
        
        // Execute command
        order.ship(command.getTrackingNumber());
        
        // Save aggregate
        orderRepository.save(order);
        
        // Publish events
        List<DomainEvent> events = order.getUncommittedEvents();
        eventPublisher.publish(events);
        order.markEventsAsCommitted();
    }
    
    private void validateCommand(CreateOrderCommand command) {
        if (command.getCustomerId() == null || command.getCustomerId().isEmpty()) {
            throw new ValidationException("Customer ID is required");
        }
        if (command.getItems() == null || command.getItems().isEmpty()) {
            throw new ValidationException("Order must have at least one item");
        }
    }
}
```

### Query Handler

**Query Handler Implementation:**

```java
@Component
public class OrderQueryHandler {
    
    private final OrderReadModelRepository readModelRepository;
    
    public OrderQueryHandler(OrderReadModelRepository readModelRepository) {
        this.readModelRepository = readModelRepository;
    }
    
    @QueryHandler
    public OrderView handle(GetOrderQuery query) {
        OrderView order = readModelRepository.findById(query.getOrderId());
        if (order == null) {
            throw new OrderNotFoundException(query.getOrderId());
        }
        return order;
    }
    
    @QueryHandler
    public List<OrderView> handle(GetOrdersByCustomerQuery query) {
        return readModelRepository.findByCustomerId(
            query.getCustomerId(),
            PageRequest.of(query.getPage(), query.getSize())
        );
    }
    
    @QueryHandler
    public OrderStatistics handle(GetOrderStatisticsQuery query) {
        return readModelRepository.getStatistics(
            query.getCustomerId(),
            query.getFromDate(),
            query.getToDate()
        );
    }
}
```

---

## Command Side (Write)

### Command Model

**Write Model (Domain Model):**

```java
public class Order {
    private String orderId;
    private String customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItem> items;
    private Address shippingAddress;
    private String trackingNumber;
    private List<DomainEvent> uncommittedEvents;
    
    public Order(String orderId, String customerId, List<OrderItem> items, Address shippingAddress) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.CREATED;
        this.totalAmount = calculateTotal(items);
        this.uncommittedEvents = new ArrayList<>();
        
        // Raise event
        raiseEvent(new OrderCreatedEvent(
            UUID.randomUUID().toString(),
            orderId,
            Instant.now(),
            customerId,
            totalAmount,
            items,
            shippingAddress
        ));
    }
    
    public void addItem(String productId, int quantity, BigDecimal price) {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot add items to " + status + " order");
        }
        
        OrderItem item = new OrderItem(productId, quantity, price);
        items.add(item);
        totalAmount = calculateTotal(items);
        
        // Raise event
        raiseEvent(new OrderItemAddedEvent(
            UUID.randomUUID().toString(),
            orderId,
            Instant.now(),
            productId,
            quantity,
            price
        ));
    }
    
    public void confirm() {
        if (status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order must be created before confirmation");
        }
        
        this.status = OrderStatus.CONFIRMED;
        
        // Raise event
        raiseEvent(new OrderConfirmedEvent(
            UUID.randomUUID().toString(),
            orderId,
            Instant.now()
        ));
    }
    
    public void ship(String trackingNumber) {
        if (status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be confirmed before shipping");
        }
        
        this.status = OrderStatus.SHIPPED;
        this.trackingNumber = trackingNumber;
        
        // Raise event
        raiseEvent(new OrderShippedEvent(
            UUID.randomUUID().toString(),
            orderId,
            Instant.now(),
            trackingNumber,
            shippingAddress
        ));
    }
    
    private void raiseEvent(DomainEvent event) {
        uncommittedEvents.add(event);
    }
    
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }
    
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    private BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public List<OrderItem> getItems() { return new ArrayList<>(items); }
    public Address getShippingAddress() { return shippingAddress; }
    public String getTrackingNumber() { return trackingNumber; }
}
```

### Command Repository

**Write Repository:**

```java
@Repository
public class OrderRepository {
    
    private final EventStore eventStore;
    
    public OrderRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    public Order findById(String orderId) {
        // Get all events for this aggregate
        List<DomainEvent> events = eventStore.getEvents(orderId);
        
        if (events.isEmpty()) {
            return null;
        }
        
        // Reconstruct aggregate from events
        return Order.fromEvents(events);
    }
    
    public void save(Order order) {
        // Get current version
        List<DomainEvent> existingEvents = eventStore.getEvents(order.getOrderId());
        long currentVersion = existingEvents.size();
        
        // Append new events
        List<DomainEvent> newEvents = order.getUncommittedEvents();
        eventStore.appendEvents(
            order.getOrderId(),
            newEvents,
            currentVersion
        );
        
        // Mark events as committed
        order.markEventsAsCommitted();
    }
}
```

### Command Controller

**REST API for Commands:**

```java
@RestController
@RequestMapping("/api/commands/orders")
public class OrderCommandController {
    
    private final OrderCommandHandler commandHandler;
    
    public OrderCommandController(OrderCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }
    
    @PostMapping
    public ResponseEntity<CommandResponse> createOrder(@RequestBody CreateOrderCommand command) {
        try {
            commandHandler.handle(command);
            return ResponseEntity.ok(new CommandResponse(
                command.getCommandId(),
                "Order created successfully",
                HttpStatus.OK.value()
            ));
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(new CommandResponse(
                command.getCommandId(),
                e.getMessage(),
                HttpStatus.BAD_REQUEST.value()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new CommandResponse(
                command.getCommandId(),
                "Internal server error",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
            ));
        }
    }
    
    @PostMapping("/{orderId}/items")
    public ResponseEntity<CommandResponse> addItem(
            @PathVariable String orderId,
            @RequestBody AddItemToOrderCommand command) {
        command.setOrderId(orderId);
        try {
            commandHandler.handle(command);
            return ResponseEntity.ok(new CommandResponse(
                command.getCommandId(),
                "Item added successfully",
                HttpStatus.OK.value()
            ));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/{orderId}/ship")
    public ResponseEntity<CommandResponse> shipOrder(
            @PathVariable String orderId,
            @RequestBody ShipOrderCommand command) {
        command.setOrderId(orderId);
        try {
            commandHandler.handle(command);
            return ResponseEntity.ok(new CommandResponse(
                command.getCommandId(),
                "Order shipped successfully",
                HttpStatus.OK.value()
            ));
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
```

---

## Query Side (Read)

### Read Model

**Read Model (View Model):**

```java
public class OrderView {
    private String orderId;
    private String customerId;
    private String customerName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemView> items;
    private AddressView shippingAddress;
    private String trackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
    
    // Getters and setters
}

public class OrderItemView {
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal subtotal;
    
    // Getters and setters
}

public class AddressView {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    
    // Getters and setters
}

public class OrderStatistics {
    private String customerId;
    private int totalOrders;
    private BigDecimal totalAmount;
    private BigDecimal averageOrderValue;
    private LocalDate fromDate;
    private LocalDate toDate;
    
    // Getters and setters
}
```

### Read Model Repository

**Read Model Repository:**

```java
@Repository
public class OrderReadModelRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public OrderReadModelRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public OrderView findById(String orderId) {
        return jdbcTemplate.queryForObject(
            "SELECT * FROM order_view WHERE order_id = ?",
            new Object[]{orderId},
            (rs, rowNum) -> mapToOrderView(rs)
        );
    }
    
    public List<OrderView> findByCustomerId(String customerId, Pageable pageable) {
        return jdbcTemplate.query(
            "SELECT * FROM order_view WHERE customer_id = ? " +
            "ORDER BY created_at DESC LIMIT ? OFFSET ?",
            new Object[]{
                customerId,
                pageable.getPageSize(),
                pageable.getOffset()
            },
            (rs, rowNum) -> mapToOrderView(rs)
        );
    }
    
    public OrderStatistics getStatistics(String customerId, LocalDate fromDate, LocalDate toDate) {
        return jdbcTemplate.queryForObject(
            "SELECT " +
            "  COUNT(*) as total_orders, " +
            "  SUM(total_amount) as total_amount, " +
            "  AVG(total_amount) as average_order_value " +
            "FROM order_view " +
            "WHERE customer_id = ? AND created_at BETWEEN ? AND ?",
            new Object[]{customerId, fromDate, toDate},
            (rs, rowNum) -> {
                OrderStatistics stats = new OrderStatistics();
                stats.setCustomerId(customerId);
                stats.setTotalOrders(rs.getInt("total_orders"));
                stats.setTotalAmount(rs.getBigDecimal("total_amount"));
                stats.setAverageOrderValue(rs.getBigDecimal("average_order_value"));
                stats.setFromDate(fromDate);
                stats.setToDate(toDate);
                return stats;
            }
        );
    }
    
    public void save(OrderView orderView) {
        jdbcTemplate.update(
            "INSERT INTO order_view " +
            "(order_id, customer_id, customer_name, status, total_amount, " +
            "shipping_address, tracking_number, created_at, updated_at, shipped_at) " +
            "VALUES (?, ?, ?, ?, ?, ?::jsonb, ?, ?, ?, ?) " +
            "ON CONFLICT (order_id) DO UPDATE SET " +
            "status = EXCLUDED.status, " +
            "total_amount = EXCLUDED.total_amount, " +
            "tracking_number = EXCLUDED.tracking_number, " +
            "updated_at = EXCLUDED.updated_at, " +
            "shipped_at = EXCLUDED.shipped_at",
            orderView.getOrderId(),
            orderView.getCustomerId(),
            orderView.getCustomerName(),
            orderView.getStatus().toString(),
            orderView.getTotalAmount(),
            serializeAddress(orderView.getShippingAddress()),
            orderView.getTrackingNumber(),
            orderView.getCreatedAt(),
            orderView.getUpdatedAt(),
            orderView.getShippedAt()
        );
    }
    
    private OrderView mapToOrderView(ResultSet rs) throws SQLException {
        OrderView view = new OrderView();
        view.setOrderId(rs.getString("order_id"));
        view.setCustomerId(rs.getString("customer_id"));
        view.setCustomerName(rs.getString("customer_name"));
        view.setStatus(OrderStatus.valueOf(rs.getString("status")));
        view.setTotalAmount(rs.getBigDecimal("total_amount"));
        view.setShippingAddress(deserializeAddress(rs.getString("shipping_address")));
        view.setTrackingNumber(rs.getString("tracking_number"));
        view.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        view.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        if (rs.getTimestamp("shipped_at") != null) {
            view.setShippedAt(rs.getTimestamp("shipped_at").toLocalDateTime());
        }
        return view;
    }
}
```

### Query Controller

**REST API for Queries:**

```java
@RestController
@RequestMapping("/api/queries/orders")
public class OrderQueryController {
    
    private final OrderQueryHandler queryHandler;
    
    public OrderQueryController(OrderQueryHandler queryHandler) {
        this.queryHandler = queryHandler;
    }
    
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderView> getOrder(@PathVariable String orderId) {
        try {
            GetOrderQuery query = new GetOrderQuery(UUID.randomUUID().toString(), Instant.now(), orderId);
            OrderView order = queryHandler.handle(query);
            return ResponseEntity.ok(order);
        } catch (OrderNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderView>> getOrdersByCustomer(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        GetOrdersByCustomerQuery query = new GetOrdersByCustomerQuery(
            UUID.randomUUID().toString(),
            Instant.now(),
            customerId,
            page,
            size
        );
        List<OrderView> orders = queryHandler.handle(query);
        return ResponseEntity.ok(orders);
    }
    
    @GetMapping("/customer/{customerId}/statistics")
    public ResponseEntity<OrderStatistics> getOrderStatistics(
            @PathVariable String customerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        GetOrderStatisticsQuery query = new GetOrderStatisticsQuery(
            UUID.randomUUID().toString(),
            Instant.now(),
            customerId,
            fromDate,
            toDate
        );
        OrderStatistics statistics = queryHandler.handle(query);
        return ResponseEntity.ok(statistics);
    }
}
```

---

## Read Models & Projections

### Event Handlers (Projections)

**Projection Handlers:**

```java
@Component
public class OrderProjection {
    
    private final OrderReadModelRepository readModelRepository;
    private final CustomerService customerService;
    
    public OrderProjection(OrderReadModelRepository readModelRepository, CustomerService customerService) {
        this.readModelRepository = readModelRepository;
        this.customerService = customerService;
    }
    
    @EventHandler
    public void handle(OrderCreatedEvent event) {
        // Get customer information
        Customer customer = customerService.findById(event.getCustomerId());
        
        // Create read model
        OrderView view = new OrderView();
        view.setOrderId(event.getOrderId());
        view.setCustomerId(event.getCustomerId());
        view.setCustomerName(customer.getName());
        view.setStatus(OrderStatus.CREATED);
        view.setTotalAmount(event.getTotalAmount());
        view.setShippingAddress(mapAddress(event.getShippingAddress()));
        view.setCreatedAt(event.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime());
        view.setUpdatedAt(event.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        // Map items
        List<OrderItemView> items = event.getItems().stream()
                .map(this::mapOrderItem)
                .collect(Collectors.toList());
        view.setItems(items);
        
        // Save read model
        readModelRepository.save(view);
    }
    
    @EventHandler
    public void handle(OrderItemAddedEvent event) {
        // Load existing read model
        OrderView view = readModelRepository.findById(event.getOrderId());
        if (view == null) {
            // Handle case where read model doesn't exist yet
            return;
        }
        
        // Update total amount
        BigDecimal newTotal = view.getTotalAmount().add(
            event.getPrice().multiply(BigDecimal.valueOf(event.getQuantity()))
        );
        view.setTotalAmount(newTotal);
        
        // Add item to view
        OrderItemView itemView = new OrderItemView();
        itemView.setProductId(event.getProductId());
        itemView.setQuantity(event.getQuantity());
        itemView.setPrice(event.getPrice());
        itemView.setSubtotal(event.getPrice().multiply(BigDecimal.valueOf(event.getQuantity())));
        
        view.getItems().add(itemView);
        view.setUpdatedAt(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        // Save read model
        readModelRepository.save(view);
    }
    
    @EventHandler
    public void handle(OrderConfirmedEvent event) {
        OrderView view = readModelRepository.findById(event.getOrderId());
        if (view == null) {
            return;
        }
        
        view.setStatus(OrderStatus.CONFIRMED);
        view.setUpdatedAt(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        readModelRepository.save(view);
    }
    
    @EventHandler
    public void handle(OrderShippedEvent event) {
        OrderView view = readModelRepository.findById(event.getOrderId());
        if (view == null) {
            return;
        }
        
        view.setStatus(OrderStatus.SHIPPED);
        view.setTrackingNumber(event.getTrackingNumber());
        view.setShippedAt(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
        view.setUpdatedAt(Instant.now().atZone(ZoneId.systemDefault()).toLocalDateTime());
        
        readModelRepository.save(view);
    }
    
    private AddressView mapAddress(Address address) {
        AddressView view = new AddressView();
        view.setStreet(address.getStreet());
        view.setCity(address.getCity());
        view.setState(address.getState());
        view.setZipCode(address.getZipCode());
        view.setCountry(address.getCountry());
        return view;
    }
    
    private OrderItemView mapOrderItem(OrderItem item) {
        OrderItemView view = new OrderItemView();
        view.setProductId(item.getProductId());
        view.setQuantity(item.getQuantity());
        view.setPrice(item.getPrice());
        view.setSubtotal(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        return view;
    }
}
```

### Multiple Read Models

**Different Read Models for Different Queries:**

```java
// Read model for order list
public class OrderListView {
    private String orderId;
    private String customerId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}

// Read model for order details
public class OrderDetailView {
    private String orderId;
    private String customerId;
    private String customerName;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemView> items;
    private AddressView shippingAddress;
    private String trackingNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime shippedAt;
}

// Read model for order statistics
public class OrderStatisticsView {
    private String customerId;
    private int totalOrders;
    private BigDecimal totalAmount;
    private BigDecimal averageOrderValue;
    private Map<OrderStatus, Integer> ordersByStatus;
}
```

---

## Event Sourcing Integration

### CQRS + Event Sourcing

**Combined Pattern:**

```java
// Command side uses Event Sourcing
@Repository
public class OrderRepository {
    private final EventStore eventStore;
    
    public Order findById(String orderId) {
        List<DomainEvent> events = eventStore.getEvents(orderId);
        return Order.fromEvents(events);
    }
    
    public void save(Order order) {
        List<DomainEvent> events = order.getUncommittedEvents();
        eventStore.appendEvents(order.getOrderId(), events, getCurrentVersion(order.getOrderId()));
        order.markEventsAsCommitted();
    }
}

// Query side uses Read Models
@Repository
public class OrderReadModelRepository {
    public OrderView findById(String orderId) {
        // Query optimized read model
        return jdbcTemplate.queryForObject(...);
    }
}

// Projections update read models from events
@Component
public class OrderProjection {
    @EventHandler
    public void handle(OrderCreatedEvent event) {
        // Update read model
        readModelRepository.save(createOrderView(event));
    }
}
```

---

## Consistency Models

### Eventual Consistency

**Eventual Consistency Model:**

```java
// Command side (Write)
@CommandHandler
public void handle(CreateOrderCommand command) {
    Order order = new Order(...);
    orderRepository.save(order);  // Write to event store
    
    // Events published asynchronously
    eventPublisher.publish(order.getUncommittedEvents());
}

// Query side (Read)
@QueryHandler
public OrderView handle(GetOrderQuery query) {
    // Read from read model (may be slightly stale)
    return readModelRepository.findById(query.getOrderId());
}

// Projection updates read model asynchronously
@EventHandler
public void handle(OrderCreatedEvent event) {
    // Update read model (eventual consistency)
    readModelRepository.save(createOrderView(event));
}
```

### Strong Consistency

**Strong Consistency (Read Your Writes):**

```java
@CommandHandler
public void handle(CreateOrderCommand command) {
    Order order = new Order(...);
    orderRepository.save(order);
    
    // Publish events synchronously
    List<DomainEvent> events = order.getUncommittedEvents();
    eventPublisher.publishSync(events);
    
    // Update read model synchronously
    projection.handle(events.get(0));
}

// Query after write returns updated read model
@QueryHandler
public OrderView handle(GetOrderQuery query) {
    // Read from read model (guaranteed to be up-to-date)
    return readModelRepository.findById(query.getOrderId());
}
```

### Consistency Strategies

**Different Consistency Levels:**

1. **Eventual Consistency**: Default, best performance
2. **Read Your Writes**: Strong consistency for user's own writes
3. **Strong Consistency**: Synchronous updates (slower)
4. **Causal Consistency**: Maintains causal relationships

---

## Implementation Patterns

### Mediator Pattern

**Command/Query Mediator:**

```java
public interface IMediator {
    <T> T send(Command command);
    <T> T send(Query<T> query);
}

@Component
public class Mediator implements IMediator {
    
    private final ApplicationContext applicationContext;
    
    public Mediator(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    
    @Override
    public <T> T send(Command command) {
        Class<?> handlerType = getHandlerType(command);
        CommandHandler handler = (CommandHandler) applicationContext.getBean(handlerType);
        handler.handle(command);
        return null;
    }
    
    @Override
    public <T> T send(Query<T> query) {
        Class<?> handlerType = getHandlerType(query);
        QueryHandler handler = (QueryHandler) applicationContext.getBean(handlerType);
        return (T) handler.handle(query);
    }
    
    private Class<?> getHandlerType(Object message) {
        // Resolve handler type from message
        return CommandHandler.class; // Simplified
    }
}
```

### Dispatcher Pattern

**Command/Query Dispatcher:**

```java
@Component
public class CommandDispatcher {
    
    private final Map<Class<? extends Command>, CommandHandler> handlers;
    
    public CommandDispatcher(List<CommandHandler> handlers) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(
                    handler -> getCommandType(handler),
                    handler -> handler
                ));
    }
    
    public void dispatch(Command command) {
        CommandHandler handler = handlers.get(command.getClass());
        if (handler == null) {
            throw new HandlerNotFoundException("No handler for " + command.getClass());
        }
        handler.handle(command);
    }
    
    private Class<? extends Command> getCommandType(CommandHandler handler) {
        // Extract command type from handler
        return CreateOrderCommand.class; // Simplified
    }
}
```

---

## Best Practices

### Command Design

1. **Idempotency**: Make commands idempotent when possible
2. **Validation**: Validate commands before execution
3. **Atomicity**: Keep commands atomic
4. **Naming**: Use clear, action-oriented names
5. **Size**: Keep commands reasonably sized

### Query Design

1. **Optimized Models**: Design read models for queries
2. **Denormalization**: Denormalize for performance
3. **Caching**: Cache frequently accessed queries
4. **Indexing**: Index read model tables properly
5. **Pagination**: Always paginate large result sets

### Projection Design

1. **Idempotency**: Make projections idempotent
2. **Error Handling**: Handle projection errors gracefully
3. **Replay**: Support replaying projections
4. **Performance**: Optimize projection performance
5. **Monitoring**: Monitor projection lag

---

## Common Challenges

### Challenge 1: Eventual Consistency

**Problem:** Read models may be stale.

**Solution:**
- Accept eventual consistency
- Use read-your-writes for user's own data
- Show last updated timestamp
- Use strong consistency when needed

### Challenge 2: Projection Lag

**Problem:** Read models lag behind writes.

**Solution:**
- Monitor projection lag
- Optimize projection performance
- Use parallel processing
- Consider synchronous updates for critical reads

### Challenge 3: Read Model Synchronization

**Problem:** Keeping read models in sync.

**Solution:**
- Use event handlers (projections)
- Make projections idempotent
- Support replaying projections
- Handle projection errors

### Challenge 4: Complex Queries

**Problem:** Complex queries across aggregates.

**Solution:**
- Create specialized read models
- Denormalize data
- Use materialized views
- Consider separate query services

---

## Interview Questions & Answers

### Q1: What is CQRS?

**Answer:**
- **CQRS**: Command Query Responsibility Segregation
- **Separation**: Separate read and write models
- **Optimization**: Optimize each side independently
- **Scaling**: Scale read and write sides separately
- **Performance**: Better performance for complex scenarios

### Q2: What is the difference between Command and Query?

**Answer:**
- **Command**: Changes state, returns void or command result
- **Query**: Reads state, returns data
- **Command**: Handled by command handlers
- **Query**: Handled by query handlers
- **Command**: May raise events
- **Query**: Should not change state

### Q3: What are Read Models?

**Answer:**
- **Read Models**: Optimized views for queries
- **Denormalized**: Denormalized for performance
- **Projections**: Updated by event handlers
- **Optimized**: Optimized for specific queries
- **Separate**: Separate from write models

### Q4: What is Eventual Consistency?

**Answer:**
- **Eventual Consistency**: Read models may be slightly stale
- **Asynchronous**: Updates happen asynchronously
- **Acceptable**: Acceptable for most use cases
- **Trade-off**: Trade-off for performance and scalability
- **Strategies**: Use read-your-writes or strong consistency when needed

### Q5: How do you keep Read Models in sync?

**Answer:**
- **Event Handlers**: Use event handlers (projections)
- **Idempotency**: Make projections idempotent
- **Replay**: Support replaying projections
- **Error Handling**: Handle projection errors gracefully
- **Monitoring**: Monitor projection lag

### Q6: When should you use CQRS?

**Answer:**
- **High Read/Write Ratio**: Many more reads than writes
- **Complex Queries**: Complex read requirements
- **Performance Issues**: Read/write performance bottlenecks
- **Different Models**: Read and write models differ significantly
- **Scalability**: Need to scale reads and writes independently
- **Event Sourcing**: Using event sourcing

### Q7: What is the relationship between CQRS and Event Sourcing?

**Answer:**
- **Complementary**: Often used together
- **Event Sourcing**: Store events instead of current state
- **CQRS**: Separate read and write models
- **Command Side**: Event sourcing for write side
- **Query Side**: Read models for query side
- **Projections**: Events update read models

### Q8: How do you handle projection failures?

**Answer:**
- **Retry**: Retry failed projections
- **Dead Letter Queue**: Move to dead letter queue
- **Monitoring**: Monitor projection failures
- **Replay**: Support replaying failed projections
- **Error Handling**: Handle errors gracefully

### Q9: What are the benefits of CQRS?

**Answer:**
1. **Performance**: Optimize each side independently
2. **Scalability**: Scale read and write sides separately
3. **Flexibility**: Different storage and query strategies
4. **Complexity Management**: Handle complex read/write requirements
5. **Security**: Different security models for reads and writes
6. **Optimization**: Optimize queries without affecting writes

### Q10: What are the drawbacks of CQRS?

**Answer:**
1. **Complexity**: More complex than traditional CRUD
2. **Eventual Consistency**: May require eventual consistency
3. **Synchronization**: Need to keep read models in sync
4. **Learning Curve**: Team needs to understand the pattern
5. **Overhead**: Additional infrastructure and code

---

## Summary

**Key Takeaways:**
1. **CQRS**: Separate read and write models
2. **Commands**: Handle writes (state changes)
3. **Queries**: Handle reads (data retrieval)
4. **Read Models**: Optimized views for queries
5. **Projections**: Update read models from events
6. **Eventual Consistency**: Accept eventual consistency

**Complete Coverage:**
- CQRS overview and principles
- Command side (write) implementation
- Query side (read) implementation
- Read models and projections
- Event sourcing integration
- Consistency models
- Implementation patterns
- Best practices and common challenges
- Interview Q&A

---

**Guide Complete** - Ready for interview preparation!


# Observer Pattern: Event-Driven, Pub/Sub Pattern

## Overview

The Observer pattern defines a one-to-many dependency between objects so that when one object changes state, all its dependents are notified and updated automatically. It's the foundation of event-driven architectures and the publish-subscribe pattern.

## Observer Pattern Structure

```
┌─────────────────────────────────────────────────────────┐
│              Observer Pattern Structure                  │
└─────────────────────────────────────────────────────────┘

        Subject (Observable)
    ┌──────────────────────┐
    │ - observers: List   │
    │ + attach(Observer)  │
    │ + detach(Observer)  │
    │ + notify()          │
    └──────────┬───────────┘
               │
               │ notifies
               ▼
        Observer (Interface)
    ┌──────────────────────┐
    │ + update()           │
    └──────────┬───────────┘
               │
    ┌───────────┼───────────┐
    │           │           │
    ▼           ▼           ▼
ConcreteObserver1  ConcreteObserver2  ConcreteObserver3
    │               │               │
    └─► update()    └─► update()    └─► update()
```

## Basic Observer Example

### Weather Station

```java
// Observer interface
public interface Observer {
    void update(String message);
}

// Subject (Observable)
public class WeatherStation {
    private List<Observer> observers = new ArrayList<>();
    private String weather;
    
    public void attach(Observer observer) {
        observers.add(observer);
    }
    
    public void detach(Observer observer) {
        observers.remove(observer);
    }
    
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(weather);
        }
    }
    
    public void setWeather(String weather) {
        this.weather = weather;
        notifyObservers();  // Notify all observers
    }
    
    public String getWeather() {
        return weather;
    }
}

// Concrete Observers
public class PhoneDisplay implements Observer {
    @Override
    public void update(String weather) {
        System.out.println("Phone Display: Weather updated to " + weather);
    }
}

public class TVDisplay implements Observer {
    @Override
    public void update(String weather) {
        System.out.println("TV Display: Weather updated to " + weather);
    }
}

public class WebDisplay implements Observer {
    @Override
    public void update(String weather) {
        System.out.println("Web Display: Weather updated to " + weather);
    }
}

// Usage
WeatherStation station = new WeatherStation();

Observer phone = new PhoneDisplay();
Observer tv = new TVDisplay();
Observer web = new WebDisplay();

station.attach(phone);
station.attach(tv);
station.attach(web);

station.setWeather("Sunny");  // All observers notified
// Output:
// Phone Display: Weather updated to Sunny
// TV Display: Weather updated to Sunny
// Web Display: Weather updated to Sunny

station.setWeather("Rainy");  // All observers notified again
```

## Java Built-in Observer (Deprecated but Educational)

```java
import java.util.Observable;
import java.util.Observer;

// Subject (Observable)
public class NewsAgency extends Observable {
    private String news;
    
    public void setNews(String news) {
        this.news = news;
        setChanged();  // Mark as changed
        notifyObservers(news);  // Notify observers
    }
}

// Observer
public class NewsChannel implements Observer {
    private String name;
    
    public NewsChannel(String name) {
        this.name = name;
    }
    
    @Override
    public void update(Observable o, Object arg) {
        System.out.println(name + " received news: " + arg);
    }
}

// Usage
NewsAgency agency = new NewsAgency();
agency.addObserver(new NewsChannel("CNN"));
agency.addObserver(new NewsChannel("BBC"));

agency.setNews("Breaking: Important news!");
```

## Modern Observer Implementation

### Using Java 8+ Features

```java
// Subject with functional interface
public class EventSource {
    private List<Consumer<String>> listeners = new ArrayList<>();
    
    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }
    
    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }
    
    public void notifyListeners(String event) {
        listeners.forEach(listener -> listener.accept(event));
    }
}

// Usage with lambda
EventSource source = new EventSource();
source.addListener(event -> System.out.println("Listener 1: " + event));
source.addListener(event -> System.out.println("Listener 2: " + event));

source.notifyListeners("Event occurred");
```

## Real-World Examples

### 1. Stock Market Observer

```java
// Subject
public class StockMarket {
    private Map<String, Double> stocks = new HashMap<>();
    private List<StockObserver> observers = new ArrayList<>();
    
    public void attach(StockObserver observer) {
        observers.add(observer);
    }
    
    public void updateStock(String symbol, double price) {
        stocks.put(symbol, price);
        notifyObservers(symbol, price);
    }
    
    private void notifyObservers(String symbol, double price) {
        for (StockObserver observer : observers) {
            observer.onStockUpdate(symbol, price);
        }
    }
}

// Observer interface
public interface StockObserver {
    void onStockUpdate(String symbol, double price);
}

// Concrete observers
public class StockTrader implements StockObserver {
    private String name;
    
    public StockTrader(String name) {
        this.name = name;
    }
    
    @Override
    public void onStockUpdate(String symbol, double price) {
        System.out.println(name + " notified: " + symbol + " = $" + price);
        // Trading logic based on price
        if (price > 100) {
            System.out.println(name + " selling " + symbol);
        }
    }
}

public class StockAnalyst implements StockObserver {
    @Override
    public void onStockUpdate(String symbol, double price) {
        System.out.println("Analyst: Analyzing " + symbol + " at $" + price);
        // Analysis logic
    }
}
```

### 2. Model-View-Controller (MVC)

```java
// Model (Subject)
public class UserModel {
    private String name;
    private String email;
    private List<ViewObserver> views = new ArrayList<>();
    
    public void attachView(ViewObserver view) {
        views.add(view);
    }
    
    public void setName(String name) {
        this.name = name;
        notifyViews();
    }
    
    public void setEmail(String email) {
        this.email = email;
        notifyViews();
    }
    
    private void notifyViews() {
        for (ViewObserver view : views) {
            view.update(name, email);
        }
    }
}

// View (Observer)
public interface ViewObserver {
    void update(String name, String email);
}

public class UserView implements ViewObserver {
    @Override
    public void update(String name, String email) {
        System.out.println("View updated: " + name + " - " + email);
        // Update UI
    }
}
```

### 3. Event Bus (Pub/Sub)

```java
// Event Bus (Subject)
public class EventBus {
    private Map<Class<?>, List<EventHandler>> handlers = new HashMap<>();
    
    public <T> void subscribe(Class<T> eventType, EventHandler<T> handler) {
        handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
    }
    
    public <T> void publish(T event) {
        List<EventHandler> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers != null) {
            for (EventHandler handler : eventHandlers) {
                handler.handle(event);
            }
        }
    }
}

// Event Handler
public interface EventHandler<T> {
    void handle(T event);
}

// Events
public class UserCreatedEvent {
    private String userId;
    private String email;
    // ...
}

public class OrderPlacedEvent {
    private String orderId;
    private double amount;
    // ...
}

// Event Handlers
public class EmailService implements EventHandler<UserCreatedEvent> {
    @Override
    public void handle(UserCreatedEvent event) {
        System.out.println("Sending welcome email to " + event.getEmail());
    }
}

public class AnalyticsService implements EventHandler<OrderPlacedEvent> {
    @Override
    public void handle(OrderPlacedEvent event) {
        System.out.println("Recording order in analytics: " + event.getOrderId());
    }
}

// Usage
EventBus bus = new EventBus();
bus.subscribe(UserCreatedEvent.class, new EmailService());
bus.subscribe(OrderPlacedEvent.class, new AnalyticsService());

bus.publish(new UserCreatedEvent("123", "user@example.com"));
bus.publish(new OrderPlacedEvent("order-456", 99.99));
```

## Observer Pattern Flow

```
┌─────────────────────────────────────────────────────────┐
│              Observer Pattern Flow                      │
└─────────────────────────────────────────────────────────┘

Subject State Changes
    │
    ▼
Subject.notify()
    │
    ├─► Observer1.update()
    ├─► Observer2.update()
    └─► Observer3.update()
    │
    ▼
Observers React to Change
```

## Observer Pattern Benefits

```
┌─────────────────────────────────────────────────────────┐
│              Observer Pattern Benefits                   │
└─────────────────────────────────────────────────────────┘

1. Loose Coupling
   └─ Subject doesn't know concrete observers
   └─ Observers can be added/removed dynamically

2. Broadcast Communication
   └─ One-to-many notification
   └─ Efficient event distribution

3. Open/Closed Principle
   └─ Easy to add new observers
   └─ No modification to subject

4. Event-Driven Architecture
   └─ Foundation for event systems
   └─ Reactive programming support
```

## Best Practices

### 1. Use Weak References for Memory Management

```java
public class WeakObserverList {
    private List<WeakReference<Observer>> observers = new ArrayList<>();
    
    public void addObserver(Observer observer) {
        observers.add(new WeakReference<>(observer));
    }
    
    public void notifyObservers() {
        observers.removeIf(ref -> ref.get() == null);  // Remove GC'd observers
        observers.forEach(ref -> {
            Observer obs = ref.get();
            if (obs != null) obs.update();
        });
    }
}
```

### 2. Provide Unsubscribe Mechanism

```java
public interface Subscription {
    void unsubscribe();
}

public class Observable {
    private List<Observer> observers = new ArrayList<>();
    
    public Subscription subscribe(Observer observer) {
        observers.add(observer);
        return () -> observers.remove(observer);  // Return unsubscribe function
    }
}
```

### 3. Handle Exceptions in Observers

```java
public void notifyObservers() {
    for (Observer observer : observers) {
        try {
            observer.update();
        } catch (Exception e) {
            // Log error, don't let one observer break others
            logger.error("Observer failed", e);
        }
    }
}
```

## Summary

Observer Pattern:
- **Purpose**: Define one-to-many dependency for automatic notification
- **Key Feature**: Event-driven, publish-subscribe mechanism
- **Use Cases**: Event systems, MVC, UI updates, event buses
- **Benefits**: Loose coupling, broadcast communication, extensibility

**Key Takeaways:**
- ✅ Use for event-driven architectures
- ✅ Enables loose coupling between subject and observers
- ✅ Supports dynamic observer registration
- ✅ Foundation for pub/sub systems
- ✅ Essential for MVC pattern

**Remember**: Observer pattern is perfect for implementing event-driven systems and maintaining loose coupling between components!

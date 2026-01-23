# Domain-Specific Answers - Part 15: Overnight Funding & Trading Systems (Q71-75)

## Question 72: You "processed 500K+ funding calculations daily." How did you design for this?

### Answer

### High-Volume Funding Calculation Design

#### 1. **Scalability Design**

```java
@Service
public class HighVolumeFundingService {
    @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
    public void processFundingCalculations() {
        LocalDate fundingDate = LocalDate.now().minusDays(1);
        
        // Get all positions (from cache)
        Map<String, Position> positions = positionCache.getAll();
        
        // Process in parallel batches
        int batchSize = 10000;
        List<List<Map.Entry<String, Position>>> batches = partition(
            new ArrayList<>(positions.entrySet()), batchSize);
        
        batches.parallelStream()
            .forEach(batch -> processBatch(batch, fundingDate));
    }
    
    private void processBatch(List<Map.Entry<String, Position>> batch, 
                              LocalDate fundingDate) {
        LIBORRate liborRate = liborRateCache.get(fundingDate);
        
        for (Map.Entry<String, Position> entry : batch) {
            Position position = entry.getValue();
            
            // Calculate funding
            BigDecimal funding = calculateFunding(position, liborRate);
            
            // Create ledger entry (async)
            createFundingLedgerEntryAsync(position, funding, fundingDate);
        }
    }
}
```

---

## Question 73: How do you integrate position/instrument details via Kafka?

### Answer

### Kafka Integration for Positions

#### 1. **Event Model**

```java
public class PositionEvent {
    private String accountId;
    private String instrumentId;
    private BigDecimal quantity;
    private BigDecimal currentPrice;
    private Instant timestamp;
    private PositionEventType type;  // CREATED, UPDATED, CLOSED
}
```

#### 2. **Consumer Implementation**

```java
@Service
public class PositionEventConsumer {
    private final Map<String, Position> positionCache = new ConcurrentHashMap<>();
    
    @KafkaListener(topics = "position-events", 
                   groupId = "funding-service",
                   concurrency = "5")
    public void handlePositionEvent(PositionEvent event) {
        String key = event.getAccountId() + ":" + event.getInstrumentId();
        
        switch (event.getType()) {
            case CREATED:
            case UPDATED:
                // Update cache
                Position position = Position.builder()
                    .accountId(event.getAccountId())
                    .instrumentId(event.getInstrumentId())
                    .quantity(event.getQuantity())
                    .currentPrice(event.getCurrentPrice())
                    .lastUpdated(event.getTimestamp())
                    .build();
                positionCache.put(key, position);
                break;
                
            case CLOSED:
                // Remove from cache
                positionCache.remove(key);
                break;
        }
    }
}
```

---

## Question 74: How do you handle trade-level LIBOR rates via JMS?

### Answer

### JMS Integration for LIBOR Rates

#### 1. **JMS Consumer**

```java
@Service
public class LIBORRateConsumer {
    private final Map<LocalDate, LIBORRate> liborRateCache = new ConcurrentHashMap<>();
    
    @JmsListener(destination = "libor-rates")
    public void handleLIBORRate(LIBORRateMessage message) {
        LIBORRate rate = LIBORRate.builder()
            .date(message.getDate())
            .rate(message.getRate())
            .currency(message.getCurrency())
            .receivedAt(Instant.now())
            .build();
        
        // Store in cache
        liborRateCache.put(rate.getDate(), rate);
        
        // Persist to database
        liborRateRepository.save(rate);
    }
    
    public LIBORRate getLIBORRate(LocalDate date) {
        // Try cache first
        LIBORRate rate = liborRateCache.get(date);
        if (rate != null) {
            return rate;
        }
        
        // Fallback to database
        return liborRateRepository.findByDate(date)
            .orElseThrow(() -> new LIBORRateNotFoundException(date));
    }
}
```

---

## Question 75: How do you integrate account details via REST API?

### Answer

### REST API Integration for Accounts

#### 1. **REST Client**

```java
@Service
public class AccountServiceClient {
    private final RestTemplate restTemplate;
    private final Cache<String, AccountDetails> accountCache;
    
    public AccountDetails getAccountDetails(String accountId) {
        // Try cache first
        AccountDetails cached = accountCache.getIfPresent(accountId);
        if (cached != null) {
            return cached;
        }
        
        // Call REST API
        try {
            AccountDetails account = restTemplate.getForObject(
                "http://account-service/accounts/{accountId}",
                AccountDetails.class,
                accountId);
            
            // Cache for 1 hour
            accountCache.put(accountId, account);
            
            return account;
        } catch (Exception e) {
            log.error("Failed to get account details", e);
            throw new AccountServiceException("Failed to get account", e);
        }
    }
}
```

---

## Question 76: What's your approach to funding calculation accuracy?

### Answer

### Funding Calculation Accuracy

#### 1. **Accuracy Mechanisms**

```java
@Service
public class FundingAccuracyService {
    public BigDecimal calculateFunding(Position position, 
                                      LIBORRate liborRate, 
                                      AccountDetails account) {
        // Validation 1: Position validation
        validatePosition(position);
        
        // Validation 2: LIBOR rate validation
        validateLIBORRate(liborRate);
        
        // Validation 3: Account validation
        validateAccount(account);
        
        // Calculate funding
        BigDecimal funding = performCalculation(position, liborRate, account);
        
        // Validation 4: Result validation
        validateFundingResult(funding, position);
        
        return funding;
    }
    
    private void validateFundingResult(BigDecimal funding, Position position) {
        // Funding should be reasonable (not negative for long positions)
        if (position.getQuantity().compareTo(BigDecimal.ZERO) > 0 &&
            funding.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidFundingCalculationException(
                "Funding cannot be negative for long positions");
        }
    }
}
```

---

## Question 77: You "maintained 24x7 Overnight Funding application with 99.95% uptime." How?

### Answer

### High Availability for Overnight Funding

#### 1. **Availability Strategies**

```java
@Service
public class HighAvailabilityFundingService {
    // Health check
    @Scheduled(fixedRate = 30000) // Every 30 seconds
    public void healthCheck() {
        // Check Kafka connectivity
        checkKafkaHealth();
        
        // Check JMS connectivity
        checkJMSHealth();
        
        // Check REST API connectivity
        checkRESTAPIHealth();
        
        // Check database connectivity
        checkDatabaseHealth();
    }
    
    // Circuit breaker for external services
    @CircuitBreaker(name = "account-service", fallbackMethod = "getAccountDetailsFallback")
    public AccountDetails getAccountDetails(String accountId) {
        return accountServiceClient.getAccountDetails(accountId);
    }
    
    public AccountDetails getAccountDetailsFallback(String accountId, Exception e) {
        // Use cached account or default
        return accountCache.getIfPresent(accountId) 
            ?? AccountDetails.defaultAccount(accountId);
    }
}
```

---

## Question 81: You "designed OTC Trade Processing systems (SecDB, SecTM, CBM)." Explain these systems.

### Answer

### OTC Trade Processing Systems

#### 1. **System Overview**

```
┌─────────────────────────────────────────────────────────┐
│         OTC Trade Processing Systems                   │
└─────────────────────────────────────────────────────────┘

SecDB (Securities Database):
├─ Trade storage
├─ Trade validation
└─ Trade queries

SecTM (Securities Trade Matching):
├─ Trade matching
├─ Confirmation
└─ Settlement coordination

CBM (Contract Builder & Manager):
├─ Contract generation
├─ FpML generation
└─ Contract management
```

#### 2. **SecDB Implementation**

```java
@Service
public class SecDBService {
    public Trade storeTrade(Trade trade) {
        // Validate trade
        validateTrade(trade);
        
        // Store in database
        return tradeRepository.save(trade);
    }
    
    public Trade getTrade(String tradeId) {
        return tradeRepository.findById(tradeId)
            .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }
}
```

#### 3. **SecTM Implementation**

```java
@Service
public class SecTMService {
    public TradeMatchResult matchTrade(Trade trade1, Trade trade2) {
        // Match criteria
        boolean matched = matches(trade1, trade2);
        
        if (matched) {
            // Create match
            TradeMatch match = createMatch(trade1, trade2);
            
            // Generate confirmation
            generateConfirmation(match);
            
            return TradeMatchResult.builder()
                .matched(true)
                .match(match)
                .build();
        }
        
        return TradeMatchResult.builder()
            .matched(false)
            .build();
    }
    
    private boolean matches(Trade trade1, Trade trade2) {
        return trade1.getInstrumentId().equals(trade2.getInstrumentId()) &&
               trade1.getQuantity().compareTo(trade2.getQuantity()) == 0 &&
               trade1.getPrice().compareTo(trade2.getPrice()) == 0 &&
               trade1.getTradeDate().equals(trade2.getTradeDate());
    }
}
```

#### 4. **CBM Implementation**

```java
@Service
public class CBMService {
    public Contract generateContract(Trade trade) {
        // Generate contract
        Contract contract = Contract.builder()
            .contractId(generateContractId())
            .tradeId(trade.getTradeId())
            .parties(createParties(trade))
            .terms(createTerms(trade))
            .build();
        
        // Generate FpML
        String fpml = generateFpML(contract);
        contract.setFpML(fpml);
        
        return contract;
    }
    
    private String generateFpML(Contract contract) {
        // Generate FpML XML
        FpMLGenerator generator = new FpMLGenerator();
        return generator.generate(contract);
    }
}
```

---

## Summary

Part 15 covers:
- **High-Volume Funding**: Scalability, batch processing, 500K+ calculations/day
- **Kafka Integration**: Position events, consumer implementation
- **JMS Integration**: LIBOR rates, consumer implementation
- **REST API Integration**: Account details, caching
- **Funding Accuracy**: Validation mechanisms
- **High Availability**: Health checks, circuit breakers, 99.95% uptime
- **OTC Trade Processing**: SecDB, SecTM, CBM systems

Key principles:
- Parallel batch processing for scalability
- Event-driven integration
- Comprehensive validation
- High availability mechanisms
- Multi-system integration

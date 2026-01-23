# Domain-Specific Answers - Part 5: Bot Services (Q21-25)

## Question 21: You "maintained and enhanced Bot services, improving response accuracy by 25%." How did you do this?

### Answer

### Bot Accuracy Improvement

#### 1. **Accuracy Improvement Strategies**

```
┌─────────────────────────────────────────────────────────┐
│         Accuracy Improvement Strategies                │
└─────────────────────────────────────────────────────────┘

Before Optimization:
├─ Response Accuracy: 70%
├─ False Positives: 15%
├─ False Negatives: 15%
└─ User Satisfaction: 75%

After Optimization:
├─ Response Accuracy: 95% (25% improvement)
├─ False Positives: 5% (67% reduction)
├─ False Negatives: 5% (67% reduction)
└─ User Satisfaction: 90%
```

#### 2. **Strategy 1: Intent Confidence Thresholds**

```java
@Service
public class BotAccuracyService {
    private static final double CONFIDENCE_THRESHOLD = 0.8;
    
    public BotResponse processMessage(String message, String conversationId) {
        // Get NLU response
        NLUResponse nluResponse = nluFacadeService.processMessage(
            message, conversationId);
        
        // Check confidence threshold
        if (nluResponse.getConfidence() < CONFIDENCE_THRESHOLD) {
            // Low confidence - escalate to human agent
            return escalateToHumanAgent(conversationId, nluResponse);
        }
        
        // High confidence - process with bot
        return processWithBot(nluResponse, conversationId);
    }
    
    public void optimizeConfidenceThreshold() {
        // Analyze historical data
        List<ConversationData> conversations = getConversationHistory();
        
        // Find optimal threshold
        double optimalThreshold = findOptimalThreshold(conversations);
        
        // Update threshold
        updateConfidenceThreshold(optimalThreshold);
    }
    
    private double findOptimalThreshold(List<ConversationData> conversations) {
        // Test different thresholds
        double bestThreshold = 0.5;
        double bestAccuracy = 0.0;
        
        for (double threshold = 0.5; threshold <= 0.95; threshold += 0.05) {
            double accuracy = calculateAccuracy(conversations, threshold);
            if (accuracy > bestAccuracy) {
                bestAccuracy = accuracy;
                bestThreshold = threshold;
            }
        }
        
        return bestThreshold;
    }
}
```

#### 3. **Strategy 2: Context-Aware Responses**

```java
@Service
public class ContextAwareBotService {
    public BotResponse processWithContext(String message, String conversationId) {
        // Get conversation context
        ConversationContext context = getConversationContext(conversationId);
        
        // Get NLU response with context
        NLUResponse nluResponse = nluFacadeService.processMessageWithContext(
            message, conversationId, context);
        
        // Use context to improve accuracy
        BotResponse response = generateResponse(nluResponse, context);
        
        // Update context
        updateContext(conversationId, message, response);
        
        return response;
    }
    
    private ConversationContext getConversationContext(String conversationId) {
        // Get recent messages
        List<Message> recentMessages = getRecentMessages(conversationId, 5);
        
        // Extract context
        ConversationContext context = new ConversationContext();
        context.setRecentMessages(recentMessages);
        context.setPreviousIntent(getPreviousIntent(conversationId));
        context.setEntities(getPreviousEntities(conversationId));
        
        return context;
    }
}
```

#### 4. **Strategy 3: Multi-Intent Handling**

```java
@Service
public class MultiIntentBotService {
    public BotResponse processMultiIntent(String message, String conversationId) {
        // Get NLU response with multiple intents
        NLUResponse nluResponse = nluFacadeService.processMessage(
            message, conversationId);
        
        // Check if multiple intents detected
        if (nluResponse.getAlternativeIntents().size() > 1) {
            // Handle ambiguous intent
            return handleAmbiguousIntent(nluResponse, conversationId);
        }
        
        // Single clear intent
        return processSingleIntent(nluResponse, conversationId);
    }
    
    private BotResponse handleAmbiguousIntent(NLUResponse nluResponse, 
                                             String conversationId) {
        // Check confidence difference
        double primaryConfidence = nluResponse.getConfidence();
        double secondaryConfidence = nluResponse.getAlternativeIntents()
            .get(1).getConfidence();
        
        double confidenceDiff = primaryConfidence - secondaryConfidence;
        
        if (confidenceDiff < 0.1) {
            // Very close confidence - ask for clarification
            return askForClarification(nluResponse, conversationId);
        } else {
            // Use primary intent
            return processSingleIntent(nluResponse, conversationId);
        }
    }
}
```

#### 5. **Strategy 4: Feedback Loop**

```java
@Service
public class BotFeedbackService {
    public void recordFeedback(String conversationId, boolean wasHelpful) {
        // Record feedback
        BotFeedback feedback = BotFeedback.builder()
            .conversationId(conversationId)
            .wasHelpful(wasHelpful)
            .timestamp(Instant.now())
            .build();
        
        feedbackRepository.save(feedback);
        
        // Analyze feedback for improvement
        analyzeFeedback(feedback);
    }
    
    @Scheduled(fixedRate = 3600000) // Hourly
    public void analyzeFeedback() {
        List<BotFeedback> recentFeedback = getRecentFeedback(Duration.ofDays(7));
        
        // Calculate accuracy metrics
        double accuracy = calculateAccuracy(recentFeedback);
        double falsePositiveRate = calculateFalsePositiveRate(recentFeedback);
        double falseNegativeRate = calculateFalseNegativeRate(recentFeedback);
        
        // Update bot configuration based on feedback
        if (falsePositiveRate > 0.1) {
            // Too many false positives - increase confidence threshold
            increaseConfidenceThreshold();
        }
        
        if (falseNegativeRate > 0.1) {
            // Too many false negatives - decrease confidence threshold
            decreaseConfidenceThreshold();
        }
    }
}
```

---

## Question 22: You "reduced false positives by 30%." What was your approach?

### Answer

### False Positive Reduction

#### 1. **False Positive Analysis**

```
┌─────────────────────────────────────────────────────────┐
│         False Positive Causes                          │
└─────────────────────────────────────────────────────────┘

Common Causes:
├─ Low confidence threshold
├─ Ambiguous intents
├─ Context misunderstanding
├─ Entity extraction errors
└─ Intent misclassification

Impact:
├─ User frustration
├─ Escalation to human agents
├─ Increased costs
└─ Reduced user satisfaction
```

#### 2. **Strategy 1: Confidence Threshold Optimization**

```java
@Service
public class FalsePositiveReductionService {
    private double confidenceThreshold = 0.7;
    
    public BotResponse processMessage(String message, String conversationId) {
        NLUResponse nluResponse = nluFacadeService.processMessage(
            message, conversationId);
        
        // Check confidence threshold
        if (nluResponse.getConfidence() < confidenceThreshold) {
            // Low confidence - don't respond, escalate
            return escalateToHumanAgent(conversationId, nluResponse);
        }
        
        // High confidence - process
        return processWithBot(nluResponse, conversationId);
    }
    
    public void optimizeThreshold() {
        // Analyze false positives
        List<FalsePositiveCase> falsePositives = getFalsePositives();
        
        // Find minimum confidence that would have prevented false positives
        double minConfidence = falsePositives.stream()
            .mapToDouble(FalsePositiveCase::getConfidence)
            .min()
            .orElse(0.7);
        
        // Set threshold above minimum
        this.confidenceThreshold = minConfidence + 0.1;
    }
}
```

#### 3. **Strategy 2: Intent Validation**

```java
@Service
public class IntentValidationService {
    public boolean validateIntent(NLUResponse nluResponse, String conversationId) {
        // Check confidence
        if (nluResponse.getConfidence() < 0.8) {
            return false;
        }
        
        // Check entity requirements
        if (!hasRequiredEntities(nluResponse)) {
            return false;
        }
        
        // Check context consistency
        if (!isContextConsistent(nluResponse, conversationId)) {
            return false;
        }
        
        // Check for ambiguous intents
        if (isAmbiguous(nluResponse)) {
            return false;
        }
        
        return true;
    }
    
    private boolean isContextConsistent(NLUResponse nluResponse, 
                                       String conversationId) {
        ConversationContext context = getConversationContext(conversationId);
        String previousIntent = context.getPreviousIntent();
        
        // Check if intent transition makes sense
        return isValidIntentTransition(previousIntent, nluResponse.getIntent());
    }
    
    private boolean isAmbiguous(NLUResponse nluResponse) {
        if (nluResponse.getAlternativeIntents().isEmpty()) {
            return false;
        }
        
        double primaryConfidence = nluResponse.getConfidence();
        double secondaryConfidence = nluResponse.getAlternativeIntents()
            .get(0).getConfidence();
        
        // Ambiguous if confidence difference < 0.15
        return (primaryConfidence - secondaryConfidence) < 0.15;
    }
}
```

#### 4. **Strategy 3: Entity Validation**

```java
@Service
public class EntityValidationService {
    public boolean validateEntities(NLUResponse nluResponse) {
        List<Entity> entities = nluResponse.getEntities();
        
        // Validate each entity
        for (Entity entity : entities) {
            if (!isValidEntity(entity)) {
                return false;
            }
        }
        
        // Check required entities for intent
        List<String> requiredEntities = getRequiredEntities(nluResponse.getIntent());
        for (String requiredEntity : requiredEntities) {
            if (!hasEntity(entities, requiredEntity)) {
                return false;
            }
        }
        
        return true;
    }
    
    private boolean isValidEntity(Entity entity) {
        // Check confidence
        if (entity.getConfidence() < 0.7) {
            return false;
        }
        
        // Check value format
        if (!isValidEntityValue(entity)) {
            return false;
        }
        
        return true;
    }
}
```

#### 5. **Strategy 4: User Confirmation**

```java
@Service
public class UserConfirmationService {
    public BotResponse processWithConfirmation(String message, 
                                              String conversationId) {
        NLUResponse nluResponse = nluFacadeService.processMessage(
            message, conversationId);
        
        // Check if confirmation needed
        if (needsConfirmation(nluResponse)) {
            return askForConfirmation(nluResponse, conversationId);
        }
        
        // Process directly
        return processWithBot(nluResponse, conversationId);
    }
    
    private boolean needsConfirmation(NLUResponse nluResponse) {
        // Need confirmation if:
        // 1. Confidence is medium (0.7-0.85)
        // 2. Intent is critical (payment, account changes)
        // 3. Ambiguous intent
        
        double confidence = nluResponse.getConfidence();
        boolean isCriticalIntent = isCriticalIntent(nluResponse.getIntent());
        boolean isAmbiguous = isAmbiguous(nluResponse);
        
        return (confidence >= 0.7 && confidence < 0.85) || 
               (isCriticalIntent && confidence < 0.9) ||
               isAmbiguous;
    }
    
    private BotResponse askForConfirmation(NLUResponse nluResponse, 
                                          String conversationId) {
        return BotResponse.builder()
            .type(ResponseType.CONFIRMATION)
            .message("Did you mean: " + nluResponse.getIntent() + "?")
            .options(Arrays.asList("Yes", "No"))
            .build();
    }
}
```

---

## Question 23: How do you train and improve bot accuracy?

### Answer

### Bot Training & Improvement

#### 1. **Training Data Management**

```java
@Service
public class BotTrainingService {
    public void trainBot(TrainingData trainingData) {
        // Prepare training data
        List<TrainingExample> examples = prepareTrainingExamples(trainingData);
        
        // Train NLU model
        trainNLUModel(examples);
        
        // Validate model
        validateModel(examples);
        
        // Deploy model
        deployModel();
    }
    
    private List<TrainingExample> prepareTrainingExamples(TrainingData data) {
        return data.getExamples().stream()
            .map(example -> TrainingExample.builder()
                .text(example.getText())
                .intent(example.getIntent())
                .entities(example.getEntities())
                .build())
            .collect(Collectors.toList());
    }
}
```

#### 2. **Continuous Learning**

```java
@Service
public class ContinuousLearningService {
    @Scheduled(fixedRate = 86400000) // Daily
    public void learnFromConversations() {
        // Get recent conversations
        List<Conversation> conversations = getRecentConversations(Duration.ofDays(7));
        
        // Extract training examples
        List<TrainingExample> examples = extractTrainingExamples(conversations);
        
        // Filter high-quality examples
        List<TrainingExample> qualityExamples = filterQualityExamples(examples);
        
        // Add to training data
        addToTrainingData(qualityExamples);
        
        // Retrain model if significant new data
        if (qualityExamples.size() > 100) {
            retrainModel();
        }
    }
    
    private List<TrainingExample> filterQualityExamples(
            List<TrainingExample> examples) {
        return examples.stream()
            .filter(example -> example.getConfidence() > 0.9)
            .filter(example -> example.getUserFeedback() == Feedback.POSITIVE)
            .filter(example -> !example.isAmbiguous())
            .collect(Collectors.toList());
    }
}
```

#### 3. **A/B Testing**

```java
@Service
public class BotABTestingService {
    public BotResponse processWithABTest(String message, String conversationId) {
        // Determine which variant to use
        String variant = selectVariant(conversationId);
        
        // Process with variant
        BotResponse response = processWithVariant(message, conversationId, variant);
        
        // Track variant usage
        trackVariantUsage(conversationId, variant, response);
        
        return response;
    }
    
    private String selectVariant(String conversationId) {
        // Use consistent variant for conversation
        String variant = getConversationVariant(conversationId);
        if (variant != null) {
            return variant;
        }
        
        // Assign new variant (50/50 split)
        variant = Math.random() < 0.5 ? "A" : "B";
        setConversationVariant(conversationId, variant);
        return variant;
    }
    
    @Scheduled(fixedRate = 3600000) // Hourly
    public void analyzeABTestResults() {
        // Compare variants
        VariantMetrics variantA = getVariantMetrics("A");
        VariantMetrics variantB = getVariantMetrics("B");
        
        // Determine winner
        if (variantB.getAccuracy() > variantA.getAccuracy() + 0.05) {
            // Variant B is significantly better - switch to B
            switchToVariant("B");
        } else if (variantA.getAccuracy() > variantB.getAccuracy() + 0.05) {
            // Variant A is significantly better - switch to A
            switchToVariant("A");
        }
    }
}
```

#### 4. **Error Analysis & Correction**

```java
@Service
public class BotErrorAnalysisService {
    @Scheduled(fixedRate = 86400000) // Daily
    public void analyzeErrors() {
        // Get failed conversations
        List<Conversation> failedConversations = getFailedConversations();
        
        // Analyze errors
        ErrorAnalysis analysis = analyzeErrors(failedConversations);
        
        // Identify patterns
        List<ErrorPattern> patterns = identifyErrorPatterns(analysis);
        
        // Create fixes
        for (ErrorPattern pattern : patterns) {
            createFix(pattern);
        }
    }
    
    private ErrorAnalysis analyzeErrors(List<Conversation> conversations) {
        ErrorAnalysis analysis = new ErrorAnalysis();
        
        for (Conversation conversation : conversations) {
            // Analyze each error
            ConversationError error = analyzeConversationError(conversation);
            analysis.addError(error);
        }
        
        // Categorize errors
        analysis.categorizeErrors();
        
        return analysis;
    }
    
    private void createFix(ErrorPattern pattern) {
        switch (pattern.getType()) {
            case LOW_CONFIDENCE:
                // Increase confidence threshold
                increaseConfidenceThreshold();
                break;
            case MISSING_ENTITY:
                // Add entity extraction rules
                addEntityExtractionRules(pattern);
                break;
            case INTENT_MISCLASSIFICATION:
                // Add training examples
                addTrainingExamples(pattern);
                break;
        }
    }
}
```

---

## Question 24: What's your approach to bot conversation flow management?

### Answer

### Bot Conversation Flow Management

#### 1. **Flow Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         Conversation Flow Architecture                  │
└─────────────────────────────────────────────────────────┘

Flow Components:
├─ Flow Definition (states, transitions)
├─ State Machine
├─ Context Management
├─ Flow Execution Engine
└─ Flow Analytics
```

#### 2. **State Machine Implementation**

```java
public enum BotState {
    GREETING,
    COLLECTING_INFO,
    PROCESSING,
    CONFIRMATION,
    COMPLETED,
    ESCALATED
}

@Service
public class BotFlowManager {
    private final Map<String, BotState> conversationStates = new ConcurrentHashMap<>();
    
    public BotResponse processMessage(String message, String conversationId) {
        // Get current state
        BotState currentState = getCurrentState(conversationId);
        
        // Get NLU response
        NLUResponse nluResponse = nluFacadeService.processMessage(
            message, conversationId);
        
        // Determine next state
        BotState nextState = determineNextState(currentState, nluResponse);
        
        // Generate response
        BotResponse response = generateResponse(nextState, nluResponse, conversationId);
        
        // Update state
        updateState(conversationId, nextState);
        
        return response;
    }
    
    private BotState determineNextState(BotState currentState, NLUResponse nluResponse) {
        // State transition logic
        switch (currentState) {
            case GREETING:
                return handleGreetingState(nluResponse);
            case COLLECTING_INFO:
                return handleCollectingInfoState(nluResponse);
            case PROCESSING:
                return handleProcessingState(nluResponse);
            case CONFIRMATION:
                return handleConfirmationState(nluResponse);
            default:
                return currentState;
        }
    }
}
```

#### 3. **Flow Definition**

```java
@Configuration
public class BotFlowConfiguration {
    @Bean
    public BotFlow orderFlow() {
        return BotFlow.builder()
            .name("order_flow")
            .states(Arrays.asList(
                BotState.GREETING,
                BotState.COLLECTING_INFO,
                BotState.CONFIRMATION,
                BotState.PROCESSING,
                BotState.COMPLETED
            ))
            .transitions(createTransitions())
            .build();
    }
    
    private List<FlowTransition> createTransitions() {
        return Arrays.asList(
            new FlowTransition(BotState.GREETING, "order_intent", BotState.COLLECTING_INFO),
            new FlowTransition(BotState.COLLECTING_INFO, "all_info_collected", BotState.CONFIRMATION),
            new FlowTransition(BotState.CONFIRMATION, "confirmed", BotState.PROCESSING),
            new FlowTransition(BotState.CONFIRMATION, "not_confirmed", BotState.COLLECTING_INFO),
            new FlowTransition(BotState.PROCESSING, "completed", BotState.COMPLETED)
        );
    }
}
```

#### 4. **Context-Aware Flow**

```java
@Service
public class ContextAwareFlowManager {
    public BotResponse processWithContext(String message, String conversationId) {
        // Get conversation context
        ConversationContext context = getConversationContext(conversationId);
        
        // Get current flow state
        FlowState flowState = getFlowState(conversationId);
        
        // Process message with context
        NLUResponse nluResponse = processWithContext(message, context);
        
        // Update context
        updateContext(conversationId, message, nluResponse);
        
        // Determine next action based on flow
        BotResponse response = executeFlowStep(flowState, nluResponse, context);
        
        // Update flow state
        updateFlowState(conversationId, response.getNextState());
        
        return response;
    }
    
    private BotResponse executeFlowStep(FlowState flowState, 
                                       NLUResponse nluResponse, 
                                       ConversationContext context) {
        // Execute current flow step
        FlowStep step = flowState.getCurrentStep();
        
        return step.execute(nluResponse, context);
    }
}
```

#### 5. **Flow Analytics**

```java
@Service
public class BotFlowAnalytics {
    public FlowAnalytics analyzeFlow(String flowName, Duration period) {
        // Get flow executions
        List<FlowExecution> executions = getFlowExecutions(flowName, period);
        
        FlowAnalytics analytics = new FlowAnalytics();
        
        // Calculate metrics
        analytics.setTotalExecutions(executions.size());
        analytics.setCompletionRate(calculateCompletionRate(executions));
        analytics.setAverageSteps(calculateAverageSteps(executions));
        analytics.setDropOffPoints(identifyDropOffPoints(executions));
        analytics.setAverageDuration(calculateAverageDuration(executions));
        
        return analytics;
    }
    
    private double calculateCompletionRate(List<FlowExecution> executions) {
        long completed = executions.stream()
            .filter(FlowExecution::isCompleted)
            .count();
        
        return (double) completed / executions.size();
    }
    
    private List<FlowState> identifyDropOffPoints(List<FlowExecution> executions) {
        // Find states where most users drop off
        Map<FlowState, Long> dropOffs = executions.stream()
            .filter(execution -> !execution.isCompleted())
            .collect(Collectors.groupingBy(
                FlowExecution::getLastState,
                Collectors.counting()
            ));
        
        return dropOffs.entrySet().stream()
            .sorted(Map.Entry.<FlowState, Long>comparingByValue().reversed())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}
```

---

## Question 25: How do you handle bot context and memory?

### Answer

### Bot Context & Memory Management

#### 1. **Context Model**

```
┌─────────────────────────────────────────────────────────┐
│         Bot Context Model                              │
└─────────────────────────────────────────────────────────┘

Conversation Context:
├─ Conversation ID
├─ User ID
├─ Recent Messages (last 10)
├─ Current Intent
├─ Extracted Entities
├─ Flow State
├─ User Preferences
└─ Session Data
```

#### 2. **Context Storage**

```java
@Service
public class BotContextManager {
    private final RedisTemplate<String, ConversationContext> redisTemplate;
    
    public ConversationContext getContext(String conversationId) {
        String key = "bot:context:" + conversationId;
        ConversationContext context = redisTemplate.opsForValue().get(key);
        
        if (context == null) {
            // Create new context
            context = ConversationContext.builder()
                .conversationId(conversationId)
                .messages(new ArrayList<>())
                .entities(new HashMap<>())
                .build();
            
            // Store in Redis
            redisTemplate.opsForValue().set(key, context, Duration.ofHours(24));
        }
        
        return context;
    }
    
    public void updateContext(String conversationId, Message message, NLUResponse nluResponse) {
        ConversationContext context = getContext(conversationId);
        
        // Add message to history
        context.getMessages().add(message);
        if (context.getMessages().size() > 10) {
            // Keep only last 10 messages
            context.getMessages().remove(0);
        }
        
        // Update intent
        context.setCurrentIntent(nluResponse.getIntent());
        context.setPreviousIntent(context.getCurrentIntent());
        
        // Update entities
        for (Entity entity : nluResponse.getEntities()) {
            context.getEntities().put(entity.getName(), entity.getValue());
        }
        
        // Update timestamp
        context.setLastUpdated(Instant.now());
        
        // Save to Redis
        String key = "bot:context:" + conversationId;
        redisTemplate.opsForValue().set(key, context, Duration.ofHours(24));
    }
}
```

#### 3. **Memory Management**

```java
@Service
public class BotMemoryService {
    private final ConversationContextRepository contextRepository;
    
    public void persistContext(String conversationId) {
        ConversationContext context = getContext(conversationId);
        
        // Persist to database for long-term storage
        contextRepository.save(context);
    }
    
    public ConversationContext loadContext(String conversationId) {
        // Try Redis first
        ConversationContext context = getContextFromRedis(conversationId);
        if (context != null) {
            return context;
        }
        
        // Load from database
        context = contextRepository.findByConversationId(conversationId)
            .orElse(null);
        
        if (context != null) {
            // Restore to Redis
            restoreToRedis(conversationId, context);
        }
        
        return context;
    }
    
    @Scheduled(fixedRate = 3600000) // Hourly
    public void cleanupOldContexts() {
        // Clean up contexts older than 30 days
        Instant cutoff = Instant.now().minus(Duration.ofDays(30));
        List<ConversationContext> oldContexts = contextRepository
            .findByLastUpdatedBefore(cutoff);
        
        for (ConversationContext context : oldContexts) {
            // Archive context
            archiveContext(context);
            
            // Remove from Redis
            removeFromRedis(context.getConversationId());
        }
    }
}
```

#### 4. **Context-Aware Responses**

```java
@Service
public class ContextAwareBotService {
    public BotResponse generateContextualResponse(String message, 
                                                 String conversationId) {
        // Get context
        ConversationContext context = getContext(conversationId);
        
        // Get NLU response with context
        NLUResponse nluResponse = nluFacadeService.processMessageWithContext(
            message, conversationId, context);
        
        // Generate response using context
        BotResponse response = generateResponse(nluResponse, context);
        
        // Update context
        updateContext(conversationId, message, nluResponse);
        
        return response;
    }
    
    private BotResponse generateResponse(NLUResponse nluResponse, 
                                       ConversationContext context) {
        // Use context to improve response
        String intent = nluResponse.getIntent();
        Map<String, String> entities = context.getEntities();
        
        // Check if we have all required entities
        if (!hasAllRequiredEntities(intent, entities)) {
            // Ask for missing entities
            return askForMissingEntities(intent, entities);
        }
        
        // Generate response with context
        return generateResponseWithContext(intent, entities, context);
    }
}
```

#### 5. **Context Persistence**

```java
@Entity
public class ConversationContext {
    @Id
    private String conversationId;
    
    @ElementCollection
    private List<Message> messages;
    
    @ElementCollection
    private Map<String, String> entities;
    
    private String currentIntent;
    private String previousIntent;
    
    private Instant createdAt;
    private Instant lastUpdated;
    
    // Getters and setters
}
```

---

## Summary

Part 5 covers:
- **Bot Accuracy Improvement**: Confidence thresholds, context-aware responses, multi-intent handling, feedback loop
- **False Positive Reduction**: Threshold optimization, intent validation, entity validation, user confirmation
- **Bot Training**: Training data management, continuous learning, A/B testing, error analysis
- **Flow Management**: State machine, flow definition, context-aware flow, flow analytics
- **Context & Memory**: Context model, storage, memory management, context-aware responses

Key principles:
- Confidence thresholds for accuracy
- Context-aware processing for better understanding
- Continuous learning from conversations
- State machine for flow management
- Persistent context for multi-turn conversations

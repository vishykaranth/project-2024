# Domain-Specific Answers - Part 8: Real-Time Communication Continued (Q36-40)

## Question 36: What's your approach to presence and typing indicators?

### Answer

### Presence & Typing Indicators

#### 1. **Presence Management**

```java
@Service
public class PresenceService {
    private final RedisTemplate<String, Presence> redisTemplate;
    
    public void updatePresence(String userId, PresenceStatus status) {
        Presence presence = Presence.builder()
            .userId(userId)
            .status(status)
            .lastSeen(Instant.now())
            .build();
        
        String key = "presence:user:" + userId;
        redisTemplate.opsForValue().set(key, presence, Duration.ofMinutes(5));
        
        // Publish presence update
        publishPresenceUpdate(userId, status);
    }
    
    public Presence getPresence(String userId) {
        String key = "presence:user:" + userId;
        Presence presence = redisTemplate.opsForValue().get(key);
        
        if (presence == null) {
            return Presence.builder()
                .userId(userId)
                .status(PresenceStatus.OFFLINE)
                .build();
        }
        
        // Check if presence is stale
        Duration timeSinceLastSeen = Duration.between(
            presence.getLastSeen(), Instant.now());
        
        if (timeSinceLastSeen.toMinutes() > 5) {
            // Consider offline
            presence.setStatus(PresenceStatus.OFFLINE);
        }
        
        return presence;
    }
}
```

#### 2. **Typing Indicators**

```java
@Service
public class TypingIndicatorService {
    private final RedisTemplate<String, String> redisTemplate;
    
    public void startTyping(String userId, String conversationId) {
        String key = "typing:conversation:" + conversationId + ":user:" + userId;
        
        // Set typing indicator (expires in 3 seconds)
        redisTemplate.opsForValue().set(key, "typing", Duration.ofSeconds(3));
        
        // Notify other participants
        notifyTyping(conversationId, userId, true);
    }
    
    public void stopTyping(String userId, String conversationId) {
        String key = "typing:conversation:" + conversationId + ":user:" + userId;
        redisTemplate.delete(key);
        
        // Notify other participants
        notifyTyping(conversationId, userId, false);
    }
    
    public List<String> getTypingUsers(String conversationId) {
        String pattern = "typing:conversation:" + conversationId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        
        return keys.stream()
            .map(key -> extractUserId(key))
            .collect(Collectors.toList());
    }
}
```

---

## Question 37: How do you handle file sharing in conversations?

### Answer

### File Sharing Implementation

#### 1. **File Upload Flow**

```
┌─────────────────────────────────────────────────────────┐
│         File Upload Flow                               │
└─────────────────────────────────────────────────────────┘

1. Client uploads file
   │
   ▼
2. File Service receives
   │
   ├─► Validate file (size, type)
   ├─► Scan for viruses
   └─► Generate file ID
   │
   ▼
3. Store file
   │
   ├─► Upload to S3/Cloud Storage
   ├─► Store metadata in database
   └─► Generate download URL
   │
   ▼
4. Create message with file reference
   │
   ▼
5. Deliver message
```

#### 2. **File Service Implementation**

```java
@Service
public class FileSharingService {
    private final S3Service s3Service;
    private final FileRepository fileRepository;
    
    public FileMetadata uploadFile(MultipartFile file, String conversationId) {
        // Validate file
        validateFile(file);
        
        // Generate file ID
        String fileId = UUID.randomUUID().toString();
        
        // Upload to S3
        String s3Key = "conversations/" + conversationId + "/" + fileId;
        String s3Url = s3Service.uploadFile(file, s3Key);
        
        // Create metadata
        FileMetadata metadata = FileMetadata.builder()
            .fileId(fileId)
            .conversationId(conversationId)
            .fileName(file.getOriginalFilename())
            .fileSize(file.getSize())
            .fileType(file.getContentType())
            .s3Url(s3Url)
            .uploadedAt(Instant.now())
            .build();
        
        // Store metadata
        fileRepository.save(metadata);
        
        return metadata;
    }
    
    public String getDownloadUrl(String fileId) {
        FileMetadata metadata = fileRepository.findById(fileId)
            .orElseThrow(() -> new FileNotFoundException(fileId));
        
        // Generate signed URL (expires in 1 hour)
        return s3Service.generateSignedUrl(metadata.getS3Url(), Duration.ofHours(1));
    }
    
    private void validateFile(MultipartFile file) {
        // Check file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new FileTooLargeException("File size exceeds 10MB");
        }
        
        // Check file type
        String contentType = file.getContentType();
        List<String> allowedTypes = Arrays.asList(
            "image/jpeg", "image/png", "image/gif",
            "application/pdf", "text/plain"
        );
        
        if (!allowedTypes.contains(contentType)) {
            throw new InvalidFileTypeException("File type not allowed");
        }
    }
}
```

---

## Question 38: What's your strategy for message history and search?

### Answer

### Message History & Search

#### 1. **History Storage**

```java
@Service
public class MessageHistoryService {
    private final MessageRepository messageRepository;
    private final ElasticsearchTemplate elasticsearchTemplate;
    
    public void storeMessage(Message message) {
        // Store in database
        messageRepository.save(message);
        
        // Index in Elasticsearch for search
        indexMessage(message);
    }
    
    private void indexMessage(Message message) {
        MessageDocument document = MessageDocument.builder()
            .messageId(message.getId())
            .conversationId(message.getConversationId())
            .senderId(message.getSenderId())
            .content(message.getContent())
            .timestamp(message.getTimestamp())
            .build();
        
        elasticsearchTemplate.save(document);
    }
}
```

#### 2. **Search Implementation**

```java
@Service
public class MessageSearchService {
    private final ElasticsearchTemplate elasticsearchTemplate;
    
    public List<Message> searchMessages(String conversationId, String query) {
        // Build search query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
            .must(QueryBuilders.termQuery("conversationId", conversationId))
            .must(QueryBuilders.multiMatchQuery(query, "content")
                .type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                .fuzziness(Fuzziness.AUTO));
        
        // Execute search
        SearchHits<MessageDocument> hits = elasticsearchTemplate.search(
            new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(0, 50))
                .build(),
            MessageDocument.class
        );
        
        // Convert to messages
        return hits.stream()
            .map(hit -> hit.getContent().toMessage())
            .collect(Collectors.toList());
    }
}
```

---

## Question 39: How do you handle multi-channel conversations (web, mobile, API)?

### Answer

### Multi-Channel Support

#### 1. **Channel Abstraction**

```java
public interface MessageChannel {
    void sendMessage(String userId, Message message);
    void receiveMessage(String userId, MessageHandler handler);
    boolean isUserOnline(String userId);
}

@Component
public class WebSocketChannel implements MessageChannel {
    // WebSocket implementation
}

@Component
public class MobilePushChannel implements MessageChannel {
    // Mobile push notification implementation
}

@Component
public class APIRestChannel implements MessageChannel {
    // REST API implementation
}
```

#### 2. **Channel Router**

```java
@Service
public class MultiChannelRouter {
    private final Map<ChannelType, MessageChannel> channels = new HashMap<>();
    
    public void sendMessage(String userId, Message message) {
        // Determine user's active channels
        List<ChannelType> activeChannels = getUserActiveChannels(userId);
        
        // Send to all active channels
        for (ChannelType channelType : activeChannels) {
            MessageChannel channel = channels.get(channelType);
            if (channel != null) {
                try {
                    channel.sendMessage(userId, message);
                } catch (Exception e) {
                    log.warn("Failed to send via channel: {}", channelType, e);
                }
            }
        }
    }
    
    private List<ChannelType> getUserActiveChannels(String userId) {
        List<ChannelType> active = new ArrayList<>();
        
        // Check WebSocket
        if (webSocketChannel.isUserOnline(userId)) {
            active.add(ChannelType.WEBSOCKET);
        }
        
        // Check mobile (has push token)
        if (hasMobilePushToken(userId)) {
            active.add(ChannelType.MOBILE_PUSH);
        }
        
        // Always available via API
        active.add(ChannelType.API);
        
        return active;
    }
}
```

---

## Question 40: What's your approach to conversation analytics?

### Answer

### Conversation Analytics

#### 1. **Analytics Architecture**

```java
@Service
public class ConversationAnalyticsService {
    private final KafkaTemplate<String, AnalyticsEvent> kafkaTemplate;
    
    public void trackConversationEvent(ConversationEvent event) {
        // Publish to analytics topic
        kafkaTemplate.send("analytics-events", 
            event.getConversationId(), event);
    }
    
    public ConversationAnalytics generateAnalytics(String conversationId, Duration period) {
        // Get events from Kafka/Database
        List<ConversationEvent> events = getEvents(conversationId, period);
        
        ConversationAnalytics analytics = new ConversationAnalytics();
        
        // Calculate metrics
        analytics.setTotalMessages(countMessages(events));
        analytics.setAverageResponseTime(calculateAverageResponseTime(events));
        analytics.setBotAccuracy(calculateBotAccuracy(events));
        analytics.setEscalationRate(calculateEscalationRate(events));
        analytics.setUserSatisfaction(calculateUserSatisfaction(events));
        
        return analytics;
    }
}
```

---

## Summary

Part 8 covers:
- **Presence & Typing**: Presence management, typing indicators
- **File Sharing**: Upload flow, file service, validation
- **Message History & Search**: Storage, Elasticsearch indexing, search
- **Multi-Channel**: Channel abstraction, router, active channel detection
- **Conversation Analytics**: Analytics architecture, metrics calculation

Key principles:
- Real-time presence updates
- Typing indicators with expiration
- Secure file sharing with validation
- Searchable message history
- Multi-channel message delivery
- Comprehensive conversation analytics

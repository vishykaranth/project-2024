# System Design Interview Questions for Java Principal Engineers - Part 9

## Real-World System Design Problems (Part 1)

This part covers complete system design solutions for common interview problems.

---

## Interview Question 41: Design Twitter/X

### Requirements

- Post tweets (280 characters)
- Follow/unfollow users
- Timeline feed (home and user)
- Like, retweet, reply
- Search tweets
- Real-time updates

### Capacity Estimation

```
Assumptions:
- 500M users, 200M daily active
- 100M tweets/day = ~1,200 tweets/sec
- 5:1 read:write ratio
- Average tweet: 280 chars = ~1KB
- Media: 20% tweets have media (avg 200KB)

Storage:
- Tweets: 100M/day × 365 days × 5 years × 1KB = 182.5 TB
- Media: 20M/day × 365 days × 5 years × 200KB = 7.3 PB
- User data: 500M users × 10KB = 5 TB
- Follow relationships: 500M users × 200 avg follows × 8 bytes = 800 GB

Bandwidth:
- Write: 1,200 tweets/sec × 1KB = 1.2 MB/sec
- Read: 6,000 reads/sec × 10KB = 60 MB/sec
```

### High-Level Design

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Load Balancer  │
└──────┬──────────┘
       │
   ┌───┴───┬────────┬────────┐
   ▼       ▼        ▼        ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│Tweet│ │Feed │ │User │ │Search│
│Svc  │ │Svc  │ │Svc  │ │Svc  │
└──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘
   │       │       │       │
   ▼       ▼       ▼       ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│Redis│ │Redis│ │MySQL│ │Elastic│
│Cache│ │Cache│ │     │ │Search │
└─────┘ └─────┘ └─────┘ └─────┘
```

### Database Schema

```sql
-- Users table
CREATE TABLE users (
    user_id BIGINT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) PARTITION BY HASH(user_id);

-- Tweets table (sharded by user_id)
CREATE TABLE tweets (
    tweet_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    retweet_count INT DEFAULT 0,
    like_count INT DEFAULT 0,
    INDEX idx_user_created (user_id, created_at),
    INDEX idx_created (created_at)
) PARTITION BY HASH(user_id);

-- Follow relationships
CREATE TABLE follows (
    follower_id BIGINT NOT NULL,
    followee_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, followee_id),
    INDEX idx_followee (followee_id)
) PARTITION BY HASH(follower_id);

-- Timeline cache (Redis)
-- Key: timeline:user_id
-- Value: Sorted Set of tweet_ids (score = timestamp)
```

### Java Implementation

```java
@Service
public class TweetService {
    @Autowired
    private TweetRepository tweetRepository;
    
    @Autowired
    private RedisTemplate<String, String> redis;
    
    @Autowired
    private TimelineService timelineService;
    
    public Tweet postTweet(String userId, String content) {
        // 1. Create tweet
        Tweet tweet = new Tweet();
        tweet.setId(generateId());
        tweet.setUserId(userId);
        tweet.setContent(content);
        tweet.setCreatedAt(Instant.now());
        
        tweetRepository.save(tweet);
        
        // 2. Add to user's timeline
        timelineService.addToUserTimeline(userId, tweet.getId());
        
        // 3. Add to followers' home timelines (async)
        timelineService.fanoutToFollowers(userId, tweet.getId());
        
        // 4. Publish event
        eventPublisher.publish(new TweetCreatedEvent(tweet));
        
        return tweet;
    }
}

@Service
public class TimelineService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    @Autowired
    private FollowRepository followRepository;
    
    public void addToUserTimeline(String userId, String tweetId) {
        String key = "timeline:user:" + userId;
        long score = System.currentTimeMillis();
        redis.opsForZSet().add(key, tweetId, score);
        
        // Keep only last 1000 tweets
        redis.opsForZSet().removeRange(key, 0, -1001);
    }
    
    @Async
    public void fanoutToFollowers(String userId, String tweetId) {
        List<String> followers = followRepository.getFollowers(userId);
        
        for (String followerId : followers) {
            String key = "timeline:home:" + followerId;
            long score = System.currentTimeMillis();
            redis.opsForZSet().add(key, tweetId, score);
            
            // Keep only last 1000 tweets
            redis.opsForZSet().removeRange(key, 0, -1001);
        }
    }
    
    public List<Tweet> getHomeTimeline(String userId, int page, int size) {
        String key = "timeline:home:" + userId;
        
        // Get tweet IDs from cache
        Set<String> tweetIds = redis.opsForZSet()
            .reverseRange(key, page * size, (page + 1) * size - 1);
        
        if (tweetIds.isEmpty()) {
            // Cache miss - rebuild from database
            return rebuildHomeTimeline(userId, page, size);
        }
        
        // Fetch tweets
        return tweetRepository.findByIds(new ArrayList<>(tweetIds));
    }
    
    private List<Tweet> rebuildHomeTimeline(String userId, int page, int size) {
        // Get followed users
        List<String> followees = followRepository.getFollowees(userId);
        
        // Get recent tweets from followees
        List<Tweet> tweets = tweetRepository.findRecentTweetsByUsers(
            followees, page, size
        );
        
        // Cache results
        for (Tweet tweet : tweets) {
            addToHomeTimeline(userId, tweet.getId());
        }
        
        return tweets;
    }
}
```

---

## Interview Question 42: Design a Chat/Messaging System (like WhatsApp)

### Requirements

- Send/receive messages
- One-on-one and group chats
- Message delivery status
- Online/offline status
- Media sharing

### Architecture

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  Message Server │
│  (WebSocket)    │
└──────┬──────────┘
       │
   ┌───┴───┬────────┬────────┐
   ▼       ▼        ▼        ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│Msg  │ │Pres │ │Notif│ │Media│
│Svc  │ │Svc  │ │Svc  │ │Svc  │
└──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘
   │       │       │       │
   ▼       ▼       ▼       ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│Redis│ │Redis│ │MySQL│ │  S3  │
│Cache│ │Pres │ │     │ │      │
└─────┘ └─────┘ └─────┘ └─────┘
```

### Implementation

```java
@RestController
public class MessageController {
    @Autowired
    private MessageService messageService;
    
    @Autowired
    private WebSocketService webSocketService;
    
    @PostMapping("/messages")
    public Message sendMessage(@RequestBody MessageRequest request) {
        Message message = messageService.sendMessage(
            request.getSenderId(),
            request.getReceiverId(),
            request.getContent(),
            request.getType()
        );
        
        // Push to receiver if online
        webSocketService.sendToUser(request.getReceiverId(), message);
        
        return message;
    }
}

@Service
public class MessageService {
    @Autowired
    private MessageRepository messageRepository;
    
    @Autowired
    private RedisTemplate<String, String> redis;
    
    public Message sendMessage(String senderId, String receiverId, 
                              String content, MessageType type) {
        Message message = new Message();
        message.setId(generateId());
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setType(type);
        message.setStatus(MessageStatus.SENT);
        message.setCreatedAt(Instant.now());
        
        messageRepository.save(message);
        
        // Store in conversation cache
        String conversationId = getConversationId(senderId, receiverId);
        redis.opsForList().rightPush("conversation:" + conversationId, 
            message.getId());
        
        return message;
    }
    
    public List<Message> getConversation(String userId1, String userId2, 
                                         int page, int size) {
        String conversationId = getConversationId(userId1, userId2);
        
        // Get from cache
        List<String> messageIds = redis.opsForList().range(
            "conversation:" + conversationId,
            page * size,
            (page + 1) * size - 1
        );
        
        if (messageIds.isEmpty()) {
            // Load from database
            return messageRepository.findConversation(userId1, userId2, page, size);
        }
        
        return messageRepository.findByIds(messageIds);
    }
}

@Component
public class WebSocketService {
    private final Map<String, SimpMessagingTemplate> userSessions = 
        new ConcurrentHashMap<>();
    
    public void sendToUser(String userId, Message message) {
        SimpMessagingTemplate session = userSessions.get(userId);
        if (session != null) {
            session.convertAndSend("/user/" + userId + "/messages", message);
        } else {
            // User offline - store for later delivery
            offlineMessageService.storeMessage(userId, message);
        }
    }
    
    @EventListener
    public void handleSessionConnected(SessionConnectedEvent event) {
        String userId = extractUserId(event);
        userSessions.put(userId, event.getMessagingTemplate());
        
        // Deliver offline messages
        offlineMessageService.deliverOfflineMessages(userId);
    }
}
```

---

## Interview Question 43: Design a Video Streaming System (like YouTube)

### Requirements

- Upload videos
- Stream videos
- Video recommendations
- Comments and likes
- Video processing

### Architecture

```
┌─────────────┐
│   Clients   │
└──────┬──────┘
       │
       ▼
┌─────────────────┐
│  CDN (CloudFront)│
└──────┬──────────┘
       │
       ▼
┌─────────────────┐
│  Video Servers  │
└──────┬──────────┘
       │
   ┌───┴───┬────────┬────────┐
   ▼       ▼        ▼        ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│Upload│ │Proc │ │Meta │ │Rec  │
│Svc  │ │Svc  │ │Svc  │ │Svc  │
└──┬──┘ └──┬──┘ └──┬──┘ └──┬──┘
   │       │       │       │
   ▼       ▼       ▼       ▼
┌─────┐ ┌─────┐ ┌─────┐ ┌─────┐
│  S3  │ │ECS  │ │MySQL│ │Redis│
│      │ │     │ │     │ │     │
└─────┘ └─────┘ └─────┘ └─────┘
```

### Implementation

```java
@Service
public class VideoUploadService {
    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private VideoProcessingService processingService;
    
    public Video uploadVideo(MultipartFile file, VideoMetadata metadata) {
        // 1. Generate video ID
        String videoId = generateId();
        
        // 2. Upload to S3
        String s3Key = "videos/" + videoId + "/original";
        s3Service.uploadFile(s3Key, file);
        
        // 3. Create video record
        Video video = new Video();
        video.setId(videoId);
        video.setTitle(metadata.getTitle());
        video.setStatus(VideoStatus.UPLOADING);
        videoRepository.save(video);
        
        // 4. Trigger processing (async)
        processingService.processVideo(videoId, s3Key);
        
        return video;
    }
}

@Service
public class VideoProcessingService {
    @Autowired
    private ECSClient ecsClient;
    
    @Async
    public void processVideo(String videoId, String s3Key) {
        // Launch processing task
        RunTaskRequest request = RunTaskRequest.builder()
            .cluster("video-processing")
            .taskDefinition("video-processor")
            .overrides(TaskOverride.builder()
                .containerOverrides(ContainerOverride.builder()
                    .name("processor")
                    .environment(
                        EnvironmentVariable.builder()
                            .name("VIDEO_ID").value(videoId)
                            .build(),
                        EnvironmentVariable.builder()
                            .name("S3_KEY").value(s3Key)
                            .build()
                    )
                    .build()
                )
                .build()
            )
            .build();
        
        ecsClient.runTask(request);
    }
}

@Service
public class VideoStreamingService {
    @Autowired
    private CloudFrontService cloudFrontService;
    
    @Autowired
    private VideoRepository videoRepository;
    
    public StreamingResponse getVideoStream(String videoId, String quality) {
        Video video = videoRepository.findById(videoId)
            .orElseThrow(() -> new VideoNotFoundException());
        
        if (video.getStatus() != VideoStatus.READY) {
            throw new VideoNotReadyException();
        }
        
        // Get CDN URL
        String cdnUrl = cloudFrontService.getSignedUrl(
            "videos/" + videoId + "/" + quality + ".mp4"
        );
        
        return new StreamingResponse(cdnUrl, video.getDuration());
    }
}
```

---

## Interview Question 44: Design a News Feed System (like Facebook)

### Requirements

- Post updates
- News feed generation
- Like, comment, share
- Real-time updates

### Feed Generation Strategies

#### Pull Model (Fan-out on Read)

```java
@Service
public class NewsFeedService {
    @Autowired
    private PostRepository postRepository;
    
    @Autowired
    private FollowRepository followRepository;
    
    public List<Post> getNewsFeed(String userId, int page, int size) {
        // Get followed users
        List<String> followees = followRepository.getFollowees(userId);
        
        // Get recent posts from followees
        return postRepository.findRecentPostsByUsers(followees, page, size);
    }
}
```

#### Push Model (Fan-out on Write)

```java
@Service
public class NewsFeedService {
    @Autowired
    private RedisTemplate<String, String> redis;
    
    public void publishPost(Post post) {
        // Fan-out to all followers
        List<String> followers = followRepository.getFollowers(post.getUserId());
        
        for (String followerId : followers) {
            String key = "feed:" + followerId;
            redis.opsForZSet().add(key, post.getId(), 
                post.getCreatedAt().toEpochMilli());
            
            // Keep only last 1000 posts
            redis.opsForZSet().removeRange(key, 0, -1001);
        }
    }
    
    public List<Post> getNewsFeed(String userId, int page, int size) {
        String key = "feed:" + userId;
        
        // Get from cache
        Set<String> postIds = redis.opsForZSet()
            .reverseRange(key, page * size, (page + 1) * size - 1);
        
        return postRepository.findByIds(new ArrayList<>(postIds));
    }
}
```

#### Hybrid Model

```java
@Service
public class HybridNewsFeedService {
    private static final int CELEBRITY_THRESHOLD = 10000; // followers
    
    public void publishPost(Post post) {
        User user = userRepository.findById(post.getUserId());
        
        if (user.getFollowerCount() > CELEBRITY_THRESHOLD) {
            // Celebrity - use pull model (don't fan-out)
            // Store post, fetch on read
        } else {
            // Regular user - use push model (fan-out)
            fanoutToFollowers(post);
        }
    }
}
```

---

## Summary: Part 9

### Key Topics Covered:
1. ✅ Twitter/X design (tweets, timelines, feeds)
2. ✅ Chat/Messaging system (WebSocket, real-time)
3. ✅ Video streaming (upload, processing, CDN)
4. ✅ News feed system (pull vs push models)

### Design Patterns:
- **Fan-out on Write**: Push to followers
- **Fan-out on Read**: Fetch on demand
- **Hybrid**: Best of both worlds
- **Caching**: Redis for hot data
- **CDN**: For media delivery

---

**Next**: Part 10 will cover more real-world problems and best practices.


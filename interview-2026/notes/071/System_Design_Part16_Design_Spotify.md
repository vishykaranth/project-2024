# Design Spotify - Google System Design Interview

## Overview

This system design interview focuses on designing Spotify, a music streaming platform. The interview covers music streaming, playlist management, recommendations, search, and scalability for handling millions of songs and users.

## Requirements

### Functional Requirements
- User registration and authentication
- Stream music (audio playback)
- Create and manage playlists
- Search songs, artists, albums
- Music recommendations
- Follow artists and users
- Share playlists
- Offline playback (download)

### Non-Functional Requirements
- High availability (99.9%)
- Low latency (< 100ms for metadata)
- Streaming quality (multiple bitrates)
- Scalability (400M+ users, 70M+ songs)
- Support continuous playback
- Bandwidth optimization

## Capacity Estimation

```
┌─────────────────────────────────────────────────────────┐
│         Capacity Estimates                              │
└─────────────────────────────────────────────────────────┘

Users: 400 million
Daily Active Users (DAU): 180 million (45%)
Songs: 70 million
Average song size: 5MB (320kbps, 3 minutes)
Average listening time: 2.5 hours/day per user

Traffic Estimates:
- Streams per day: 180M × 50 songs/day = 9B streams/day
- Metadata reads: 180M × 100 reads/day = 18B reads/day
- Read:Write ratio: 1000:1
- Peak QPS: 18B / (24 × 3600) × 3 = ~625K reads/sec
- Streaming bandwidth: 9B × 5MB = 45PB/day

Storage:
- Songs: 70M × 5MB × multiple bitrates = ~1.75PB
- With multiple formats (MP3, OGG): ~3.5PB
- With 3x replication: ~10.5PB
- Metadata: 70M × 10KB = 700GB
```

## High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Spotify System Architecture                     │
└─────────────────────────────────────────────────────────┘

                    [Users]
                       │
                       ▼
              ┌────────────────┐
              │  CDN / LB       │
              └────────┬────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
   [API Gateway]  [API Gateway]  [API Gateway]
        │              │              │
        └──────┬───────┴──────┬───────┘
               │              │
    ┌──────────┼──────────────┼──────────┐
    │          │              │          │
    ▼          ▼              ▼          ▼
[Streaming] [Search]  [Playlist] [Recommendation]
 Service    Service    Service      Service
    │          │          │          │
    │          │          │          │
    ▼          ▼          ▼          ▼
[CDN]    [Search]  [Playlist] [ML Models]
Network    Index      DB
```

## Core Components

### 1. Music Streaming Service

```
┌─────────────────────────────────────────────────────────┐
│         Music Streaming Flow                            │
└─────────────────────────────────────────────────────────┘

User requests song
    │
    ▼
[Streaming Service]
    ├─► Get song metadata
    ├─► Determine user's connection
    ├─► Select bitrate
    │   ├─► 320kbps (premium)
    │   ├─► 160kbps (free)
    │   └─► 96kbps (low bandwidth)
    └─► Serve from CDN
         │
         ▼
    [CDN Network]
    ├─► Edge servers globally
    ├─► Cache popular songs
    └─► Progressive streaming
```

**Streaming Strategies:**

**Progressive Download:**
- Download and play simultaneously
- Buffer ahead
- Support seeking

**Adaptive Streaming:**
- Adjust quality based on bandwidth
- Multiple bitrate files
- Seamless quality switching

### 2. Recommendation Service

```
┌─────────────────────────────────────────────────────────┐
│         Recommendation System                          │
└─────────────────────────────────────────────────────────┘

[Recommendation Service]
    │
    ├─► Collaborative Filtering
    │   ├─► User-based
    │   └─► Item-based
    │
    ├─► Content-Based Filtering
    │   ├─► Audio features
    │   ├─► Genre, mood, tempo
    │   └─► User preferences
    │
    ├─► Deep Learning Models
    │   ├─► Neural networks
    │   ├─► Audio embeddings
    │   └─► Sequence models
    │
    └─► Hybrid Approach
        ├─► Combine multiple signals
        └─► Real-time personalization
             │
             ▼
        [ML Pipeline]
        ├─► Feature extraction
        ├─► Model training
        └─► A/B testing
```

**Recommendation Factors:**
- Listening history
- Skip behavior
- Playlist creation
- Artist follows
- Audio features (genre, mood, tempo)
- Time of day
- Geographic location

### 3. Playlist Service

```
┌─────────────────────────────────────────────────────────┐
│         Playlist Management                           │
└─────────────────────────────────────────────────────────┘

[Playlist Service]
    │
    ├─► Create playlist
    ├─► Add/remove songs
    ├─► Reorder songs
    ├─► Share playlists
    └─► Collaborative playlists
         │
         ▼
    [Playlist Database]
    ├─► Playlist metadata
    ├─► Song order
    └─► Sharing permissions
```

**Playlist Features:**
- User playlists
- Collaborative playlists
- Public/private playlists
- Playlist following
- Auto-generated playlists (Discover Weekly, Daily Mix)

### 4. Search Service

```
┌─────────────────────────────────────────────────────────┐
│         Search Architecture                            │
└─────────────────────────────────────────────────────────┘

[Search Service]
    │
    ├─► Song search
    ├─► Artist search
    ├─► Album search
    ├─► Playlist search
    └─► Auto-complete
         │
         ▼
    [Search Index] (Elasticsearch)
    ├─► Song titles
    ├─► Artist names
    ├─► Album names
    ├─► Lyrics (if available)
    └─► Genres
```

**Search Features:**
- Full-text search
- Fuzzy matching
- Autocomplete
- Filters (genre, year, duration)
- Sorting (relevance, popularity, date)

### 5. Offline Playback Service

```
┌─────────────────────────────────────────────────────────┐
│         Offline Download Flow                          │
└─────────────────────────────────────────────────────────┘

User downloads song/playlist
    │
    ▼
[Download Service]
    ├─► Validate subscription (premium)
    ├─► Encrypt content (DRM)
    ├─► Download to device
    └─► Store locally
         │
         ▼
    [Device Storage]
    ├─► Encrypted files
    └─► Playback metadata
```

**Offline Features:**
- Download songs/playlists
- DRM protection
- Sync across devices
- Download limits
- Expiration (if subscription ends)

## Database Design

### Data Models

```
┌─────────────────────────────────────────────────────────┐
│         Database Schema                                 │
└─────────────────────────────────────────────────────────┘

Songs Table:
- song_id (PK)
- title
- duration
- artist_id (FK)
- album_id (FK)
- genre
- release_date
- play_count
- like_count

Artists Table:
- artist_id (PK)
- name
- bio
- follower_count
- image_url
- created_at

Albums Table:
- album_id (PK)
- title
- artist_id (FK)
- release_date
- cover_image_url
- track_count

Playlists Table:
- playlist_id (PK)
- user_id (FK)
- name
- description
- is_public
- follower_count
- created_at

Playlist_Songs Table:
- playlist_id (FK)
- song_id (FK)
- position
- added_at
- PRIMARY KEY (playlist_id, position)

Users Table:
- user_id (PK)
- email
- subscription_type (free, premium)
- created_at
```

### Database Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Database Selection                             │
└─────────────────────────────────────────────────────────┘

MySQL/PostgreSQL:
├─► User data
├─► Song metadata
├─► Artist/Album data
└─► Playlists

Redis (Cache):
├─► Popular songs
├─► User playlists
├─► Recommendations
└─► Search cache

Object Storage (S3/GCS):
└─► Audio files (all bitrates)

CDN:
└─► Cached popular songs

Elasticsearch:
└─► Search index
```

## Scalability Solutions

### Music Storage

```
┌─────────────────────────────────────────────────────────┐
│         Storage Strategy                               │
└─────────────────────────────────────────────────────────┘

Object Storage:
├─► Original master files
├─► Multiple bitrate versions
└─► Multiple formats (MP3, OGG)

CDN Distribution:
├─► Popular songs cached at edge
├─► Geographic distribution
└─► Reduced latency

Storage Optimization:
├─► Compression (OGG Vorbis, AAC)
├─► Multiple quality options
├─► Lazy loading for less popular
└─► Archive old/unpopular songs
```

### Caching Strategy

```
┌─────────────────────────────────────────────────────────┐
│         Multi-Layer Caching                           │
└─────────────────────────────────────────────────────────┘

L1: Application Cache
    ├─► Song metadata
    └─► TTL: 5 minutes

L2: Distributed Cache (Redis)
    ├─► Popular songs
    ├─► User playlists
    ├─► Recommendations
    └─► TTL: 1 hour

L3: CDN
    ├─► Audio files
    ├─► Album art
    └─► TTL: 24 hours (popular), 1 hour (others)
```

## Key Design Decisions

### 1. Streaming Quality
- **Decision**: Multiple bitrates with adaptive streaming
- **Reason**: Handle varying network conditions
- **Trade-off**: Storage cost vs. User experience

### 2. Recommendation System
- **Decision**: Hybrid (collaborative + content-based + ML)
- **Reason**: Better accuracy and diversity
- **Trade-off**: Complexity vs. Engagement

### 3. Offline Playback
- **Decision**: DRM-protected downloads
- **Reason**: Content protection, premium feature
- **Trade-off**: Complexity vs. Revenue

### 4. Storage
- **Decision**: Object storage + CDN
- **Reason**: Cost-effective, scalable, global
- **Trade-off**: Cost vs. Performance

## API Design

### Key Endpoints

```
GET    /api/v1/songs/{id}/stream
GET    /api/v1/songs/{id}

GET    /api/v1/search?q={query}&type={type}

GET    /api/v1/playlists
POST   /api/v1/playlists
GET    /api/v1/playlists/{id}
PUT    /api/v1/playlists/{id}/songs

GET    /api/v1/recommendations
GET    /api/v1/recommendations/discover-weekly

POST   /api/v1/songs/{id}/download
GET    /api/v1/offline/songs
```

## Interview Takeaways

### Key Points Covered
1. ✅ Requirements clarification
2. ✅ Capacity estimation (massive storage)
3. ✅ Music streaming architecture
4. ✅ Recommendation system (ML)
5. ✅ Playlist management
6. ✅ Search functionality
7. ✅ Offline playback
8. ✅ CDN strategy

### Common Pitfalls
- ❌ Not considering multiple bitrates
- ❌ Ignoring recommendation algorithm
- ❌ Not discussing CDN strategy
- ❌ Overlooking offline playback
- ❌ Not considering bandwidth costs

## Summary

Designing Spotify requires:
- **Massive storage** (petabytes for millions of songs)
- **Music streaming** (multiple bitrates, CDN)
- **ML recommendation system** (personalized playlists)
- **Playlist management** (user and collaborative)
- **Search functionality** (songs, artists, albums)
- **Offline playback** (DRM-protected downloads)
- **Scalable architecture** for millions of users

**Key Learning**: Music streaming platforms require efficient storage and delivery of audio files, sophisticated recommendation algorithms, and support for offline playback. The recommendation system is critical for user engagement and retention.

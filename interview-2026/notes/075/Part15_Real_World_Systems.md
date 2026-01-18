# Part 15: Real-World System Designs - Quick Revision

## Twitter/X Design

- **Scale**: 500M+ tweets/day, 100:1 read-to-write ratio
- **Timeline**: Home timeline (following), User timeline (user's tweets)
- **Social Graph**: Follow/unfollow relationships, efficient storage
- **Search**: Full-text search, hashtags, trending topics
- **Media**: Image/video uploads, processing, CDN delivery

## Uber/Lyft Design

- **Matching**: Match drivers with riders based on location
- **Location Tracking**: Real-time GPS, geospatial indexing
- **Surge Pricing**: Dynamic pricing based on demand/supply
- **Scale**: Millions of concurrent users, real-time updates
- **Payment**: Payment processing, driver payouts

## Video Streaming (YouTube/Netflix)

- **Storage**: Object storage (S3), multiple resolutions
- **Encoding**: Transcode to multiple formats, adaptive bitrate (HLS, DASH)
- **Delivery**: CDN for global distribution, edge caching
- **Recommendations**: ML-based, user history, trending
- **Scale**: Petabytes of storage, terabits of bandwidth

## E-Commerce (Amazon)

- **Catalog**: Product information, search, recommendations
- **Cart**: Session management, checkout flow
- **Inventory**: Stock tracking, reservation, prevent overselling
- **Payment**: Payment gateway, transactions, security (PCI)
- **Orders**: Order management, fulfillment, shipping

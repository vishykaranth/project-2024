# GraphQL: Query Language, Schema Design, Resolvers

## Overview

GraphQL is a query language and runtime for APIs that allows clients to request exactly the data they need. It provides a single endpoint and enables clients to specify the structure of the response.

## What is GraphQL?

```
┌─────────────────────────────────────────────────────────┐
│              GraphQL Architecture                       │
└─────────────────────────────────────────────────────────┘

Client
    │
    ▼
┌─────────────────┐
│ GraphQL Query   │
│ {               │
│   user(id: 1) { │
│     name        │
│     email       │
│   }             │
│ }               │
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ GraphQL Server  │
│                 │
│  - Schema       │
│  - Resolvers    │
│  - Execution    │
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
Service A  Service B
```

## GraphQL vs REST

```
┌─────────────────────────────────────────────────────────┐
│         GraphQL vs REST                                 │
└─────────────────────────────────────────────────────────┘

REST:
GET /users/1
GET /users/1/orders
GET /users/1/profile
→ Multiple requests

GraphQL:
query {
  user(id: 1) {
    name
    orders {
      id
      total
    }
    profile {
      bio
    }
  }
}
→ Single request
```

## GraphQL Schema

### Schema Definition Language (SDL)

```graphql
type User {
  id: ID!
  name: String!
  email: String!
  posts: [Post!]!
  createdAt: DateTime!
}

type Post {
  id: ID!
  title: String!
  content: String!
  author: User!
  comments: [Comment!]!
}

type Comment {
  id: ID!
  text: String!
  author: User!
  post: Post!
}

type Query {
  user(id: ID!): User
  users(limit: Int, offset: Int): [User!]!
  post(id: ID!): Post
  posts: [Post!]!
}

type Mutation {
  createUser(input: CreateUserInput!): User!
  updateUser(id: ID!, input: UpdateUserInput!): User!
  deleteUser(id: ID!): Boolean!
}

input CreateUserInput {
  name: String!
  email: String!
  password: String!
}

input UpdateUserInput {
  name: String
  email: String
}
```

### Scalar Types

```graphql
# Built-in scalars
String
Int
Float
Boolean
ID

# Custom scalars
scalar DateTime
scalar Email
scalar URL
```

### Type Modifiers

```graphql
String      # Nullable
String!     # Non-nullable
[String]    # List of nullable strings
[String!]   # List of non-nullable strings
[String!]!  # Non-nullable list of non-nullable strings
```

## GraphQL Queries

### Simple Query

```graphql
query {
  user(id: "1") {
    id
    name
    email
  }
}
```

### Nested Query

```graphql
query {
  user(id: "1") {
    id
    name
    posts {
      id
      title
      comments {
        id
        text
        author {
          name
        }
      }
    }
  }
}
```

### Query with Variables

```graphql
query GetUser($userId: ID!) {
  user(id: $userId) {
    id
    name
    email
  }
}

# Variables
{
  "userId": "1"
}
```

### Query with Fragments

```graphql
fragment UserFields on User {
  id
  name
  email
  createdAt
}

query {
  user(id: "1") {
    ...UserFields
    posts {
      id
      title
    }
  }
}
```

### Query with Aliases

```graphql
query {
  user1: user(id: "1") {
    name
  }
  user2: user(id: "2") {
    name
  }
}
```

## GraphQL Mutations

### Create Mutation

```graphql
mutation {
  createUser(input: {
    name: "John Doe"
    email: "john@example.com"
    password: "secret123"
  }) {
    id
    name
    email
  }
}
```

### Update Mutation

```graphql
mutation {
  updateUser(id: "1", input: {
    name: "Jane Doe"
  }) {
    id
    name
    email
  }
}
```

### Delete Mutation

```graphql
mutation {
  deleteUser(id: "1")
}
```

## Resolvers

### What are Resolvers?

Resolvers are functions that resolve a field in the schema. They fetch data from data sources (databases, APIs, etc.).

### Resolver Structure

```java
@Component
public class UserResolver implements GraphQLQueryResolver {
    
    public User user(String id) {
        return userService.getUser(id);
    }
    
    public List<User> users(Integer limit, Integer offset) {
        return userService.getUsers(limit, offset);
    }
}
```

### Field Resolvers

```java
@Component
public class UserResolver implements GraphQLResolver<User> {
    
    public List<Post> posts(User user) {
        return postService.getPostsByUserId(user.getId());
    }
    
    public Profile profile(User user) {
        return profileService.getProfile(user.getId());
    }
}
```

### Nested Resolvers

```java
@Component
public class PostResolver implements GraphQLResolver<Post> {
    
    public User author(Post post) {
        return userService.getUser(post.getAuthorId());
    }
    
    public List<Comment> comments(Post post) {
        return commentService.getCommentsByPostId(post.getId());
    }
}
```

## GraphQL Implementation (Spring Boot)

### Dependencies

```xml
<dependency>
    <groupId>com.graphql-java</groupId>
    <artifactId>graphql-spring-boot-starter</artifactId>
    <version>5.0.2</version>
</dependency>
<dependency>
    <groupId>com.graphql-java</groupId>
    <artifactId>graphql-java-tools</artifactId>
    <version>5.2.4</version>
</dependency>
```

### Schema File

```graphql
# schema.graphqls
type User {
  id: ID!
  name: String!
  email: String!
  posts: [Post!]!
}

type Post {
  id: ID!
  title: String!
  content: String!
  author: User!
}

type Query {
  user(id: ID!): User
  users: [User!]!
}

type Mutation {
  createUser(input: CreateUserInput!): User!
}

input CreateUserInput {
  name: String!
  email: String!
}
```

### Resolvers

```java
@Component
public class UserQueryResolver implements GraphQLQueryResolver {
    
    private final UserService userService;
    
    public User user(String id) {
        return userService.getUser(id);
    }
    
    public List<User> users() {
        return userService.getAllUsers();
    }
}

@Component
public class UserMutationResolver implements GraphQLMutationResolver {
    
    private final UserService userService;
    
    public User createUser(CreateUserInput input) {
        return userService.createUser(input);
    }
}

@Component
public class UserResolver implements GraphQLResolver<User> {
    
    private final PostService postService;
    
    public List<Post> posts(User user) {
        return postService.getPostsByUserId(user.getId());
    }
}
```

## GraphQL Advantages

```
┌─────────────────────────────────────────────────────────┐
│         GraphQL Advantages                              │
└─────────────────────────────────────────────────────────┘

├─ Single Endpoint
│  └─ One endpoint for all operations
│
├─ Flexible Queries
│  └─ Client requests exactly what it needs
│
├─ No Over-fetching
│  └─ Only requested fields returned
│
├─ No Under-fetching
│  └─ Get related data in one query
│
├─ Strongly Typed
│  └─ Schema defines types
│
└─ Introspection
   └─ Self-documenting API
```

## GraphQL Challenges

```
┌─────────────────────────────────────────────────────────┐
│         GraphQL Challenges                               │
└─────────────────────────────────────────────────────────┘

├─ Complexity
│  └─ Can be complex for simple use cases
│
├─ Caching
│  └─ HTTP caching less effective
│
├─ File Uploads
│  └─ Requires special handling
│
├─ Error Handling
│  └─ Different from REST
│
└─ N+1 Problem
   └─ Need data loaders
```

## Data Loaders (N+1 Problem)

### Problem

```java
// N+1 Problem
public List<Post> posts(User user) {
    // 1 query for posts
    List<Post> posts = postService.getPostsByUserId(user.getId());
    // N queries for authors (one per post)
    posts.forEach(post -> {
        post.setAuthor(userService.getUser(post.getAuthorId()));
    });
    return posts;
}
```

### Solution: DataLoader

```java
@Component
public class UserDataLoader {
    
    @Bean
    public DataLoader<String, User> userDataLoader() {
        return DataLoader.newDataLoader(userIds -> {
            List<User> users = userService.getUsers(userIds);
            Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
            return CompletableFuture.completedFuture(
                userIds.stream()
                    .map(userMap::get)
                    .collect(Collectors.toList())
            );
        });
    }
}

@Component
public class PostResolver implements GraphQLResolver<Post> {
    
    @Autowired
    private DataLoader<String, User> userDataLoader;
    
    public CompletableFuture<User> author(Post post) {
        return userDataLoader.load(post.getAuthorId());
    }
}
```

## GraphQL Subscriptions

### Real-time Updates

```graphql
subscription {
  postAdded {
    id
    title
    author {
      name
    }
  }
}
```

### Implementation

```java
@Component
public class PostSubscriptionResolver implements GraphQLSubscriptionResolver {
    
    private final PostPublisher postPublisher;
    
    public Publisher<Post> postAdded() {
        return postPublisher.getPostStream();
    }
}
```

## Best Practices

### 1. Design Schema First

```graphql
# Design schema before implementation
type User {
  id: ID!
  name: String!
  # ... other fields
}
```

### 2. Use DataLoaders

```java
// Prevent N+1 queries
@Autowired
private DataLoader<String, User> userDataLoader;
```

### 3. Implement Pagination

```graphql
type Query {
  users(first: Int, after: String): UserConnection!
}

type UserConnection {
  edges: [UserEdge!]!
  pageInfo: PageInfo!
}

type UserEdge {
  node: User!
  cursor: String!
}
```

### 4. Handle Errors Gracefully

```java
public User user(String id) {
    try {
        return userService.getUser(id);
    } catch (UserNotFoundException e) {
        throw new GraphQLException("User not found", e);
    }
}
```

### 5. Use Fragments

```graphql
fragment UserFields on User {
  id
  name
  email
}
```

## Summary

GraphQL:
- **Query Language**: Flexible, client-specified queries
- **Schema Design**: Strongly typed, self-documenting
- **Resolvers**: Functions that fetch data
- **Benefits**: Single endpoint, no over/under-fetching
- **Challenges**: Complexity, caching, N+1 problem

**Key Concepts:**
- Schema Definition Language (SDL)
- Queries and Mutations
- Resolvers
- DataLoaders
- Subscriptions

**Best Practices:**
- Design schema first
- Use DataLoaders
- Implement pagination
- Handle errors gracefully
- Use fragments

**Remember**: GraphQL provides flexible, efficient data fetching with a single endpoint and client-controlled responses!

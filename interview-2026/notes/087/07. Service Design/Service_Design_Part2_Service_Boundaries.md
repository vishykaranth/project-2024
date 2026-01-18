# Service Design Part 2: How Do You Define Service Boundaries?

## Question 127: How do you define service boundaries?

### Answer

### Service Boundary Definition Strategies

#### 1. **Domain-Driven Design (DDD) Approach**

```
┌─────────────────────────────────────────────────────────┐
│         Domain-Driven Design                          │
└─────────────────────────────────────────────────────────┘

Bounded Contexts:
├─ Each bounded context = potential service
├─ Clear domain boundaries
├─ Ubiquitous language
└─ Domain experts involved

Our Bounded Contexts:
├─ Agent Management Context → Agent Match Service
├─ Conversation Context → Conversation Service
├─ Natural Language Context → NLU Facade Service
├─ Messaging Context → Message Service
└─ Session Context → Session Service
```

**Domain Model:**

```
┌─────────────────────────────────────────────────────────┐
│         Domain Model                                    │
└─────────────────────────────────────────────────────────┘

Agent Management Domain:
├─ Entities: Agent, AgentState, AgentSkill
├─ Value Objects: AgentStatus, SkillLevel
├─ Aggregates: Agent (root)
└─ Services: AgentRouting, AgentMatching

Conversation Domain:
├─ Entities: Conversation, Message, Participant
├─ Value Objects: ConversationStatus, MessageType
├─ Aggregates: Conversation (root)
└─ Services: ConversationOrchestration
```

#### 2. **Business Capability Mapping**

```
┌─────────────────────────────────────────────────────────┐
│         Business Capabilities                         │
└─────────────────────────────────────────────────────────┘

Capability → Service Mapping:
├─ Agent Matching → Agent Match Service
├─ Conversation Management → Conversation Service
├─ Natural Language Understanding → NLU Facade Service
├─ Message Delivery → Message Service
├─ Bot Interactions → Bot Service
└─ Session Management → Session Service

Each Capability:
├─ Owns its data
├─ Has clear responsibilities
├─ Independent deployment
└─ Own team
```

#### 3. **Data Ownership Principle**

```
┌─────────────────────────────────────────────────────────┐
│         Data Ownership                                 │
└─────────────────────────────────────────────────────────┘

Rule: Each service owns its data

Agent Match Service:
├─ Owns: Agent data, Agent state
├─ Database: agent_db
└─ No other service writes to agent_db

Conversation Service:
├─ Owns: Conversation data, Messages
├─ Database: conversation_db
└─ No other service writes to conversation_db

NLU Facade Service:
├─ Owns: NLU responses, Provider configs
├─ Database: nlu_db
└─ No other service writes to nlu_db
```

**Data Ownership Rules:**
- Service owns its database
- Other services access via API only
- No shared databases
- Event-driven for data synchronization

#### 4. **Communication Patterns**

```
┌─────────────────────────────────────────────────────────┐
│         Service Communication                          │
└─────────────────────────────────────────────────────────┘

High Communication → Same Service:
├─ Agent Match ↔ Conversation: High
├─ Reason: Tightly coupled
├─ Decision: Could be same service (but we kept separate for scaling)

Low Communication → Separate Services:
├─ NLU Facade ↔ Session: Low
├─ Reason: Loosely coupled
├─ Decision: Separate services

Communication Matrix:
         │ Agent │ Conv │ NLU │ Msg │ Bot │ Session
---------|-------|------|-----|-----|-----|--------
Agent    │  -    │ High │ Med │ Low │ Low │ Low
Conv     │ High  │  -   │ Med │ High│ Med │ High
NLU      │ Med   │ Med  │  -  │ Low │ High│ Low
Message  │ Low   │ High │ Low │  -  │ Low │ High
Bot      │ Low   │ Med  │ High│ Low │  -  │ Med
Session  │ Low   │ High │ Low │ High│ Med │  -
```

#### 5. **Team Structure Alignment**

```
┌─────────────────────────────────────────────────────────┐
│         Team-Service Alignment                         │
└─────────────────────────────────────────────────────────┘

Two Pizza Team Rule:
├─ Team size: 5-8 engineers
├─ One team = One or two services
└─ Clear ownership

Our Teams:
├─ Team 1: Agent Match + Conversation (5 engineers)
├─ Team 2: NLU Facade + Bot (4 engineers)
├─ Team 3: Message + Session (3 engineers)
└─ Platform Team: Infrastructure (6 engineers)

Benefits:
├─ Clear ownership
├─ Faster decision making
├─ Better accountability
└─ Reduced coordination overhead
```

### Service Boundary Criteria

#### 1. **Cohesion (High Internal Cohesion)**

```
┌─────────────────────────────────────────────────────────┐
│         Cohesion Principle                             │
└─────────────────────────────────────────────────────────┘

High Cohesion:
├─ Related functionality together
├─ Strong internal relationships
└─ Clear purpose

Agent Match Service:
├─ Agent state management
├─ Agent routing
├─ Agent matching
└─ All agent-related functionality

Low Cohesion (Bad):
├─ Agent management + Message delivery
├─ Unrelated functionality
└─ Difficult to maintain
```

#### 2. **Coupling (Low External Coupling)**

```
┌─────────────────────────────────────────────────────────┐
│         Coupling Principle                             │
└─────────────────────────────────────────────────────────┘

Low Coupling:
├─ Minimal dependencies
├─ Loose connections
└─ Independent services

High Coupling (Bad):
├─ Direct database access
├─ Tight dependencies
└─ Difficult to change

Our Approach:
├─ API-based communication
├─ Event-driven updates
├─ No direct database access
└─ Loose coupling
```

#### 3. **Autonomy**

```
┌─────────────────────────────────────────────────────────┐
│         Service Autonomy                               │
└─────────────────────────────────────────────────────────┘

Autonomous Service:
├─ Independent deployment
├─ Own database
├─ Own technology stack
└─ Can evolve independently

Agent Match Service:
├─ Deploys independently
├─ Own Redis for state
├─ Own PostgreSQL for config
└─ Can change without affecting others

Non-Autonomous (Bad):
├─ Shared database
├─ Shared deployment
└─ Cannot change independently
```

### Service Boundary Examples

#### 1. **Agent Match Service Boundary**

```
┌─────────────────────────────────────────────────────────┐
│         Agent Match Service Boundary                   │
└─────────────────────────────────────────────────────────┘

Inside Boundary:
├─ Agent state management
├─ Agent routing logic
├─ Agent matching algorithm
├─ Agent availability tracking
└─ Agent skill matching

Outside Boundary (Other Services):
├─ Conversation management
├─ Message delivery
├─ NLU processing
└─ Session management

Data Owned:
├─ Agent profiles
├─ Agent state
├─ Agent skills
└─ Routing configuration

APIs Provided:
├─ POST /agents/match
├─ GET /agents/{id}/state
├─ PUT /agents/{id}/state
└─ GET /agents/available
```

#### 2. **NLU Facade Service Boundary**

```
┌─────────────────────────────────────────────────────────┐
│         NLU Facade Service Boundary                    │
└─────────────────────────────────────────────────────────┘

Inside Boundary:
├─ NLU provider abstraction
├─ Provider selection
├─ Response caching
├─ Circuit breaker logic
└─ Fallback mechanisms

Outside Boundary:
├─ Agent management
├─ Conversation management
├─ Message delivery
└─ Bot logic

Data Owned:
├─ NLU responses (cache)
├─ Provider configurations
├─ Circuit breaker state
└─ Provider metrics

APIs Provided:
├─ POST /nlu/process
├─ GET /nlu/providers
└─ GET /nlu/health
```

### Anti-Patterns to Avoid

#### 1. **Anemic Domain Model**

```
┌─────────────────────────────────────────────────────────┐
│         Anemic Domain Anti-Pattern                     │
└─────────────────────────────────────────────────────────┘

Bad: Service with only data access
├─ No business logic
├─ Just CRUD operations
└─ Logic in other services

Good: Rich domain model
├─ Business logic in service
├─ Encapsulated behavior
└─ Self-contained service
```

#### 2. **God Service**

```
┌─────────────────────────────────────────────────────────┐
│         God Service Anti-Pattern                       │
└─────────────────────────────────────────────────────────┘

Bad: One service does everything
├─ Too many responsibilities
├─ Difficult to maintain
└─ Cannot scale independently

Good: Focused services
├─ Single responsibility
├─ Clear boundaries
└─ Easy to maintain
```

#### 3. **Shared Database**

```
┌─────────────────────────────────────────────────────────┐
│         Shared Database Anti-Pattern                   │
└─────────────────────────────────────────────────────────┘

Bad: Multiple services share database
├─ Tight coupling
├─ Cannot evolve independently
└─ Data ownership unclear

Good: Database per service
├─ Clear ownership
├─ Independent evolution
└─ Loose coupling
```

### Boundary Evolution

```
┌─────────────────────────────────────────────────────────┐
│         Service Boundary Evolution                     │
└─────────────────────────────────────────────────────────┘

Initial Boundaries:
├─ Based on initial requirements
├─ May not be perfect
└─ Will evolve

Evolution Triggers:
├─ High communication between services
├─ Shared data patterns
├─ Team structure changes
└─ Performance issues

Evolution Strategy:
├─ Monitor service interactions
├─ Identify boundary issues
├─ Refactor gradually
└─ Use strangler fig pattern
```

### Boundary Testing

```
┌─────────────────────────────────────────────────────────┐
│         Boundary Testing                               │
└─────────────────────────────────────────────────────────┘

Contract Testing:
├─ API contracts between services
├─ Ensure compatibility
└─ Prevent breaking changes

Integration Testing:
├─ Test service interactions
├─ Verify boundaries
└─ End-to-end scenarios

Monitoring:
├─ Track service calls
├─ Identify tight coupling
└─ Measure service independence
```

### Summary

**Service Boundary Definition Principles:**

1. **Domain-Driven Design**: Align with business domains
2. **Business Capabilities**: Map to business functions
3. **Data Ownership**: Each service owns its data
4. **High Cohesion**: Related functionality together
5. **Low Coupling**: Minimal dependencies
6. **Team Alignment**: Match team structure
7. **Autonomy**: Independent deployment and evolution

**Key Rules:**
- One service = One database (no sharing)
- API-based communication (no direct DB access)
- Event-driven for data sync
- Clear ownership and responsibility
- Independent scaling and deployment

**Result:**
- Clear service boundaries
- Independent teams and deployments
- Easy to maintain and evolve
- Scalable architecture

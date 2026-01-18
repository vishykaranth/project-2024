# Interview Answers - Part 5: Leadership & Team Management (Questions 21-25)

## Question 21: Tell me about a time when you had to lead a team through a difficult project.

### Answer

One of the most challenging projects I led was scaling the conversational AI platform at LivePerson from 4M to 12M+ conversations/month while the system was already under stress and experiencing performance issues.

**The Situation:**
- **Challenge**: Scale platform 3x while maintaining performance and reliability
- **Timeline**: 6 months
- **Team**: Cross-functional team of 8 engineers
- **Stakes**: Business growth depended on this, system was already struggling

**The Difficulties:**
1. **System Under Stress**: Platform was already experiencing high latency and occasional crashes
2. **Tight Timeline**: Business needed scale within 6 months
3. **Zero Downtime**: Couldn't afford downtime during scaling
4. **Team Concerns**: Team was worried about making changes to a stressed system
5. **Multiple Dependencies**: Database, caching, services, infrastructure all needed changes

**My Leadership Approach:**

**1. Setting Clear Vision:**
- Communicated the "why" - business growth, customer impact
- Set clear goals: 12M+ conversations/month, 99.9% uptime, 40% cost reduction
- Created roadmap with milestones

**2. Building Team Confidence:**
- Acknowledged concerns and addressed them
- Provided technical guidance and support
- Broke down work into manageable chunks
- Celebrated small wins

**3. Technical Leadership:**
- Designed architecture (stateless services, event-driven, caching)
- Made key technical decisions
- Provided hands-on guidance
- Code reviewed critical changes

**4. Risk Management:**
- Identified risks early (database bottleneck, service crashes)
- Created mitigation plans
- Implemented incremental changes
- Had rollback plans for each phase

**5. Communication:**
- Daily standups for alignment
- Weekly progress reviews with stakeholders
- Transparent communication about challenges
- Regular updates to management

**6. Problem-Solving:**
- When database became bottleneck: Implemented read replicas and caching
- When services crashed: Implemented horizontal scaling and resource optimization
- When latency increased: Optimized queries and implemented caching
- When costs rose: Right-sized instances and optimized resources

**7. Team Support:**
- Pair programming for complex changes
- Mentoring on new technologies (Kafka, Redis)
- Removing blockers
- Providing resources and tools

**8. Execution:**
- Phased approach: Architecture → Database → Services → Optimization
- Incremental deployment: Test each phase before next
- Continuous monitoring: Track metrics at each step
- Quick iteration: Fix issues as they arise

**The Results:**
- ✅ Successfully scaled to 12M+ conversations/month
- ✅ Achieved 99.9% uptime
- ✅ Reduced infrastructure costs by 40%
- ✅ Reduced P95 latency by 60%
- ✅ Zero production incidents
- ✅ Team grew in confidence and skills

**What I Learned:**
1. **Clear Communication**: Critical for team alignment and stakeholder management
2. **Incremental Approach**: Safer than big-bang changes
3. **Team Support**: Providing guidance and removing blockers is essential
4. **Risk Management**: Identify and mitigate risks early
5. **Celebrate Wins**: Acknowledge progress to maintain momentum

**Key Leadership Lessons:**
- Lead by example (hands-on when needed)
- Build confidence through small wins
- Communicate transparently
- Support team through challenges
- Make tough decisions when needed

This project taught me that leading through difficult projects requires a combination of technical leadership, people management, and strong execution.

---

## Question 22: You mention "mentoring teams using pair programming." Can you give a specific example?

### Answer

Absolutely. Here's a specific example of using pair programming for mentoring:

**Situation:**
At LivePerson, I was mentoring a mid-level engineer who was transitioning to work on the Agent Match Service. The service needed to handle agent state management and real-time routing, which involved distributed systems concepts (Redis, Kafka, event-driven architecture) that the engineer wasn't familiar with.

**The Challenge:**
- Engineer had strong Java/Spring skills but limited experience with:
  - Distributed state management
  - Event-driven architecture
  - Redis operations
  - Kafka event publishing
- Needed to contribute to critical service quickly
- Service was already in production, so changes needed to be careful

**My Pair Programming Approach:**

**Session 1: Understanding the Problem (2 hours)**
- **Together**: Reviewed Agent Match Service architecture
- **Together**: Walked through existing code
- **I Explained**: Distributed state management concepts
- **I Explained**: Event-driven architecture patterns
- **Together**: Identified areas for improvement

**Session 2: Design Discussion (1 hour)**
- **Together**: Designed new feature (agent state synchronization)
- **I Guided**: Asked questions to help engineer think through design
- **Together**: Discussed trade-offs
- **Together**: Created design document

**Session 3: Implementation - Part 1 (3 hours)**
- **Engineer Drove**: Wrote code with me observing
- **I Guided**: Asked questions, suggested improvements
- **Together**: Discussed Redis operations
- **Together**: Discussed error handling
- **I Explained**: Distributed locking concepts

**Session 4: Implementation - Part 2 (2 hours)**
- **Engineer Drove**: Continued implementation
- **I Guided**: Code review in real-time
- **Together**: Discussed Kafka event publishing
- **Together**: Discussed idempotency
- **I Explained**: Event ordering guarantees

**Session 5: Testing & Review (2 hours)**
- **Together**: Wrote unit tests
- **Together**: Discussed integration testing
- **I Explained**: Testing distributed systems
- **Together**: Code review and refactoring

**Follow-up Sessions:**
- **Engineer Implemented**: Similar features independently
- **I Reviewed**: Code reviews with detailed feedback
- **Together**: Discussed improvements
- **Engineer Grew**: Became confident in distributed systems

**Key Mentoring Techniques Used:**

**1. Gradual Handoff:**
```
Session 1-2: I lead, engineer observes
Session 3-4: Engineer drives, I guide
Session 5+: Engineer implements, I review
```

**2. Socratic Method:**
- Asked questions instead of giving answers
- Helped engineer discover solutions
- Encouraged critical thinking

**3. Real-Time Feedback:**
- Immediate code review
- Discussed alternatives
- Explained trade-offs
- Shared best practices

**4. Knowledge Transfer:**
- Explained concepts (distributed state, event-driven)
- Shared patterns (circuit breaker, retry)
- Discussed anti-patterns to avoid
- Provided resources for further learning

**5. Confidence Building:**
- Started with simpler tasks
- Gradually increased complexity
- Celebrated progress
- Provided positive reinforcement

**Results:**

**For the Engineer:**
- ✅ Gained confidence in distributed systems
- ✅ Learned Redis and Kafka patterns
- ✅ Contributed to critical service
- ✅ Became mentor for other engineers
- ✅ Promoted to senior engineer

**For the Team:**
- ✅ Knowledge spread to other team members
- ✅ Improved code quality
- ✅ Reduced code review cycles
- ✅ Better architecture understanding

**For Me:**
- ✅ Reinforced my own learning
- ✅ Improved my mentoring skills
- ✅ Built stronger team
- ✅ Created multiplier effect

**Key Learnings:**

**What Worked:**
- Gradual handoff (observe → guide → review)
- Real-time feedback
- Socratic method
- Practical application
- Follow-up support

**What I'd Improve:**
- More structured learning path
- Additional resources earlier
- More pair programming sessions
- Peer pairing opportunities

**Pair Programming Benefits:**
1. **Knowledge Transfer**: Fast, effective learning
2. **Code Quality**: Real-time review improves quality
3. **Team Building**: Builds relationships
4. **Confidence**: Engineer gains confidence quickly
5. **Culture**: Creates learning culture

This experience demonstrated that pair programming is one of the most effective mentoring techniques, especially for complex technical concepts.

---

## Question 23: How do you handle conflicts within your team?

### Answer

Handling conflicts within teams is a critical leadership skill. Here's my approach:

**Prevention (Best Approach):**

**1. Clear Expectations:**
- Set clear goals and expectations
- Define roles and responsibilities
- Establish team norms and values
- Create safe space for discussion

**2. Open Communication:**
- Encourage open dialogue
- Regular 1-on-1s to surface issues early
- Team retrospectives
- Anonymous feedback channels

**3. Shared Goals:**
- Align team around common goals
- Focus on "we" not "I"
- Celebrate team wins
- Build team identity

**When Conflicts Arise:**

**1. Address Early:**
- Don't let conflicts fester
- Address issues as soon as they surface
- Create safe space for discussion
- Listen to all perspectives

**2. Understand Root Cause:**
```
┌─────────────────────────────────────────────────────────┐
│         Conflict Analysis                               │
└─────────────────────────────────────────────────────────┘

Types of Conflicts:
├─ Technical disagreements
├─ Personality conflicts
├─ Resource conflicts
├─ Process conflicts
└─ Communication issues

Root Causes:
├─ Miscommunication
├─ Unclear expectations
├─ Competing priorities
├─ Personality differences
└─ Stress and pressure
```

**3. Listen Actively:**
- Give each person opportunity to speak
- Listen without interrupting
- Understand perspectives
- Acknowledge feelings
- Don't take sides initially

**4. Facilitate Discussion:**
- Create safe environment
- Focus on facts, not emotions
- Find common ground
- Explore solutions together
- Focus on "what" and "why", not "who"

**5. Resolution Strategies:**

**For Technical Disagreements:**
- **Data-Driven**: Use data and metrics to decide
- **Prototype**: Build POC to test approaches
- **Expert Opinion**: Consult experts
- **Trade-off Analysis**: Evaluate pros and cons
- **Decision Framework**: Use established criteria

**Example:**
Two engineers disagreed on caching strategy (Redis vs local cache). I:
1. Listened to both perspectives
2. Analyzed requirements (scale, consistency, cost)
3. Created decision matrix
4. Prototyped both approaches
5. Made data-driven decision
6. Documented rationale

**For Personality Conflicts:**
- **Mediation**: Facilitate discussion between parties
- **Focus on Behavior**: Address behavior, not personality
- **Find Common Ground**: Identify shared goals
- **Set Boundaries**: Establish working agreements
- **Follow-up**: Monitor and support

**Example:**
Two engineers had communication style conflicts. I:
1. Met with each individually to understand
2. Facilitated joint discussion
3. Identified common goals
4. Established communication norms
5. Checked in regularly

**For Resource Conflicts:**
- **Prioritization**: Use clear prioritization framework
- **Transparency**: Make priorities visible
- **Negotiation**: Find win-win solutions
- **Escalation**: Escalate if needed

**For Process Conflicts:**
- **Review Process**: Evaluate if process needs change
- **Team Input**: Get team input on improvements
- **Experiment**: Try different approaches
- **Iterate**: Improve based on feedback

**6. Decision Making:**
- **Consensus**: When possible, build consensus
- **Data-Driven**: Use data to make decisions
- **Authority**: Make decision if consensus not possible
- **Document**: Document decision and rationale
- **Communicate**: Communicate decision clearly

**7. Follow-up:**
- Monitor situation
- Check in with involved parties
- Ensure resolution is working
- Adjust if needed
- Learn from experience

**Real Example: Architecture Disagreement**

**Situation:**
At IG India, two senior engineers disagreed on whether to use event sourcing for the Prime Broker system. One favored event sourcing for audit trail, the other preferred traditional database approach for simplicity.

**My Approach:**

**1. Understanding:**
- Met with each engineer individually
- Understood their perspectives and concerns
- Identified underlying concerns (complexity vs audit requirements)

**2. Analysis:**
- Analyzed requirements (audit trail, scale, complexity)
- Evaluated both approaches
- Consulted with finance team on audit requirements
- Reviewed industry best practices

**3. Facilitation:**
- Brought engineers together
- Facilitated technical discussion
- Focused on requirements, not preferences
- Explored hybrid approaches

**4. Decision:**
- Made data-driven decision: Event sourcing needed for audit requirements
- Addressed complexity concerns: Phased approach, training, support
- Documented decision and rationale

**5. Implementation:**
- Provided training on event sourcing
- Supported implementation
- Addressed concerns as they arose
- Celebrated success

**6. Follow-up:**
- Both engineers became advocates for event sourcing
- Conflict resolved, relationship improved
- System successfully implemented

**Key Principles:**

**1. Address Early:**
- Don't let conflicts escalate
- Surface issues in 1-on-1s
- Create safe space

**2. Focus on Problem, Not People:**
- Separate people from problem
- Focus on "what" and "why"
- Avoid personal attacks

**3. Listen First:**
- Understand all perspectives
- Acknowledge feelings
- Don't jump to solutions

**4. Data-Driven:**
- Use facts and data
- Evaluate objectively
- Make informed decisions

**5. Win-Win:**
- Look for solutions that work for all
- Compromise when needed
- Focus on shared goals

**6. Learn and Improve:**
- Learn from conflicts
- Improve processes
- Prevent future conflicts

**Prevention Strategies:**

**1. Team Building:**
- Regular team building activities
- Build relationships
- Create team identity
- Shared experiences

**2. Clear Processes:**
- Clear decision-making process
- Defined roles and responsibilities
- Established norms
- Regular retrospectives

**3. Communication:**
- Open communication channels
- Regular feedback
- Transparent decision-making
- Clear expectations

**4. Culture:**
- Respectful culture
- Psychological safety
- Focus on learning
- Celebrate diversity

**Results:**
- Conflicts resolved quickly and effectively
- Team relationships improved
- Better decision-making
- Stronger team cohesion
- Learning culture

Handling conflicts effectively is essential for building high-performing teams.

---

## Question 24: Describe a situation where you had to make a difficult decision as a leader.

### Answer

One of the most difficult decisions I had to make as a leader was deciding to refactor a critical production system (Prime Broker system at IG India) while it was handling 1M+ trades per day, knowing that any mistake could result in financial losses or compliance issues.

**The Situation:**
- **System**: Prime Broker system processing 1M+ trades/day
- **Problem**: System was working but had technical debt that was:
  - Slowing down feature development
  - Making it hard to maintain
  - Creating risk for future scaling
  - Causing occasional bugs
- **Stakes**: High - any downtime or errors could result in:
  - Financial losses
  - Compliance issues
  - Customer impact
  - Regulatory problems
- **Timeline**: Business wanted new features, but refactoring would delay them
- **Team**: Team was divided - some wanted to refactor, others wanted to add features

**The Decision:**
I decided to proceed with a phased refactoring approach:
- Refactor incrementally in phases
- Maintain system functionality throughout
- Add new features in parallel where possible
- Extensive testing at each phase
- Rollback plan for each phase

**Why It Was Difficult:**

**1. High Stakes:**
- Financial system with real money at risk
- Compliance requirements
- Customer impact
- Regulatory scrutiny

**2. Competing Priorities:**
- Business wanted new features (revenue impact)
- Technical team wanted refactoring (long-term health)
- Both were valid concerns

**3. Team Division:**
- Some engineers wanted to refactor
- Others wanted to focus on features
- Needed to align team

**4. Uncertainty:**
- Risk of introducing bugs
- Risk of performance degradation
- Risk of delays
- Unknown timeline

**5. Pressure:**
- From business (features)
- From team (refactoring)
- From management (balance both)
- From myself (do the right thing)

**My Decision-Making Process:**

**1. Analysis:**
- Assessed technical debt impact
- Evaluated business impact of delays
- Analyzed risks of refactoring vs not refactoring
- Reviewed industry best practices

**2. Stakeholder Consultation:**
- Discussed with business stakeholders
- Consulted with finance team
- Talked with compliance team
- Engaged with engineering team

**3. Risk Assessment:**
```
┌─────────────────────────────────────────────────────────┐
│         Risk Analysis                                   │
└─────────────────────────────────────────────────────────┘

Refactor Now:
├─ Risk: Introduce bugs, delays
├─ Benefit: Long-term health, faster future development
└─ Impact: Short-term pain, long-term gain

Don't Refactor:
├─ Risk: Technical debt grows, harder to maintain
├─ Benefit: Faster feature delivery now
└─ Impact: Short-term gain, long-term pain
```

**4. Solution Design:**
- Phased approach (minimize risk)
- Extensive testing (ensure quality)
- Parallel work (features + refactoring)
- Clear rollback plans (safety net)

**5. Communication:**
- Explained decision to all stakeholders
- Communicated risks and mitigation
- Set expectations
- Got buy-in

**6. Execution:**
- Started with lowest-risk phase
- Extensive testing
- Careful monitoring
- Quick rollback if issues

**The Decision:**
Proceed with phased refactoring because:
1. **Long-term Health**: Technical debt was becoming critical
2. **Risk Mitigation**: Phased approach minimized risk
3. **Business Alignment**: Explained long-term benefits to business
4. **Team Alignment**: Got team buy-in through involvement
5. **Safety**: Extensive testing and rollback plans

**Implementation:**

**Phase 1: Foundation (Low Risk)**
- Improved test coverage
- Added monitoring
- Documented architecture
- Result: ✅ No issues

**Phase 2: Service Extraction (Medium Risk)**
- Extracted one service
- Extensive testing
- Gradual traffic migration
- Result: ✅ Successful

**Phase 3: Architecture Improvements (Higher Risk)**
- Introduced event-driven patterns
- Improved data model
- Result: ✅ Successful with minor issues (quickly resolved)

**Phase 4: Optimization (Lower Risk)**
- Performance optimization
- Code cleanup
- Result: ✅ Successful

**Results:**

**Positive:**
- ✅ System more maintainable
- ✅ Faster feature development (40% improvement)
- ✅ Reduced bugs (30% reduction)
- ✅ Better performance
- ✅ Team more confident
- ✅ Zero production incidents during refactoring

**Challenges:**
- ⚠️ Some feature delays (acceptable trade-off)
- ⚠️ Team stress during refactoring (managed through support)
- ⚠️ Additional testing overhead (worth it)

**What I Learned:**

**1. Data-Driven Decisions:**
- Use data to support decisions
- Quantify risks and benefits
- Make informed choices

**2. Stakeholder Management:**
- Communicate clearly
- Get buy-in
- Manage expectations
- Show progress

**3. Risk Management:**
- Identify and mitigate risks
- Have rollback plans
- Test extensively
- Monitor closely

**4. Team Support:**
- Support team through challenges
- Provide resources
- Remove blockers
- Celebrate wins

**5. Balance:**
- Balance short-term and long-term
- Balance technical and business needs
- Balance risk and reward
- Balance speed and quality

**Key Leadership Lessons:**

**1. Courage:**
- Sometimes need to make tough decisions
- Can't please everyone
- Focus on what's right long-term

**2. Communication:**
- Explain rationale clearly
- Get stakeholder buy-in
- Manage expectations
- Show progress

**3. Execution:**
- Plan carefully
- Execute methodically
- Monitor closely
- Adjust as needed

**4. Learning:**
- Learn from decisions
- Improve process
- Share learnings
- Apply to future decisions

This decision taught me that leadership sometimes requires making difficult choices that balance multiple competing concerns, and that careful planning, communication, and execution can mitigate risks even in high-stakes situations.

---

## Question 25: How do you motivate your team members?

### Answer

Motivating team members is crucial for building high-performing teams. Here's my approach:

**1. Understand Individual Motivators**

**Different People, Different Motivators:**
- **Technical Growth**: Learning new technologies, solving complex problems
- **Impact**: Seeing their work make a difference
- **Recognition**: Acknowledgment and appreciation
- **Autonomy**: Freedom to make decisions
- **Purpose**: Understanding "why" their work matters
- **Career Growth**: Opportunities for advancement

**My Approach:**
- **1-on-1s**: Regular conversations to understand what motivates each person
- **Observe**: Watch what energizes them
- **Ask**: Directly ask what motivates them
- **Adapt**: Tailor approach to individual

**Example:**
- Engineer A: Motivated by technical challenges → Assign complex problems
- Engineer B: Motivated by impact → Show business impact of their work
- Engineer C: Motivated by learning → Provide learning opportunities
- Engineer D: Motivated by recognition → Publicly acknowledge contributions

**2. Provide Meaningful Work**

**Connect Work to Purpose:**
- Explain "why" work matters
- Show business impact
- Connect to customer value
- Share success stories

**Example:**
When working on Agent Match Service:
- Explained: "This service routes conversations to agents, directly impacting customer satisfaction"
- Showed metrics: "35% improvement in agent utilization means faster response times"
- Shared feedback: "Customers are happier with faster responses"

**3. Enable Growth & Learning**

**Learning Opportunities:**
- Challenging projects
- New technologies
- Training and courses
- Conference attendance
- Internal tech talks
- Pair programming
- Code reviews

**Example:**
- Assigned engineer to learn Kafka for Prime Broker system
- Provided training and resources
- Paired programming for support
- Engineer became Kafka expert
- Engineer then mentored others

**4. Provide Autonomy & Ownership**

**Empowerment:**
- Give ownership of features/services
- Allow decision-making
- Trust team to execute
- Support when needed

**Example:**
- Assigned engineer to own NLU Facade Service
- Gave autonomy to make technical decisions
- Provided guidance when needed
- Engineer delivered excellent results
- Engineer grew in confidence

**5. Recognition & Appreciation**

**Ways to Recognize:**
- Public acknowledgment (team meetings, all-hands)
- Private appreciation (1-on-1s, emails)
- Celebrate wins (team celebrations)
- Share success stories
- Highlight contributions

**Example:**
- Engineer reduced deployment time significantly
- Acknowledged in team meeting
- Shared in all-hands
- Wrote recommendation
- Engineer felt valued and motivated

**6. Clear Goals & Progress**

**Goal Setting:**
- Clear, achievable goals
- Aligned with career aspirations
- Regular progress reviews
- Celebrate milestones

**Example:**
- Set goal: "Become expert in event-driven architecture"
- Provided learning path
- Regular check-ins
- Celebrated progress
- Engineer achieved goal

**7. Support & Remove Blockers**

**Support:**
- Remove blockers quickly
- Provide resources
- Offer guidance
- Be available

**Example:**
- Engineer blocked on database performance issue
- Immediately helped debug
- Provided tools and resources
- Engineer unblocked quickly
- Engineer felt supported

**8. Create Positive Environment**

**Culture:**
- Psychological safety
- Respect and trust
- Collaboration
- Learning from failures
- Fun and enjoyment

**Example:**
- Created culture where mistakes are learning opportunities
- Team feels safe to experiment
- Team enjoys working together
- High team satisfaction

**9. Career Development**

**Growth Path:**
- Understand career goals
- Create development plans
- Provide opportunities
- Support advancement

**Example:**
- Engineer wanted to become tech lead
- Created development plan
- Provided opportunities to lead
- Mentored on leadership
- Engineer promoted to tech lead

**10. Show Impact**

**Connect to Results:**
- Share metrics and results
- Show customer impact
- Celebrate achievements
- Connect individual work to team success

**Example:**
- Team scaled platform to 12M+ conversations/month
- Shared metrics with team
- Highlighted individual contributions
- Celebrated achievement
- Team felt proud and motivated

**Real Example: Motivating Team During Scaling Project**

**Situation:**
Team was stressed during platform scaling project. High pressure, tight timeline, complex technical challenges.

**My Approach:**

**1. Set Clear Vision:**
- Explained why scaling matters (business growth, customer impact)
- Set clear, achievable goals
- Created roadmap with milestones

**2. Break Down Work:**
- Broke complex work into manageable chunks
- Assigned based on strengths and interests
- Provided clear expectations

**3. Support & Guidance:**
- Available for questions and help
- Pair programming for complex parts
- Code reviews with constructive feedback
- Removed blockers quickly

**4. Celebrate Wins:**
- Celebrated each milestone
- Acknowledged individual contributions
- Shared progress with stakeholders
- Team dinners after major milestones

**5. Learning Opportunities:**
- Team learned Kafka, Redis, scaling patterns
- Provided training and resources
- Knowledge sharing sessions
- Team grew skills significantly

**6. Show Impact:**
- Shared metrics (latency reduction, cost savings)
- Showed customer feedback
- Connected work to business outcomes
- Team saw their impact

**Results:**
- ✅ Team stayed motivated throughout
- ✅ Delivered on time with quality
- ✅ Team grew significantly
- ✅ High team satisfaction
- ✅ Team wanted to take on more challenges

**Key Principles:**

**1. Individual Approach:**
- Different people, different motivators
- Understand what motivates each person
- Tailor approach accordingly

**2. Multiple Strategies:**
- Use combination of approaches
- Not just one thing
- Adapt based on situation

**3. Consistency:**
- Regular recognition
- Consistent support
- Ongoing development
- Continuous improvement

**4. Authenticity:**
- Genuine care for team
- Real appreciation
- Sincere support
- Honest feedback

**5. Balance:**
- Balance individual and team
- Balance challenge and support
- Balance recognition and development
- Balance short-term and long-term

**What Doesn't Work:**
- ❌ Generic approaches
- ❌ Only monetary incentives
- ❌ Micromanagement
- ❌ Lack of recognition
- ❌ Ignoring individual needs

**What Works:**
- ✅ Understanding individual motivators
- ✅ Meaningful work
- ✅ Growth opportunities
- ✅ Recognition and appreciation
- ✅ Support and empowerment

Motivating teams requires understanding individuals, providing meaningful work, enabling growth, and creating a positive environment where people can do their best work.

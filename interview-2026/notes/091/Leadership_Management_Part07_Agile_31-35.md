# Leadership & Management Answers - Part 7: Agile & Scrum (Questions 31-35)

## Question 31: You mention Agile and Scrum. What's your experience with these methodologies?

### Answer

### Agile & Scrum Experience

#### 1. **Agile Journey**

```
┌─────────────────────────────────────────────────────────┐
│         Agile Experience Timeline                      │
└─────────────────────────────────────────────────────────┘

Goldman Sachs (2005-2015):
├─ Introduction to Agile
├─ Scrum implementation
├─ Iterative development
└─ Continuous improvement

IG India (2015-2020):
├─ Full Scrum adoption
├─ Agile transformation
├─ Cross-functional teams
└─ Sprint-based delivery

Allstate (2020-2021):
├─ Agile at scale
├─ Distributed teams
├─ Kanban adoption
└─ Continuous delivery

LivePerson (2021-Present):
├─ Mature Agile practices
├─ DevOps integration
├─ Metrics-driven Agile
└─ Continuous optimization
```

#### 2. **Agile Practices Implemented**

```java
@Service
public class AgileImplementationService {
    public AgilePractices implementAgile(Team team) {
        AgilePractices practices = new AgilePractices();
        
        // Scrum practices
        practices.addPractice("Sprint Planning", implementSprintPlanning(team));
        practices.addPractice("Daily Standups", implementDailyStandups(team));
        practices.addPractice("Sprint Review", implementSprintReview(team));
        practices.addPractice("Retrospectives", implementRetrospectives(team));
        
        // Agile principles
        practices.addPrinciple("Iterative Development", implementIterative(team));
        practices.addPrinciple("Continuous Delivery", implementContinuousDelivery(team));
        practices.addPrinciple("Customer Collaboration", implementCollaboration(team));
        practices.addPrinciple("Responding to Change", implementChangeResponse(team));
        
        return practices;
    }
}
```

#### 3. **Agile Transformation Results**

```
┌─────────────────────────────────────────────────────────┐
│         Agile Transformation Results                   │
└─────────────────────────────────────────────────────────┘

Before Agile:
├─ Waterfall approach
├─ Long release cycles (months)
├─ Late feedback
└─ Low adaptability

After Agile:
├─ Sprint-based delivery (2 weeks)
├─ Early feedback
├─ High adaptability
└─ Continuous improvement

Metrics:
├─ Delivery velocity: Increased 3x
├─ Time to market: Reduced 60%
├─ Quality: Improved (fewer bugs)
└─ Team satisfaction: Increased
```

---

## Question 32: How do you run effective sprint planning meetings?

### Answer

### Sprint Planning Strategy

#### 1. **Sprint Planning Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Sprint Planning Structure                     │
└─────────────────────────────────────────────────────────┘

Preparation (Before Meeting):
├─ Product backlog refinement
├─ Capacity calculation
├─ Dependencies identification
└─ Story preparation

Sprint Planning (2-4 hours):
├─ Part 1: What (1-2 hours)
│  ├─ Review product backlog
│  ├─ Select user stories
│  └─ Define sprint goal
│
└─ Part 2: How (1-2 hours)
   ├─ Break down stories
   ├─ Estimate tasks
   ├─ Assign owners
   └─ Identify risks
```

#### 2. **Sprint Planning Process**

```java
@Service
public class SprintPlanningService {
    public SprintPlan conductSprintPlanning(
            Team team, 
            ProductBacklog backlog) {
        
        SprintPlan plan = new SprintPlan();
        
        // Step 1: Calculate capacity
        double capacity = calculateTeamCapacity(team);
        plan.setCapacity(capacity);
        
        // Step 2: Select stories
        List<UserStory> selectedStories = selectStories(
            backlog, capacity);
        plan.setStories(selectedStories);
        
        // Step 3: Break down stories
        for (UserStory story : selectedStories) {
            List<Task> tasks = breakDownStory(story, team);
            plan.addTasks(story, tasks);
        }
        
        // Step 4: Estimate tasks
        estimateTasks(plan, team);
        
        // Step 5: Validate capacity
        if (!validateCapacity(plan, capacity)) {
            adjustPlan(plan, capacity);
        }
        
        // Step 6: Define sprint goal
        String sprintGoal = defineSprintGoal(selectedStories);
        plan.setSprintGoal(sprintGoal);
        
        // Step 7: Identify risks
        List<Risk> risks = identifyRisks(plan);
        plan.setRisks(risks);
        
        return plan;
    }
    
    private double calculateTeamCapacity(Team team) {
        double totalCapacity = 0.0;
        
        for (TeamMember member : team.getMembers()) {
            // Available hours per sprint (2 weeks)
            double availableHours = 80.0; // 40 hours/week * 2
            
            // Subtract time off
            availableHours -= member.getTimeOffHours();
            
            // Subtract meetings
            availableHours -= 16.0; // ~2 hours/day
            
            // Subtract support/on-call
            availableHours -= 8.0; // Support time
            
            totalCapacity += availableHours;
        }
        
        // Apply buffer (20%)
        return totalCapacity * 0.8;
    }
}
```

#### 3. **Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Sprint Planning Best Practices                 │
└─────────────────────────────────────────────────────────┘

Preparation:
├─ Refine backlog before planning
├─ Prepare stories (ready for development)
├─ Calculate capacity accurately
└─ Identify dependencies early

During Planning:
├─ Time-box the meeting
├─ Focus on sprint goal
├─ Break down stories properly
└─ Estimate realistically

After Planning:
├─ Document sprint plan
├─ Communicate sprint goal
├─ Set up tracking
└─ Schedule daily standups
```

---

## Question 33: What's your approach to daily standups?

### Answer

### Daily Standup Strategy

#### 1. **Standup Format**

```
┌─────────────────────────────────────────────────────────┐
│         Daily Standup Format                           │
└─────────────────────────────────────────────────────────┘

Three Questions:
├─ What did I do yesterday?
├─ What will I do today?
└─ Are there any blockers?

Duration: 15 minutes max
Time: Same time every day
Format: Standing (if in-person) or video
```

#### 2. **Standup Implementation**

```java
@Service
public class DailyStandupService {
    public void conductStandup(Team team) {
        DailyStandup standup = new DailyStandup();
        standup.setTeam(team);
        standup.setTime(LocalTime.of(10, 0)); // 10 AM
        standup.setDuration(Duration.ofMinutes(15));
        
        // Round-robin format
        for (TeamMember member : team.getMembers()) {
            StandupUpdate update = member.provideUpdate();
            
            // What did I do yesterday?
            standup.recordYesterday(member, update.getYesterday());
            
            // What will I do today?
            standup.recordToday(member, update.getToday());
            
            // Blockers?
            if (update.hasBlockers()) {
                standup.recordBlocker(member, update.getBlockers());
                addressBlocker(member, update.getBlockers());
            }
        }
        
        // Update sprint board
        updateSprintBoard(standup);
        
        // Follow up on blockers
        followUpOnBlockers(standup);
    }
}
```

#### 3. **Standup Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Standup Best Practices                        │
└─────────────────────────────────────────────────────────┘

Keep It Focused:
├─ Time-boxed (15 min)
├─ No problem-solving in standup
├─ Move discussions offline
└─ Focus on coordination

Make It Valuable:
├─ Share relevant updates
├─ Identify blockers early
├─ Coordinate work
└─ Track progress

Avoid:
├─ Status reporting to manager
├─ Long discussions
├─ Problem-solving sessions
└─ Going over time
```

---

## Question 34: How do you conduct sprint retrospectives?

### Answer

### Sprint Retrospective Strategy

#### 1. **Retrospective Format**

```
┌─────────────────────────────────────────────────────────┐
│         Retrospective Format (Start-Stop-Continue)    │
└─────────────────────────────────────────────────────────┘

Structure:
├─ What went well? (Start)
├─ What didn't go well? (Stop)
└─ What should we continue? (Continue)

Duration: 1-2 hours
Frequency: End of each sprint
Format: Collaborative discussion
```

#### 2. **Retrospective Process**

```java
@Service
public class RetrospectiveService {
    public RetrospectiveResult conductRetrospective(
            Team team, 
            Sprint sprint) {
        
        RetrospectiveResult result = new RetrospectiveResult();
        
        // Phase 1: Set the stage (10 min)
        setTheStage(team, sprint);
        
        // Phase 2: Gather data (20 min)
        RetrospectiveData data = gatherData(team, sprint);
        result.setData(data);
        
        // Phase 3: Generate insights (30 min)
        List<Insight> insights = generateInsights(data, team);
        result.setInsights(insights);
        
        // Phase 4: Decide what to do (30 min)
        List<ActionItem> actionItems = decideActions(insights, team);
        result.setActionItems(actionItems);
        
        // Phase 5: Close (10 min)
        closeRetrospective(result, team);
        
        return result;
    }
    
    private RetrospectiveData gatherData(Team team, Sprint sprint) {
        RetrospectiveData data = new RetrospectiveData();
        
        // What went well?
        List<String> wentWell = team.identifyWentWell(sprint);
        data.setWentWell(wentWell);
        
        // What didn't go well?
        List<String> didntGoWell = team.identifyDidntGoWell(sprint);
        data.setDidntGoWell(didntGoWell);
        
        // Metrics
        SprintMetrics metrics = getSprintMetrics(sprint);
        data.setMetrics(metrics);
        
        return data;
    }
    
    private List<ActionItem> decideActions(
            List<Insight> insights, 
            Team team) {
        
        List<ActionItem> actionItems = new ArrayList<>();
        
        for (Insight insight : insights) {
            if (insight.requiresAction()) {
                ActionItem item = new ActionItem();
                item.setDescription(insight.getAction());
                item.setOwner(team.assignOwner(insight));
                item.setDueDate(calculateDueDate());
                actionItems.add(item);
            }
        }
        
        return actionItems;
    }
}
```

#### 3. **Retrospective Techniques**

```
┌─────────────────────────────────────────────────────────┐
│         Retrospective Techniques                       │
└─────────────────────────────────────────────────────────┘

Start-Stop-Continue:
├─ Simple and effective
├─ Easy to understand
└─ Action-oriented

4Ls (Liked, Learned, Lacked, Longed):
├─ More structured
├─ Covers different aspects
└─ Comprehensive

Mad-Sad-Glad:
├─ Emotional aspect
├─ Team feelings
└─ Relationship building

Retrospective Prime Directive:
├─ "Regardless of what we discover,
│  we understand and truly believe
│  that everyone did the best job
│  they could, given what they knew
│  at the time, their skills and
│  abilities, the resources available,
│  and the situation at hand."
```

---

## Question 35: How do you handle scope creep during sprints?

### Answer

### Scope Creep Management

#### 1. **Scope Creep Prevention**

```
┌─────────────────────────────────────────────────────────┐
│         Scope Creep Prevention                        │
└─────────────────────────────────────────────────────────┘

Before Sprint:
├─ Clear sprint goal
├─ Well-defined stories
├─ Acceptance criteria
└─ Stakeholder alignment

During Sprint:
├─ Protect sprint scope
├─ Say "no" to new requests
├─ Defer to next sprint
└─ Document requests
```

#### 2. **Handling Scope Creep**

```java
@Service
public class ScopeCreepManagementService {
    public void handleScopeCreep(
            Sprint sprint, 
            NewRequest request) {
        
        // Step 1: Evaluate request
        RequestEvaluation evaluation = evaluateRequest(request);
        
        // Step 2: Assess impact
        SprintImpact impact = assessImpact(sprint, request);
        
        // Step 3: Decision
        if (impact.isCritical()) {
            // Critical: Adjust sprint
            adjustSprint(sprint, request);
        } else {
            // Non-critical: Defer
            deferToNextSprint(request);
        }
    }
    
    private SprintImpact assessImpact(Sprint sprint, NewRequest request) {
        SprintImpact impact = new SprintImpact();
        
        // Estimate effort
        double effort = estimateEffort(request);
        impact.setEffort(effort);
        
        // Check capacity
        double availableCapacity = sprint.getRemainingCapacity();
        impact.setFitsInSprint(effort <= availableCapacity);
        
        // Check dependencies
        List<Dependency> dependencies = identifyDependencies(request);
        impact.setDependencies(dependencies);
        
        // Assess criticality
        boolean isCritical = assessCriticality(request);
        impact.setCritical(isCritical);
        
        return impact;
    }
    
    private void adjustSprint(Sprint sprint, NewRequest request) {
        // Remove lower priority items
        List<SprintItem> itemsToRemove = identifyItemsToRemove(
            sprint, request);
        sprint.removeItems(itemsToRemove);
        
        // Add new request
        sprint.addItem(request);
        
        // Communicate changes
        communicateSprintChanges(sprint, itemsToRemove, request);
    }
}
```

#### 3. **Scope Management Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Scope Management Best Practices                │
└─────────────────────────────────────────────────────────┘

Prevention:
├─ Clear sprint goal
├─ Well-defined acceptance criteria
├─ Stakeholder alignment
└─ Protected sprint time

Handling:
├─ Evaluate all requests
├─ Assess impact
├─ Make informed decisions
└─ Communicate changes

Process:
├─ Document all requests
├─ Track scope changes
├─ Learn from patterns
└─ Improve planning
```

---

## Summary

Part 7 covers:
31. **Agile Experience**: Journey, practices, transformation results
32. **Sprint Planning**: Structure, process, best practices
33. **Daily Standups**: Format, implementation, best practices
34. **Retrospectives**: Format, process, techniques
35. **Scope Creep**: Prevention, handling, best practices

Key principles:
- Structured Agile implementation
- Effective sprint planning with capacity management
- Focused daily standups
- Actionable retrospectives
- Proactive scope creep management

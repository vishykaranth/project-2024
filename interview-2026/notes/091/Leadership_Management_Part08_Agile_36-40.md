# Leadership & Management Answers - Part 8: Agile & Scrum (Questions 36-40)

## Question 36: What's your approach to sprint velocity and capacity planning?

### Answer

### Sprint Velocity & Capacity Planning

#### 1. **Velocity Calculation**

```
┌─────────────────────────────────────────────────────────┐
│         Velocity Calculation                          │
└─────────────────────────────────────────────────────────┘

Velocity Definition:
├─ Story points completed per sprint
├─ Based on historical data
├─ Average of last 3-5 sprints
└─ Used for planning

Calculation:
├─ Track story points per sprint
├─ Calculate average
├─ Consider trends
└─ Account for team changes
```

#### 2. **Velocity Tracking**

```java
@Service
public class VelocityTrackingService {
    public VelocityMetrics calculateVelocity(Team team) {
        VelocityMetrics metrics = new VelocityMetrics();
        
        // Get last 5 sprints
        List<Sprint> recentSprints = getRecentSprints(team, 5);
        
        // Calculate story points per sprint
        List<Double> velocities = recentSprints.stream()
            .map(sprint -> sprint.getCompletedStoryPoints())
            .collect(Collectors.toList());
        
        // Average velocity
        double averageVelocity = velocities.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        metrics.setAverageVelocity(averageVelocity);
        
        // Trend
        VelocityTrend trend = calculateTrend(velocities);
        metrics.setTrend(trend);
        
        // Predict next sprint
        double predictedVelocity = predictNextSprint(velocities, trend);
        metrics.setPredictedVelocity(predictedVelocity);
        
        return metrics;
    }
    
    private VelocityTrend calculateTrend(List<Double> velocities) {
        if (velocities.size() < 2) {
            return VelocityTrend.STABLE;
        }
        
        // Simple trend calculation
        double firstHalf = velocities.subList(0, velocities.size() / 2)
            .stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double secondHalf = velocities.subList(velocities.size() / 2, velocities.size())
            .stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        if (secondHalf > firstHalf * 1.1) {
            return VelocityTrend.INCREASING;
        } else if (secondHalf < firstHalf * 0.9) {
            return VelocityTrend.DECREASING;
        } else {
            return VelocityTrend.STABLE;
        }
    }
}
```

#### 3. **Capacity Planning**

```java
@Service
public class CapacityPlanningService {
    public CapacityPlan createPlan(Team team, Sprint sprint) {
        CapacityPlan plan = new CapacityPlan();
        
        // Calculate team capacity
        double teamCapacity = calculateTeamCapacity(team, sprint);
        plan.setTeamCapacity(teamCapacity);
        
        // Get velocity
        double velocity = getTeamVelocity(team);
        plan.setVelocity(velocity);
        
        // Use lower of capacity or velocity
        double planningCapacity = Math.min(teamCapacity, velocity);
        plan.setPlanningCapacity(planningCapacity);
        
        // Apply buffer (20%)
        double usableCapacity = planningCapacity * 0.8;
        plan.setUsableCapacity(usableCapacity);
        
        return plan;
    }
    
    private double calculateTeamCapacity(Team team, Sprint sprint) {
        double totalCapacity = 0.0;
        
        for (TeamMember member : team.getMembers()) {
            // Base capacity
            double baseCapacity = 80.0; // 40 hours/week * 2 weeks
            
            // Subtract time off
            double timeOff = member.getTimeOffHours(sprint);
            baseCapacity -= timeOff;
            
            // Subtract meetings
            double meetingTime = estimateMeetingTime(member, sprint);
            baseCapacity -= meetingTime;
            
            // Subtract support/on-call
            double supportTime = estimateSupportTime(member, sprint);
            baseCapacity -= supportTime;
            
            totalCapacity += baseCapacity;
        }
        
        return totalCapacity;
    }
}
```

---

## Question 37: How do you balance technical work with feature delivery?

### Answer

### Balancing Technical Work & Features

#### 1. **Work Allocation Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Work Allocation (70-20-10 Rule)                │
└─────────────────────────────────────────────────────────┘

Feature Work (70%):
├─ New features
├─ Enhancements
└─ Bug fixes

Technical Work (20%):
├─ Refactoring
├─ Technical debt
├─ Infrastructure
└─ Performance optimization

Innovation (10%):
├─ Proof of concepts
├─ Technology exploration
├─ Process improvements
└─ Learning time
```

#### 2. **Balancing Framework**

```java
@Service
public class WorkBalancingService {
    public WorkAllocation balanceWork(Team team, Sprint sprint) {
        WorkAllocation allocation = new WorkAllocation();
        
        // Calculate capacity
        double capacity = calculateCapacity(team, sprint);
        
        // Allocate work types
        allocation.setFeatureWork(capacity * 0.7);
        allocation.setTechnicalWork(capacity * 0.2);
        allocation.setInnovation(capacity * 0.1);
        
        // Adjust based on technical debt
        if (hasHighTechnicalDebt(team)) {
            allocation.setTechnicalWork(capacity * 0.3);
            allocation.setFeatureWork(capacity * 0.6);
        }
        
        // Adjust based on critical features
        if (hasCriticalFeatures(sprint)) {
            allocation.setFeatureWork(capacity * 0.8);
            allocation.setTechnicalWork(capacity * 0.15);
            allocation.setInnovation(capacity * 0.05);
        }
        
        return allocation;
    }
}
```

#### 3. **Technical Debt Management**

```
┌─────────────────────────────────────────────────────────┐
│         Technical Debt Management                      │
└─────────────────────────────────────────────────────────┘

Regular Allocation:
├─ 20% of sprint capacity
├─ Continuous improvement
└─ Prevent accumulation

High Debt Periods:
├─ Increase to 30-40%
├─ Focused debt reduction
└─ Sprint dedicated to debt

Tracking:
├─ Track debt items
├─ Prioritize by impact
└─ Measure reduction
```

---

## Question 38: What's your experience with Kanban?

### Answer

### Kanban Experience

#### 1. **Kanban Implementation**

```
┌─────────────────────────────────────────────────────────┐
│         Kanban Board Structure                         │
└─────────────────────────────────────────────────────────┘

Columns:
├─ Backlog
├─ To Do
├─ In Progress
├─ Code Review
├─ Testing
└─ Done

Work in Progress (WIP) Limits:
├─ In Progress: 3 items
├─ Code Review: 2 items
├─ Testing: 2 items
└─ Total WIP: 7 items
```

#### 2. **Kanban vs Scrum**

```java
@Service
public class KanbanScrumComparison {
    public MethodologySelection selectMethodology(
            Team team, 
            ProjectType projectType) {
        
        if (projectType == ProjectType.SUPPORT || 
            projectType == ProjectType.MAINTENANCE) {
            // Kanban for continuous flow
            return MethodologySelection.KANBAN;
        } else if (projectType == ProjectType.FEATURE_DEVELOPMENT) {
            // Scrum for feature development
            return MethodologySelection.SCRUM;
        } else {
            // Hybrid approach
            return MethodologySelection.HYBRID;
        }
    }
}
```

#### 3. **Kanban Benefits**

```
┌─────────────────────────────────────────────────────────┐
│         Kanban Benefits                                │
└─────────────────────────────────────────────────────────┘

Continuous Flow:
├─ No sprint boundaries
├─ Continuous delivery
└─ Faster feedback

Flexibility:
├─ Easy to reprioritize
├─ Adapt to changes
└─ No sprint commitments

Visibility:
├─ Clear work status
├─ Bottleneck identification
└─ Flow metrics

WIP Limits:
├─ Focus on completion
├─ Reduce multitasking
└─ Improve quality
```

---

## Question 39: How do you handle dependencies between teams?

### Answer

### Dependency Management

#### 1. **Dependency Types**

```
┌─────────────────────────────────────────────────────────┐
│         Dependency Types                               │
└─────────────────────────────────────────────────────────┘

Technical Dependencies:
├─ API dependencies
├─ Shared libraries
├─ Infrastructure
└─ Data dependencies

Resource Dependencies:
├─ Shared team members
├─ Specialized skills
├─ External teams
└─ Third-party services

Timeline Dependencies:
├─ Sequential work
├─ Blocking dependencies
├─ Critical path items
└─ Integration points
```

#### 2. **Dependency Management Process**

```java
@Service
public class DependencyManagementService {
    public DependencyPlan manageDependencies(
            Team team, 
            Sprint sprint) {
        
        DependencyPlan plan = new DependencyPlan();
        
        // Identify dependencies
        List<Dependency> dependencies = identifyDependencies(sprint);
        plan.setDependencies(dependencies);
        
        // Categorize dependencies
        Map<DependencyType, List<Dependency>> categorized = 
            categorizeDependencies(dependencies);
        plan.setCategorized(categorized);
        
        // Resolve dependencies
        for (Dependency dependency : dependencies) {
            if (dependency.isBlocking()) {
                resolveBlockingDependency(dependency, plan);
            } else {
                planDependencyResolution(dependency, plan);
            }
        }
        
        // Track dependencies
        trackDependencies(plan);
        
        return plan;
    }
    
    private void resolveBlockingDependency(
            Dependency dependency, 
            DependencyPlan plan) {
        
        // Option 1: Remove dependency
        if (canRemoveDependency(dependency)) {
            removeDependency(dependency);
            return;
        }
        
        // Option 2: Workaround
        if (hasWorkaround(dependency)) {
            implementWorkaround(dependency);
            return;
        }
        
        // Option 3: Escalate
        escalateDependency(dependency);
    }
}
```

#### 3. **Dependency Prevention**

```
┌─────────────────────────────────────────────────────────┐
│         Dependency Prevention Strategies               │
└─────────────────────────────────────────────────────────┘

Architecture:
├─ Decouple services
├─ API-first design
├─ Event-driven architecture
└─ Independent deployments

Planning:
├─ Identify dependencies early
├─ Plan around dependencies
├─ Buffer time for dependencies
└─ Alternative approaches

Communication:
├─ Regular sync meetings
├─ Shared backlogs
├─ Cross-team collaboration
└─ Dependency tracking
```

---

## Question 40: How do you ensure sprint commitments are met?

### Answer

### Ensuring Sprint Commitments

#### 1. **Commitment Management**

```
┌─────────────────────────────────────────────────────────┐
│         Commitment Management                          │
└─────────────────────────────────────────────────────────┘

Realistic Planning:
├─ Accurate capacity calculation
├─ Proper story estimation
├─ Account for unknowns
└─ Buffer for unexpected

Daily Tracking:
├─ Daily standups
├─ Sprint board updates
├─ Progress tracking
└─ Early warning signs

Proactive Management:
├─ Identify blockers early
├─ Resolve issues quickly
├─ Adjust if needed
└─ Communicate changes
```

#### 2. **Commitment Tracking**

```java
@Service
public class SprintCommitmentService {
    public void trackCommitments(Sprint sprint) {
        // Daily tracking
        @Scheduled(cron = "0 0 10 * * MON-FRI") // Daily at 10 AM
        public void trackDailyProgress() {
            SprintProgress progress = calculateProgress(sprint);
            
            // Check if on track
            if (!progress.isOnTrack()) {
                // Identify issues
                List<Issue> issues = identifyIssues(sprint);
                
                // Take corrective action
                takeCorrectiveAction(sprint, issues);
            }
        }
        
        // Mid-sprint review
        @Scheduled(cron = "0 0 10 * * MON") // Monday
        public void conductMidSprintReview(Sprint sprint) {
            if (isMidSprint(sprint)) {
                SprintProgress progress = calculateProgress(sprint);
                
                // Assess completion likelihood
                CompletionLikelihood likelihood = 
                    assessCompletionLikelihood(sprint, progress);
                
                if (likelihood == CompletionLikelihood.LOW) {
                    // Adjust sprint
                    adjustSprint(sprint);
                }
            }
        }
    }
    
    private void takeCorrectiveAction(Sprint sprint, List<Issue> issues) {
        for (Issue issue : issues) {
            if (issue.isBlocker()) {
                // Resolve blocker immediately
                resolveBlocker(issue);
            } else if (issue.affectsCommitment()) {
                // Adjust sprint scope
                adjustSprintScope(sprint, issue);
            }
        }
    }
}
```

#### 3. **Commitment Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Commitment Best Practices                     │
└─────────────────────────────────────────────────────────┘

Planning:
├─ Realistic estimates
├─ Account for capacity
├─ Identify risks early
└─ Set achievable goals

Execution:
├─ Daily progress tracking
├─ Early issue identification
├─ Proactive problem-solving
└─ Regular communication

Adjustment:
├─ Adjust early if needed
├─ Communicate changes
├─ Learn from misses
└─ Improve planning
```

---

## Summary

Part 8 covers:
36. **Velocity & Capacity**: Calculation, tracking, planning
37. **Work Balancing**: 70-20-10 rule, technical debt management
38. **Kanban Experience**: Implementation, benefits, vs Scrum
39. **Dependency Management**: Types, process, prevention
40. **Sprint Commitments**: Management, tracking, best practices

Key principles:
- Track velocity for realistic planning
- Balance feature and technical work (70-20-10)
- Use Kanban for continuous flow
- Manage dependencies proactively
- Track commitments daily and adjust early

# Leadership & Management Answers - Part 6: Performance Management (Questions 26-30)

## Question 26: What's your process for handling performance issues?

### Answer

### Performance Issue Handling Process

#### 1. **Issue Identification**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Issue Identification               │
└─────────────────────────────────────────────────────────┘

Early Warning Signs:
├─ Missed deadlines
├─ Quality issues
├─ Negative feedback
├─ Low engagement
└─ Team concerns

Data Collection:
├─ Performance metrics
├─ Code quality metrics
├─ Peer feedback
├─ Project outcomes
└─ Client/stakeholder feedback
```

#### 2. **Handling Process**

```java
@Service
public class PerformanceIssueHandlingService {
    public IssueResolutionPlan handleIssue(
            TeamMember member, 
            PerformanceIssue issue) {
        
        IssueResolutionPlan plan = new IssueResolutionPlan();
        
        // Step 1: Document issue
        documentIssue(issue, member);
        
        // Step 2: Investigate
        InvestigationResult investigation = investigateIssue(issue);
        plan.setInvestigation(investigation);
        
        // Step 3: Understand root cause
        RootCause rootCause = identifyRootCause(investigation);
        plan.setRootCause(rootCause);
        
        // Step 4: Create improvement plan
        ImprovementPlan improvementPlan = createImprovementPlan(
            member, rootCause);
        plan.setImprovementPlan(improvementPlan);
        
        // Step 5: Communicate
        communicateIssue(member, plan);
        
        // Step 6: Implement plan
        implementPlan(member, improvementPlan);
        
        // Step 7: Monitor progress
        monitorProgress(member, improvementPlan);
        
        // Step 8: Evaluate outcome
        evaluateOutcome(member, improvementPlan);
        
        return plan;
    }
}
```

#### 3. **Improvement Plan Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Improvement Plan                               │
└─────────────────────────────────────────────────────────┘

Phase 1: Support (30 days)
├─ Identify specific issues
├─ Provide resources and support
├─ Set clear expectations
├─ Regular check-ins (weekly)
└─ Document progress

Phase 2: Development (60 days)
├─ Skill development activities
├─ Mentoring and coaching
├─ Stretch assignments
├─ Progress tracking
└─ Mid-point review

Phase 3: Evaluation (30 days)
├─ Assess improvement
├─ Compare with expectations
├─ Make decision
└─ Document outcome
```

---

## Question 27: How do you measure team performance?

### Answer

### Team Performance Measurement

#### 1. **Performance Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Team Performance Metrics                       │
└─────────────────────────────────────────────────────────┘

Delivery Metrics:
├─ Velocity (story points/sprint)
├─ On-time delivery rate
├─ Feature completion rate
└─ Project success rate

Quality Metrics:
├─ Code quality score
├─ Test coverage
├─ Bug rate
└─ Production incidents

Efficiency Metrics:
├─ Cycle time
├─ Lead time
├─ Deployment frequency
└─ MTTR

Team Health:
├─ Team satisfaction
├─ Retention rate
├─ Engagement
└─ Collaboration
```

#### 2. **Measurement Framework**

```java
@Service
public class TeamPerformanceMeasurement {
    public TeamPerformanceMetrics measure(Team team) {
        TeamPerformanceMetrics metrics = new TeamPerformanceMetrics();
        
        // Delivery metrics
        DeliveryMetrics delivery = measureDelivery(team);
        metrics.setDelivery(delivery);
        
        // Quality metrics
        QualityMetrics quality = measureQuality(team);
        metrics.setQuality(quality);
        
        // Efficiency metrics
        EfficiencyMetrics efficiency = measureEfficiency(team);
        metrics.setEfficiency(efficiency);
        
        // Team health
        TeamHealth health = measureTeamHealth(team);
        metrics.setHealth(health);
        
        return metrics;
    }
    
    private DeliveryMetrics measureDelivery(Team team) {
        DeliveryMetrics metrics = new DeliveryMetrics();
        
        // Velocity
        double velocity = calculateVelocity(team);
        metrics.setVelocity(velocity);
        
        // On-time delivery
        double onTimeRate = calculateOnTimeDeliveryRate(team);
        metrics.setOnTimeDeliveryRate(onTimeRate);
        
        // Feature completion
        double completionRate = calculateFeatureCompletionRate(team);
        metrics.setCompletionRate(completionRate);
        
        return metrics;
    }
}
```

#### 3. **Performance Dashboard**

```
┌─────────────────────────────────────────────────────────┐
│         Team Performance Dashboard                     │
└─────────────────────────────────────────────────────────┘

Delivery:
├─ Velocity: 45 story points/sprint
├─ On-time: 95%
├─ Features completed: 12/12
└─ Projects: 100% success

Quality:
├─ Code quality: 8.5/10
├─ Test coverage: 85%
├─ Bug rate: 2 bugs/feature
└─ Incidents: 0

Efficiency:
├─ Cycle time: 3 days
├─ Lead time: 5 days
├─ Deployments: 10/week
└─ MTTR: 30 minutes

Team Health:
├─ Satisfaction: 4.5/5
├─ Retention: 100%
├─ Engagement: High
└─ Collaboration: Excellent
```

---

## Question 28: What metrics do you track for your team?

### Answer

### Team Metrics Tracking

#### 1. **Key Metrics**

```java
@Component
public class TeamMetricsTracker {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 3600000) // Hourly
    public void trackTeamMetrics(Team team) {
        // Delivery metrics
        trackDeliveryMetrics(team);
        
        // Quality metrics
        trackQualityMetrics(team);
        
        // Efficiency metrics
        trackEfficiencyMetrics(team);
        
        // Team health metrics
        trackTeamHealthMetrics(team);
    }
    
    private void trackDeliveryMetrics(Team team) {
        // Velocity
        double velocity = calculateVelocity(team);
        Gauge.builder("team.delivery.velocity")
            .tag("team", team.getName())
            .register(meterRegistry)
            .set(velocity);
        
        // On-time delivery
        double onTimeRate = calculateOnTimeDeliveryRate(team);
        Gauge.builder("team.delivery.on_time_rate")
            .tag("team", team.getName())
            .register(meterRegistry)
            .set(onTimeRate);
    }
    
    private void trackQualityMetrics(Team team) {
        // Code quality
        double codeQuality = calculateCodeQuality(team);
        Gauge.builder("team.quality.code_quality")
            .tag("team", team.getName())
            .register(meterRegistry)
            .set(codeQuality);
        
        // Test coverage
        double testCoverage = calculateTestCoverage(team);
        Gauge.builder("team.quality.test_coverage")
            .tag("team", team.getName())
            .register(meterRegistry)
            .set(testCoverage);
        
        // Bug rate
        double bugRate = calculateBugRate(team);
        Gauge.builder("team.quality.bug_rate")
            .tag("team", team.getName())
            .register(meterRegistry)
            .set(bugRate);
    }
}
```

#### 2. **Metrics Categories**

```
┌─────────────────────────────────────────────────────────┐
│         Metrics Categories                             │
└─────────────────────────────────────────────────────────┘

Output Metrics:
├─ Features delivered
├─ Story points completed
├─ Projects completed
└─ Value delivered

Quality Metrics:
├─ Code quality
├─ Test coverage
├─ Bug rate
└─ Production incidents

Efficiency Metrics:
├─ Cycle time
├─ Lead time
├─ Deployment frequency
└─ MTTR

Team Health:
├─ Team satisfaction
├─ Retention rate
├─ Engagement score
└─ Collaboration index
```

---

## Question 29: How do you balance individual performance with team performance?

### Answer

### Balancing Individual & Team Performance

#### 1. **Performance Balance Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Balance                            │
└─────────────────────────────────────────────────────────┘

Individual Performance (40%):
├─ Technical skills
├─ Code quality
├─ Problem-solving
└─ Learning and growth

Team Performance (40%):
├─ Collaboration
├─ Knowledge sharing
├─ Team delivery
└─ Team goals

Business Impact (20%):
├─ Feature delivery
├─ Business value
├─ Customer impact
└─ Strategic alignment
```

#### 2. **Balanced Evaluation**

```java
@Service
public class BalancedPerformanceEvaluation {
    public PerformanceEvaluation evaluate(
            TeamMember member, 
            Team team) {
        
        PerformanceEvaluation evaluation = new PerformanceEvaluation();
        
        // Individual performance (40%)
        IndividualPerformance individual = evaluateIndividual(member);
        evaluation.setIndividual(individual);
        evaluation.addWeight(individual, 0.4);
        
        // Team performance (40%)
        TeamPerformance teamPerf = evaluateTeamContribution(member, team);
        evaluation.setTeam(teamPerf);
        evaluation.addWeight(teamPerf, 0.4);
        
        // Business impact (20%)
        BusinessImpact impact = evaluateBusinessImpact(member);
        evaluation.setImpact(impact);
        evaluation.addWeight(impact, 0.2);
        
        // Overall score
        double overallScore = calculateOverallScore(evaluation);
        evaluation.setOverallScore(overallScore);
        
        return evaluation;
    }
    
    private TeamPerformance evaluateTeamContribution(
            TeamMember member, 
            Team team) {
        
        TeamPerformance performance = new TeamPerformance();
        
        // Collaboration
        double collaboration = assessCollaboration(member, team);
        performance.setCollaboration(collaboration);
        
        // Knowledge sharing
        double knowledgeSharing = assessKnowledgeSharing(member, team);
        performance.setKnowledgeSharing(knowledgeSharing);
        
        // Team goals
        double teamGoals = assessTeamGoalContribution(member, team);
        performance.setTeamGoals(teamGoals);
        
        return performance;
    }
}
```

#### 3. **Team-First Culture**

```
┌─────────────────────────────────────────────────────────┐
│         Team-First Culture                             │
└─────────────────────────────────────────────────────────┘

Principles:
├─ Team success over individual
├─ Shared ownership
├─ Collective accountability
└─ Mutual support

Practices:
├─ Team goals
├─ Shared recognition
├─ Collaborative work
└─ Knowledge sharing

Balance:
├─ Recognize individual contributions
├─ Reward team achievements
├─ Support individual growth
└─ Foster team collaboration
```

---

## Question 30: What's your approach to promotions and career progression?

### Answer

### Promotion & Career Progression

#### 1. **Career Progression Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Career Progression Levels                      │
└─────────────────────────────────────────────────────────┘

Junior Engineer:
├─ 0-3 years experience
├─ Learning and growth
├─ Simple to moderate tasks
└─ Close supervision

Mid-level Engineer:
├─ 3-7 years experience
├─ Independent work
├─ Moderate to complex tasks
└─ Some mentoring

Senior Engineer:
├─ 7+ years experience
├─ Complex tasks
├─ Architecture contributions
└─ Mentoring others

Staff/Principal Engineer:
├─ 10+ years experience
├─ Technical leadership
├─ Architecture decisions
└─ Strategic impact
```

#### 2. **Promotion Criteria**

```java
@Service
public class PromotionService {
    public PromotionDecision evaluatePromotion(
            TeamMember member, 
            TargetLevel targetLevel) {
        
        PromotionDecision decision = new PromotionDecision();
        
        // Technical skills
        TechnicalSkillsAssessment techSkills = 
            assessTechnicalSkills(member, targetLevel);
        decision.addCriteria(techSkills);
        
        // Leadership skills
        LeadershipSkillsAssessment leadership = 
            assessLeadershipSkills(member, targetLevel);
        decision.addCriteria(leadership);
        
        // Impact
        ImpactAssessment impact = assessImpact(member, targetLevel);
        decision.addCriteria(impact);
        
        // Consistency
        ConsistencyAssessment consistency = 
            assessConsistency(member);
        decision.addCriteria(consistency);
        
        // Make decision
        if (meetsAllCriteria(decision)) {
            decision.setOutcome(PromotionOutcome.APPROVE);
        } else {
            decision.setOutcome(PromotionOutcome.NOT_READY);
            decision.setGaps(identifyGaps(decision));
        }
        
        return decision;
    }
}
```

#### 3. **Promotion Process**

```
┌─────────────────────────────────────────────────────────┐
│         Promotion Process                               │
└─────────────────────────────────────────────────────────┘

1. Self-Nomination or Manager Recommendation
   ├─ Member expresses interest
   ├─ Manager identifies potential
   └─ Document achievements

2. Assessment
   ├─ Review performance history
   ├─ Evaluate against level criteria
   ├─ Gather feedback
   └─ Assess readiness

3. Decision
   ├─ Review committee evaluation
   ├─ Compare with peers
   ├─ Consider business needs
   └─ Make decision

4. Communication
   ├─ Discuss decision
   ├─ Provide feedback
   ├─ Create development plan (if not ready)
   └─ Set timeline (if approved)
```

---

## Summary

Part 6 covers:
26. **Performance Issue Handling**: Identification, process, improvement plan
27. **Team Performance Measurement**: Metrics, framework, dashboard
28. **Metrics Tracking**: Key metrics, categories, tracking
29. **Balancing Performance**: Individual vs team, balanced evaluation
30. **Promotions**: Career progression, criteria, process

Key principles:
- Systematic approach to performance issues
- Comprehensive metrics for team performance
- Balance individual and team performance
- Clear promotion criteria and process
- Support career progression

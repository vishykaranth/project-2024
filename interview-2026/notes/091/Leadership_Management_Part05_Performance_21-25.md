# Leadership & Management Answers - Part 5: Performance Management (Questions 21-25)

## Question 21: What's your approach to performance reviews?

### Answer

### Performance Review Approach

#### 1. **Review Process**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Review Process                     │
└─────────────────────────────────────────────────────────┘

Preparation (1 week before):
├─ Collect feedback (360 feedback)
├─ Review goals and achievements
├─ Gather data and metrics
└─ Prepare discussion points

Review Meeting (1 hour):
├─ Self-assessment (15 min)
├─ Manager assessment (20 min)
├─ Discussion (20 min)
└─ Goal setting (5 min)

Follow-up:
├─ Document review
├─ Create development plan
├─ Set action items
└─ Schedule check-ins
```

#### 2. **Review Framework**

```java
@Service
public class PerformanceReviewService {
    public PerformanceReview conductReview(TeamMember member) {
        PerformanceReview review = new PerformanceReview();
        
        // Collect data
        ReviewData data = collectReviewData(member);
        review.setData(data);
        
        // Self-assessment
        SelfAssessment selfAssessment = member.selfAssess();
        review.setSelfAssessment(selfAssessment);
        
        // Manager assessment
        ManagerAssessment managerAssessment = assessMember(member);
        review.setManagerAssessment(managerAssessment);
        
        // Peer feedback
        List<PeerFeedback> peerFeedback = collectPeerFeedback(member);
        review.setPeerFeedback(peerFeedback);
        
        // 360 feedback
        Feedback360 feedback360 = collect360Feedback(member);
        review.setFeedback360(feedback360);
        
        // Discussion
        ReviewDiscussion discussion = conductDiscussion(review);
        review.setDiscussion(discussion);
        
        // Development plan
        DevelopmentPlan developmentPlan = createDevelopmentPlan(review);
        review.setDevelopmentPlan(developmentPlan);
        
        return review;
    }
}
```

#### 3. **Evaluation Criteria**

```
┌─────────────────────────────────────────────────────────┐
│         Performance Evaluation Criteria                │
└─────────────────────────────────────────────────────────┘

Technical Performance (40%):
├─ Code quality
├─ Technical skills
├─ Problem-solving
└─ Architecture contributions

Delivery (30%):
├─ Feature delivery
├─ On-time delivery
├─ Quality of deliverables
└─ Project completion

Collaboration (20%):
├─ Team collaboration
├─ Code reviews
├─ Knowledge sharing
└─ Communication

Growth (10%):
├─ Skill development
├─ Learning initiatives
├─ Mentoring others
└─ Process improvements
```

---

## Question 22: How do you set goals and expectations for your team?

### Answer

### Goal Setting Strategy

#### 1. **Goal Setting Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Goal Setting Framework (OKRs)                 │
└─────────────────────────────────────────────────────────┘

Objectives (What):
├─ Clear and inspiring
├─ Aligned with company goals
├─ Time-bound
└─ Measurable

Key Results (How):
├─ Specific metrics
├─ Achievable
├─ Relevant
└─ Time-bound
```

#### 2. **Goal Setting Process**

```java
@Service
public class GoalSettingService {
    public GoalPlan createGoals(TeamMember member, 
                               CompanyGoals companyGoals) {
        GoalPlan plan = new GoalPlan();
        
        // Align with company goals
        List<Objective> objectives = alignWithCompanyGoals(
            member, companyGoals);
        
        // Set team goals
        List<Objective> teamObjectives = createTeamObjectives(member);
        
        // Set individual goals
        List<Objective> individualObjectives = 
            createIndividualObjectives(member);
        
        // Combine
        plan.setObjectives(combineObjectives(
            objectives, teamObjectives, individualObjectives));
        
        // Set key results
        for (Objective objective : plan.getObjectives()) {
            List<KeyResult> keyResults = createKeyResults(objective);
            objective.setKeyResults(keyResults);
        }
        
        return plan;
    }
    
    private List<KeyResult> createKeyResults(Objective objective) {
        List<KeyResult> keyResults = new ArrayList<>();
        
        // Key Result 1: Specific metric
        KeyResult kr1 = new KeyResult();
        kr1.setDescription("Reduce P95 latency by 20%");
        kr1.setTarget(0.20);
        kr1.setMetric("p95_latency_reduction");
        keyResults.add(kr1);
        
        // Key Result 2: Another metric
        KeyResult kr2 = new KeyResult();
        kr2.setDescription("Achieve 90% test coverage");
        kr2.setTarget(0.90);
        kr2.setMetric("test_coverage");
        keyResults.add(kr2);
        
        return keyResults;
    }
}
```

#### 3. **Expectation Setting**

```
┌─────────────────────────────────────────────────────────┐
│         Setting Clear Expectations                     │
└─────────────────────────────────────────────────────────┘

Role Expectations:
├─ Responsibilities
├─ Deliverables
├─ Quality standards
└─ Timeline expectations

Behavior Expectations:
├─ Communication
├─ Collaboration
├─ Ownership
└─ Professionalism

Performance Expectations:
├─ Code quality
├─ Delivery speed
├─ Problem-solving
└─ Continuous improvement
```

---

## Question 23: How do you handle underperforming team members?

### Answer

### Handling Underperformance

#### 1. **Identification & Assessment**

```
┌─────────────────────────────────────────────────────────┐
│         Underperformance Identification                │
└─────────────────────────────────────────────────────────┘

Signs:
├─ Missed deadlines
├─ Low code quality
├─ Lack of engagement
└─ Negative feedback

Assessment:
├─ Gather data
├─ Understand root cause
├─ Check for external factors
└─ Evaluate consistently
```

#### 2. **Handling Process**

```java
@Service
public class UnderperformanceHandlingService {
    public ImprovementPlan handleUnderperformance(
            TeamMember member) {
        
        ImprovementPlan plan = new ImprovementPlan();
        
        // Step 1: Understand root cause
        RootCauseAnalysis analysis = analyzeRootCause(member);
        plan.setRootCause(analysis);
        
        // Step 2: Create improvement plan
        if (analysis.isSkillGap()) {
            plan = createSkillDevelopmentPlan(member, analysis);
        } else if (analysis.isMotivationIssue()) {
            plan = createMotivationPlan(member, analysis);
        } else if (analysis.isExternalFactor()) {
            plan = createSupportPlan(member, analysis);
        }
        
        // Step 3: Set clear expectations
        setClearExpectations(member, plan);
        
        // Step 4: Provide support
        provideSupport(member, plan);
        
        // Step 5: Monitor progress
        monitorProgress(member, plan);
        
        // Step 6: Evaluate outcome
        evaluateOutcome(member, plan);
        
        return plan;
    }
    
    private RootCauseAnalysis analyzeRootCause(TeamMember member) {
        RootCauseAnalysis analysis = new RootCauseAnalysis();
        
        // Check performance data
        PerformanceData data = getPerformanceData(member);
        
        // Check feedback
        List<Feedback> feedback = getFeedback(member);
        
        // Check external factors
        ExternalFactors factors = checkExternalFactors(member);
        
        // Determine root cause
        if (data.showsSkillGap()) {
            analysis.setRootCause(RootCause.SKILL_GAP);
        } else if (feedback.showsMotivationIssue()) {
            analysis.setRootCause(RootCause.MOTIVATION);
        } else if (factors.hasExternalIssues()) {
            analysis.setRootCause(RootCause.EXTERNAL);
        }
        
        return analysis;
    }
}
```

#### 3. **Improvement Plan**

```
┌─────────────────────────────────────────────────────────┐
│         Improvement Plan Structure                     │
└─────────────────────────────────────────────────────────┘

Phase 1: Support (30 days)
├─ Identify issues
├─ Provide resources
├─ Set clear expectations
└─ Regular check-ins

Phase 2: Development (60 days)
├─ Skill development
├─ Mentoring
├─ Stretch assignments
└─ Progress tracking

Phase 3: Evaluation (30 days)
├─ Assess improvement
├─ Make decision
├─ Continue or escalate
└─ Document outcome
```

---

## Question 24: What's your approach to giving constructive feedback?

### Answer

### Constructive Feedback Approach

#### 1. **Feedback Framework (SBI Model)**

```
┌─────────────────────────────────────────────────────────┐
│         SBI Feedback Model                            │
└─────────────────────────────────────────────────────────┘

Situation:
├─ Specific context
├─ When and where
└─ Clear setting

Behavior:
├─ Observable actions
├─ Specific examples
└─ Not personality

Impact:
├─ Effect on team/project
├─ Consequences
└─ Business impact
```

#### 2. **Feedback Delivery**

```java
@Service
public class FeedbackDeliveryService {
    public void deliverFeedback(TeamMember member, 
                              Feedback feedback) {
        // Prepare feedback
        PreparedFeedback prepared = prepareFeedback(feedback);
        
        // Schedule meeting
        scheduleFeedbackMeeting(member, prepared);
        
        // Deliver feedback
        deliverFeedback(member, prepared);
        
        // Follow up
        followUp(member, prepared);
    }
    
    private PreparedFeedback prepareFeedback(Feedback feedback) {
        PreparedFeedback prepared = new PreparedFeedback();
        
        // Use SBI model
        prepared.setSituation(feedback.getSituation());
        prepared.setBehavior(feedback.getBehavior());
        prepared.setImpact(feedback.getImpact());
        
        // Add suggestions
        prepared.setSuggestions(createSuggestions(feedback));
        
        // Prepare examples
        prepared.setExamples(collectExamples(feedback));
        
        return prepared;
    }
    
    private void deliverFeedback(TeamMember member, 
                                PreparedFeedback prepared) {
        // Create safe environment
        createSafeEnvironment(member);
        
        // Start with positive
        startWithPositive(member);
        
        // Deliver feedback
        explainSituation(prepared.getSituation());
        describeBehavior(prepared.getBehavior());
        explainImpact(prepared.getImpact());
        
        // Provide suggestions
        provideSuggestions(prepared.getSuggestions());
        
        // Listen to response
        listenToResponse(member);
        
        // Agree on action items
        agreeOnActionItems(member);
    }
}
```

#### 3. **Feedback Best Practices**

```
┌─────────────────────────────────────────────────────────┐
│         Feedback Best Practices                       │
└─────────────────────────────────────────────────────────┘

Timing:
├─ Timely (soon after event)
├─ Regular (not just annual)
├─ Appropriate setting
└─ Private (for negative)

Delivery:
├─ Specific and actionable
├─ Focus on behavior, not person
├─ Balance positive and negative
└─ Two-way conversation

Follow-up:
├─ Document action items
├─ Check progress
├─ Provide support
└─ Recognize improvement
```

---

## Question 25: How do you recognize and reward good performance?

### Answer

### Recognition & Rewards Strategy

#### 1. **Recognition Types**

```
┌─────────────────────────────────────────────────────────┐
│         Recognition Types                             │
└─────────────────────────────────────────────────────────┘

Formal Recognition:
├─ Performance bonuses
├─ Promotions
├─ Awards
└─ Public recognition

Informal Recognition:
├─ Thank you messages
├─ Team shoutouts
├─ Peer recognition
└─ Appreciation

Career Recognition:
├─ Stretch assignments
├─ Leadership opportunities
├─ Conference attendance
└─ Training opportunities
```

#### 2. **Recognition Program**

```java
@Service
public class RecognitionService {
    public RecognitionProgram createProgram(Team team) {
        RecognitionProgram program = new RecognitionProgram();
        
        // Immediate recognition
        program.addRecognition("Spot Awards", () -> {
            recognizeImmediateAchievements(team);
            provideSpotBonuses(team);
            publicShoutouts(team);
        });
        
        // Monthly recognition
        program.addRecognition("Monthly Awards", () -> {
            selectMonthlyWinners(team);
            recognizeAchievements(team);
            shareSuccessStories(team);
        });
        
        // Quarterly recognition
        program.addRecognition("Quarterly Awards", () -> {
            selectQuarterlyWinners(team);
            provideRewards(team);
            celebrateAchievements(team);
        });
        
        // Peer recognition
        program.addRecognition("Peer Recognition", () -> {
            enablePeerRecognition(team);
            sharePeerAppreciation(team);
            celebrateTeamSuccess(team);
        });
        
        return program;
    }
}
```

#### 3. **Recognition Examples**

```
┌─────────────────────────────────────────────────────────┐
│         Recognition Examples                           │
└─────────────────────────────────────────────────────────┘

Technical Excellence:
├─ "Outstanding code quality this sprint"
├─ "Excellent architecture design"
└─ "Great problem-solving on production issue"

Delivery Excellence:
├─ "Delivered feature ahead of schedule"
├─ "Zero bugs in production"
└─ "Exceeded performance targets"

Collaboration:
├─ "Helped team member solve complex problem"
├─ "Great knowledge sharing in tech talk"
└─ "Excellent code review feedback"

Innovation:
├─ "Implemented innovative solution"
├─ "Improved process efficiency"
└─ "Introduced new best practice"
```

---

## Summary

Part 5 covers:
21. **Performance Reviews**: Process, framework, evaluation criteria
22. **Goal Setting**: Framework, process, expectations
23. **Underperformance**: Identification, handling process, improvement plan
24. **Constructive Feedback**: SBI model, delivery, best practices
25. **Recognition & Rewards**: Types, program, examples

Key principles:
- Structured performance review process
- Clear goal setting with OKRs
- Supportive approach to underperformance
- Constructive feedback using SBI model
- Regular recognition and rewards

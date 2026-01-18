# Team & Process - Part 2: Disagreements & Standards

## Question 355: How do you handle architecture disagreements?

### Answer

### Handling Architecture Disagreements

#### 1. **Disagreement Resolution Framework**

```
┌─────────────────────────────────────────────────────────┐
│         Disagreement Resolution Framework              │
└─────────────────────────────────────────────────────────┘

1. Understand Positions
   ├─ Listen to all perspectives
   ├─ Understand reasoning
   ├─ Identify concerns
   └─ Find common ground

2. Gather Data
   ├─ Research solutions
   ├─ Evaluate alternatives
   ├─ Assess trade-offs
   └─ Collect evidence

3. Facilitate Discussion
   ├─ Create safe space
   ├─ Encourage dialogue
   ├─ Focus on facts
   └─ Avoid personal attacks

4. Make Decision
   ├─ Use decision criteria
   ├─ Consider all inputs
   ├─ Document rationale
   └─ Get buy-in

5. Follow-up
   ├─ Monitor outcomes
   ├─ Review decision
   └─ Learn from experience
```

#### 2. **Resolution Strategies**

```java
@Service
public class DisagreementResolutionService {
    public ResolutionResult resolveDisagreement(
            ArchitectureDisagreement disagreement) {
        
        ResolutionResult result = new ResolutionResult();
        
        // Strategy 1: Data-driven decision
        if (canResolveWithData(disagreement)) {
            return resolveWithData(disagreement);
        }
        
        // Strategy 2: Consensus building
        if (canBuildConsensus(disagreement)) {
            return buildConsensus(disagreement);
        }
        
        // Strategy 3: Expert opinion
        if (requiresExpertOpinion(disagreement)) {
            return getExpertOpinion(disagreement);
        }
        
        // Strategy 4: Escalation
        if (requiresEscalation(disagreement)) {
            return escalate(disagreement);
        }
        
        // Strategy 5: Compromise
        return findCompromise(disagreement);
    }
    
    private ResolutionResult resolveWithData(
            ArchitectureDisagreement disagreement) {
        
        // Gather data
        DataCollection data = collectData(disagreement);
        
        // Analyze data
        DataAnalysis analysis = analyzeData(data);
        
        // Make data-driven decision
        Decision decision = makeDecision(analysis);
        
        // Document rationale
        documentRationale(decision, analysis);
        
        return new ResolutionResult(decision);
    }
}
```

#### 3. **Consensus Building**

```java
@Service
public class ConsensusBuildingService {
    public ConsensusResult buildConsensus(
            ArchitectureDisagreement disagreement) {
        
        ConsensusResult result = new ConsensusResult();
        
        // Step 1: Identify common goals
        List<Goal> commonGoals = findCommonGoals(
            disagreement.getParticipants());
        result.setCommonGoals(commonGoals);
        
        // Step 2: Identify shared constraints
        List<Constraint> sharedConstraints = findSharedConstraints(
            disagreement.getParticipants());
        result.setSharedConstraints(sharedConstraints);
        
        // Step 3: Explore options together
        List<Option> options = exploreOptionsTogether(
            disagreement.getParticipants());
        result.setOptions(options);
        
        // Step 4: Evaluate options collaboratively
        OptionEvaluation evaluation = evaluateOptionsCollaboratively(
            options, commonGoals, sharedConstraints);
        result.setEvaluation(evaluation);
        
        // Step 5: Select option with consensus
        Option selected = selectWithConsensus(evaluation);
        result.setSelectedOption(selected);
        
        return result;
    }
}
```

#### 4. **Escalation Process**

```java
@Service
public class EscalationService {
    public EscalationResult escalate(
            ArchitectureDisagreement disagreement) {
        
        EscalationResult result = new EscalationResult();
        
        // Determine escalation level
        EscalationLevel level = determineEscalationLevel(disagreement);
        
        // Prepare escalation package
        EscalationPackage package = createEscalationPackage(disagreement);
        package.setLevel(level);
        package.setParticipants(disagreement.getParticipants());
        package.setPositions(disagreement.getPositions());
        package.setData(disagreement.getData());
        package.setAttemptedResolutions(disagreement.getAttemptedResolutions());
        
        // Escalate to appropriate level
        switch (level) {
            case TEAM_LEAD:
                result = escalateToTeamLead(package);
                break;
            case ARCHITECTURE_BOARD:
                result = escalateToArchitectureBoard(package);
                break;
            case CTO:
                result = escalateToCTO(package);
                break;
        }
        
        return result;
    }
}
```

#### 5. **Learning from Disagreements**

```java
@Service
public class DisagreementLearningService {
    public void learnFromDisagreement(
            ArchitectureDisagreement disagreement,
            ResolutionResult resolution) {
        
        // Document disagreement
        DisagreementRecord record = new DisagreementRecord();
        record.setDisagreement(disagreement);
        record.setResolution(resolution);
        record.setOutcomes(monitorOutcomes(resolution));
        
        // Analyze patterns
        analyzePatterns(record);
        
        // Update processes
        updateProcesses(record);
        
        // Share learnings
        shareLearnings(record);
    }
}
```

---

## Question 356: What's the process for architecture RFCs?

### Answer

### Architecture RFC Process

#### 1. **RFC Process Flow**

```
┌─────────────────────────────────────────────────────────┐
│         RFC Process Flow                               │
└─────────────────────────────────────────────────────────┘

1. Draft RFC
   ├─ Write RFC document
   ├─ Research alternatives
   ├─ Create diagrams
   └─ Get initial feedback

2. Submit RFC
   ├─ Submit to RFC repository
   ├─ Tag reviewers
   ├─ Notify stakeholders
   └─ Set review deadline

3. Review Period
   ├─ Reviewers review RFC
   ├─ Provide feedback
   ├─ Discuss in forum
   └─ Request changes

4. Revision
   ├─ Address feedback
   ├─ Update RFC
   └─ Resubmit if needed

5. Decision
   ├─ Approve
   ├─ Request more work
   └─ Reject

6. Implementation
   ├─ Implement approved RFC
   ├─ Track progress
   └─ Update status
```

#### 2. **RFC Template**

```markdown
# RFC-001: Title

## Summary
Brief summary of the proposal.

## Motivation
Why is this change needed? What problem does it solve?

## Detailed Design
Detailed description of the proposed solution.

## Alternatives Considered
What other options were considered and why were they rejected?

## Implementation Plan
How will this be implemented? What are the phases?

## Risks and Mitigation
What are the risks and how will they be mitigated?

## Open Questions
What questions need to be answered?

## References
Links to related documents, discussions, etc.

## Status
- [ ] Draft
- [ ] Under Review
- [ ] Approved
- [ ] Rejected
- [ ] Implemented
```

#### 3. **RFC Review Process**

```java
@Service
public class RFCReviewService {
    public ReviewResult reviewRFC(RFC rfc) {
        ReviewResult result = new ReviewResult();
        
        // Completeness check
        if (!isComplete(rfc)) {
            result.addIssue("RFC is incomplete");
        }
        
        // Technical review
        TechnicalReview technicalReview = reviewTechnical(rfc);
        result.addTechnicalIssues(technicalReview.getIssues());
        
        // Architecture review
        ArchitectureReview archReview = reviewArchitecture(rfc);
        result.addArchitectureIssues(archReview.getIssues());
        
        // Business review
        BusinessReview businessReview = reviewBusiness(rfc);
        result.addBusinessIssues(businessReview.getIssues());
        
        // Risk assessment
        RiskAssessment riskAssessment = assessRisks(rfc);
        result.addRisks(riskAssessment.getRisks());
        
        return result;
    }
}
```

#### 4. **RFC Approval Process**

```java
@Service
public class RFCApprovalService {
    public ApprovalResult approveRFC(RFC rfc) {
        ApprovalResult result = new ApprovalResult();
        
        // Check review status
        if (!rfc.isReviewed()) {
            throw new RFCNotReviewedException();
        }
        
        // Check for blocking issues
        if (rfc.hasBlockingIssues()) {
            result.setStatus(ApprovalStatus.REJECTED);
            result.setReason("Blocking issues found");
            return result;
        }
        
        // Get approvals
        List<Approval> approvals = collectApprovals(rfc);
        
        // Check if sufficient approvals
        if (hasSufficientApprovals(approvals, rfc)) {
            result.setStatus(ApprovalStatus.APPROVED);
            result.setApprovals(approvals);
            
            // Update RFC status
            rfc.setStatus(RFCStatus.APPROVED);
            rfc.setApprovedAt(Instant.now());
            rfcRepository.save(rfc);
        } else {
            result.setStatus(ApprovalStatus.PENDING);
            result.setRequiredApprovals(getRequiredApprovals(rfc));
        }
        
        return result;
    }
}
```

#### 5. **RFC Tracking**

```java
@Service
public class RFCTrackingService {
    public void trackRFC(RFC rfc) {
        // Track status
        trackStatus(rfc);
        
        // Track implementation
        if (rfc.getStatus() == RFCStatus.APPROVED) {
            trackImplementation(rfc);
        }
        
        // Track outcomes
        if (rfc.getStatus() == RFCStatus.IMPLEMENTED) {
            trackOutcomes(rfc);
        }
    }
    
    @Scheduled(fixedRate = 86400000) // Daily
    public void updateRFCStatuses() {
        List<RFC> rfcList = rfcRepository.findByStatus(RFCStatus.APPROVED);
        
        for (RFC rfc : rfcList) {
            // Check implementation status
            ImplementationStatus implStatus = 
                checkImplementationStatus(rfc);
            
            if (implStatus == ImplementationStatus.COMPLETE) {
                rfc.setStatus(RFCStatus.IMPLEMENTED);
                rfc.setImplementedAt(Instant.now());
                rfcRepository.save(rfc);
            }
        }
    }
}
```

---

## Question 357: How do you ensure architecture standards across services?

### Answer

### Architecture Standards Enforcement

#### 1. **Standards Definition**

```java
@Configuration
public class ArchitectureStandards {
    // Technology standards
    public static final List<String> APPROVED_LANGUAGES = 
        Arrays.asList("Java", "Python", "JavaScript", "TypeScript");
    
    public static final List<String> APPROVED_FRAMEWORKS = 
        Arrays.asList("Spring Boot", "Django", "Express", "NestJS");
    
    public static final List<String> APPROVED_DATABASES = 
        Arrays.asList("PostgreSQL", "Redis", "MongoDB");
    
    public static final List<String> APPROVED_MESSAGE_BROKERS = 
        Arrays.asList("Kafka", "RabbitMQ");
    
    // Pattern standards
    public static final List<String> APPROVED_PATTERNS = 
        Arrays.asList("Microservices", "Event-Driven", 
                     "API Gateway", "Circuit Breaker");
    
    // Naming conventions
    public static final String SERVICE_NAME_PATTERN = 
        "^[a-z]+(-[a-z]+)*$"; // kebab-case
    
    public static final String API_VERSION_PATTERN = 
        "^/api/v\\d+/.*$"; // /api/v1/...
    
    // Architecture principles
    public static final List<String> ARCHITECTURE_PRINCIPLES = 
        Arrays.asList(
            "Stateless services",
            "API-first design",
            "Event-driven communication",
            "Horizontal scalability",
            "Fault tolerance"
        );
}
```

#### 2. **Standards Enforcement**

```java
@Service
public class StandardsEnforcementService {
    public ComplianceResult checkCompliance(Service service) {
        ComplianceResult result = new ComplianceResult();
        
        // Check technology standards
        TechnologyCompliance techCompliance = 
            checkTechnologyCompliance(service);
        result.addIssues(techCompliance.getIssues());
        
        // Check pattern compliance
        PatternCompliance patternCompliance = 
            checkPatternCompliance(service);
        result.addIssues(patternCompliance.getIssues());
        
        // Check naming conventions
        NamingCompliance namingCompliance = 
            checkNamingCompliance(service);
        result.addIssues(namingCompliance.getIssues());
        
        // Check architecture principles
        PrinciplesCompliance principlesCompliance = 
            checkPrinciplesCompliance(service);
        result.addIssues(principlesCompliance.getIssues());
        
        return result;
    }
    
    private TechnologyCompliance checkTechnologyCompliance(Service service) {
        TechnologyCompliance compliance = new TechnologyCompliance();
        
        // Check language
        if (!ArchitectureStandards.APPROVED_LANGUAGES
            .contains(service.getLanguage())) {
            compliance.addIssue("Language not approved: " + 
                              service.getLanguage());
        }
        
        // Check framework
        if (!ArchitectureStandards.APPROVED_FRAMEWORKS
            .contains(service.getFramework())) {
            compliance.addIssue("Framework not approved: " + 
                              service.getFramework());
        }
        
        // Check dependencies
        for (Dependency dep : service.getDependencies()) {
            if (!isApprovedDependency(dep)) {
                compliance.addIssue("Dependency not approved: " + dep);
            }
        }
        
        return compliance;
    }
}
```

#### 3. **Automated Compliance Checking**

```java
@Component
public class AutomatedComplianceChecker {
    @Scheduled(fixedRate = 3600000) // Hourly
    public void checkAllServices() {
        List<Service> services = getAllServices();
        
        for (Service service : services) {
            ComplianceResult result = 
                standardsEnforcementService.checkCompliance(service);
            
            if (!result.isCompliant()) {
                // Alert
                alertService.nonCompliantService(service, result);
                
                // Record metric
                Counter.builder("standards.compliance.violations")
                    .tag("service", service.getName())
                    .register(meterRegistry)
                    .increment();
            }
        }
    }
    
    public ComplianceResult checkPullRequest(PullRequest pr) {
        ComplianceResult result = new ComplianceResult();
        
        // Check for new dependencies
        List<Dependency> newDeps = findNewDependencies(pr);
        for (Dependency dep : newDeps) {
            if (!isApprovedDependency(dep)) {
                result.addIssue("New dependency requires approval: " + dep);
            }
        }
        
        // Check naming conventions
        if (!followsNamingConventions(pr)) {
            result.addIssue("Naming convention violations");
        }
        
        // Check patterns
        List<Pattern> patterns = detectPatterns(pr);
        for (Pattern pattern : patterns) {
            if (!isApprovedPattern(pattern)) {
                result.addIssue("Unapproved pattern: " + pattern);
            }
        }
        
        return result;
    }
}
```

#### 4. **Standards Documentation**

```markdown
# Architecture Standards

## Technology Standards

### Approved Languages
- Java (11+)
- Python (3.8+)
- JavaScript/TypeScript (ES2020+)

### Approved Frameworks
- Spring Boot (Java)
- Django (Python)
- Express/NestJS (Node.js)

### Approved Databases
- PostgreSQL (primary)
- Redis (caching)
- MongoDB (document store)

## Pattern Standards

### Required Patterns
- Microservices architecture
- API Gateway pattern
- Event-driven communication

### Recommended Patterns
- Circuit Breaker
- Saga pattern
- CQRS (where applicable)

## Naming Conventions

### Service Names
- Format: kebab-case
- Example: `agent-match-service`

### API Endpoints
- Format: `/api/v{version}/{resource}`
- Example: `/api/v1/conversations`

## Architecture Principles

1. Stateless services
2. API-first design
3. Event-driven communication
4. Horizontal scalability
5. Fault tolerance
```

#### 5. **Standards Training**

```java
@Service
public class StandardsTrainingService {
    public TrainingProgram createStandardsTraining() {
        TrainingProgram program = new TrainingProgram();
        
        // Module 1: Technology Standards
        TrainingModule techStandards = new TrainingModule(
            "Technology Standards");
        techStandards.addSession("Approved Technologies", 1);
        techStandards.addSession("Dependency Management", 1);
        techStandards.addSession("Version Control", 1);
        program.addModule(techStandards);
        
        // Module 2: Pattern Standards
        TrainingModule patternStandards = new TrainingModule(
            "Pattern Standards");
        patternStandards.addSession("Required Patterns", 1);
        patternStandards.addSession("Pattern Implementation", 1);
        patternStandards.addSession("Pattern Library", 1);
        program.addModule(patternStandards);
        
        // Module 3: Naming & Conventions
        TrainingModule namingStandards = new TrainingModule(
            "Naming & Conventions");
        namingStandards.addSession("Naming Conventions", 1);
        namingStandards.addSession("API Design", 1);
        namingStandards.addSession("Code Style", 1);
        program.addModule(namingStandards);
        
        return program;
    }
}
```

---

## Question 358: What's the architecture governance process?

### Answer

### Architecture Governance Process

#### 1. **Governance Structure**

```
┌─────────────────────────────────────────────────────────┐
│         Architecture Governance Structure               │
└─────────────────────────────────────────────────────────┘

Architecture Board:
├─ Chief Architect
├─ Principal Architects
├─ Tech Leads
└─ Business Stakeholders

Responsibilities:
├─ Review architecture decisions
├─ Approve technology choices
├─ Enforce standards
└─ Resolve conflicts

Architecture Team:
├─ Architects
├─ Senior Engineers
└─ Subject Matter Experts

Responsibilities:
├─ Design architecture
├─ Review proposals
├─ Provide guidance
└─ Maintain documentation
```

#### 2. **Governance Process**

```java
@Service
public class ArchitectureGovernanceService {
    public GovernanceDecision makeGovernanceDecision(
            GovernanceRequest request) {
        
        GovernanceDecision decision = new GovernanceDecision();
        
        // Determine decision level
        DecisionLevel level = determineDecisionLevel(request);
        
        switch (level) {
            case ARCHITECT:
                decision = architectDecision(request);
                break;
            case ARCHITECTURE_TEAM:
                decision = teamDecision(request);
                break;
            case ARCHITECTURE_BOARD:
                decision = boardDecision(request);
                break;
        }
        
        // Document decision
        documentDecision(decision);
        
        // Communicate decision
        communicateDecision(decision);
        
        return decision;
    }
    
    private DecisionLevel determineDecisionLevel(
            GovernanceRequest request) {
        
        if (request.getImpact() == Impact.LOW &&
            request.getScope() == Scope.SINGLE_SERVICE) {
            return DecisionLevel.ARCHITECT;
        }
        
        if (request.getImpact() == Impact.MEDIUM ||
            request.getScope() == Scope.MULTIPLE_SERVICES) {
            return DecisionLevel.ARCHITECTURE_TEAM;
        }
        
        if (request.getImpact() == Impact.HIGH ||
            request.getScope() == Scope.ENTERPRISE) {
            return DecisionLevel.ARCHITECTURE_BOARD;
        }
        
        return DecisionLevel.ARCHITECTURE_TEAM;
    }
}
```

#### 3. **Review & Approval Process**

```java
@Service
public class GovernanceReviewService {
    public ReviewResult reviewProposal(ArchitectureProposal proposal) {
        ReviewResult result = new ReviewResult();
        
        // Initial screening
        if (!passesInitialScreening(proposal)) {
            result.setStatus(ReviewStatus.REJECTED);
            result.setReason("Fails initial screening criteria");
            return result;
        }
        
        // Assign reviewers
        List<Reviewer> reviewers = assignReviewers(proposal);
        
        // Conduct review
        List<Review> reviews = conductReviews(proposal, reviewers);
        result.setReviews(reviews);
        
        // Make decision
        ReviewDecision decision = makeDecision(reviews);
        result.setDecision(decision);
        
        return result;
    }
    
    private ReviewDecision makeDecision(List<Review> reviews) {
        // Count approvals
        long approvals = reviews.stream()
            .filter(Review::isApproved)
            .count();
        
        // Count rejections
        long rejections = reviews.stream()
            .filter(Review::isRejected)
            .count();
        
        // Decision logic
        if (approvals > rejections && approvals >= getRequiredApprovals()) {
            return ReviewDecision.APPROVED;
        } else if (rejections > approvals) {
            return ReviewDecision.REJECTED;
        } else {
            return ReviewDecision.REQUIRES_REVISION;
        }
    }
}
```

#### 4. **Compliance Monitoring**

```java
@Component
public class ComplianceMonitoringService {
    @Scheduled(fixedRate = 86400000) // Daily
    public void monitorCompliance() {
        List<Service> services = getAllServices();
        
        ComplianceReport report = new ComplianceReport();
        
        for (Service service : services) {
            ComplianceResult result = 
                checkServiceCompliance(service);
            
            if (!result.isCompliant()) {
                report.addNonCompliantService(service, result);
            }
        }
        
        // Generate report
        generateComplianceReport(report);
        
        // Alert on issues
        if (report.hasCriticalIssues()) {
            alertArchitectureBoard(report);
        }
    }
    
    private ComplianceResult checkServiceCompliance(Service service) {
        ComplianceResult result = new ComplianceResult();
        
        // Check standards
        result.merge(checkStandardsCompliance(service));
        
        // Check patterns
        result.merge(checkPatternCompliance(service));
        
        // Check principles
        result.merge(checkPrinciplesCompliance(service));
        
        return result;
    }
}
```

#### 5. **Governance Metrics**

```java
@Component
public class GovernanceMetricsService {
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 3600000) // Hourly
    public void calculateGovernanceMetrics() {
        // Decision metrics
        long totalDecisions = getTotalDecisions();
        long approvedDecisions = getApprovedDecisions();
        long rejectedDecisions = getRejectedDecisions();
        
        Gauge.builder("governance.decisions.total")
            .register(meterRegistry)
            .set(totalDecisions);
        
        Gauge.builder("governance.decisions.approved")
            .register(meterRegistry)
            .set(approvedDecisions);
        
        Gauge.builder("governance.decisions.rejected")
            .register(meterRegistry)
            .set(rejectedDecisions);
        
        // Compliance metrics
        double complianceRate = calculateComplianceRate();
        Gauge.builder("governance.compliance.rate")
            .register(meterRegistry)
            .set(complianceRate);
        
        // Review metrics
        Duration avgReviewTime = calculateAverageReviewTime();
        Timer.builder("governance.review.duration")
            .register(meterRegistry)
            .record(avgReviewTime);
    }
}
```

---

## Summary

Part 2 covers:

1. **Handling Architecture Disagreements**: Resolution framework, strategies, consensus building, escalation
2. **RFC Process**: Process flow, template, review, approval, tracking
3. **Architecture Standards**: Definition, enforcement, automated checking, documentation, training
4. **Architecture Governance**: Structure, process, review, compliance monitoring, metrics

Key principles:
- Resolve disagreements through data and consensus
- Use structured RFC process for major changes
- Enforce standards consistently
- Maintain strong governance structure

# Best Practices - Part 2: Security & Quality

## Question 255: What's the secret management strategy?

### Answer

### Secret Management

#### 1. **Secret Management Principles**

```
┌─────────────────────────────────────────────────────────┐
│         Secret Management Principles                    │
└─────────────────────────────────────────────────────────┘

Never:
├─ Commit secrets to version control
├─ Hardcode secrets in code
├─ Log secrets
└─ Share secrets via email/chat

Always:
├─ Use secret management services
├─ Rotate secrets regularly
├─ Use least privilege
└─ Encrypt secrets at rest and in transit
```

#### 2. **Secret Management Solutions**

```java
// Option 1: Environment Variables
@Configuration
public class DatabaseConfiguration {
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create()
            .url(System.getenv("DB_URL"))
            .username(System.getenv("DB_USERNAME"))
            .password(System.getenv("DB_PASSWORD"))
            .build();
    }
}

// Option 2: AWS Secrets Manager
@Service
public class SecretsManagerService {
    private final AWSSecretsManager secretsManager;
    
    public String getSecret(String secretName) {
        GetSecretValueRequest request = new GetSecretValueRequest()
            .withSecretId(secretName);
        
        GetSecretValueResult result = secretsManager.getSecretValue(request);
        return result.getSecretString();
    }
}

// Option 3: HashiCorp Vault
@Service
public class VaultService {
    private final VaultOperations vaultOperations;
    
    public String getSecret(String path, String key) {
        VaultResponseSupport<Map<String, Object>> response = 
            vaultOperations.read(path);
        return (String) response.getData().get(key);
    }
}
```

#### 3. **Secret Rotation**

```java
@Component
public class SecretRotationService {
    @Scheduled(cron = "0 0 2 * * *") // Daily at 2 AM
    public void rotateSecrets() {
        // Rotate database passwords
        rotateDatabasePassword();
        
        // Rotate API keys
        rotateApiKeys();
        
        // Rotate certificates
        rotateCertificates();
    }
    
    private void rotateDatabasePassword() {
        String newPassword = generateSecurePassword();
        
        // Update in secret manager
        updateSecret("db-password", newPassword);
        
        // Update database
        updateDatabasePassword(newPassword);
        
        // Restart services
        restartServices();
    }
}
```

#### 4. **Secret Injection**

```yaml
# Kubernetes Secrets
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
type: Opaque
data:
  username: <base64-encoded>
  password: <base64-encoded>

---
# Pod using secret
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: app
    env:
    - name: DB_USERNAME
      valueFrom:
        secretKeyRef:
          name: db-secret
          key: username
    - name: DB_PASSWORD
      valueFrom:
        secretKeyRef:
          name: db-secret
          key: password
```

---

## Question 256: How do you handle feature flags?

### Answer

### Feature Flags

#### 1. **Feature Flag Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Feature Flag Types                               │
└─────────────────────────────────────────────────────────┘

Release Flags:
├─ Control feature rollout
├─ Gradual rollout (10%, 50%, 100%)
└─ Quick rollback

Experiment Flags:
├─ A/B testing
├─ Feature experiments
└─ User segmentation

Operational Flags:
├─ Circuit breakers
├─ Performance toggles
└─ Maintenance mode
```

#### 2. **Feature Flag Implementation**

```java
@Service
public class FeatureFlagService {
    private final RedisTemplate<String, Boolean> redisTemplate;
    
    public boolean isEnabled(String featureName) {
        // Check Redis cache
        Boolean cached = redisTemplate.opsForValue()
            .get("feature:" + featureName);
        if (cached != null) {
            return cached;
        }
        
        // Load from database
        FeatureFlag flag = featureFlagRepository.findByName(featureName);
        boolean enabled = flag != null && flag.isEnabled();
        
        // Cache result
        redisTemplate.opsForValue().set(
            "feature:" + featureName,
            enabled,
            Duration.ofMinutes(5)
        );
        
        return enabled;
    }
    
    public boolean isEnabledForUser(String featureName, String userId) {
        FeatureFlag flag = featureFlagRepository.findByName(featureName);
        if (flag == null || !flag.isEnabled()) {
            return false;
        }
        
        // Check user-specific rules
        if (flag.getUserIds().contains(userId)) {
            return true;
        }
        
        // Check percentage rollout
        int userHash = userId.hashCode();
        int percentage = flag.getRolloutPercentage();
        return Math.abs(userHash % 100) < percentage;
    }
}
```

#### 3. **Feature Flag Usage**

```java
@Service
public class AgentMatchService {
    private final FeatureFlagService featureFlagService;
    
    public Agent matchAgent(ConversationRequest request) {
        // Use feature flag to control behavior
        if (featureFlagService.isEnabled("new-routing-algorithm")) {
            return matchAgentWithNewAlgorithm(request);
        } else {
            return matchAgentWithOldAlgorithm(request);
        }
    }
    
    public Agent matchAgentForUser(ConversationRequest request) {
        // User-specific feature flag
        if (featureFlagService.isEnabledForUser(
                "premium-routing", 
                request.getCustomerId())) {
            return matchAgentWithPremiumRouting(request);
        } else {
            return matchAgentWithStandardRouting(request);
        }
    }
}
```

#### 4. **Feature Flag Management**

```java
@RestController
@RequestMapping("/api/admin/feature-flags")
public class FeatureFlagController {
    
    @PostMapping("/{featureName}/enable")
    public void enableFeature(@PathVariable String featureName) {
        featureFlagService.enableFeature(featureName);
    }
    
    @PostMapping("/{featureName}/disable")
    public void disableFeature(@PathVariable String featureName) {
        featureFlagService.disableFeature(featureName);
    }
    
    @PostMapping("/{featureName}/rollout")
    public void setRolloutPercentage(
            @PathVariable String featureName,
            @RequestParam int percentage) {
        featureFlagService.setRolloutPercentage(featureName, percentage);
    }
}
```

---

## Question 257: What's the code review process?

### Answer

### Code Review Process

#### 1. **Code Review Workflow**

```
┌─────────────────────────────────────────────────────────┐
│         Code Review Workflow                           │
└─────────────────────────────────────────────────────────┘

1. Developer creates PR
   ├─ Write code
   ├─ Add tests
   ├─ Update documentation
   └─ Create pull request

2. Automated Checks
   ├─ CI/CD pipeline
   ├─ Unit tests
   ├─ Integration tests
   ├─ Code quality checks
   └─ Security scans

3. Code Review
   ├─ At least 2 reviewers
   ├─ Review checklist
   ├─ Provide feedback
   └─ Approve or request changes

4. Merge
   ├─ All checks pass
   ├─ All approvals received
   └─ Merge to main branch
```

#### 2. **Code Review Checklist**

```
┌─────────────────────────────────────────────────────────┐
│         Code Review Checklist                           │
└─────────────────────────────────────────────────────────┘

Functionality:
├─ Does the code solve the problem?
├─ Are edge cases handled?
├─ Are error cases handled?
└─ Is the logic correct?

Code Quality:
├─ Is the code readable?
├─ Are naming conventions followed?
├─ Is there code duplication?
└─ Are design patterns used appropriately?

Testing:
├─ Are unit tests present?
├─ Are integration tests present?
├─ Is test coverage adequate?
└─ Are tests meaningful?

Performance:
├─ Are there performance issues?
├─ Are database queries optimized?
├─ Is caching used appropriately?
└─ Are there memory leaks?

Security:
├─ Are inputs validated?
├─ Are secrets handled properly?
├─ Are SQL injections prevented?
└─ Are authentication/authorization checks present?

Documentation:
├─ Is code documented?
├─ Is API documented?
├─ Are complex algorithms explained?
└─ Is README updated?
```

#### 3. **Code Review Tools**

```yaml
# GitHub Actions for automated reviews
name: Code Review
on:
  pull_request:
    types: [opened, synchronize]

jobs:
  review:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Run tests
      run: ./gradlew test
    
    - name: Code quality check
      run: ./gradlew check
    
    - name: Security scan
      uses: github/super-linter@v4
```

---

## Question 258: How do you ensure code quality?

### Answer

### Code Quality Assurance

#### 1. **Static Code Analysis**

```java
// SonarQube Configuration
sonar.projectKey=agent-match-service
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.coveragePlugin=jacoco
sonar.coverage.jacoco.xmlReportPaths=build/reports/jacoco/test/jacocoTestReport.xml

// Checkstyle Configuration
checkstyle {
    toolVersion = '9.0'
    configFile = file("config/checkstyle.xml")
}

// PMD Configuration
pmd {
    toolVersion = '6.0.0'
    ruleSetFiles = files("config/pmd-ruleset.xml")
}
```

#### 2. **Code Quality Metrics**

```
┌─────────────────────────────────────────────────────────┐
│         Code Quality Metrics                           │
└─────────────────────────────────────────────────────────┘

Maintainability:
├─ Cyclomatic complexity < 10
├─ Code duplication < 3%
├─ Technical debt ratio < 5%
└─ Code smells < 10

Reliability:
├─ Bugs < 0
├─ Vulnerabilities < 0
├─ Code coverage > 80%
└─ Test success rate > 95%

Security:
├─ Security hotspots < 5
├─ Vulnerabilities < 0
└─ Security rating A

Performance:
├─ No performance issues
├─ No memory leaks
└─ Response time < SLA
```

#### 3. **Quality Gates**

```yaml
# SonarQube Quality Gate
quality_gate:
  conditions:
    - metric: coverage
      operator: greater_than
      value: 80
    - metric: bugs
      operator: equals
      value: 0
    - metric: vulnerabilities
      operator: equals
      value: 0
    - metric: code_smells
      operator: less_than
      value: 10
```

#### 4. **Automated Quality Checks**

```java
// Pre-commit hooks
#!/bin/bash
# .git/hooks/pre-commit

# Run tests
./gradlew test
if [ $? -ne 0 ]; then
    echo "Tests failed"
    exit 1
fi

# Run code quality checks
./gradlew check
if [ $? -ne 0 ]; then
    echo "Code quality checks failed"
    exit 1
fi

# Run linters
./gradlew spotlessCheck
if [ $? -ne 0 ]; then
    echo "Code formatting issues"
    exit 1
fi
```

---

## Summary

Part 2 covers:

1. **Secret Management**: Environment variables, secret managers, rotation
2. **Feature Flags**: Release flags, experiments, operational toggles
3. **Code Review Process**: Workflow, checklist, tools
4. **Code Quality**: Static analysis, metrics, quality gates

Key principles:
- Never commit secrets, use secret management services
- Use feature flags for gradual rollouts and experiments
- Follow structured code review process
- Maintain high code quality through automation

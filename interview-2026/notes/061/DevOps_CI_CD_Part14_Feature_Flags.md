# Feature Flags: Toggle Features, A/B Testing

## Overview

Feature Flags (also known as Feature Toggles) are a software development technique that allows you to enable or disable features in production without deploying new code. They provide control over feature releases, enable A/B testing, and allow gradual rollouts with the ability to quickly disable features if issues arise.

## Feature Flags Concept

```
┌─────────────────────────────────────────────────────────┐
│         Feature Flags Architecture                     │
└─────────────────────────────────────────────────────────┘

Application Code
    │
    ├─► Feature Flag Check
    │   │
    │   ├─► Flag ON → New Feature
    │   │
    │   └─► Flag OFF → Old Feature / No Feature
    │
    ▼
Feature Flag Service
    ├─► Flag Configuration
    ├─► User Targeting
    ├─► Percentage Rollout
    └─► A/B Testing
```

## Feature Flag Types

### 1. Release Flags
```
Purpose: Control feature releases
Lifetime: Short (days/weeks)
Example: New UI design, new feature

if (featureFlags.isEnabled("new-ui")) {
    renderNewUI();
} else {
    renderOldUI();
}
```

### 2. Experiment Flags (A/B Testing)
```
Purpose: Test different variants
Lifetime: Medium (weeks/months)
Example: Test button colors, pricing

if (featureFlags.getVariant("button-color") == "blue") {
    renderBlueButton();
} else {
    renderRedButton();
}
```

### 3. Ops Flags
```
Purpose: Operational control
Lifetime: Long (months/years)
Example: Circuit breakers, maintenance mode

if (featureFlags.isEnabled("maintenance-mode")) {
    showMaintenancePage();
} else {
    showNormalPage();
}
```

### 4. Permission Flags
```
Purpose: Access control
Lifetime: Long (permanent)
Example: Premium features, admin features

if (featureFlags.isEnabledForUser("premium-feature", user)) {
    showPremiumFeature();
}
```

## Feature Flag Implementation

### Basic Implementation

```java
// Feature Flag Service
public class FeatureFlagService {
    public boolean isEnabled(String flagName) {
        // Check flag configuration
        return getFlagConfig(flagName).isEnabled();
    }
    
    public String getVariant(String flagName, String userId) {
        // Get variant for A/B testing
        return getFlagConfig(flagName).getVariant(userId);
    }
}

// Usage in Code
if (featureFlagService.isEnabled("new-checkout")) {
    return newCheckoutFlow();
} else {
    return oldCheckoutFlow();
}
```

### Feature Flag Service Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Feature Flag Service Architecture              │
└─────────────────────────────────────────────────────────┘

Application
    │
    ▼
Feature Flag SDK
    │
    ├─► Local Cache
    │
    ▼
Feature Flag Service
    │
    ├─► Flag Configuration
    ├─► User Targeting Rules
    ├─► Percentage Rollout
    └─► A/B Testing Variants
```

## Feature Flag Use Cases

### 1. Gradual Rollout

```
┌─────────────────────────────────────────────────────────┐
│         Gradual Feature Rollout                        │
└─────────────────────────────────────────────────────────┘

Week 1: 1% of users
  - Enable for 1% of users
  - Monitor metrics
  - Check for issues

Week 2: 10% of users
  - Increase to 10%
  - Continue monitoring
  - Gather feedback

Week 3: 50% of users
  - Increase to 50%
  - Monitor performance
  - Compare metrics

Week 4: 100% of users
  - Full rollout
  - Remove flag (optional)
```

### 2. A/B Testing

```
┌─────────────────────────────────────────────────────────┐
│         A/B Testing with Feature Flags                 │
└─────────────────────────────────────────────────────────┘

Flag: "checkout-button-color"
  Variant A: Blue button (50% of users)
  Variant B: Red button (50% of users)

Metrics to Track:
  - Click-through rate
  - Conversion rate
  - User engagement
  - Revenue

Decision:
  - Variant with better metrics wins
  - Rollout winning variant to 100%
```

### 3. Kill Switch

```
┌─────────────────────────────────────────────────────────┐
│         Kill Switch Pattern                            │
└─────────────────────────────────────────────────────────┘

Scenario: Feature causing issues

Action:
  1. Disable feature flag
  2. Feature immediately disabled
  3. No deployment needed
  4. Instant rollback

Example:
  if (featureFlags.isEnabled("new-payment")) {
      // This code won't execute if flag is off
      processNewPayment();
  } else {
      processOldPayment();
  }
```

### 4. Canary Deployment

```
┌─────────────────────────────────────────────────────────┐
│         Canary with Feature Flags                     │
└─────────────────────────────────────────────────────────┘

Deploy new version to all instances
  - Code for both versions present
  - Feature flag controls which version

Canary Group:
  - Flag enabled for 10% of users
  - New version active

Production Group:
  - Flag disabled for 90% of users
  - Old version active

Gradually increase flag percentage
```

## Feature Flag Targeting

### Targeting Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Feature Flag Targeting                        │
└─────────────────────────────────────────────────────────┘

1. All Users
   └─► Flag enabled for everyone

2. Percentage Rollout
   └─► Flag enabled for X% of users
       - Random selection
       - Consistent per user

3. User Segments
   └─► Flag enabled for specific groups
       - Internal users
       - Beta users
       - Premium users
       - Geographic regions

4. Custom Rules
   └─► Complex targeting logic
       - User attributes
       - Device type
       - Time-based
       - Custom conditions
```

### Targeting Examples

```java
// Percentage Rollout
if (featureFlags.isEnabled("new-feature", user, 25)) {
    // Enabled for 25% of users
    showNewFeature();
}

// User Segment
if (featureFlags.isEnabledForSegment("premium-feature", "premium-users")) {
    showPremiumFeature();
}

// Custom Rule
if (featureFlags.isEnabled("mobile-optimization", user) && 
    user.getDevice().isMobile()) {
    showMobileOptimizedUI();
}
```

## Feature Flag Tools

### Popular Tools

```
┌─────────────────────────────────────────────────────────┐
│         Feature Flag Tools                            │
└─────────────────────────────────────────────────────────┘

Cloud-Based:
  ├─► LaunchDarkly
  ├─► Split.io
  ├─► Optimizely
  ├─► Unleash
  └─► CloudBees Feature Flags

Self-Hosted:
  ├─► Unleash (Open Source)
  ├─► Flagsmith
  └─► Custom Implementation

Cloud Provider:
  ├─► AWS AppConfig
  ├─► Azure App Configuration
  └─► Google Cloud Feature Flags
```

### LaunchDarkly Example

```java
// Initialize client
LDClient client = new LDClient("your-sdk-key");

// Check flag
boolean showNewFeature = client.boolVariation("new-feature", user, false);

if (showNewFeature) {
    renderNewFeature();
} else {
    renderOldFeature();
}
```

## Feature Flag Best Practices

### 1. Naming Conventions
```
✅ Clear, descriptive names
✅ Consistent naming pattern
✅ Include feature name

Examples:
  - "new-checkout-flow"
  - "premium-features"
  - "mobile-redesign-v2"
```

### 2. Clean Up Old Flags
```
- Remove flags after feature is stable
- Document flag purpose
- Set expiration dates
- Regular flag audit
```

### 3. Default Values
```
- Always provide default value
- Default to safe option (usually false)
- Handle flag service failures
```

### 4. Testing
```
- Test with flag on
- Test with flag off
- Test flag transitions
- Test targeting rules
```

### 5. Monitoring
```
- Track flag usage
- Monitor feature metrics
- Alert on flag changes
- Track flag performance
```

## Feature Flag Patterns

### 1. Strangler Pattern
```
┌─────────────────────────────────────────────────────────┐
│         Strangler Pattern with Feature Flags         │
└─────────────────────────────────────────────────────────┘

Gradually replace old system:
  - Start: 0% new system
  - Increase: 10%, 25%, 50%
  - Complete: 100% new system
  - Remove old system

if (featureFlags.isEnabled("new-system")) {
    useNewSystem();
} else {
    useOldSystem();
}
```

### 2. Dark Launch
```
┌─────────────────────────────────────────────────────────┐
│         Dark Launch Pattern                           │
└─────────────────────────────────────────────────────────┘

Deploy feature but don't show to users:
  - Code deployed
  - Feature disabled
  - Test in production
  - Enable when ready

if (featureFlags.isEnabled("dark-launch-feature")) {
    // Execute but don't show to user
    collectMetrics();
    testFunctionality();
}
```

### 3. Circuit Breaker
```
┌─────────────────────────────────────────────────────────┐
│         Circuit Breaker with Feature Flags             │
└─────────────────────────────────────────────────────────┘

Disable feature on high error rate:
  - Monitor error rate
  - Auto-disable if threshold exceeded
  - Fallback to old behavior

if (featureFlags.isEnabled("new-api") && 
    errorRate < threshold) {
    callNewAPI();
} else {
    callOldAPI();
}
```

## Feature Flags in CI/CD

### Integration with Deployment

```
┌─────────────────────────────────────────────────────────┐
│         Feature Flags in Deployment Pipeline           │
└─────────────────────────────────────────────────────────┘

Deploy Code
    │
    ├─► New feature code deployed
    │
    ├─► Feature flag: OFF (default)
    │
    ▼
Test in Production
    │
    ├─► Enable for internal users
    │
    ├─► Monitor metrics
    │
    ▼
Gradual Rollout
    │
    ├─► Enable for 10% of users
    │
    ├─► Increase gradually
    │
    ▼
Full Rollout
    │
    ├─► Enable for 100% of users
    │
    └─► Remove flag (optional)
```

## Summary

Feature Flags:
- **Purpose**: Control feature releases without deployments
- **Types**: Release, Experiment, Ops, Permission flags
- **Use Cases**: Gradual rollout, A/B testing, kill switch, canary
- **Benefits**: Risk reduction, instant control, experimentation

**Key Components:**
- Feature flag service
- Targeting rules
- Percentage rollout
- A/B testing variants
- Monitoring and metrics

**Best Practices:**
- Clear naming conventions
- Clean up old flags
- Provide default values
- Test thoroughly
- Monitor continuously

**Remember**: Feature flags provide powerful control over feature releases and enable safe experimentation in production!

# Git Workflows: GitFlow, GitHub Flow, Trunk-Based Development

## Overview

Git workflows define how teams collaborate using Git. Different workflows suit different team sizes, release cycles, and project requirements. This guide covers three popular workflows: GitFlow, GitHub Flow, and Trunk-Based Development.

## Workflow Comparison

```
┌─────────────────────────────────────────────────────────┐
│              Workflow Comparison                        │
└─────────────────────────────────────────────────────────┘

GitFlow:
├─ Complexity: High
├─ Branches: Many (main, develop, feature, release, hotfix)
├─ Best For: Large teams, scheduled releases
└─ History: Complex but organized

GitHub Flow:
├─ Complexity: Low
├─ Branches: Few (main, feature)
├─ Best For: Small teams, continuous deployment
└─ History: Simple and linear

Trunk-Based:
├─ Complexity: Very Low
├─ Branches: Minimal (main, short-lived features)
├─ Best For: Small teams, continuous integration
└─ History: Very simple
```

## 1. GitFlow Workflow

### Overview

GitFlow is a branching model designed for projects with scheduled release cycles. It uses multiple branch types to manage development, releases, and hotfixes.

### Branch Structure

```
┌─────────────────────────────────────────────────────────┐
│              GitFlow Branch Structure                   │
└─────────────────────────────────────────────────────────┘

main (production)
    │
    ├─► Tags: v1.0.0, v1.1.0, v2.0.0
    │
    ├─► develop (integration)
    │   ├─► feature/user-auth
    │   ├─► feature/payment
    │   └─► release/1.2.0
    │
    └─► hotfix/security-patch
```

### Branch Types

#### 1. Main Branch
- **Purpose**: Production-ready code
- **Protection**: Highly protected
- **Merges From**: Release branches, hotfix branches
- **Tags**: Version tags (v1.0.0)

#### 2. Develop Branch
- **Purpose**: Integration branch for features
- **Protection**: Protected (requires PR)
- **Merges From**: Feature branches, release branches, hotfix branches
- **Always**: Should be deployable

#### 3. Feature Branches
- **Purpose**: New feature development
- **Branch From**: develop
- **Merge To**: develop
- **Naming**: `feature/feature-name`
- **Lifespan**: Days to weeks

#### 4. Release Branches
- **Purpose**: Prepare new production release
- **Branch From**: develop
- **Merge To**: main and develop
- **Naming**: `release/version`
- **Lifespan**: Days to weeks

#### 5. Hotfix Branches
- **Purpose**: Urgent production fixes
- **Branch From**: main
- **Merge To**: main and develop
- **Naming**: `hotfix/description`
- **Lifespan**: Hours to days

### GitFlow Workflow Diagram

```
┌─────────────────────────────────────────────────────────┐
│         GitFlow Workflow Timeline                       │
└─────────────────────────────────────────────────────────┘

main:     A---B-------------------F---G---H (v1.0, v1.1)
           \                     /   /   /
develop:    C---D---E-----------I---J---K
                 \           /   \
feature:          L---M---N       \
                                   \
release:                            O---P (v1.1 prep)
```

### Feature Development

```bash
# 1. Start feature
git checkout develop
git pull origin develop
git checkout -b feature/user-authentication

# 2. Develop feature
git add .
git commit -m "Add login form"
git commit -m "Add authentication logic"
git push origin feature/user-authentication

# 3. Create Pull Request
# (via GitHub/GitLab UI: feature → develop)

# 4. After approval, merge
git checkout develop
git pull origin develop
git merge --no-ff feature/user-authentication
git push origin develop

# 5. Delete feature branch
git branch -d feature/user-authentication
git push origin --delete feature/user-authentication
```

### Release Process

```bash
# 1. Start release
git checkout develop
git pull origin develop
git checkout -b release/1.2.0

# 2. Update version numbers
# Update pom.xml, package.json, etc.
git commit -m "Bump version to 1.2.0"

# 3. Bug fixes only (no new features)
git commit -m "Fix bug in payment processing"

# 4. Finish release - merge to main
git checkout main
git pull origin main
git merge --no-ff release/1.2.0
git tag -a v1.2.0 -m "Release version 1.2.0"
git push origin main
git push origin --tags

# 5. Merge back to develop
git checkout develop
git merge --no-ff release/1.2.0
git push origin develop

# 6. Delete release branch
git branch -d release/1.2.0
git push origin --delete release/1.2.0
```

### Hotfix Process

```bash
# 1. Start hotfix from main
git checkout main
git pull origin main
git checkout -b hotfix/security-patch

# 2. Fix the issue
git commit -m "Fix security vulnerability in authentication"

# 3. Merge to main
git checkout main
git merge --no-ff hotfix/security-patch
git tag -a v1.2.1 -m "Hotfix version 1.2.1"
git push origin main
git push origin --tags

# 4. Merge to develop
git checkout develop
git merge --no-ff hotfix/security-patch
git push origin develop

# 5. Delete hotfix branch
git branch -d hotfix/security-patch
git push origin --delete hotfix/security-patch
```

### GitFlow Pros and Cons

**Pros:**
- ✅ Clear separation of concerns
- ✅ Supports multiple releases
- ✅ Organized history
- ✅ Good for large teams
- ✅ Supports scheduled releases

**Cons:**
- ❌ Complex workflow
- ❌ Many branches to manage
- ❌ Can be overkill for small projects
- ❌ Requires discipline
- ❌ Slower release cycle

## 2. GitHub Flow

### Overview

GitHub Flow is a lightweight workflow designed for continuous deployment. It uses only two branch types: main and feature branches.

### Branch Structure

```
┌─────────────────────────────────────────────────────────┐
│              GitHub Flow Structure                      │
└─────────────────────────────────────────────────────────┘

main (production)
    │
    ├─► feature/user-auth
    ├─► feature/payment
    └─► feature/api-endpoints
```

### Workflow Steps

```bash
# 1. Create feature branch from main
git checkout main
git pull origin main
git checkout -b feature/user-authentication

# 2. Develop feature
git add .
git commit -m "Add user authentication"
git push origin feature/user-authentication

# 3. Create Pull Request
# (via GitHub UI: feature → main)

# 4. Code review and CI checks
# (automated tests, code review)

# 5. Merge to main
# (via GitHub UI: Squash and merge / Merge commit)

# 6. Deploy automatically
# (CI/CD pipeline deploys from main)

# 7. Delete feature branch
git checkout main
git pull origin main
git branch -d feature/user-authentication
```

### GitHub Flow Diagram

```
main:     A---B---C---D---E---F
           \       /   /   /
feature1:   G---H---I
feature2:         J---K
feature3:             L---M
```

### Key Principles

1. **Main is always deployable**
   - Never commit directly to main
   - Always use feature branches
   - Main should always be in deployable state

2. **Feature branches from main**
   - Create from latest main
   - Keep branches short-lived
   - Merge back to main via PR

3. **Pull Requests for everything**
   - All changes via PR
   - Code review required
   - CI checks must pass

4. **Deploy from main**
   - Automatic deployment
   - Continuous deployment
   - Fast feedback

### GitHub Flow Pros and Cons

**Pros:**
- ✅ Simple and easy to understand
- ✅ Fast release cycle
- ✅ Good for continuous deployment
- ✅ Minimal branching overhead
- ✅ Works well for small teams

**Cons:**
- ❌ No release branches
- ❌ Can be chaotic with many features
- ❌ Less structure for large teams
- ❌ No versioning strategy
- ❌ All features in main

## 3. Trunk-Based Development

### Overview

Trunk-Based Development is the simplest workflow. All developers work on short-lived feature branches that merge quickly to main (trunk).

### Branch Structure

```
┌─────────────────────────────────────────────────────────┐
│         Trunk-Based Development                        │
└─────────────────────────────────────────────────────────┘

main (trunk)
    │
    ├─► feature/user-auth (hours/days)
    ├─► feature/payment (hours/days)
    └─► feature/api (hours/days)
```

### Workflow Steps

```bash
# 1. Create short-lived branch
git checkout main
git pull origin main
git checkout -b feature/user-auth

# 2. Develop quickly (same day)
git add .
git commit -m "Add user authentication"
git push origin feature/user-auth

# 3. Merge immediately (same day)
git checkout main
git pull origin main
git merge feature/user-auth
git push origin main

# 4. Delete branch
git branch -d feature/user-auth
git push origin --delete feature/user-auth
```

### Trunk-Based Diagram

```
main:     A---B---C---D---E---F---G---H
           \   /   \   /   \   /   \
feature1:   I       J       K       L
```

### Key Principles

1. **Single main branch (trunk)**
   - All code in main
   - No long-lived branches
   - Main is always deployable

2. **Short-lived branches**
   - Hours to days maximum
   - Merge within same day if possible
   - Small, incremental changes

3. **Feature flags**
   - Hide incomplete features
   - Enable/disable in production
   - Allows incomplete code in main

4. **Continuous integration**
   - Merge frequently
   - Fast feedback
   - Automated testing

### Feature Flags Example

```java
// Feature flag to hide incomplete feature
if (featureFlags.isEnabled("new-payment-system")) {
    // New payment logic
} else {
    // Old payment logic
}
```

### Trunk-Based Pros and Cons

**Pros:**
- ✅ Simplest workflow
- ✅ Fastest integration
- ✅ Minimal merge conflicts
- ✅ Continuous integration
- ✅ Good for small teams

**Cons:**
- ❌ Requires discipline
- ❌ Feature flags needed
- ❌ Can be chaotic
- ❌ Less structure
- ❌ Requires good testing

## 4. Choosing a Workflow

### Decision Matrix

```
┌─────────────────────────────────────────────────────────┐
│         Workflow Selection Guide                        │
└─────────────────────────────────────────────────────────┘

Team Size:
├─ Small (1-5): GitHub Flow or Trunk-Based
├─ Medium (6-15): GitHub Flow or GitFlow
└─ Large (15+): GitFlow

Release Cycle:
├─ Continuous: GitHub Flow or Trunk-Based
├─ Weekly: GitHub Flow
└─ Scheduled: GitFlow

Project Complexity:
├─ Simple: GitHub Flow or Trunk-Based
└─ Complex: GitFlow
```

### Workflow Comparison Table

| Feature | GitFlow | GitHub Flow | Trunk-Based |
|---------|---------|-------------|-------------|
| **Complexity** | High | Low | Very Low |
| **Branches** | 5 types | 2 types | 1-2 types |
| **Release Cycle** | Scheduled | Continuous | Continuous |
| **Team Size** | Large | Small-Medium | Small |
| **Learning Curve** | Steep | Gentle | Very Gentle |
| **History** | Complex | Simple | Very Simple |

## 5. Hybrid Approaches

### GitFlow + Feature Flags

```bash
# Use GitFlow structure but with feature flags
# Allows incomplete features in develop
```

### GitHub Flow + Release Branches

```bash
# Add release branches to GitHub Flow
# For projects needing versioning
```

## 6. Best Practices

### 1. Protect Main Branch
- Require PR for all merges
- Require code review
- Require CI checks to pass
- No direct commits

### 2. Keep Branches Short-Lived
- Merge within days
- Avoid long-lived branches
- Regular integration

### 3. Use Descriptive Names
```bash
# Good
feature/user-authentication
bugfix/login-error
hotfix/security-patch

# Bad
branch1
test
fix
```

### 4. Regular Integration
- Merge frequently
- Update feature branches
- Avoid large merges

### 5. Clear Communication
- Document workflow
- Team alignment
- Regular reviews

## Summary

Git Workflows:
- **GitFlow**: Complex, structured, for large teams and scheduled releases
- **GitHub Flow**: Simple, continuous deployment, for small-medium teams
- **Trunk-Based**: Simplest, continuous integration, for small teams

**Key Principles:**
- Choose workflow that fits your team
- Protect main branch
- Keep branches short-lived
- Regular integration
- Clear communication

**Remember**: The best workflow is the one your team can consistently follow!

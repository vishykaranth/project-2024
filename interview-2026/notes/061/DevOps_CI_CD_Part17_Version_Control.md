# Version Control: Git Workflows, Branching Strategies

## Overview

Version Control (also known as Source Control) is the practice of tracking and managing changes to code and configuration files. Git is the most widely used version control system, and effective Git workflows and branching strategies are essential for collaborative development and DevOps practices.

## Version Control Concepts

```
┌─────────────────────────────────────────────────────────┐
│         Version Control Benefits                      │
└─────────────────────────────────────────────────────────┘

Without Version Control:
  ├─► No change history
  ├─► Hard to collaborate
  ├─► Risk of losing work
  └─► No rollback capability

With Version Control:
  ├─► Complete history
  ├─► Easy collaboration
  ├─► Safe experimentation
  └─► Easy rollback
```

## Git Fundamentals

### Git Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Git Architecture                               │
└─────────────────────────────────────────────────────────┘

Working Directory
    │
    ▼
Staging Area (Index)
    │
    ▼
Local Repository
    │
    ▼
Remote Repository (GitHub, GitLab, etc.)
```

### Basic Git Workflow

```
┌─────────────────────────────────────────────────────────┐
│         Basic Git Workflow                            │
└─────────────────────────────────────────────────────────┘

1. Modify Files
   └─► Working directory

2. Stage Changes
   └─► git add

3. Commit Changes
   └─► git commit

4. Push to Remote
   └─► git push
```

## Git Branching Strategies

### 1. Git Flow

```
┌─────────────────────────────────────────────────────────┐
│         Git Flow Branching Strategy                   │
└─────────────────────────────────────────────────────────┘

Main Branches:
  main/master    → Production code
  develop        → Integration branch

Supporting Branches:
  feature/*      → New features
  release/*      → Release preparation
  hotfix/*       → Production fixes

Workflow:
  feature → develop → release → main
  hotfix → main (and develop)
```

### Git Flow Example

```bash
# Create feature branch
git checkout -b feature/user-authentication develop

# Work on feature
git add .
git commit -m "Add user authentication"

# Merge to develop
git checkout develop
git merge feature/user-authentication

# Create release branch
git checkout -b release/1.2.0 develop

# Prepare release
# Fix bugs, update version

# Merge to main and develop
git checkout main
git merge release/1.2.0
git tag -a v1.2.0

git checkout develop
git merge release/1.2.0
```

### 2. GitHub Flow

```
┌─────────────────────────────────────────────────────────┐
│         GitHub Flow Branching Strategy                │
└─────────────────────────────────────────────────────────┘

Branches:
  main          → Production code
  feature/*     → Feature branches

Workflow:
  1. Create feature branch from main
  2. Develop and commit
  3. Create pull request
  4. Review and merge to main
  5. Deploy from main
```

### GitHub Flow Example

```bash
# Create feature branch
git checkout -b feature/new-feature main

# Develop
git add .
git commit -m "Implement new feature"

# Push and create PR
git push origin feature/new-feature
# Create PR on GitHub

# After review, merge PR
# Then deploy from main
```

### 3. GitLab Flow

```
┌─────────────────────────────────────────────────────────┐
│         GitLab Flow Branching Strategy                 │
└─────────────────────────────────────────────────────────┘

Branches:
  main          → Production
  pre-production → Staging
  feature/*     → Features

Workflow:
  feature → main → pre-production → production
```

### 4. Trunk-Based Development

```
┌─────────────────────────────────────────────────────────┐
│         Trunk-Based Development                        │
└─────────────────────────────────────────────────────────┘

Approach:
  - Main branch (trunk) is always deployable
  - Short-lived feature branches
  - Frequent merges to main
  - Feature flags for incomplete features

Benefits:
  - Simple
  - Fast integration
  - Continuous deployment
```

## Git Workflows

### 1. Feature Branch Workflow

```
┌─────────────────────────────────────────────────────────┐
│         Feature Branch Workflow                       │
└─────────────────────────────────────────────────────────┘

Developer
    │
    ▼
Create Feature Branch
    │
    ▼
Develop Feature
    │
    ▼
Push Feature Branch
    │
    ▼
Create Pull Request
    │
    ▼
Code Review
    │
    ├─► Changes Requested → Update
    │
    └─► Approved → Merge
        │
        ▼
    Deploy
```

### 2. Forking Workflow

```
┌─────────────────────────────────────────────────────────┐
│         Forking Workflow                               │
└─────────────────────────────────────────────────────────┘

Contributor
    │
    ▼
Fork Repository
    │
    ▼
Clone Fork
    │
    ▼
Create Feature Branch
    │
    ▼
Develop and Push
    │
    ▼
Create Pull Request
    │
    ▼
Maintainer Reviews
    │
    ▼
Merge to Upstream
```

### 3. Centralized Workflow

```
┌─────────────────────────────────────────────────────────┐
│         Centralized Workflow                          │
└─────────────────────────────────────────────────────────┘

All developers work with single remote:
  - Pull before work
  - Commit changes
  - Push to remote
  - Resolve conflicts if needed
```

## Branch Protection

### Branch Protection Rules

```
┌─────────────────────────────────────────────────────────┐
│         Branch Protection Rules                       │
└─────────────────────────────────────────────────────────┘

Main Branch Protection:
  ├─► Require pull request reviews
  ├─► Require status checks
  ├─► Require branches to be up to date
  ├─► No force push
  └─► No deletion

Status Checks:
  ├─► CI pipeline must pass
  ├─► Code coverage threshold
  ├─► Security scans
  └─► Quality gates
```

### GitHub Branch Protection Example

```yaml
# .github/branch-protection.yml
branch_protection:
  main:
    required_pull_request_reviews:
      required_approving_review_count: 2
      dismiss_stale_reviews: true
    required_status_checks:
      strict: true
      contexts:
        - ci/build
        - ci/test
        - ci/quality
    enforce_admins: true
    restrictions: null
```

## Git Best Practices

### 1. Commit Messages

```
✅ Clear and descriptive
✅ Follow conventions (Conventional Commits)
✅ Reference issues/tickets
✅ Explain why, not just what

Examples:
  feat: Add user authentication
  fix: Resolve login timeout issue
  docs: Update API documentation
  refactor: Simplify payment processing
```

### 2. Branch Naming

```
✅ Consistent naming convention
✅ Descriptive names
✅ Include issue numbers

Examples:
  feature/user-authentication
  bugfix/login-timeout
  hotfix/security-patch
  release/1.2.0
```

### 3. Frequent Commits

```
✅ Commit often
✅ Small, logical changes
✅ Working code
✅ Meaningful commits
```

### 4. Pull Request Best Practices

```
✅ Small, focused PRs
✅ Clear description
✅ Link to issues
✅ Request reviews
✅ Address feedback
```

## Git in CI/CD

### Integration with CI/CD

```
┌─────────────────────────────────────────────────────────┐
│         Git in CI/CD Pipeline                         │
└─────────────────────────────────────────────────────────┘

Git Events Trigger CI/CD:
  ├─► Push to branch
  ├─► Pull request created
  ├─► Tag created
  └─► Merge to main

CI/CD Actions:
  ├─► Run tests
  ├─► Build artifacts
  ├─► Deploy to environments
  └─► Update status in Git
```

### Git Hooks

```
┌─────────────────────────────────────────────────────────┐
│         Git Hooks                                      │
└─────────────────────────────────────────────────────────┘

Pre-commit:
  - Run linters
  - Run tests
  - Check formatting

Pre-push:
  - Run full test suite
  - Check coverage

Post-merge:
  - Install dependencies
  - Update documentation
```

## Summary

Version Control:
- **Purpose**: Track and manage code changes
- **Tool**: Git (most common)
- **Workflows**: Git Flow, GitHub Flow, GitLab Flow, Trunk-Based
- **Benefits**: History, collaboration, safety, rollback

**Key Components:**
- Branching strategies
- Workflow patterns
- Branch protection
- CI/CD integration
- Best practices

**Best Practices:**
- Clear commit messages
- Consistent branch naming
- Frequent commits
- Small PRs
- Branch protection

**Remember**: Effective version control is the foundation of good DevOps practices!

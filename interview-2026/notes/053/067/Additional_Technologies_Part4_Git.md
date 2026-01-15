# Git: Branching Strategies, Merge vs Rebase, Workflows

## Overview

Git is a distributed version control system that tracks changes in source code. Understanding branching strategies, merge vs rebase, and workflows is essential for effective collaboration in software development.

## Git Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Git Architecture                            │
└─────────────────────────────────────────────────────────┘

Working Directory
    │ (git add)
    ▼
Staging Area (Index)
    │ (git commit)
    ▼
Local Repository
    │ (git push)
    ▼
Remote Repository
```

## 1. Branching Strategies

### What are Branches?

Branches are independent lines of development that allow you to work on features without affecting the main codebase.

### Branch Structure

```
┌─────────────────────────────────────────────────────────┐
│              Branch Visualization                        │
└─────────────────────────────────────────────────────────┘

main/master
    │
    ├─► feature/user-auth
    │   └─► feature/user-profile
    │
    ├─► feature/payment
    │
    └─► hotfix/security-patch
```

### Common Branch Types

#### 1. Main/Master Branch
```bash
# Production-ready code
git checkout main
```

**Purpose:**
- Stable, production code
- Always deployable
- Protected (requires PR)

#### 2. Develop Branch
```bash
# Integration branch
git checkout develop
```

**Purpose:**
- Integration of features
- Pre-production testing
- Continuous integration

#### 3. Feature Branches
```bash
# Feature development
git checkout -b feature/user-authentication
```

**Naming:**
- `feature/user-authentication`
- `feature/payment-integration`
- `feature/api-endpoints`

**Purpose:**
- Isolated feature development
- Easy to review and test
- Can be deleted after merge

#### 4. Release Branches
```bash
# Release preparation
git checkout -b release/1.2.0
```

**Purpose:**
- Prepare new release
- Bug fixes only
- Final testing

#### 5. Hotfix Branches
```bash
# Critical production fixes
git checkout -b hotfix/security-patch
```

**Purpose:**
- Urgent production fixes
- Bypass normal workflow
- Merge to main and develop

## 2. Merge vs Rebase

### Merge

#### What is Merge?

Merge combines changes from one branch into another, creating a merge commit.

#### Merge Process

```
┌─────────────────────────────────────────────────────────┐
│              Merge Process                               │
└─────────────────────────────────────────────────────────┘

Before Merge:
main:     A---B---C
                \
feature:         D---E

After Merge:
main:     A---B---C-------F (merge commit)
                \       /
feature:         D---E
```

#### Merge Types

**1. Fast-Forward Merge**
```bash
# When no divergence
git checkout main
git merge feature
```

```
Before:        After:
main: A---B    main: A---B---C
              feature: A---B---C
```

**2. Three-Way Merge**
```bash
# When branches diverged
git checkout main
git merge feature
```

```
Before:              After:
main: A---B---C      main: A---B---C---F
      \                     \         /
feature: D---E             D---E
```

**3. Merge Commit**
```bash
# Creates merge commit
git merge --no-ff feature
```

### Rebase

#### What is Rebase?

Rebase replays commits from one branch onto another, creating a linear history.

#### Rebase Process

```
┌─────────────────────────────────────────────────────────┐
│              Rebase Process                              │
└─────────────────────────────────────────────────────────┘

Before Rebase:
main:     A---B---C
                \
feature:         D---E

After Rebase:
main:     A---B---C
                      \
feature:                 D'---E'
```

#### Rebase Commands

```bash
# Interactive rebase
git rebase -i main

# Rebase current branch onto main
git checkout feature
git rebase main

# Continue after conflict
git rebase --continue

# Abort rebase
git rebase --abort
```

### Merge vs Rebase Comparison

| Aspect | Merge | Rebase |
|--------|-------|--------|
| **History** | Preserves branch history | Linear history |
| **Commits** | Creates merge commit | Rewrites commits |
| **Complexity** | Simple | More complex |
| **Safety** | Safe (non-destructive) | Rewrites history |
| **Use Case** | Public branches | Feature branches |
| **Conflict** | One-time resolution | May need multiple |

### When to Use Merge

```
┌─────────────────────────────────────────────────────────┐
│         When to Use Merge                                │
└─────────────────────────────────────────────────────────┘

✓ Merging to main/master
✓ Preserving branch history
✓ Shared/public branches
✓ When you want merge commits
✓ Simpler conflict resolution
```

### When to Use Rebase

```
┌─────────────────────────────────────────────────────────┐
│         When to Use Rebase                              │
└─────────────────────────────────────────────────────────┘

✓ Feature branches (local)
✓ Before merging to main
✓ Clean linear history
✓ Squashing commits
✓ Updating feature branch
```

## 3. Git Workflows

### Workflow Types

```
┌─────────────────────────────────────────────────────────┐
│              Git Workflow Types                         │
└─────────────────────────────────────────────────────────┘

1. GitFlow
   ├─ Feature branches
   ├─ Develop branch
   ├─ Release branches
   └─ Hotfix branches

2. GitHub Flow
   ├─ Main branch
   └─ Feature branches

3. Trunk-Based Development
   ├─ Single main branch
   └─ Short-lived feature branches
```

## 4. GitFlow Workflow

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              GitFlow Structure                          │
└─────────────────────────────────────────────────────────┘

main (production)
    │
    ├─► develop (integration)
    │   ├─► feature/user-auth
    │   ├─► feature/payment
    │   └─► release/1.2.0
    │
    └─► hotfix/security-patch
```

### Workflow Steps

#### 1. Feature Development
```bash
# Start feature
git checkout develop
git pull origin develop
git checkout -b feature/user-auth

# Develop feature
git add .
git commit -m "Add user authentication"

# Finish feature
git checkout develop
git merge --no-ff feature/user-auth
git branch -d feature/user-auth
git push origin develop
```

#### 2. Release Preparation
```bash
# Start release
git checkout develop
git checkout -b release/1.2.0

# Bug fixes only
git commit -m "Fix bug in release"

# Finish release
git checkout main
git merge --no-ff release/1.2.0
git tag -a v1.2.0
git checkout develop
git merge --no-ff release/1.2.0
git branch -d release/1.2.0
```

#### 3. Hotfix
```bash
# Start hotfix
git checkout main
git checkout -b hotfix/security-patch

# Fix issue
git commit -m "Fix security vulnerability"

# Finish hotfix
git checkout main
git merge --no-ff hotfix/security-patch
git tag -a v1.2.1
git checkout develop
git merge --no-ff hotfix/security-patch
git branch -d hotfix/security-patch
```

### GitFlow Diagram

```
main:     A---B-------------------F---G (tags)
           \                     /   /
develop:    C---D---E-----------H---I
                 \           /
feature:          J---K---L
```

## 5. GitHub Flow

### Structure

```
┌─────────────────────────────────────────────────────────┐
│              GitHub Flow Structure                      │
└─────────────────────────────────────────────────────────┘

main (production)
    │
    ├─► feature/user-auth
    ├─► feature/payment
    └─► feature/api
```

### Workflow Steps

```bash
# 1. Create feature branch
git checkout main
git pull origin main
git checkout -b feature/user-auth

# 2. Develop and commit
git add .
git commit -m "Add user authentication"
git push origin feature/user-auth

# 3. Create Pull Request
# (via GitHub UI)

# 4. Review and merge
# (via GitHub UI - merge to main)

# 5. Deploy
# (automated deployment from main)
```

### GitHub Flow Diagram

```
main:     A---B---C---D---E
           \       /   /
feature1:   F---G---H
feature2:         I---J
```

## 6. Trunk-Based Development

### Structure

```
┌─────────────────────────────────────────────────────────┐
│         Trunk-Based Development                        │
└─────────────────────────────────────────────────────────┘

main (trunk)
    │
    ├─► feature/user-auth (short-lived)
    ├─► feature/payment (short-lived)
    └─► feature/api (short-lived)
```

### Principles

1. **Single Main Branch**: All code in main
2. **Short-Lived Branches**: Merge within hours/days
3. **Feature Flags**: Hide incomplete features
4. **Continuous Integration**: Frequent merges

### Workflow

```bash
# 1. Create short-lived branch
git checkout main
git pull origin main
git checkout -b feature/user-auth

# 2. Develop quickly
git add .
git commit -m "Add user auth"
git push origin feature/user-auth

# 3. Merge quickly (same day)
git checkout main
git merge feature/user-auth
git push origin main
git branch -d feature/user-auth
```

## 7. Common Git Commands

### Branch Management

```bash
# List branches
git branch
git branch -a  # All branches
git branch -r  # Remote branches

# Create branch
git branch feature/new
git checkout -b feature/new

# Delete branch
git branch -d feature/old
git branch -D feature/old  # Force delete

# Rename branch
git branch -m old-name new-name
```

### Merging

```bash
# Merge branch
git checkout main
git merge feature/branch

# Merge with no fast-forward
git merge --no-ff feature/branch

# Merge with squash
git merge --squash feature/branch
```

### Rebasing

```bash
# Rebase onto main
git checkout feature/branch
git rebase main

# Interactive rebase
git rebase -i HEAD~3

# Continue/abort
git rebase --continue
git rebase --abort
```

### Conflict Resolution

```bash
# During merge/rebase
# 1. Edit conflicted files
# 2. Stage resolved files
git add file.txt

# 3. Continue
git commit  # For merge
git rebase --continue  # For rebase
```

## 8. Best Practices

### 1. Branch Naming
```bash
# Good names
feature/user-authentication
bugfix/login-error
hotfix/security-patch
release/1.2.0

# Bad names
branch1
test
fix
```

### 2. Commit Messages
```bash
# Good commit message
git commit -m "Add user authentication

- Implement login functionality
- Add password validation
- Update user model

Fixes #123"

# Bad commit message
git commit -m "fix"
```

### 3. Regular Merges
- Merge frequently to avoid large conflicts
- Keep branches short-lived
- Update feature branches regularly

### 4. Use Pull Requests
- Code review before merge
- Automated checks
- Discussion and collaboration

### 5. Protect Main Branch
- Require PR for merges
- Require approvals
- Run CI/CD checks

## Summary

Git Workflows:
- **Branching Strategies**: Feature, release, hotfix branches
- **Merge**: Preserves history, creates merge commits
- **Rebase**: Linear history, rewrites commits
- **Workflows**: GitFlow, GitHub Flow, Trunk-Based

**Key Concepts:**
- Branch types and purposes
- When to merge vs rebase
- Workflow patterns
- Conflict resolution

**Best Practices:**
- Use descriptive branch names
- Write clear commit messages
- Merge frequently
- Use pull requests
- Protect main branch

**Remember**: Choose the workflow that fits your team size and release cycle!

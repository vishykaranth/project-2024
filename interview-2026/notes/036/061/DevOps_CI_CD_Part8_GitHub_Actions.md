# GitHub Actions: Workflows, Actions, Matrix Builds

## Overview

GitHub Actions is a CI/CD platform integrated directly into GitHub. It allows you to automate workflows using YAML files stored in your repository. It provides GitHub-hosted runners and a marketplace of reusable actions, making it easy to build, test, and deploy code.

## GitHub Actions Architecture

```
┌─────────────────────────────────────────────────────────┐
│         GitHub Actions Architecture                    │
└─────────────────────────────────────────────────────────┘

GitHub Repository
    │
    ├─► .github/workflows/ (Workflow files)
    │
    ▼
GitHub Actions Service
    │
    ├─► Parses workflows
    ├─► Schedules jobs
    └─► Manages execution
         │
         ▼
GitHub Runners
    ├─► Ubuntu (Linux)
    ├─► Windows
    └─► macOS
```

## GitHub Actions Workflows

### Workflow File Structure

```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build
        run: mvn clean compile
      - name: Test
        run: mvn test
```

## Workflow Triggers

```
┌─────────────────────────────────────────────────────────┐
│              Workflow Triggers                         │
└─────────────────────────────────────────────────────────┘

1. Push Events
   on:
     push:
       branches: [ main ]

2. Pull Request Events
   on:
     pull_request:
       branches: [ main ]

3. Scheduled Events
   on:
     schedule:
       - cron: '0 0 * * *'  # Daily at midnight

4. Manual Events
   on:
     workflow_dispatch:

5. Webhook Events
   on:
     repository_dispatch:
       types: [deploy]
```

## GitHub Actions

### Using Actions

```yaml
steps:
  # Checkout code
  - uses: actions/checkout@v3
  
  # Setup Java
  - uses: actions/setup-java@v3
    with:
      java-version: '17'
      distribution: 'temurin'
  
  # Setup Node.js
  - uses: actions/setup-node@v3
    with:
      node-version: '18'
  
  # Run custom command
  - name: Build
    run: mvn clean package
```

### Popular Actions

```
┌─────────────────────────────────────────────────────────┐
│              Popular GitHub Actions                   │
└─────────────────────────────────────────────────────────┘

Version Control:
  - actions/checkout@v3
  - actions/checkout@v2

Languages:
  - actions/setup-java@v3
  - actions/setup-node@v3
  - actions/setup-python@v4
  - actions/setup-go@v3

Docker:
  - docker/login-action@v2
  - docker/build-push-action@v4

Deployment:
  - azure/webapps-deploy@v2
  - aws-actions/configure-aws-credentials@v2

Notifications:
  - 8398a7/action-slack@v3
  - actions/github-script@v6
```

## Matrix Builds

### Matrix Strategy

```yaml
jobs:
  test:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        java-version: [11, 17, 21]
        os: [ubuntu-latest, windows-latest, macos-latest]
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: ${{ matrix.java-version }}
      - name: Test
        run: mvn test
```

### Matrix Build Example

```
┌─────────────────────────────────────────────────────────┐
│         Matrix Build Execution                        │
└─────────────────────────────────────────────────────────┘

Matrix:
  java-version: [11, 17, 21]
  os: [ubuntu, windows, macos]

Creates 9 jobs:
  - Java 11 + Ubuntu
  - Java 11 + Windows
  - Java 11 + macOS
  - Java 17 + Ubuntu
  - Java 17 + Windows
  - Java 17 + macOS
  - Java 21 + Ubuntu
  - Java 21 + Windows
  - Java 21 + macOS
```

## Workflow Examples

### Maven Build and Test

```yaml
name: Maven CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Build with Maven
        run: mvn clean compile
      
      - name: Run Tests
        run: mvn test
      
      - name: Generate Coverage
        run: mvn jacoco:report
      
      - name: Upload Coverage
        uses: codecov/codecov-action@v3
        with:
          file: ./target/site/jacoco/jacoco.xml
```

### Docker Build and Push

```yaml
name: Docker Build and Push

on:
  push:
    branches: [ main ]
    tags:
      - 'v*'

jobs:
  build-and-push:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Build and Push
        uses: docker/build-push-action@v4
        with:
          context: .
          push: ${{ github.event_name != 'pull_request' }}
          tags: |
            myapp:latest
            myapp:${{ github.sha }}
            myapp:${{ github.ref_name }}
```

### Kubernetes Deployment

```yaml
name: Deploy to Kubernetes

on:
  push:
    branches: [ main ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up kubectl
        uses: azure/setup-kubectl@v3
      
      - name: Configure kubectl
        run: |
          echo "${{ secrets.KUBE_CONFIG }}" | base64 -d > kubeconfig
          export KUBECONFIG=kubeconfig
      
      - name: Deploy
        run: |
          kubectl set image deployment/myapp \
            myapp=myapp:${{ github.sha }} \
            -n production
```

## Workflow Features

### 1. Job Dependencies

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - run: mvn clean package
    outputs:
      version: ${{ steps.version.outputs.version }}
  
  test:
    needs: build
    runs-on: ubuntu-latest
    steps:
      - run: mvn test
  
  deploy:
    needs: [build, test]
    runs-on: ubuntu-latest
    steps:
      - run: ./deploy.sh
```

### 2. Conditional Execution

```yaml
jobs:
  deploy:
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    steps:
      - run: ./deploy.sh
```

### 3. Caching

```yaml
steps:
  - uses: actions/cache@v3
    with:
      path: ~/.m2
      key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      restore-keys: |
        ${{ runner.os }}-m2-
  
  - run: mvn clean package
```

### 4. Artifacts

```yaml
steps:
  - name: Build
    run: mvn clean package
  
  - name: Upload Artifact
    uses: actions/upload-artifact@v3
    with:
      name: jar
      path: target/*.jar
  
  - name: Download Artifact
    uses: actions/download-artifact@v3
    with:
      name: jar
```

### 5. Secrets

```yaml
steps:
  - name: Deploy
    env:
      API_KEY: ${{ secrets.API_KEY }}
      DATABASE_URL: ${{ secrets.DATABASE_URL }}
    run: ./deploy.sh
```

## GitHub Actions Best Practices

### 1. Use Specific Action Versions
```yaml
# BAD
- uses: actions/checkout@main

# GOOD
- uses: actions/checkout@v3
```

### 2. Cache Dependencies
```yaml
- uses: actions/cache@v3
  with:
    path: ~/.m2
    key: maven-${{ hashFiles('**/pom.xml') }}
```

### 3. Use Matrix for Multiple Versions
```yaml
strategy:
  matrix:
    java-version: [11, 17, 21]
```

### 4. Set Timeouts
```yaml
jobs:
  test:
    timeout-minutes: 30
    steps:
      - run: mvn test
```

### 5. Use Workflow Reusability
```yaml
# .github/workflows/reusable.yml
on:
  workflow_call:
    inputs:
      java-version:
        required: true
        type: string

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/setup-java@v3
        with:
          java-version: ${{ inputs.java-version }}
```

## GitHub Actions vs Other Tools

| Feature | GitHub Actions | Jenkins | GitLab CI |
|---------|----------------|---------|-----------|
| **Hosting** | GitHub-hosted | Self-hosted | Self-hosted/Cloud |
| **Setup** | Very simple | Complex | Simple |
| **Cost** | Free for public | Free | Free/Paid |
| **Runners** | GitHub-provided | Self-hosted | Self-hosted |
| **Marketplace** | Large | Plugins | Actions |

## Summary

GitHub Actions:
- **Type**: Integrated CI/CD in GitHub
- **Configuration**: YAML workflows
- **Runners**: GitHub-hosted or self-hosted
- **Strengths**: Easy setup, large marketplace, native integration

**Key Features:**
- Native GitHub integration
- YAML-based workflows
- Reusable actions
- Matrix builds
- GitHub-hosted runners

**Best Practices:**
- Use specific action versions
- Cache dependencies
- Use matrix for multiple versions
- Set timeouts
- Leverage reusable workflows

**Remember**: GitHub Actions is perfect if you're using GitHub and want simple, integrated CI/CD!

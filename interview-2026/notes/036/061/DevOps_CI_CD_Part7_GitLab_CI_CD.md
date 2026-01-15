# GitLab CI/CD: Integrated Pipelines, Runners

## Overview

GitLab CI/CD is an integrated continuous integration and deployment tool built into GitLab. It uses a YAML file (`.gitlab-ci.yml`) to define pipelines and leverages GitLab Runners to execute jobs. It provides seamless integration with GitLab's version control and project management features.

## GitLab CI/CD Architecture

```
┌─────────────────────────────────────────────────────────┐
│         GitLab CI/CD Architecture                      │
└─────────────────────────────────────────────────────────┘

GitLab Repository
    │
    ├─► .gitlab-ci.yml (Pipeline definition)
    │
    ▼
GitLab CI/CD Service
    │
    ├─► Parses pipeline
    ├─► Schedules jobs
    └─► Manages pipeline execution
         │
         ▼
GitLab Runners
    ├─► Runner 1 (Linux)
    ├─► Runner 2 (Windows)
    └─► Runner 3 (Docker)
```

## GitLab CI/CD Pipeline

### Basic Pipeline Structure

```yaml
# .gitlab-ci.yml
stages:
  - build
  - test
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

cache:
  paths:
    - .m2/repository/

build:
  stage: build
  script:
    - mvn clean compile
  artifacts:
    paths:
      - target/
    expire_in: 1 hour

test:
  stage: test
  script:
    - mvn test
  coverage: '/Total.*?([0-9]{1,3})%/'

deploy:
  stage: deploy
  script:
    - ./deploy.sh
  only:
    - main
```

## GitLab Runners

### Runner Types

```
┌─────────────────────────────────────────────────────────┐
│              GitLab Runner Types                       │
└─────────────────────────────────────────────────────────┘

1. Shared Runners
   └─► Available to all projects
       - GitLab.com provides shared runners
       - Can be self-hosted

2. Group Runners
   └─► Available to group projects
       - Shared within a group
       - Useful for organization

3. Specific Runners
   └─► Assigned to specific projects
       - Project-specific resources
       - Custom configurations

4. Instance Runners
   └─► Available to all projects on instance
       - Self-hosted GitLab
       - Organization-wide
```

### Runner Executors

```
┌─────────────────────────────────────────────────────────┐
│              Runner Executors                          │
└─────────────────────────────────────────────────────────┘

1. Shell Executor
   └─► Runs commands in shell
       - Direct execution
       - No isolation

2. Docker Executor
   └─► Runs in Docker containers
       - Isolation
       - Reproducible environments

3. Kubernetes Executor
   └─► Runs in Kubernetes pods
       - Scalable
       - Dynamic allocation

4. VirtualBox Executor
   └─► Runs in VMs
       - Full isolation
       - OS-level isolation
```

### Runner Registration

```bash
# Register a runner
gitlab-runner register

# Configuration prompts:
# - GitLab URL
# - Registration token
# - Description
# - Tags
# - Executor (docker, shell, etc.)
```

## GitLab CI/CD Features

### 1. Multi-Stage Pipelines

```yaml
stages:
  - build
  - test
  - deploy-staging
  - deploy-production

build-job:
  stage: build
  script:
    - mvn clean package

test-job:
  stage: test
  script:
    - mvn test

deploy-staging:
  stage: deploy-staging
  script:
    - ./deploy.sh staging
  only:
    - develop

deploy-production:
  stage: deploy-production
  script:
    - ./deploy.sh production
  only:
    - main
  when: manual
```

### 2. Parallel Jobs

```yaml
test:
  stage: test
  parallel:
    matrix:
      - JAVA_VERSION: ["11", "17"]
        OS: ["linux", "windows"]
  script:
    - echo "Testing Java $JAVA_VERSION on $OS"
```

### 3. Job Dependencies

```yaml
build:
  stage: build
  script:
    - mvn clean package
  artifacts:
    paths:
      - target/*.jar

deploy:
  stage: deploy
  needs: [build]
  script:
    - ./deploy.sh
```

### 4. Conditional Execution

```yaml
deploy-production:
  stage: deploy
  script:
    - ./deploy.sh production
  only:
    - main
  except:
    - tags
  when: manual
  rules:
    - if: $CI_COMMIT_BRANCH == "main"
      when: manual
    - if: $CI_COMMIT_TAG
      when: on_success
```

### 5. Caching

```yaml
cache:
  key: ${CI_COMMIT_REF_SLUG}
  paths:
    - .m2/repository/
    - node_modules/

build:
  script:
    - mvn clean package
  cache:
    policy: push
```

### 6. Artifacts

```yaml
build:
  stage: build
  script:
    - mvn clean package
  artifacts:
    paths:
      - target/*.jar
    expire_in: 1 week
    reports:
      junit: target/surefire-reports/*.xml
```

## GitLab CI/CD Variables

### Predefined Variables

```yaml
# Common GitLab CI/CD variables
$CI_COMMIT_SHA          # Commit SHA
$CI_COMMIT_REF_NAME     # Branch or tag name
$CI_COMMIT_MESSAGE      # Commit message
$CI_PROJECT_NAME        # Project name
$CI_PIPELINE_ID         # Pipeline ID
$CI_JOB_NAME            # Job name
$CI_RUNNER_TAGS         # Runner tags
```

### Custom Variables

```yaml
variables:
  MAVEN_VERSION: "3.8.6"
  JAVA_VERSION: "17"
  DEPLOY_ENV: "staging"

# Or set in GitLab UI:
# Settings → CI/CD → Variables
```

## GitLab CI/CD Examples

### Maven Project

```yaml
image: maven:3.8.6-openjdk-17

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"

cache:
  paths:
    - .m2/repository/

stages:
  - build
  - test
  - deploy

build:
  stage: build
  script:
    - mvn clean compile
  artifacts:
    paths:
      - target/

test:
  stage: test
  script:
    - mvn test
  coverage: '/Total.*?([0-9]{1,3})%/'

deploy:
  stage: deploy
  script:
    - mvn deploy
  only:
    - main
```

### Docker Build and Push

```yaml
stages:
  - build
  - deploy

build-image:
  stage: build
  script:
    - docker build -t $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA .
    - docker push $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
  only:
    - main

deploy:
  stage: deploy
  script:
    - docker pull $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
    - docker tag $CI_REGISTRY_IMAGE:$CI_COMMIT_SHA $CI_REGISTRY_IMAGE:latest
    - docker push $CI_REGISTRY_IMAGE:latest
    - kubectl set image deployment/myapp myapp=$CI_REGISTRY_IMAGE:$CI_COMMIT_SHA
  only:
    - main
```

### Kubernetes Deployment

```yaml
deploy:
  stage: deploy
  image: bitnami/kubectl:latest
  script:
    - kubectl apply -f k8s/
    - kubectl rollout status deployment/myapp
  environment:
    name: production
    url: https://myapp.example.com
  only:
    - main
```

## GitLab CI/CD Best Practices

### 1. Use Docker Images
```yaml
# Specify Docker image for consistent environment
image: maven:3.8.6-openjdk-17
```

### 2. Cache Dependencies
```yaml
cache:
  paths:
    - .m2/repository/
    - node_modules/
```

### 3. Use Artifacts
```yaml
artifacts:
  paths:
    - target/
  expire_in: 1 week
```

### 4. Parallel Execution
```yaml
test:
  parallel: 4
  script:
    - mvn test
```

### 5. Job Tags
```yaml
deploy:
  tags:
    - docker
    - production
  script:
    - ./deploy.sh
```

## GitLab CI/CD vs Other Tools

| Feature | GitLab CI/CD | Jenkins | GitHub Actions |
|---------|--------------|---------|----------------|
| **Integration** | Native GitLab | Separate | Native GitHub |
| **Setup** | Simple | Complex | Simple |
| **Runners** | Self-hosted/Shared | Self-hosted | GitHub-hosted |
| **Configuration** | YAML | Groovy/YAML | YAML |
| **Cost** | Free/Paid | Free | Free/Paid |

## Summary

GitLab CI/CD:
- **Type**: Integrated CI/CD in GitLab
- **Configuration**: YAML file (.gitlab-ci.yml)
- **Runners**: Self-hosted or GitLab-provided
- **Strengths**: Native integration, simple setup

**Key Features:**
- Integrated with GitLab
- YAML-based configuration
- Flexible runners
- Parallel execution
- Artifacts and caching

**Best Practices:**
- Use Docker images
- Cache dependencies
- Leverage artifacts
- Use parallel execution
- Tag runners appropriately

**Remember**: GitLab CI/CD is excellent if you're already using GitLab!

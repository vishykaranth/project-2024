# CircleCI: Orbs, Workflows, Parallelism

## Overview

CircleCI is a cloud-based CI/CD platform that provides fast, scalable, and flexible continuous integration and deployment. It uses YAML configuration files and offers features like orbs (reusable configurations), workflows, and parallelism for efficient pipeline execution.

## CircleCI Architecture

```
┌─────────────────────────────────────────────────────────┐
│         CircleCI Architecture                          │
└─────────────────────────────────────────────────────────┘

GitHub/GitLab Repository
    │
    ├─► .circleci/config.yml
    │
    ▼
CircleCI Platform
    │
    ├─► Parses config
    ├─► Schedules jobs
    └─► Manages execution
         │
         ▼
CircleCI Runners
    ├─► Docker containers
    ├─► Machine executors
    └─► macOS executors
```

## CircleCI Configuration

### Basic Config Structure

```yaml
# .circleci/config.yml
version: 2.1

jobs:
  build:
    docker:
      - image: cimg/openjdk:17.0
    steps:
      - checkout
      - run:
          name: Build
          command: mvn clean package
      - store_artifacts:
          path: target
          destination: artifacts

workflows:
  build-and-test:
    jobs:
      - build
```

## CircleCI Orbs

### What are Orbs?

Orbs are reusable configuration packages that encapsulate common CI/CD patterns, making it easy to integrate tools and services without writing complex configuration.

### Using Orbs

```yaml
version: 2.1

orbs:
  maven: circleci/maven@1.0.0
  docker: circleci/docker@2.0.0
  aws-cli: circleci/aws-cli@2.0.0

jobs:
  build:
    executor: maven/default
    steps:
      - checkout
      - maven/with_cache:
          maven-version: "3.8.6"
          steps:
            - maven/test
            - maven/package
```

### Popular Orbs

```
┌─────────────────────────────────────────────────────────┐
│              Popular CircleCI Orbs                     │
└─────────────────────────────────────────────────────────┘

Language/Platform:
  ├─► circleci/maven@1.0.0
  ├─► circleci/gradle@1.0.0
  ├─► circleci/node@5.0.0
  └─► circleci/python@2.0.0

Cloud Services:
  ├─► circleci/aws-cli@2.0.0
  ├─► circleci/aws-ecr@8.0.0
  ├─► circleci/aws-s3@3.0.0
  └─► circleci/kubernetes@2.0.0

Tools:
  ├─► circleci/docker@2.0.0
  ├─► circleci/slack@4.10.0
  └─► circleci/sonarcloud@1.0.0
```

## CircleCI Workflows

### Workflow Structure

```yaml
workflows:
  build-test-deploy:
    jobs:
      - build
      - test:
          requires:
            - build
      - deploy:
          requires:
            - test
          filters:
            branches:
              only: main
```

### Workflow Patterns

```
┌─────────────────────────────────────────────────────────┐
│         CircleCI Workflow Patterns                     │
└─────────────────────────────────────────────────────────┘

1. Sequential Workflow
   build → test → deploy

2. Parallel Workflow
   build
     ├─► test-unit
     ├─► test-integration
     └─► test-e2e

3. Fan-out/Fan-in
   build
     ├─► deploy-staging
     └─► deploy-production
```

### Advanced Workflow

```yaml
workflows:
  build-and-deploy:
    jobs:
      - build
      - test-unit:
          requires:
            - build
      - test-integration:
          requires:
            - build
      - test-e2e:
          requires:
            - build
      - deploy-staging:
          requires:
            - test-unit
            - test-integration
            - test-e2e
          filters:
            branches:
              only: develop
      - deploy-production:
          requires:
            - deploy-staging
          filters:
            branches:
              only: main
```

## Parallelism

### Parallel Execution

```yaml
jobs:
  test:
    docker:
      - image: cimg/openjdk:17.0
    parallelism: 4
    steps:
      - checkout
      - run:
          name: Run Tests
          command: |
            TESTFILES=$(circleci tests glob "src/test/**/*Test.java" | circleci tests split --split-by=timings)
            mvn test -Dtest="$TESTFILES"
```

### Parallelism Strategies

```
┌─────────────────────────────────────────────────────────┐
│         Parallelism Strategies                         │
└─────────────────────────────────────────────────────────┘

1. Split by Timings
   └─► Distribute based on historical timing

2. Split by Name
   └─► Distribute alphabetically

3. Split by Filesize
   └─► Distribute by file size

4. Split by Count
   └─► Even distribution
```

## CircleCI Executors

### Executor Types

```
┌─────────────────────────────────────────────────────────┐
│              CircleCI Executors                        │
└─────────────────────────────────────────────────────────┘

1. Docker Executor
   └─► Run in Docker containers
       - Fast startup
       - Isolated
       - Resource efficient

2. Machine Executor
   └─► Full VM access
       - More resources
       - Longer startup
       - More control

3. macOS Executor
   └─► macOS environment
       - For iOS/macOS builds
       - Limited availability
```

### Executor Examples

```yaml
# Docker executor
jobs:
  build:
    docker:
      - image: cimg/openjdk:17.0
    steps:
      - checkout
      - run: mvn clean package

# Machine executor
jobs:
  build:
    machine:
      image: ubuntu-2004:202111-01
    steps:
      - checkout
      - run: mvn clean package

# macOS executor
jobs:
  build-ios:
    macos:
      xcode: "13.0.0"
    steps:
      - checkout
      - run: xcodebuild
```

## CircleCI Examples

### Maven Project

```yaml
version: 2.1

orbs:
  maven: circleci/maven@1.0.0

jobs:
  build-and-test:
    executor: maven/default
    steps:
      - checkout
      - maven/with_cache:
          maven-version: "3.8.6"
          steps:
            - maven/test
            - maven/package
      - store_artifacts:
          path: target
          destination: artifacts

workflows:
  build-test:
    jobs:
      - build-and-test
```

### Docker Build and Push

```yaml
version: 2.1

orbs:
  docker: circleci/docker@2.0.0
  aws-ecr: circleci/aws-ecr@8.0.0

jobs:
  build-and-push:
    docker:
      - image: cimg/base:stable
    steps:
      - checkout
      - aws-ecr/ecr_login
      - docker/build:
          image: myapp
          tag: ${CIRCLE_SHA1}
      - docker/push:
          image: myapp
          tag: ${CIRCLE_SHA1}

workflows:
  build-deploy:
    jobs:
      - build-and-push:
          filters:
            branches:
              only: main
```

### Matrix Build

```yaml
version: 2.1

jobs:
  test:
    parameters:
      java-version:
        type: string
    docker:
      - image: cimg/openjdk:<< parameters.java-version >>
    steps:
      - checkout
      - run: mvn test

workflows:
  test-matrix:
    jobs:
      - test:
          matrix:
            parameters:
              java-version: ["11.0", "17.0", "21.0"]
```

## CircleCI Features

### 1. Caching

```yaml
steps:
  - restore_cache:
      keys:
        - maven-{{ checksum "pom.xml" }}
        - maven-
  
  - run: mvn clean package
  
  - save_cache:
      paths:
        - ~/.m2
      key: maven-{{ checksum "pom.xml" }}
```

### 2. Artifacts

```yaml
steps:
  - run: mvn clean package
  - store_artifacts:
      path: target
      destination: artifacts
  - store_test_results:
      path: target/surefire-reports
```

### 3. Environment Variables

```yaml
jobs:
  deploy:
    environment:
      DEPLOY_ENV: production
      API_URL: https://api.example.com
    steps:
      - run: ./deploy.sh
```

### 4. Conditional Execution

```yaml
jobs:
  deploy:
    steps:
      - run:
          name: Deploy
          command: ./deploy.sh
          when: on_success

workflows:
  deploy:
    jobs:
      - deploy:
          filters:
            branches:
              only: main
```

## CircleCI Best Practices

### 1. Use Orbs
```
✅ Reusable configurations
✅ Community-maintained
✅ Easy integration
```

### 2. Leverage Parallelism
```yaml
parallelism: 4
# Split tests across 4 containers
```

### 3. Cache Dependencies
```yaml
- restore_cache:
    keys:
      - maven-{{ checksum "pom.xml" }}
```

### 4. Use Workflows
```yaml
workflows:
  build-test-deploy:
    jobs:
      - build
      - test:
          requires: [build]
```

### 5. Optimize Images
```
Use official CircleCI images:
  - cimg/openjdk:17.0
  - cimg/node:18.0
  - cimg/python:3.10
```

## CircleCI vs Other Tools

| Feature | CircleCI | Jenkins | GitHub Actions |
|---------|----------|---------|----------------|
| **Hosting** | Cloud | Self-hosted | GitHub-hosted |
| **Setup** | Simple | Complex | Simple |
| **Orbs** | Yes | Plugins | Actions |
| **Parallelism** | Excellent | Good | Good |
| **Cost** | Free/Paid | Free | Free/Paid |

## Summary

CircleCI:
- **Type**: Cloud-based CI/CD platform
- **Configuration**: YAML (.circleci/config.yml)
- **Features**: Orbs, workflows, parallelism
- **Strengths**: Fast, scalable, easy to use

**Key Features:**
- Orbs for reusable configurations
- Powerful workflows
- Excellent parallelism
- Cloud-hosted runners
- Easy setup

**Best Practices:**
- Use orbs
- Leverage parallelism
- Cache dependencies
- Use workflows effectively
- Optimize executor selection

**Remember**: CircleCI is great for cloud-native teams wanting fast, scalable CI/CD!

# Azure DevOps: Pipelines, Releases, Artifacts

## Overview

Azure DevOps is a comprehensive DevOps platform from Microsoft that provides version control, CI/CD pipelines, package management, and project management tools. It offers both cloud-hosted and on-premises options, making it suitable for organizations of all sizes.

## Azure DevOps Services

```
┌─────────────────────────────────────────────────────────┐
│         Azure DevOps Services                          │
└─────────────────────────────────────────────────────────┘

Azure DevOps
    │
    ├─► Azure Repos (Git/TFVC)
    ├─► Azure Pipelines (CI/CD)
    ├─► Azure Boards (Project Management)
    ├─► Azure Artifacts (Package Management)
    └─► Azure Test Plans (Testing)
```

## Azure Pipelines

### Pipeline Types

```
┌─────────────────────────────────────────────────────────┐
│              Azure Pipeline Types                     │
└─────────────────────────────────────────────────────────┘

1. Classic Pipelines
   └─► Visual designer
       - GUI-based configuration
       - Easy to use
       - Less flexible

2. YAML Pipelines
   └─► Code-based configuration
       - Version controlled
       - More flexible
       - Recommended approach
```

### YAML Pipeline Structure

```yaml
# azure-pipelines.yml
trigger:
  branches:
    include:
      - main
      - develop

pool:
  vmImage: 'ubuntu-latest'

variables:
  - name: mavenVersion
    value: '3.8.6'
  - name: javaVersion
    value: '17'

stages:
  - stage: Build
    jobs:
      - job: BuildJob
        steps:
          - task: Maven@3
            inputs:
              mavenPomFile: 'pom.xml'
              goals: 'clean compile'
          
          - task: Maven@3
            inputs:
              mavenPomFile: 'pom.xml'
              goals: 'package'
          
          - task: PublishBuildArtifacts@1
            inputs:
              pathToPublish: 'target'
              artifactName: 'drop'
  
  - stage: Test
    dependsOn: Build
    jobs:
      - job: TestJob
        steps:
          - task: Maven@3
            inputs:
              mavenPomFile: 'pom.xml'
              goals: 'test'
          
          - task: PublishTestResults@2
            inputs:
              testResultsFiles: '**/TEST-*.xml'
              testRunTitle: 'Test Results'
  
  - stage: Deploy
    dependsOn: Test
    condition: succeeded()
    jobs:
      - deployment: DeployJob
        environment: 'production'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: AzureWebApp@1
                  inputs:
                    azureSubscription: 'MyAzureSubscription'
                    appName: 'myapp'
                    package: '$(Pipeline.Workspace)/drop/*.jar'
```

## Azure Pipelines Tasks

### Common Tasks

```
┌─────────────────────────────────────────────────────────┐
│              Common Azure Pipeline Tasks               │
└─────────────────────────────────────────────────────────┘

Build Tasks:
  ├─► Maven@3
  ├─► Gradle@2
  ├─► DotNetCoreCLI@2
  └─► Npm@1

Test Tasks:
  ├─► PublishTestResults@2
  ├─► PublishCodeCoverageResults@1
  └─► VisualStudioTestPlatform@1

Deploy Tasks:
  ├─► AzureWebApp@1
  ├─► AzureKubernetesService@1
  ├─► Docker@2
  └─► Kubernetes@1
```

### Task Example

```yaml
steps:
  - task: Maven@3
    displayName: 'Build with Maven'
    inputs:
      mavenPomFile: 'pom.xml'
      goals: 'clean package'
      options: '-DskipTests'
  
  - task: PublishTestResults@2
    displayName: 'Publish Test Results'
    inputs:
      testResultsFiles: '**/TEST-*.xml'
      testRunTitle: 'Unit Tests'
  
  - task: PublishCodeCoverageResults@1
    displayName: 'Publish Coverage'
    inputs:
      codeCoverageTool: 'JaCoCo'
      summaryFileLocation: 'target/site/jacoco/jacoco.xml'
```

## Azure Releases

### Release Pipeline

```
┌─────────────────────────────────────────────────────────┐
│         Azure Release Pipeline Structure               │
└─────────────────────────────────────────────────────────┘

Artifact Source
    │
    ▼
Release Pipeline
    │
    ├─► Stage 1: Development
    │   └─► Deploy to Dev environment
    │
    ├─► Stage 2: Staging
    │   └─► Deploy to Staging (after approval)
    │
    └─► Stage 3: Production
        └─► Deploy to Production (after approval)
```

### Release Pipeline Example

```yaml
# Release pipeline configuration
stages:
  - stage: Development
    jobs:
      - deployment: DeployToDev
        environment: 'development'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: AzureWebApp@1
                  inputs:
                    appName: 'myapp-dev'
  
  - stage: Staging
    dependsOn: Development
    condition: succeeded()
    jobs:
      - deployment: DeployToStaging
        environment: 'staging'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: AzureWebApp@1
                  inputs:
                    appName: 'myapp-staging'
  
  - stage: Production
    dependsOn: Staging
    condition: succeeded()
    jobs:
      - deployment: DeployToProduction
        environment: 'production'
        strategy:
          runOnce:
            deploy:
              steps:
                - task: AzureWebApp@1
                  inputs:
                    appName: 'myapp-prod'
```

## Azure Artifacts

### Artifact Management

```
┌─────────────────────────────────────────────────────────┐
│         Azure Artifacts Features                       │
└─────────────────────────────────────────────────────────┘

Package Types:
  ├─► Maven
  ├─► npm
  ├─► NuGet
  ├─► Python
  └─► Universal Packages

Features:
  ├─► Private feeds
  ├─► Public feeds
  ├─► Upstream sources
  └─► Versioning
```

### Publishing Artifacts

```yaml
steps:
  - task: Maven@3
    inputs:
      mavenPomFile: 'pom.xml'
      goals: 'deploy'
      publishJUnitResults: false
  
  - task: PublishBuildArtifacts@1
    inputs:
      pathToPublish: 'target'
      artifactName: 'drop'
      publishLocation: 'Container'
```

## Azure DevOps Agents

### Agent Types

```
┌─────────────────────────────────────────────────────────┐
│              Azure DevOps Agents                       │
└─────────────────────────────────────────────────────────┘

1. Microsoft-hosted Agents
   └─► Pre-configured VMs
       - Windows, Linux, macOS
       - Updated regularly
       - Limited customization

2. Self-hosted Agents
   └─► Your own machines
       - Full control
       - Custom software
       - Cost-effective for many builds
```

### Agent Pools

```yaml
pool:
  vmImage: 'ubuntu-latest'  # Microsoft-hosted

# or

pool:
  name: 'MySelfHostedPool'  # Self-hosted
  demands:
    - agent.name -equals MyAgent
```

## Azure DevOps Integration

### Azure Services Integration

```yaml
steps:
  - task: AzureWebApp@1
    inputs:
      azureSubscription: 'MyAzureSubscription'
      appName: 'myapp'
      package: '$(Pipeline.Workspace)/drop/*.jar'
  
  - task: AzureKubernetesService@1
    inputs:
      connectionType: 'Azure Resource Manager'
      azureSubscriptionEndpoint: 'MyAzureSubscription'
      azureResourceGroup: 'myResourceGroup'
      kubernetesCluster: 'myCluster'
      namespace: 'default'
      command: 'apply'
      arguments: '-f k8s/'
```

## Azure DevOps Best Practices

### 1. Use YAML Pipelines
```
✅ Version controlled
✅ Code reviewable
✅ Reusable
✅ Maintainable
```

### 2. Use Templates
```yaml
# templates/build.yml
parameters:
  - name: javaVersion
    default: '17'

steps:
  - task: Maven@3
    inputs:
      mavenPomFile: 'pom.xml'
      goals: 'clean package'

# azure-pipelines.yml
resources:
  repositories:
    - repository: templates
      type: git
      name: MyProject/templates

extends:
  template: build.yml@templates
  parameters:
    javaVersion: '17'
```

### 3. Use Variable Groups
```
Store secrets and variables:
  - Library → Variable groups
  - Secure variables
  - Environment-specific values
```

### 4. Use Environments
```
Define environments:
  - Development
  - Staging
  - Production
  
Configure approvals:
  - Manual approvals
  - Automated checks
```

## Summary

Azure DevOps:
- **Type**: Comprehensive DevOps platform
- **Services**: Repos, Pipelines, Boards, Artifacts, Test Plans
- **Pipelines**: YAML and Classic
- **Strengths**: Microsoft ecosystem integration, comprehensive features

**Key Features:**
- Integrated DevOps services
- YAML and Classic pipelines
- Release management
- Artifact management
- Azure service integration

**Best Practices:**
- Use YAML pipelines
- Leverage templates
- Use variable groups
- Define environments
- Integrate with Azure services

**Remember**: Azure DevOps is excellent for Microsoft-centric organizations and Azure cloud deployments!

# Part 31: DevOps CI/CD - Quick Revision

## Continuous Integration (CI)

- **Purpose**: Automatically build and test code on every commit
- **Benefits**: Early bug detection, faster feedback, integration testing
- **Pipeline**: Source → Build → Test → Artifact
- **Tools**: Jenkins, GitLab CI, GitHub Actions, CircleCI

## Continuous Deployment (CD)

- **Purpose**: Automatically deploy to production after CI passes
- **Stages**: Dev → Staging → Production
- **Approval Gates**: Manual approval for production
- **Rollback**: Quick reversion to previous version

## Pipeline Design

- **Stages**: Build, Test, Deploy, Verify
- **Parallel Execution**: Run tests in parallel for speed
- **Artifact Management**: Store build artifacts, version control
- **Environment Management**: Separate configs for dev/staging/prod

## Deployment Strategies

- **Blue-Green**: Two identical environments, switch traffic
- **Canary**: Deploy to small subset, gradually increase
- **Rolling**: Deploy to servers one by one, zero downtime
- **Feature Flags**: Toggle features without deployment

## Infrastructure as Code

- **Purpose**: Manage infrastructure as code (version controlled)
- **Tools**: Terraform, CloudFormation, Ansible
- **Benefits**: Reproducible, versioned, automated

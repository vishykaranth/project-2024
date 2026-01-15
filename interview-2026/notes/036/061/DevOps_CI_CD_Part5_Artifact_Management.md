# Artifact Management: Nexus, Artifactory, Versioning

## Overview

Artifact Management involves storing, versioning, and distributing build artifacts (JARs, WARs, Docker images, npm packages) in centralized repositories. It ensures consistent artifact availability, version control, and efficient distribution across teams and environments.

## Artifact Management Concepts

```
┌─────────────────────────────────────────────────────────┐
│         Artifact Management Ecosystem                  │
└─────────────────────────────────────────────────────────┘

Build Process
    │
    ▼
Generate Artifact
    │
    ▼
┌─────────────────┐
│ Artifact Repository│  ← Store artifacts
│ (Nexus/Artifactory)│
└────────┬─────────┘
         │
         ▼
Version Management
    │
    ▼
Distribution
    ├─► CI/CD Pipelines
    ├─► Development Teams
    └─► Production Deployments
```

## Artifact Types

```
┌─────────────────────────────────────────────────────────┐
│              Artifact Types                            │
└─────────────────────────────────────────────────────────┘

Java:
  ├─► JAR (Java Archive)
  ├─► WAR (Web Archive)
  └─► EAR (Enterprise Archive)

JavaScript:
  ├─► npm packages
  └─► yarn packages

Containers:
  ├─► Docker images
  └─► OCI images

Other:
  ├─► Python wheels
  ├─► .NET NuGet packages
  └─► Generic binaries
```

## Artifact Repositories

### Repository Types

```
┌─────────────────────────────────────────────────────────┐
│              Repository Types                           │
└─────────────────────────────────────────────────────────┘

1. Local Repository
   └─► Private, internal artifacts
       Example: Company's internal libraries

2. Remote Repository (Proxy)
   └─► Cache of external repositories
       Example: Maven Central, npm registry

3. Hosted Repository
   └─► Store internally built artifacts
       Example: Your application builds

4. Group Repository
   └─► Aggregates multiple repositories
       Example: Single URL for all repos
```

### Repository Layout

```
┌─────────────────────────────────────────────────────────┐
│         Maven Repository Layout                        │
└─────────────────────────────────────────────────────────┘

Repository Root/
└── com/
    └── example/
        └── myapp/
            ├── 1.0.0/
            │   ├── myapp-1.0.0.jar
            │   ├── myapp-1.0.0.pom
            │   └── myapp-1.0.0-sources.jar
            ├── 1.1.0/
            │   └── ...
            └── maven-metadata.xml
```

## 1. Sonatype Nexus

### Nexus Overview

Nexus Repository Manager is a repository manager that supports multiple artifact formats (Maven, npm, Docker, etc.) and provides centralized artifact storage and distribution.

### Nexus Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Nexus Architecture                        │
└─────────────────────────────────────────────────────────┘

Clients (Maven, npm, Docker)
    │
    ▼
└─────────────────┐
│ Nexus Server     │
│                  │
│  ┌───────────┐  │
│  │ Repositories│ │
│  │ - Maven    │  │
│  │ - npm      │  │
│  │ - Docker   │  │
│  └───────────┘  │
│                  │
│  ┌───────────┐  │
│  │ Storage    │  │
│  │ (Blob Store)│ │
│  └───────────┘  │
└─────────────────┘
```

### Nexus Repository Types

```
┌─────────────────────────────────────────────────────────┐
│         Nexus Repository Configuration                 │
└─────────────────────────────────────────────────────────┘

1. Maven (hosted)
   └─► Store internal Maven artifacts

2. Maven (proxy)
   └─► Proxy Maven Central, JCenter

3. Maven (group)
   └─► Combine multiple Maven repositories

4. npm (hosted)
   └─► Store internal npm packages

5. Docker (hosted)
   └─► Store Docker images

6. Raw (hosted)
   └─► Store generic files
```

### Nexus Setup Example

```xml
<!-- Maven settings.xml -->
<settings>
    <servers>
        <server>
            <id>nexus-releases</id>
            <username>admin</username>
            <password>password</password>
        </server>
    </servers>
    
    <mirrors>
        <mirror>
            <id>nexus</id>
            <mirrorOf>*</mirrorOf>
            <url>http://nexus.example.com/repository/maven-public/</url>
        </mirror>
    </mirrors>
</settings>
```

## 2. JFrog Artifactory

### Artifactory Overview

Artifactory is a universal artifact repository manager that supports all major package formats and provides advanced features like build integration, security scanning, and distribution.

### Artifactory Features

```
┌─────────────────────────────────────────────────────────┐
│              Artifactory Features                      │
└─────────────────────────────────────────────────────────┘

├─ Universal Repository Support
│  └─► Maven, npm, Docker, NuGet, Python, etc.
│
├─ Build Integration
│  └─► Jenkins, TeamCity, Bamboo integration
│
├─ Security Scanning
│  └─► Xray for vulnerability scanning
│
├─ Distribution
│  └─► CDN, edge nodes for global distribution
│
└─ High Availability
   └─► Clustering, replication
```

### Artifactory Repository Types

```
┌─────────────────────────────────────────────────────────┐
│         Artifactory Repository Types                   │
└─────────────────────────────────────────────────────────┘

Local Repository:
  └─► Store internally built artifacts

Remote Repository:
  └─► Proxy external repositories

Virtual Repository:
  └─► Aggregate multiple repositories (like Group)

Distribution Repository:
  └─► For artifact distribution via CDN
```

## Versioning Strategies

### Semantic Versioning

```
┌─────────────────────────────────────────────────────────┐
│              Semantic Versioning                        │
└─────────────────────────────────────────────────────────┘

Format: MAJOR.MINOR.PATCH

MAJOR: Breaking changes
  Example: 1.0.0 → 2.0.0

MINOR: New features (backward compatible)
  Example: 1.0.0 → 1.1.0

PATCH: Bug fixes (backward compatible)
  Example: 1.0.0 → 1.0.1

Pre-release versions:
  1.0.0-alpha.1
  1.0.0-beta.1
  1.0.0-rc.1
```

### Versioning in CI/CD

```
┌─────────────────────────────────────────────────────────┐
│         Automated Versioning in CI/CD                   │
└─────────────────────────────────────────────────────────┘

Git Tag Based:
  - Extract version from Git tag
  - Example: v1.2.3 → 1.2.3

Build Number:
  - Use CI build number
  - Example: 1.0.0-123

Commit SHA:
  - Include commit hash
  - Example: 1.0.0-abc1234

Timestamp:
  - Use build timestamp
  - Example: 1.0.0-20231201120000
```

### Version Management Best Practices

1. **Use Semantic Versioning**
   ```
   Clear version meaning:
   - MAJOR: Breaking changes
   - MINOR: New features
   - PATCH: Bug fixes
   ```

2. **Automate Versioning**
   ```
   - Extract from Git tags
   - Use CI build numbers
   - Avoid manual versioning
   ```

3. **Version Immutability**
   ```
   - Never overwrite published versions
   - Use new version for each build
   - Enable version history
   ```

4. **Snapshot vs Release**
   ```
   Snapshots: 1.0.0-SNAPSHOT (development)
   Releases: 1.0.0 (production)
   ```

## Artifact Lifecycle

```
┌─────────────────────────────────────────────────────────┐
│              Artifact Lifecycle                        │
└─────────────────────────────────────────────────────────┘

1. Build
   └─► Create artifact (JAR, WAR, etc.)

2. Test
   └─► Verify artifact works

3. Version
   └─► Assign version number

4. Publish
   └─► Upload to repository

5. Promote
   └─► Move through environments
       (dev → staging → production)

6. Archive
   └─► Move old versions to archive

7. Delete
   └─► Remove obsolete versions
```

## Artifact Promotion

```
┌─────────────────────────────────────────────────────────┐
│         Artifact Promotion Workflow                    │
└─────────────────────────────────────────────────────────┘

Build Artifact: myapp-1.2.3.jar
    │
    ▼
┌─────────────────┐
│ Dev Repository  │  ← Initial publish
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Staging Repo     │  ← Promote after testing
└────────┬────────┘
         │
         ▼
┌─────────────────┐
│ Production Repo  │  ← Promote after approval
└─────────────────┘
```

## Integration with CI/CD

### Maven Deployment

```xml
<!-- pom.xml -->
<distributionManagement>
    <repository>
        <id>nexus-releases</id>
        <name>Release Repository</name>
        <url>http://nexus.example.com/repository/maven-releases/</url>
    </repository>
    <snapshotRepository>
        <id>nexus-snapshots</id>
        <name>Snapshot Repository</name>
        <url>http://nexus.example.com/repository/maven-snapshots/</url>
    </snapshotRepository>
</distributionManagement>
```

```bash
# Deploy to repository
mvn clean deploy
```

### Gradle Deployment

```groovy
// build.gradle
uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: "http://nexus.example.com/repository/maven-releases/") {
                authentication(userName: "admin", password: "password")
            }
            snapshotRepository(url: "http://nexus.example.com/repository/maven-snapshots/") {
                authentication(userName: "admin", password: "password")
            }
        }
    }
}
```

### Docker Image Management

```bash
# Tag image
docker tag myapp:latest nexus.example.com:5000/myapp:1.2.3

# Push to repository
docker push nexus.example.com:5000/myapp:1.2.3

# Pull from repository
docker pull nexus.example.com:5000/myapp:1.2.3
```

## Artifact Security

### Security Best Practices

1. **Access Control**
   ```
   - Role-based access control
   - Separate read/write permissions
   - Secure authentication
   ```

2. **Vulnerability Scanning**
   ```
   - Scan artifacts for vulnerabilities
   - Block vulnerable artifacts
   - Regular security audits
   ```

3. **Encryption**
   ```
   - Encrypt artifacts in transit (HTTPS)
   - Encrypt artifacts at rest
   - Secure credentials
   ```

4. **Audit Logging**
   ```
   - Log all artifact operations
   - Track who accessed what
   - Monitor for suspicious activity
   ```

## Artifact Cleanup

### Retention Policies

```
┌─────────────────────────────────────────────────────────┐
│         Artifact Retention Strategy                    │
└─────────────────────────────────────────────────────────┘

Snapshots:
  - Keep: Last 10 versions
  - Delete: Older than 30 days

Releases:
  - Keep: All versions
  - Archive: Versions older than 1 year

Development:
  - Keep: Last 5 versions
  - Delete: Older than 7 days
```

## Summary

Artifact Management:
- **Purpose**: Centralized storage and distribution of build artifacts
- **Tools**: Nexus, Artifactory, Docker Registry
- **Versioning**: Semantic versioning, automated versioning
- **Benefits**: Consistency, security, efficiency

**Key Components:**
- Repository management
- Version control
- Artifact promotion
- Security scanning
- Retention policies

**Best Practices:**
- Use semantic versioning
- Automate versioning
- Implement access control
- Regular security scanning
- Define retention policies

**Remember**: Artifact management is crucial for reliable, reproducible deployments!

# Jenkins: Pipeline as Code, Plugins, Distributed Builds

## Overview

Jenkins is an open-source automation server that enables continuous integration and continuous delivery. It's highly extensible through plugins and supports pipeline-as-code, distributed builds, and integration with various tools and technologies.

## Jenkins Architecture

```
┌─────────────────────────────────────────────────────────┐
│              Jenkins Architecture                      │
└─────────────────────────────────────────────────────────┘

Jenkins Master
    │
    ├─► Manages builds
    ├─► Schedules jobs
    ├─► Stores configuration
    └─► Serves web UI
         │
         ├─► Jenkins Agent 1 (Linux)
         ├─► Jenkins Agent 2 (Windows)
         └─► Jenkins Agent 3 (macOS)
```

## Jenkins Pipeline Types

### 1. Declarative Pipeline

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }
        
        stage('Deploy') {
            steps {
                sh './deploy.sh'
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline succeeded!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}
```

### 2. Scripted Pipeline

```groovy
node {
    stage('Build') {
        sh 'mvn clean package'
    }
    
    stage('Test') {
        sh 'mvn test'
    }
    
    stage('Deploy') {
        sh './deploy.sh'
    }
}
```

## Pipeline as Code

### Jenkinsfile

```
┌─────────────────────────────────────────────────────────┐
│              Pipeline as Code Benefits                 │
└─────────────────────────────────────────────────────────┘

1. Version Control
   └─► Pipeline stored in Git

2. Code Review
   └─► Review pipeline changes

3. Reproducibility
   └─► Same pipeline everywhere

4. Collaboration
   └─► Team can modify pipelines

5. Audit Trail
   └─► Track pipeline changes
```

### Jenkinsfile Example

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8.6'
        jdk 'JDK-17'
    }
    
    environment {
        NEXUS_URL = 'http://nexus.example.com'
        ARTIFACT_ID = 'myapp'
        VERSION = "${env.BUILD_NUMBER}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'mvn test'
                    }
                }
                stage('Integration Tests') {
                    steps {
                        sh 'mvn verify'
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package'
            }
        }
        
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                sh './deploy.sh'
            }
        }
    }
    
    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts 'target/*.jar'
        }
        success {
            emailext (
                subject: "Build Success: ${env.JOB_NAME}",
                body: "Build ${env.BUILD_NUMBER} succeeded.",
                to: "${env.CHANGE_AUTHOR_EMAIL}"
            )
        }
        failure {
            emailext (
                subject: "Build Failed: ${env.JOB_NAME}",
                body: "Build ${env.BUILD_NUMBER} failed.",
                to: "${env.CHANGE_AUTHOR_EMAIL}"
            )
        }
    }
}
```

## Jenkins Plugins

### Essential Plugins

```
┌─────────────────────────────────────────────────────────┐
│              Essential Jenkins Plugins                 │
└─────────────────────────────────────────────────────────┘

Build Tools:
  ├─► Maven Integration
  ├─► Gradle Plugin
  └─► NodeJS Plugin

Version Control:
  ├─► Git Plugin
  ├─► GitHub Plugin
  └─► GitLab Plugin

CI/CD:
  ├─► Pipeline Plugin
  ├─► Blue Ocean (UI)
  └─► Build Pipeline Plugin

Quality:
  ├─► SonarQube Scanner
  ├─► JaCoCo Plugin
  └─► Checkstyle Plugin

Deployment:
  ├─► Kubernetes Plugin
  ├─► Docker Plugin
  └─► SSH Plugin

Notifications:
  ├─► Email Extension
  ├─► Slack Notification
  └─► Teams Notification
```

### Plugin Installation

```
┌─────────────────────────────────────────────────────────┐
│         Plugin Installation Process                    │
└─────────────────────────────────────────────────────────┘

1. Jenkins Dashboard
   └─► Manage Jenkins

2. Manage Plugins
   └─► Available plugins

3. Search Plugin
   └─► Enter plugin name

4. Install
   └─► Select and install

5. Restart
   └─► Restart Jenkins
```

## Distributed Builds

### Master-Agent Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Jenkins Distributed Build Architecture         │
└─────────────────────────────────────────────────────────┘

Jenkins Master
    │
    ├─► Manages jobs
    ├─► Schedules builds
    └─► Distributes work
         │
         ├─► Agent 1 (Linux)
         │   ├─► Java builds
         │   └─► Docker builds
         │
         ├─► Agent 2 (Windows)
         │   ├─► .NET builds
         │   └─► Windows tests
         │
         └─► Agent 3 (macOS)
             ├─► iOS builds
             └─► Xcode tests
```

### Agent Types

```
┌─────────────────────────────────────────────────────────┐
│              Jenkins Agent Types                       │
└─────────────────────────────────────────────────────────┘

1. Permanent Agent
   └─► Always-on agent
       - Dedicated server
       - Persistent connection

2. Cloud Agent
   └─► Dynamic agent
       - AWS EC2
       - Azure VM
       - Kubernetes pods

3. Docker Agent
   └─► Container-based
       - Ephemeral containers
       - Isolated environments
```

### Agent Configuration

```groovy
pipeline {
    agent {
        label 'linux && java'
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
    }
}
```

## Jenkins Best Practices

### 1. Use Pipeline as Code
```
✅ Store Jenkinsfile in Git
✅ Version control pipelines
✅ Review pipeline changes
```

### 2. Use Shared Libraries
```groovy
// vars/buildApp.groovy
def call(Map config) {
    sh "mvn clean package -Dversion=${config.version}"
}

// Jenkinsfile
@Library('shared-libs') _
pipeline {
    stages {
        stage('Build') {
            steps {
                buildApp(version: '1.0.0')
            }
        }
    }
}
```

### 3. Secure Credentials
```
✅ Use Jenkins Credentials
✅ Never hardcode secrets
✅ Use credential binding
```

### 4. Optimize Builds
```
✅ Parallel execution
✅ Caching dependencies
✅ Incremental builds
✅ Agent selection
```

## Jenkins Integration Examples

### GitHub Integration

```groovy
pipeline {
    agent any
    
    triggers {
        githubPush()
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
    }
}
```

### Docker Integration

```groovy
pipeline {
    agent any
    
    stages {
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t myapp:${BUILD_NUMBER} .'
            }
        }
        
        stage('Push to Registry') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'docker-registry',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh 'docker login -u $DOCKER_USER -p $DOCKER_PASS'
                    sh 'docker push myapp:${BUILD_NUMBER}'
                }
            }
        }
    }
}
```

### Kubernetes Integration

```groovy
pipeline {
    agent any
    
    stages {
        stage('Deploy to Kubernetes') {
            steps {
                sh '''
                    kubectl set image deployment/myapp \
                      myapp=myapp:${BUILD_NUMBER} \
                      -n production
                '''
            }
        }
    }
}
```

## Jenkins Blue Ocean

### Blue Ocean Features

```
┌─────────────────────────────────────────────────────────┐
│              Blue Ocean UI Features                    │
└─────────────────────────────────────────────────────────┘

├─ Visual Pipeline Editor
│  └─► Drag-and-drop pipeline creation
│
├─ Pipeline Visualization
│  └─► Real-time pipeline status
│
├─ Branch and Pull Request Support
│  └─► GitHub/GitLab integration
│
└─ Pipeline Run Details
   └─► Detailed execution logs
```

## Jenkins Security

### Security Best Practices

1. **Authentication**
   ```
   - Enable security
   - Use LDAP/Active Directory
   - Implement SSO
   ```

2. **Authorization**
   ```
   - Role-based access control
   - Project-based permissions
   - Matrix-based security
   ```

3. **Credentials Management**
   ```
   - Use Jenkins Credentials
   - Encrypt credentials
   - Rotate credentials regularly
   ```

4. **Plugin Security**
   ```
   - Keep plugins updated
   - Review plugin permissions
   - Use trusted plugins only
   ```

## Summary

Jenkins:
- **Type**: Self-hosted CI/CD server
- **Strengths**: Flexibility, extensibility, large plugin ecosystem
- **Pipeline**: Declarative and scripted pipelines
- **Architecture**: Master-agent distributed builds

**Key Features:**
- Pipeline as code (Jenkinsfile)
- Extensive plugin ecosystem
- Distributed builds
- Integration with many tools
- Blue Ocean UI

**Best Practices:**
- Use pipeline as code
- Leverage shared libraries
- Secure credentials
- Optimize builds
- Use distributed agents

**Remember**: Jenkins is powerful but requires maintenance. Consider cloud alternatives for simpler setups!

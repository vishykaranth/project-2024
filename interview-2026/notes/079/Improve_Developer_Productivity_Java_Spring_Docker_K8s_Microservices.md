# How to Improve Developer Productivity: Java, Spring, Docker, Kubernetes & Microservices

## Overview

This comprehensive guide provides actionable strategies, tools, and best practices to significantly improve productivity for developers working with Java, Spring Boot, Docker, Kubernetes, and Microservices architecture.

---

## Table of Contents

1. [Development Environment Setup](#development-environment-setup)
2. [IDE Optimization](#ide-optimization)
3. [Build & Dependency Management](#build--dependency-management)
4. [Local Development with Docker](#local-development-with-docker)
5. [Kubernetes Development Workflow](#kubernetes-development-workflow)
6. [Microservices Best Practices](#microservices-best-practices)
7. [Testing Strategies](#testing-strategies)
8. [Debugging & Troubleshooting](#debugging--troubleshooting)
9. [CI/CD Optimization](#cicd-optimization)
10. [Code Quality & Productivity Tools](#code-quality--productivity-tools)
11. [API Development & Documentation](#api-development--documentation)
12. [Database & Data Management](#database--data-management)
13. [Performance Optimization](#performance-optimization)
14. [Learning & Skill Development](#learning--skill-development)
15. [Time Management & Workflow](#time-management--workflow)

---

## Development Environment Setup

### 1. Java Version Management

**Use SDKMAN! for Java Version Management**

```bash
# Install SDKMAN!
curl -s "https://get.sdkman.io" | bash

# Install multiple Java versions
sdk install java 17.0.8-tem
sdk install java 11.0.20-tem
sdk install java 21.0.1-tem

# Switch between versions
sdk use java 17.0.8-tem

# Set default
sdk default java 17.0.8-tem
```

**Benefits:**
- Easy version switching
- Multiple projects, different Java versions
- No manual PATH management

### 2. Maven/Gradle Optimization

**Maven Settings Optimization**

```xml
<!-- ~/.m2/settings.xml -->
<settings>
  <localRepository>${user.home}/.m2/repository</localRepository>
  
  <!-- Use mirror for faster downloads -->
  <mirrors>
    <mirror>
      <id>central-mirror</id>
      <mirrorOf>central</mirrorOf>
      <url>https://repo1.maven.org/maven2</url>
    </mirror>
  </mirrors>
  
  <!-- Parallel builds -->
  <profiles>
    <profile>
      <id>parallel-build</id>
      <properties>
        <maven.compiler.fork>true</maven.compiler.fork>
        <maven.compiler.executable>javac</maven.compiler.executable>
      </properties>
    </profile>
  </profiles>
</settings>
```

**Gradle Optimization**

```gradle
// ~/.gradle/gradle.properties
org.gradle.daemon=true
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configureondemand=true
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=512m
```

**Build Performance Tips:**
- Use build cache
- Enable parallel builds
- Use Gradle daemon
- Configure JVM memory appropriately
- Use dependency caching

### 3. Environment Variables & Configuration

**Use .env files for local development**

```bash
# .env
SPRING_PROFILES_ACTIVE=local
DATABASE_URL=jdbc:postgresql://localhost:5432/mydb
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

**Load with direnv or IDE plugins**

### 4. Shell & Terminal Setup

**Use Modern Shell Tools:**

```bash
# Oh My Zsh with useful plugins
plugins=(
  git
  docker
  kubectl
  maven
  gradle
  spring
)

# Useful aliases
alias k='kubectl'
alias d='docker'
alias dc='docker-compose'
alias mvnw='./mvnw'
alias gw='./gradlew'
```

---

## IDE Optimization

### 1. IntelliJ IDEA Configuration

**Memory Settings:**

```properties
# Help → Edit Custom VM Options
-Xms2g
-Xmx4g
-XX:ReservedCodeCacheSize=512m
-XX:+UseG1GC
```

**Essential Plugins:**
- **Lombok** - Reduces boilerplate
- **MapStruct** - Type-safe mappers
- **Spring Assistant** - Spring Boot support
- **Docker** - Docker integration
- **Kubernetes** - K8s support
- **Rainbow Brackets** - Better code readability
- **String Manipulation** - String utilities
- **GitToolBox** - Enhanced Git features
- **SonarLint** - Code quality
- **PlantUML** - Diagram generation

**Code Templates & Live Templates:**

```java
// Custom live template: serv
@Service
public class $CLASS_NAME$ {
    $END$
}

// Custom live template: restc
@RestController
@RequestMapping("/api/$PATH$")
public class $CLASS_NAME$ {
    $END$
}
```

**Key Shortcuts:**
- `Ctrl+Shift+A` - Find action
- `Ctrl+E` - Recent files
- `Ctrl+B` - Go to declaration
- `Ctrl+Alt+B` - Go to implementation
- `Alt+Enter` - Quick fixes
- `Ctrl+Alt+L` - Reformat code
- `Ctrl+Shift+F10` - Run current class
- `Ctrl+F9` - Build project
- `Double Shift` - Search everywhere

### 2. VS Code Setup (Alternative)

**Essential Extensions:**
- **Extension Pack for Java** - Microsoft
- **Spring Boot Extension Pack** - VMware
- **Docker** - Microsoft
- **Kubernetes** - Microsoft
- **GitLens** - Git supercharged
- **Thunder Client** - API testing

### 3. IDE Productivity Tips

**Multi-Cursor Editing:**
- `Alt+Click` - Add cursor
- `Ctrl+Alt+Shift+Click` - Column selection
- `Alt+J` - Select next occurrence

**Code Navigation:**
- `Ctrl+N` - Go to class
- `Ctrl+Shift+N` - Go to file
- `Ctrl+Alt+Shift+N` - Go to symbol
- `Ctrl+Shift+F` - Find in files
- `Ctrl+H` - Replace in files

**Refactoring:**
- `Shift+F6` - Rename
- `Ctrl+Alt+M` - Extract method
- `Ctrl+Alt+V` - Extract variable
- `Ctrl+Alt+F` - Extract field
- `Ctrl+Alt+P` - Extract parameter

---

## Build & Dependency Management

### 1. Maven Optimization

**Multi-Module Project Structure:**

```xml
<!-- Parent POM -->
<project>
  <modules>
    <module>api</module>
    <module>service</module>
    <module>common</module>
  </modules>
  
  <properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <spring-boot.version>3.1.0</spring-boot.version>
  </properties>
  
  <dependencyManagement>
    <dependencies>
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring-boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>
</project>
```

**Build Profiles:**

```xml
<profiles>
  <profile>
    <id>dev</id>
    <properties>
      <spring.profiles.active>dev</spring.profiles.active>
    </properties>
  </profile>
  <profile>
    <id>prod</id>
    <properties>
      <spring.profiles.active>prod</spring.profiles.active>
    </properties>
  </profile>
</profiles>
```

**Maven Wrapper:**

```bash
# Use Maven Wrapper for consistent builds
mvn wrapper:wrapper

# Build with wrapper
./mvnw clean install
```

### 2. Gradle Optimization

**Build Script Optimization:**

```gradle
// build.gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.1.0'
    id 'io.spring.dependency-management' version '1.1.0'
}

java {
    sourceCompatibility = '17'
    targetCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

**Gradle Wrapper:**

```bash
# Use Gradle Wrapper
./gradlew build

# Update wrapper
./gradlew wrapper --gradle-version=8.3
```

### 3. Dependency Management

**Version Management:**

```xml
<!-- Use BOM for version management -->
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-dependencies</artifactId>
      <version>2022.0.3</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

**Dependency Analysis:**

```bash
# Maven
mvn dependency:tree
mvn dependency:analyze

# Gradle
./gradlew dependencies
./gradlew dependencyInsight --dependency <dependency>
```

---

## Local Development with Docker

### 1. Docker Compose for Local Stack

**docker-compose.yml:**

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  kafka:
    image: confluentinc/cp-kafka:latest
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.8.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"

volumes:
  postgres_data:
```

**Usage:**

```bash
# Start all services
docker-compose up -d

# Stop all services
docker-compose down

# View logs
docker-compose logs -f

# Rebuild specific service
docker-compose up -d --build postgres
```

### 2. Dockerfile Optimization

**Multi-Stage Build:**

```dockerfile
# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Build Optimization:**
- Use .dockerignore
- Leverage build cache
- Multi-stage builds
- Use specific tags, not latest

**.dockerignore:**

```
target/
.git/
.idea/
*.iml
.env
README.md
```

### 3. Development Containers

**Use Docker for Development:**

```dockerfile
# Dockerfile.dev
FROM maven:3.9-eclipse-temurin-17
WORKDIR /app
# Install additional tools
RUN apt-get update && apt-get install -y \
    vim \
    curl \
    && rm -rf /var/lib/apt/lists/*
```

**Docker Compose for Dev:**

```yaml
services:
  app:
    build:
      context: .
      dockerfile: Dockerfile.dev
    volumes:
      - .:/app
      - maven-cache:/root/.m2
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    command: mvn spring-boot:run
```

---

## Kubernetes Development Workflow

### 1. Local Kubernetes Setup

**Options:**
- **Minikube** - Local K8s cluster
- **Kind** - Kubernetes in Docker
- **Docker Desktop** - Built-in K8s
- **k3d** - Lightweight K8s

**Kind Setup:**

```bash
# Install kind
brew install kind  # macOS
# or
curl -Lo ./kind https://kind.sigs.k8s.io/dl/v0.20.0/kind-linux-amd64
chmod +x ./kind
sudo mv ./kind /usr/local/bin/kind

# Create cluster
kind create cluster --name dev

# Switch context
kubectl cluster-info --context kind-dev
```

### 2. Development Manifests

**Deployment Template:**

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: user-service
spec:
  replicas: 1
  selector:
    matchLabels:
      app: user-service
  template:
    metadata:
      labels:
        app: user-service
    spec:
      containers:
      - name: user-service
        image: user-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "k8s"
        - name: DATABASE_URL
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: url
---
apiVersion: v1
kind: Service
metadata:
  name: user-service
spec:
  selector:
    app: user-service
  ports:
  - port: 80
    targetPort: 8080
  type: ClusterIP
```

### 3. kubectl Productivity

**Useful Aliases:**

```bash
alias k='kubectl'
alias kgp='kubectl get pods'
alias kgs='kubectl get services'
alias kgd='kubectl get deployments'
alias kdp='kubectl describe pod'
alias klf='kubectl logs -f'
alias ke='kubectl exec -it'
```

**kubectl Plugins:**

```bash
# Install krew (kubectl plugin manager)
(
  set -x; cd "$(mktemp -d)" &&
  OS="$(uname | tr '[:upper:]' '[:lower:]')" &&
  ARCH="$(uname -m | sed -e 's/x86_64/amd64/' -e 's/\(arm\)\(64\)\?.*/\1\2/' -e 's/aarch64$/arm64/')" &&
  KREW="krew-${OS}_${ARCH}" &&
  curl -fsSLO "https://github.com/kubernetes-sigs/krew/releases/latest/download/${KREW}.tar.gz" &&
  tar zxvf "${KREW}.tar.gz" &&
  ./"${KREW}" install krew
)

# Install useful plugins
kubectl krew install ctx
kubectl krew install ns
kubectl krew install get-all
```

**Context Switching:**

```bash
# List contexts
kubectl config get-contexts

# Switch context
kubectl config use-context dev-cluster

# Use kubectx (plugin)
kubectl ctx dev-cluster
```

### 4. Development Tools

**Skaffold - Continuous Development:**

```yaml
# skaffold.yaml
apiVersion: skaffold/v4beta1
kind: Config
metadata:
  name: microservices
build:
  artifacts:
  - image: user-service
    docker:
      dockerfile: Dockerfile
deploy:
  kubectl:
    manifests:
    - k8s/*.yaml
portForward:
- resourceType: service
  resourceName: user-service
  port: 8080
  localPort: 8080
```

**Usage:**

```bash
# Continuous development
skaffold dev

# Build and deploy once
skaffold run
```

**Tilt - Fast Development:**

```python
# Tiltfile
docker_build('user-service', '.')
k8s_yaml('k8s/deployment.yaml')
k8s_resource('user-service', port_forwards=8080)
```

**Telepresence - Local Development:**

```bash
# Install
brew install datawire/blackbird/telepresence

# Intercept service
telepresence intercept user-service --port 8080:8080
```

---

## Microservices Best Practices

### 1. Service Communication

**Synchronous - RestTemplate/WebClient:**

```java
@Service
public class UserServiceClient {
    
    private final WebClient webClient;
    
    public UserServiceClient(WebClient.Builder builder) {
        this.webClient = builder
            .baseUrl("http://user-service")
            .build();
    }
    
    public Mono<User> getUser(Long id) {
        return webClient.get()
            .uri("/api/users/{id}", id)
            .retrieve()
            .bodyToMono(User.class)
            .timeout(Duration.ofSeconds(5))
            .retry(3);
    }
}
```

**Asynchronous - Kafka/RabbitMQ:**

```java
@Component
public class OrderEventPublisher {
    
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public void publishOrderCreated(Order order) {
        OrderEvent event = new OrderEvent(order);
        kafkaTemplate.send("order-events", event);
    }
}

@Component
public class OrderEventListener {
    
    @KafkaListener(topics = "order-events")
    public void handleOrderEvent(OrderEvent event) {
        // Process event
    }
}
```

### 2. Service Discovery

**Spring Cloud Eureka:**

```java
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}

// Client
@SpringBootApplication
@EnableEurekaClient
public class UserServiceApplication {
    // ...
}
```

**Kubernetes Service Discovery:**

```java
@Configuration
public class ServiceDiscoveryConfig {
    
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
            .baseUrl("http://user-service"); // K8s service name
    }
}
```

### 3. Configuration Management

**Spring Cloud Config:**

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    // ...
}

// Client
@SpringBootApplication
@EnableConfigClient
public class UserServiceApplication {
    // ...
}
```

**Kubernetes ConfigMaps and Secrets:**

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
data:
  application.yml: |
    spring:
      datasource:
        url: jdbc:postgresql://postgres:5432/mydb
---
apiVersion: v1
kind: Secret
metadata:
  name: db-secret
type: Opaque
data:
  username: dXNlcg==
  password: cGFzc3dvcmQ=
```

### 4. API Gateway

**Spring Cloud Gateway:**

```java
@SpringBootApplication
public class GatewayApplication {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/users/**")
                .uri("lb://user-service"))
            .route("order-service", r -> r
                .path("/api/orders/**")
                .uri("lb://order-service"))
            .build();
    }
}
```

### 5. Distributed Tracing

**Spring Cloud Sleuth + Zipkin:**

```java
// Add dependency
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-sleuth'
    implementation 'org.springframework.cloud:spring-cloud-sleuth-zipkin'
}

// Configuration
spring:
  zipkin:
    base-url: http://zipkin:9411
  sleuth:
    sampler:
      probability: 1.0
```

---

## Testing Strategies

### 1. Unit Testing

**JUnit 5 + Mockito:**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserService userService;
    
    @Test
    void shouldCreateUser() {
        // Given
        User user = new User("John", "john@example.com");
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        User created = userService.createUser(user);
        
        // Then
        assertThat(created).isNotNull();
        verify(userRepository).save(user);
    }
}
```

### 2. Integration Testing

**Spring Boot Test:**

```java
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldCreateUser() throws Exception {
        // Given
        String userJson = """
            {
                "name": "John",
                "email": "john@example.com"
            }
            """;
        
        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("John"));
    }
}
```

### 3. Test Containers**

**Database Testing:**

```java
@SpringBootTest
@Testcontainers
class UserRepositoryTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private UserRepository userRepository;
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
    
    @Test
    void shouldSaveUser() {
        User user = new User("John", "john@example.com");
        User saved = userRepository.save(user);
        assertThat(saved.getId()).isNotNull();
    }
}
```

### 4. Contract Testing

**Spring Cloud Contract:**

```groovy
// Contract definition
Contract.make {
    request {
        method 'GET'
        url '/api/users/1'
    }
    response {
        status 200
        body([
            id: 1,
            name: "John",
            email: "john@example.com"
        ])
        headers {
            contentType(applicationJson())
        }
    }
}
```

---

## Debugging & Troubleshooting

### 1. Remote Debugging

**IntelliJ IDEA Setup:**

```bash
# Start application with debug
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar app.jar

# Or with Maven
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

**IntelliJ Configuration:**
- Run → Edit Configurations
- Add Remote JVM Debug
- Port: 5005
- Attach debugger

### 2. Docker Debugging

**Debug in Container:**

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080 5005
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "app.jar"]
```

**docker-compose.yml:**

```yaml
services:
  app:
    build: .
    ports:
      - "8080:8080"
      - "5005:5005"
    environment:
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
```

### 3. Kubernetes Debugging

**Port Forward for Debugging:**

```bash
# Port forward to pod
kubectl port-forward pod/user-service-xxx 8080:8080 5005:5005

# Port forward to service
kubectl port-forward service/user-service 8080:8080
```

**Debug Pod:**

```bash
# Get pod logs
kubectl logs -f user-service-xxx

# Execute command in pod
kubectl exec -it user-service-xxx -- /bin/sh

# Describe pod
kubectl describe pod user-service-xxx
```

### 4. Logging Best Practices

**Structured Logging:**

```java
@Slf4j
@Service
public class UserService {
    
    public User createUser(User user) {
        log.info("Creating user: name={}, email={}", user.getName(), user.getEmail());
        try {
            User created = userRepository.save(user);
            log.info("User created successfully: id={}", created.getId());
            return created;
        } catch (Exception e) {
            log.error("Failed to create user: name={}, email={}", 
                user.getName(), user.getEmail(), e);
            throw e;
        }
    }
}
```

**Logback Configuration:**

```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <includeContext>true</includeContext>
            <includeCallerData>true</includeCallerData>
        </encoder>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

---

## CI/CD Optimization

### 1. GitHub Actions

**.github/workflows/ci.yml:**

```yaml
name: CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main, develop ]

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
      run: mvn clean verify
    
    - name: Run tests
      run: mvn test
    
    - name: Build Docker image
      run: docker build -t user-service:${{ github.sha }} .
    
    - name: Push to registry
      run: |
        echo "${{ secrets.DOCKER_PASSWORD }}" | docker login -u "${{ secrets.DOCKER_USERNAME }}" --password-stdin
        docker push user-service:${{ github.sha }}
```

### 2. GitLab CI

**.gitlab-ci.yml:**

```yaml
stages:
  - build
  - test
  - package
  - deploy

variables:
  MAVEN_OPTS: "-Dmaven.repo.local=.m2/repository"
  DOCKER_DRIVER: overlay2

build:
  stage: build
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn clean compile
  cache:
    paths:
      - .m2/repository/

test:
  stage: test
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn test
  cache:
    paths:
      - .m2/repository/

package:
  stage: package
  image: maven:3.9-eclipse-temurin-17
  script:
    - mvn package -DskipTests
  artifacts:
    paths:
      - target/*.jar

deploy:
  stage: deploy
  image: docker:latest
  services:
    - docker:dind
  script:
    - docker build -t user-service:$CI_COMMIT_SHA .
    - docker push user-service:$CI_COMMIT_SHA
```

### 3. Jenkins Pipeline

**Jenkinsfile:**

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    stages {
        stage('Build') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            steps {
                sh 'docker build -t user-service:${BUILD_NUMBER} .'
            }
        }
        
        stage('Deploy') {
            steps {
                sh 'kubectl set image deployment/user-service user-service=user-service:${BUILD_NUMBER}'
            }
        }
    }
}
```

### 4. CI/CD Best Practices

**Optimize Build Times:**
- Use build cache
- Parallel test execution
- Incremental builds
- Skip unnecessary steps
- Use build matrix for multiple versions

**Security:**
- Scan dependencies
- Container image scanning
- Secret management
- Least privilege access

---

## Code Quality & Productivity Tools

### 1. Code Generation

**Lombok:**

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Long id;
    private String name;
    private String email;
}
```

**MapStruct:**

```java
@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    
    UserDTO toDTO(User user);
    User toEntity(UserDTO dto);
}
```

### 2. Static Analysis

**SonarQube:**

```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>3.10.0.2594</version>
</plugin>
```

**Checkstyle:**

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.2.0</version>
</plugin>
```

**SpotBugs:**

```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.7.3.6</version>
</plugin>
```

### 3. Documentation

**JavaDoc:**

```java
/**
 * Service for managing users.
 * 
 * @author John Doe
 * @since 1.0
 */
@Service
public class UserService {
    
    /**
     * Creates a new user.
     * 
     * @param user the user to create
     * @return the created user with generated ID
     * @throws IllegalArgumentException if user is null
     */
    public User createUser(User user) {
        // Implementation
    }
}
```

**API Documentation - Swagger/OpenAPI:**

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management API")
public class UserController {
    
    @PostMapping
    @Operation(summary = "Create user", description = "Creates a new user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created"),
        @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // Implementation
    }
}
```

### 4. Code Formatting

**Google Java Format:**

```xml
<plugin>
    <groupId>com.spotify.fmt</groupId>
    <artifactId>fmt-maven-plugin</artifactId>
    <version>2.20.1</version>
    <executions>
        <execution>
            <goals>
                <goal>format</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

**Pre-commit Hooks:**

```bash
#!/bin/sh
# .git/hooks/pre-commit

mvn fmt:check
if [ $? -ne 0 ]; then
    echo "Code formatting check failed. Run 'mvn fmt:format' to fix."
    exit 1
fi
```

---

## API Development & Documentation

### 1. REST API Best Practices

**RESTful Design:**

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @GetMapping
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Implementation
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // Implementation
    }
    
    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        // Implementation
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, 
                                           @Valid @RequestBody UserDTO userDTO) {
        // Implementation
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        // Implementation
    }
}
```

### 2. API Versioning

**URL Versioning:**

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller {
    // V1 implementation
}

@RestController
@RequestMapping("/api/v2/users")
public class UserV2Controller {
    // V2 implementation
}
```

**Header Versioning:**

```java
@GetMapping(value = "/api/users", headers = "API-Version=1")
public ResponseEntity<List<User>> getUsersV1() {
    // V1 implementation
}
```

### 3. Error Handling

**Global Exception Handler:**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) {
        ErrorResponse error = new ErrorResponse(
            "NOT_FOUND",
            e.getMessage(),
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .collect(Collectors.toList());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Validation failed",
            errors,
            LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

---

## Database & Data Management

### 1. JPA Best Practices

**Entity Design:**

```java
@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String name;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
}
```

**Repository Optimization:**

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findByEmail(@Param("email") String email);
    
    @Query(value = "SELECT * FROM users WHERE created_at > :date", nativeQuery = true)
    List<User> findRecentUsers(@Param("date") LocalDateTime date);
    
    @Modifying
    @Query("UPDATE User u SET u.name = :name WHERE u.id = :id")
    void updateName(@Param("id") Long id, @Param("name") String name);
}
```

### 2. Database Migrations

**Flyway:**

```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>9.22.0</version>
</plugin>
```

**Migration File:**

```sql
-- V1__Create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- V2__Add_index_on_email.sql
CREATE INDEX idx_users_email ON users(email);
```

**Liquibase:**

```xml
<plugin>
    <groupId>org.liquibase</groupId>
    <artifactId>liquibase-maven-plugin</artifactId>
    <version>4.23.0</version>
</plugin>
```

---

## Performance Optimization

### 1. Application Performance

**Connection Pooling:**

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

**Caching:**

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("users", "orders");
    }
}

@Service
public class UserService {
    
    @Cacheable(value = "users", key = "#id")
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    
    @CacheEvict(value = "users", key = "#user.id")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

### 2. JVM Tuning

**JVM Options:**

```bash
java -Xms512m \
     -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseStringDeduplication \
     -jar app.jar
```

### 3. Monitoring

**Spring Boot Actuator:**

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

**Micrometer:**

```java
@Service
public class UserService {
    
    private final Counter userCreatedCounter;
    private final Timer userCreationTimer;
    
    public UserService(MeterRegistry meterRegistry) {
        this.userCreatedCounter = Counter.builder("users.created")
            .description("Number of users created")
            .register(meterRegistry);
        this.userCreationTimer = Timer.builder("users.creation.time")
            .description("Time taken to create user")
            .register(meterRegistry);
    }
    
    public User createUser(User user) {
        return userCreationTimer.recordCallable(() -> {
            User created = userRepository.save(user);
            userCreatedCounter.increment();
            return created;
        });
    }
}
```

---

## Learning & Skill Development

### 1. Recommended Resources

**Books:**
- "Effective Java" by Joshua Bloch
- "Spring in Action" by Craig Walls
- "Kubernetes in Action" by Marko Lukša
- "Building Microservices" by Sam Newman

**Online Courses:**
- Spring Academy
- Kubernetes.io tutorials
- Docker official docs
- Pluralsight / Udemy courses

**Practice:**
- Build side projects
- Contribute to open source
- Solve coding challenges
- Read other people's code

### 2. Stay Updated

**Follow:**
- Spring Blog
- Kubernetes Blog
- Docker Blog
- Java Community Process (JCP)

**Newsletters:**
- Java Weekly
- DevOps Weekly
- Kubernetes Weekly

**Conferences:**
- SpringOne
- KubeCon
- JavaOne

---

## Time Management & Workflow

### 1. Daily Routine

**Morning:**
- Review tasks and priorities
- Check CI/CD status
- Review code reviews

**Development:**
- Focus on one task at a time
- Use Pomodoro technique (25 min focus, 5 min break)
- Take regular breaks

**End of Day:**
- Commit and push work
- Update task status
- Plan next day

### 2. Productivity Tools

**Task Management:**
- Jira
- Trello
- Asana
- GitHub Issues

**Time Tracking:**
- RescueTime
- Toggl
- Clockify

**Note Taking:**
- Notion
- Obsidian
- OneNote

### 3. Automation

**Scripts for Common Tasks:**

```bash
#!/bin/bash
# deploy.sh

set -e

echo "Building application..."
mvn clean package

echo "Building Docker image..."
docker build -t user-service:latest .

echo "Pushing to registry..."
docker push user-service:latest

echo "Deploying to Kubernetes..."
kubectl set image deployment/user-service user-service=user-service:latest

echo "Deployment complete!"
```

---

## Summary

### Key Productivity Tips

1. **Optimize Your Environment**
   - Use proper IDE setup
   - Configure build tools
   - Set up local development stack

2. **Leverage Tools**
   - Code generation (Lombok, MapStruct)
   - Static analysis
   - Automated testing

3. **Containerize Everything**
   - Docker for local development
   - Kubernetes for orchestration
   - Consistent environments

4. **Automate Repetitive Tasks**
   - CI/CD pipelines
   - Scripts for common operations
   - Code generation

5. **Focus on Quality**
   - Write tests
   - Code reviews
   - Documentation

6. **Continuous Learning**
   - Stay updated with technology
   - Practice regularly
   - Learn from others

### Quick Wins

- Set up Maven/Gradle wrapper
- Use Docker Compose for local services
- Configure IDE shortcuts and templates
- Enable build caching
- Use code generation tools
- Set up pre-commit hooks
- Automate CI/CD
- Use kubectl aliases
- Set up remote debugging
- Enable application monitoring

---

**Remember:** Productivity is about working smarter, not harder. Focus on automation, tooling, and best practices to maximize your development efficiency.

---

*Last Updated: [Current Date]*
*Keep iterating and improving your development workflow!*

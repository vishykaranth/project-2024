# Keycloak Bootstrap - High-Level Design (HLD)

## 1. Executive Summary

### 1.1 Overview
The **Keycloak Bootstrap** project is a comprehensive configuration and customization framework for Keycloak Identity and Access Management (IAM) system. It provides multi-tenant support, custom authentication flows, token exchange mechanisms, and theme customization for the APEX platform.

### 1.2 Purpose
- **Centralized IAM Configuration**: Manage Keycloak realms, clients, and authentication flows across multiple environments
- **Custom Extensions**: Provide custom authenticators, protocol mappers, and event listeners for federated user management
- **Multi-Tenant Themes**: Support branded login experiences for different clients/tenants
- **Automated Deployment**: CI/CD pipelines for deploying configurations and themes to various environments

### 1.3 Key Capabilities
- ✅ Custom federated user authentication and synchronization
- ✅ Token exchange with custom claims (tenant ID, target client)
- ✅ Event-driven user provisioning to external IAM system
- ✅ Multi-tenant theme management
- ✅ Realm configuration management
- ✅ Automated deployment to S3 and Keycloak instances

---

## 2. System Architecture

### 2.1 High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      Keycloak Bootstrap System                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┼─────────────┐
                │             │             │
        ┌───────▼──────┐ ┌───▼────┐ ┌─────▼──────┐
        │   Custom     │ │ Themes │ │   Realm    │
        │  Extensions  │ │        │ │  Configs   │
        └───────┬──────┘ └───┬────┘ └─────┬──────┘
                │            │             │
                └────────────┼─────────────┘
                             │
                ┌────────────▼────────────┐
                │   CI/CD Pipelines      │
                │  (Jenkins)             │
                └────────────┬────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
┌───────▼────────┐  ┌─────────▼─────────┐  ┌──────▼────────┐
│   AWS S3       │  │  Keycloak         │  │  External IAM │
│  (Configs &    │  │  Instances        │  │  (APEX IAM)  │
│   Themes)      │  │  (Multi-Env)      │  │              │
└────────────────┘  └──────────────────┘  └───────────────┘
```

### 2.2 Component Overview

#### 2.2.1 Custom Extensions (`IFG/custommapper/`)
- **Custom Authenticators**: Federated user authentication flows
- **Protocol Mappers**: Token claim mapping (IAM roles, tenant ID)
- **Token Exchange Provider**: Custom OAuth2 token exchange logic
- **Event Listeners**: User creation and synchronization events

#### 2.2.2 Themes (`themes/`)
- Multi-tenant branded login pages
- Email templates
- Custom styling and branding

#### 2.2.3 Realm Configurations (`Kountable/`)
- Realm JSON exports
- Client configurations
- Authentication flow definitions

#### 2.2.4 CI/CD Infrastructure
- Jenkins pipelines for build and deployment
- S3 integration for configuration storage
- Environment-specific configurations

---

## 3. Detailed Component Design

### 3.1 Custom Extensions Module

#### 3.1.1 Custom Authenticators

**Location**: `IFG/custommapper/src/main/java/org/jiffy/keycloak/authentication/`

**Components**:

1. **FederatedUserCreateAuthenticator**
   - **Purpose**: Creates federated users during first login from external IdP
   - **Flow**: Handles user creation in Keycloak when authenticating via external identity provider
   - **Integration**: Calls `FederatedUserFlowHandler` for user creation logic

2. **FederatedUserUpdateAuthenticator**
   - **Purpose**: Updates federated user attributes during subsequent logins
   - **Flow**: Synchronizes user data from external IdP to Keycloak

3. **BrowserLoginFederatedUserSyncAuthenticatorFactory**
   - **Purpose**: Factory for browser-based federated user synchronization
   - **Configuration**: Configurable authentication flow step

4. **FirstBrokerLoginFederatedUserAuthenticatorFactory**
   - **Purpose**: Handles first-time login from external identity provider
   - **Use Case**: SSO integration with external IdPs (SAML, OIDC)

**Authentication Flow**:
```
External IdP → Keycloak → FederatedUserCreateAuthenticator → 
FederatedUserFlowHandler → External IAM API → User Created
```

#### 3.1.2 Custom Protocol Mappers

**Location**: `IFG/custommapper/src/main/java/org/jiffy/keycloak/mapper/`

**Components**:

1. **CustomIAMRoleMapper**
   - **Purpose**: Maps IAM roles and tenant information to JWT tokens
   - **Token Claims Added**:
     - `tenantId`: Realm name (tenant identifier)
     - `target_client`: Target client ID for token exchange
   - **Configuration**:
     - `iam.url.attribute.name`: IAM host endpoint
   - Include in: Access Token, ID Token, UserInfo
   - **Token Exchange Support**: Handles token exchange requests by updating `client_id` claim

**Token Transformation Flow**:
```
User Login → Keycloak Session → CustomIAMRoleMapper → 
Transform Access Token → Add tenantId, target_client → 
Return Enhanced Token
```

2. **DataFilter**
   - **Purpose**: Filters and transforms user data during token generation

#### 3.1.3 Custom Token Exchange Provider

**Location**: `IFG/custommapper/src/main/java/org/jiffy/keycloak/provider/`

**Component**: **CustomTokenExchangeProvider**

**Purpose**: Extends Keycloak's default token exchange to support custom client-to-client token exchange with additional claims.

**Key Features**:
- Custom token exchange for client-to-OIDC client scenarios
- Supports `target_client` parameter for audience modification
- Handles `token_exchange_request` flag for custom claim injection
- Maintains session context during token exchange
- Supports impersonation scenarios

**Token Exchange Flow**:
```
Client A → Token Exchange Request (with target_client) → 
CustomTokenExchangeProvider → Create Auth Session → 
Update Client Session Notes → Generate Token for Target Client → 
Return Access Token with Custom Claims
```

**Configuration Parameters**:
- `target_client`: Target client ID for token exchange
- `token_exchange_request`: Boolean flag to enable custom processing

#### 3.1.4 Event Listeners

**Location**: `IFG/custommapper/src/main/java/org/jiffy/keycloak/mapper/`

**Component**: **UserCreationEventListener**

**Purpose**: Listens to Keycloak events and triggers federated user creation/updates in external IAM system.

**Event Types Handled**:
1. **IDENTITY_PROVIDER_FIRST_LOGIN**
   - Triggered when user first logs in via external IdP
   - Stores `code_id` for tracking
   - Prepares for user creation

2. **LOGIN**
   - Triggered on user login
   - Matches with `IDENTITY_PROVIDER_FIRST_LOGIN` event using `code_id`
   - Calls external IAM API to create/update user
   - Uses federated ID token from session

**Event Flow**:
```
External IdP Login → IDENTITY_PROVIDER_FIRST_LOGIN Event → 
Store code_id → LOGIN Event → Match code_id → 
Extract Federated Token → Call External IAM API → 
User Created/Updated in External System
```

**Integration Points**:
- **FederatedUserClient**: HTTP client for calling external IAM API
- **UserSessionModel**: Access to federated ID token
- **ClientModel**: Client context for API calls

---

### 3.2 Theme Management

#### 3.2.1 Theme Structure

**Location**: `themes/`

**Supported Themes**:
- `jiffy-theme`: Default Jiffy platform theme
- `aveta-theme`: Aveta client branding
- `dxc-theme`: DXC client branding
- `finserv-theme`: Financial services theme
- `mbo-theme`: MBO client theme
- `mckesson-theme`: McKesson client theme
- `trancone-theme`: Trancone client theme
- `triad-theme`: Triad client theme

**Theme Components**:
```
theme-name/
├── login/
│   ├── login.ftl                    # Main login template
│   ├── login-reset-password.ftl     # Password reset template
│   ├── login-update-password.ftl   # Password update template
│   ├── login-otp.ftl                # OTP authentication template
│   ├── login-config-totp.ftl        # TOTP configuration template
│   ├── template.ftl                 # Base template
│   ├── resources/                   # CSS, JS, images
│   ├── messages/                    # i18n messages
│   └── theme.properties             # Theme configuration
└── email/
    ├── html/                        # Email HTML templates
    ├── messages/                    # Email i18n messages
    └── theme.properties             # Email theme config
```

#### 3.2.2 Theme Deployment

**Process**:
1. Themes stored in Git repository
2. Jenkins pipeline packages themes
3. Uploaded to S3 bucket (`keycloak-configs-apex-dev`)
4. Keycloak instances pull themes from S3 on startup/refresh

**S3 Structure**:
```
s3://keycloak-configs-apex-dev/
├── {environment}/
│   ├── themes/
│   │   ├── jiffy-theme/
│   │   ├── aveta-theme/
│   │   └── ...
│   └── themesversion.txt
```

---

### 3.3 Realm Configuration Management

#### 3.3.1 Realm Files

**Location**: `Kountable/`

**Realm Configurations**:
- `jiffy-default-realm.json`: Default Jiffy realm configuration
- `kountable-realm.json`: Kountable client realm
- `master-realm.json`: Master realm configuration

**Realm Configuration Includes**:
- Realm settings (SSO timeout, token lifespan, OTP policy)
- Client configurations
- Authentication flows
- Protocol mappers
- Identity provider configurations
- User federation settings
- Role definitions

#### 3.3.2 Realm Import/Export

**Process**:
1. Realms exported from Keycloak Admin Console
2. Stored in Git repository
3. Imported to Keycloak instances via Admin API or startup scripts

---

### 3.4 CI/CD Pipeline

#### 3.4.1 Build Pipeline (`Jenkinsfile`)

**Purpose**: Build and version custom extensions

**Stages**:
1. **Fetch Build Number & Create Version**
   - Calls PRM (Product Release Management) API
   - Generates version: `{versionPrefix}.{buildNumber}`
   - Handles PR, main branch, and hotfix versions

2. **Git Tag and Push**
   - Creates Git tag with version
   - Pushes tag to repository

**Version Format**:
- Main branch: `1.1.{buildNumber}`
- PR: `1.1-pr-{buildNumber}`
- Hotfix: `1.1-hotfix-{buildNumber}`

#### 3.4.2 Promotion Pipeline (`promotionJenkinsfile`)

**Purpose**: Deploy themes and providers to environments

**Parameters**:
- `GIT_TAG`: Git tag to checkout
- `ENV`: Target environment (dev-avengers, metropolis, platform-editor, apex-stage-2002)
- `COPY_TARGET`: What to copy (provider, themes, both)

**Stages**:
1. **Clone Keycloak Bootstrap**
   - Checks out specific Git tag
   - Validates repository

2. **Read Config & Upload**
   - Reads `env_config.json` for environment configuration
   - Uploads themes to S3 bucket
   - Updates `themesversion.txt` with Git tag

**Environment Configuration** (`env_config.json`):
```json
{
  "environment-name": {
    "bucket": "keycloak-configs-apex-dev",
    "runner": "apex-dev-workers"
  }
}
```

**Supported Environments**:
- `at-integration`, `at-qa`
- `dev-minnal`, `dev-avengers`, `dev-ninja`, `dev-workflow`
- `integration-test`, `metropolis`, `platform-editor`
- `apex-stage-2002`, `apex-perf-1021`, `apex-axos-uat`
- `apex-prod-3001`, `apex-prod-3002`

---

## 4. Data Flow and Integration

### 4.1 Federated User Authentication Flow

```
┌──────────┐         ┌──────────┐         ┌──────────┐         ┌──────────┐
│  Client  │         │ Keycloak │         │External  │         │External  │
│          │         │          │         │   IdP    │         │   IAM    │
└────┬─────┘         └────┬─────┘         └────┬─────┘         └────┬─────┘
     │                    │                    │                    │
     │ 1. Initiate Login  │                    │                    │
     ├───────────────────>│                    │                    │
     │                    │                    │                    │
     │ 2. Redirect to IdP│                    │                    │
     │<──────────────────┤                    │                    │
     │                    │                    │                    │
     │ 3. Authenticate    │                    │                    │
     ├────────────────────────────────────────>│                    │
     │                    │                    │                    │
     │ 4. ID Token        │                    │                    │
     │<────────────────────────────────────────┤                    │
     │                    │                    │                    │
     │ 5. Post to Keycloak│                    │                    │
     ├───────────────────>│                    │                    │
     │                    │                    │                    │
     │ 6. IDENTITY_PROVIDER│                   │                    │
     │    _FIRST_LOGIN Event│                  │                    │
     │                    │                    │                    │
     │ 7. FederatedUser   │                    │                    │
     │    CreateAuth      │                    │                    │
     │                    │                    │                    │
     │ 8. LOGIN Event     │                    │                    │
     │                    │                    │                    │
     │ 9. UserCreation    │                    │                    │
     │    EventListener   │                    │                    │
     │                    │                    │                    │
     │10. Call IAM API    │                    │                    │
     ├─────────────────────────────────────────────────────────────>│
     │                    │                    │                    │
     │11. User Created    │                    │                    │
     │<─────────────────────────────────────────────────────────────┤
     │                    │                    │                    │
     │12. Access Token    │                    │                    │
     │<───────────────────┤                    │                    │
     │                    │                    │                    │
```

### 4.2 Token Exchange Flow

```
┌──────────┐         ┌──────────┐
│ Client A │         │ Keycloak │
└────┬─────┘         └────┬─────┘
     │                    │
     │ 1. Token Exchange  │
     │    (target_client)  │
     ├───────────────────>│
     │                    │
     │ 2. CustomToken     │
     │    ExchangeProvider│
     │                    │
     │ 3. Create Auth     │
     │    Session         │
     │                    │
     │ 4. Update Client   │
     │    Session Notes   │
     │                    │
     │ 5. CustomIAMRole   │
     │    Mapper          │
     │                    │
     │ 6. Add tenantId,   │
     │    target_client   │
     │                    │
     │ 7. Generate Token  │
     │    for Target     │
     │                    │
     │ 8. Access Token    │
     │<───────────────────┤
     │                    │
```

### 4.3 Theme Deployment Flow

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│   Git    │    │ Jenkins  │    │   AWS    │    │ Keycloak │
│          │    │          │    │    S3    │    │          │
└────┬─────┘    └────┬─────┘    └────┬─────┘    └────┬─────┘
     │               │               │               │
     │ 1. Push Theme │               │               │
     ├──────────────>│               │               │
     │               │               │               │
     │ 2. Build &    │               │               │
     │    Package    │               │               │
     │               │               │               │
     │ 3. Upload to  │               │               │
     │    S3         │               │               │
     │               ├──────────────>│               │
     │               │               │               │
     │ 4. Update     │               │               │
     │    Version    │               │               │
     │               ├──────────────>│               │
     │               │               │               │
     │ 5. Pull Theme │               │               │
     │               │               │               │
     │               │               │<──────────────┤
     │               │               │               │
```

---

## 5. Technology Stack

### 5.1 Core Technologies

- **Keycloak**: 26.1.3 (Identity and Access Management)
- **Java**: 17 (Custom extensions)
- **Maven**: Build tool
- **Jenkins**: CI/CD pipeline
- **AWS S3**: Configuration and theme storage
- **Git/Bitbucket**: Version control

### 5.2 Key Dependencies

**Custom Mapper Dependencies**:
- `keycloak-core`: 26.1.3
- `keycloak-server-spi`: 26.1.3
- `keycloak-services`: 26.1.3
- `keycloak-saml-core`: 26.1.3
- `slf4j-api`: 2.0.16
- `logback-classic`: 1.5.15
- `commons-lang3`: 3.12.0
- `micrometer-tracing`: 1.5.1

### 5.3 Integration Technologies

- **OAuth2/OIDC**: Token exchange and authentication
- **SAML**: Federated authentication
- **REST APIs**: External IAM integration
- **FreeMarker Templates**: Theme templates (.ftl)

---

## 6. Security Considerations

### 6.1 Authentication Security

- **Federated Authentication**: Secure integration with external IdPs
- **Token Exchange**: Controlled client-to-client token exchange
- **Session Management**: Secure session handling with SSO timeout
- **OTP Support**: TOTP-based multi-factor authentication

### 6.2 Data Security

- **Token Claims**: Sensitive data (tenant ID, roles) in JWT tokens
- **Federated Tokens**: Secure storage and transmission of IdP tokens
- **Event Tracking**: `code_id` tracking for event correlation

### 6.3 Access Control

- **Client Authentication**: Client credentials for token exchange
- **Realm Isolation**: Multi-tenant isolation via realms
- **Role-Based Access**: IAM role mapping to tokens

---

## 7. Scalability and Performance

### 7.1 Scalability Features

- **Multi-Tenant Support**: Multiple realms for different tenants
- **Horizontal Scaling**: Keycloak cluster support
- **Theme Caching**: S3-based theme distribution
- **Event Processing**: Asynchronous event handling

### 7.2 Performance Optimizations

- **Token Caching**: Efficient token generation and validation
- **Session Management**: Configurable session timeouts
- **Code ID Cache**: In-memory cache for event correlation (`ConcurrentHashMap`)
- **Batch Operations**: Efficient user creation/update APIs

---

## 8. Deployment Architecture

### 8.1 Deployment Model

**Multi-Environment Support**:
- Development environments (dev-avengers, dev-ninja, etc.)
- Integration/QA environments (at-integration, at-qa)
- Staging environments (apex-stage-2002)
- Production environments (apex-prod-3001, apex-prod-3002)

### 8.2 Deployment Process

1. **Build Phase**:
   - Compile custom extensions
   - Package JAR file
   - Create version tag

2. **Promotion Phase**:
   - Select Git tag
   - Choose target environment
   - Upload themes/providers to S3
   - Update version file

3. **Keycloak Integration**:
   - Keycloak instances pull from S3
   - Custom extensions deployed as JAR
   - Realms imported via Admin API

### 8.3 Configuration Management

**Environment-Specific Configuration**:
- `env_config.json`: Environment-to-S3 bucket mapping
- Realm configurations: Environment-specific realm settings
- Theme versions: Tracked via `themesversion.txt`

---

## 9. Monitoring and Observability

### 9.1 Logging

- **SLF4J/Logback**: Structured logging in custom extensions
- **Event Logging**: Keycloak event logging
- **Error Tracking**: Exception logging in event listeners

### 9.2 Metrics

- **Micrometer Tracing**: Distributed tracing support
- **Keycloak Metrics**: Built-in Keycloak metrics
- **Event Tracking**: `code_id` tracking for event correlation

### 9.3 Health Checks

- **Keycloak Health Endpoints**: Standard Keycloak health checks
- **S3 Connectivity**: Theme availability checks
- **External IAM**: API connectivity monitoring

---

## 10. Future Enhancements

### 10.1 Potential Improvements

1. **Enhanced Event Handling**:
   - Support for logout events
   - Session invalidation tracking
   - Token refresh event handling

2. **Advanced Token Exchange**:
   - Support for more token exchange scenarios
   - Enhanced claim mapping
   - Token transformation rules

3. **Theme Management**:
   - Dynamic theme selection based on tenant
   - Theme preview functionality
   - A/B testing support

4. **Configuration as Code**:
   - Infrastructure as Code (Terraform/CloudFormation)
   - Automated realm provisioning
   - Configuration validation

5. **Observability**:
   - Enhanced distributed tracing
   - Custom metrics dashboard
   - Alerting for critical events

---

## 11. Dependencies and Integration Points

### 11.1 External Dependencies

- **APEX IAM Service**: User creation/update API
- **External Identity Providers**: SAML/OIDC IdPs
- **AWS S3**: Configuration storage
- **Jenkins**: CI/CD infrastructure
- **PRM (Product Release Management)**: Version management

### 11.2 Keycloak Dependencies

- Keycloak Admin API
- Keycloak SPI (Service Provider Interface)
- Keycloak Event System
- Keycloak Authentication SPI
- Keycloak Protocol Mapper SPI

---

## 12. Risk Assessment and Mitigation

### 12.1 Identified Risks

1. **Single Point of Failure**: S3 bucket availability
   - **Mitigation**: Multi-region S3, backup configurations

2. **Event Loss**: Event listener failures
   - **Mitigation**: Retry logic, event queuing

3. **Token Security**: Token exposure in logs
   - **Mitigation**: Secure logging, token masking

4. **Version Conflicts**: Theme version mismatches
   - **Mitigation**: Version tracking, rollback capability

### 12.2 Disaster Recovery

- **Configuration Backup**: Git repository as source of truth
- **Theme Backup**: S3 versioning enabled
- **Realm Backup**: Regular realm exports
- **Extension Backup**: JAR artifacts in repository

---

## 13. Conclusion

The Keycloak Bootstrap project provides a comprehensive framework for managing Keycloak configurations, custom extensions, and multi-tenant themes across multiple environments. It enables:

- **Centralized IAM Management**: Single source of truth for Keycloak configurations
- **Custom Authentication Flows**: Federated user authentication and synchronization
- **Multi-Tenant Support**: Branded experiences for different clients
- **Automated Deployment**: CI/CD pipelines for efficient deployments
- **Extensibility**: Custom extensions for specific business requirements

The architecture supports scalability, security, and maintainability while providing flexibility for future enhancements.

---

## Appendix A: File Structure

```
keycloak-bootstrap/
├── IFG/
│   └── custommapper/
│       ├── pom.xml
│       └── src/
│           └── main/
│               ├── java/
│               │   └── org/jiffy/keycloak/
│               │       ├── authentication/
│               │       ├── mapper/
│               │       └── provider/
│               └── resources/
│                   └── META-INF/
│                       └── services/
├── Kountable/
│   ├── jiffy-default-realm.json
│   ├── kountable-realm.json
│   ├── master-realm.json
│   └── *.jar
├── themes/
│   ├── jiffy-theme/
│   ├── aveta-theme/
│   ├── dxc-theme/
│   ├── finserv-theme/
│   └── ...
├── env_config.json
├── Jenkinsfile
└── promotionJenkinsfile
```

---

## Appendix B: Key Configuration Files

### B.1 env_config.json
Environment-to-S3 bucket mapping for deployment.

### B.2 Realm Configuration
JSON exports containing realm settings, clients, flows, and mappers.

### B.3 Theme Properties
Theme configuration files defining CSS classes, templates, and resources.

---

*Document Version: 1.0*  
*Last Updated: 2024*  
*Author: System Design Team*

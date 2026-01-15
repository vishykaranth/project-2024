# Application Security - Complete Diagrams Guide (Part 2: Authorization)

## 🛡️ Authorization: RBAC, ABAC, Policy-Based Access Control

---

## 1. Authorization Fundamentals

### Authorization vs Authentication
```
┌─────────────────────────────────────────────────────────────┐
│              Authorization Process                           │
└─────────────────────────────────────────────────────────────┘

    User (Authenticated)
    │
    │ Request: "Access /admin/dashboard"
    │
    ▼
┌──────────────────┐
│ Authorization    │
│ Engine           │
│                  │
│ Check:           │
│ - Roles          │
│ - Permissions     │
│ - Policies        │
│ - Attributes      │
└──────────────────┘
    │
    ├─► Allowed ──► Access Granted
    │
    └─► Denied ───► Access Denied (403 Forbidden)

Authorization Answers:
- WHO can do WHAT on WHICH resource?
- Based on: Roles, Attributes, Policies, Context
```

### Authorization Models
```
┌─────────────────────────────────────────────────────────────┐
│              Authorization Models                           │
└─────────────────────────────────────────────────────────────┘

1. Discretionary Access Control (DAC):
   Owner controls access
   Example: File permissions (Unix)

2. Mandatory Access Control (MAC):
   System-enforced, based on security labels
   Example: Military classifications

3. Role-Based Access Control (RBAC):
   Access based on roles
   Example: Admin, User, Guest roles

4. Attribute-Based Access Control (ABAC):
   Access based on attributes
   Example: Department, Time, Location

5. Policy-Based Access Control:
   Access based on policies/rules
   Example: Complex business rules
```

---

## 2. Role-Based Access Control (RBAC)

### RBAC Core Concepts
```
┌─────────────────────────────────────────────────────────────┐
│              RBAC Model                                     │
└─────────────────────────────────────────────────────────────┘

    Users                    Roles                  Permissions
    │                        │                      │
    │                        │                      │
    ├──► John ──────────────┼──► Admin ────────────┼──► Read
    │                        │                      │
    ├──► Jane ──────────────┼──► Editor ──────────┼──► Write
    │                        │                      │
    └──► Bob ────────────────┼──► Viewer ───────────┼──► Delete
                             │                      │
                             └──► Guest ────────────┘

RBAC Components:
- Users: People or systems
- Roles: Job functions (Admin, Editor, Viewer)
- Permissions: Actions on resources (Read, Write, Delete)
- Resources: Objects being protected (Files, APIs, Data)
```

### RBAC Hierarchy
```
┌─────────────────────────────────────────────────────────────┐
│              RBAC Role Hierarchy                            │
└─────────────────────────────────────────────────────────────┘

                    Super Admin
                    │
                    ├──► Admin
                    │    │
                    │    ├──► Manager
                    │    │    │
                    │    │    ├──► Editor
                    │    │    │    │
                    │    │    │    └──► Viewer
                    │    │    │
                    │    │    └──► Contributor
                    │    │
                    │    └──► Moderator
                    │
                    └──► Guest

Inheritance:
- Super Admin inherits all permissions
- Admin inherits Manager permissions
- Manager inherits Editor permissions
- Editor inherits Viewer permissions
```

### RBAC Permission Matrix
```
┌─────────────────────────────────────────────────────────────┐
│              RBAC Permission Matrix                         │
└─────────────────────────────────────────────────────────────┘

Resource      │  Admin  │  Editor  │  Viewer  │  Guest
──────────────┼─────────┼──────────┼──────────┼─────────
Users         │  CRUD   │   R      │    R     │    -
Posts          │  CRUD   │   CRU    │    R    │    R
Comments       │  CRUD   │   CRUD   │    R    │    C
Settings       │   RU    │    -     │    -    │    -
Analytics      │    R    │    R     │    -    │    -

C = Create, R = Read, U = Update, D = Delete
- = No Access
```

### RBAC Implementation Flow
```
┌─────────────────────────────────────────────────────────────┐
│              RBAC Authorization Flow                       │
└─────────────────────────────────────────────────────────────┘

Step 1: User Makes Request
    User ──► Application: GET /api/users/123

Step 2: Extract User Identity
    Application:
    - Get user from session/token
    - User ID: user123

Step 3: Get User Roles
    Application ──► Database:
    SELECT roles FROM user_roles WHERE user_id = 'user123'
    Result: ['Editor', 'Viewer']

Step 4: Get Permissions for Roles
    Application ──► Database:
    SELECT permission FROM role_permissions 
    WHERE role IN ('Editor', 'Viewer')
    Result: ['read:users', 'read:posts', 'write:posts']

Step 5: Check Permission
    Request: GET /api/users/123
    Required Permission: 'read:users'
    User Permissions: ['read:users', 'read:posts', 'write:posts']
    
    Check: 'read:users' IN user_permissions? ✓ YES

Step 6: Authorization Decision
    Application: Access GRANTED
    Application ──► User: User data (200 OK)
```

### RBAC Database Schema
```
┌─────────────────────────────────────────────────────────────┐
│              RBAC Database Schema                           │
└─────────────────────────────────────────────────────────────┘

users table:
    id, username, email, ...

roles table:
    id, name, description

permissions table:
    id, name, resource, action
    Example: 'read:users', 'write:posts', 'delete:comments'

user_roles table (Many-to-Many):
    user_id, role_id

role_permissions table (Many-to-Many):
    role_id, permission_id

Example Data:
    users: {id: 1, username: 'john'}
    roles: {id: 1, name: 'Admin'}, {id: 2, name: 'Editor'}
    permissions: 
        {id: 1, name: 'read:users'},
        {id: 2, name: 'write:posts'}
    user_roles: {user_id: 1, role_id: 1}
    role_permissions: 
        {role_id: 1, permission_id: 1},
        {role_id: 1, permission_id: 2}
```

---

## 3. Attribute-Based Access Control (ABAC)

### ABAC Core Concepts
```
┌─────────────────────────────────────────────────────────────┐
│              ABAC Model                                     │
└─────────────────────────────────────────────────────────────┘

    Subject Attributes        Environment Attributes
    │                        │
    │ - User ID              │ - Time
    │ - Department           │ - Location
    │ - Clearance Level      │ - IP Address
    │ - Age                  │ - Device Type
    │                        │
    │                        │
    ▼                        ▼
┌──────────────────────────────────┐
│      Policy Decision Point       │
│      (PDP)                       │
│                                   │
│  Policy Rules:                    │
│  IF subject.department = "HR"     │
│  AND resource.type = "salary"    │
│  AND time.hour BETWEEN 9 AND 17  │
│  THEN ALLOW                      │
│  ELSE DENY                       │
└──────────────────────────────────┘
    │
    │
    ▼
    Resource Attributes
    │
    │ - Type
    │ - Owner
    │ - Classification
    │ - Sensitivity
```

### ABAC Attributes
```
┌─────────────────────────────────────────────────────────────┐
│              ABAC Attribute Categories                      │
└─────────────────────────────────────────────────────────────┘

Subject Attributes (User):
    - Identity: user_id, username, email
    - Organization: department, division, company
    - Role: job_title, clearance_level
    - Characteristics: age, nationality, certifications
    - Relationships: manager, team, project

Resource Attributes (Object):
    - Type: document, database, API endpoint
    - Classification: public, internal, confidential, secret
    - Owner: creator, department, organization
    - Metadata: creation_date, last_modified, tags
    - Content: file_type, size, keywords

Environment Attributes (Context):
    - Time: current_time, day_of_week, business_hours
    - Location: IP_address, geo_location, network
    - Device: device_type, OS, browser
    - Network: VPN, internal, external
    - Threat: risk_level, threat_intelligence

Action Attributes:
    - Operation: read, write, delete, execute
    - Method: GET, POST, PUT, DELETE
    - Purpose: business_justification
```

### ABAC Policy Examples
```
┌─────────────────────────────────────────────────────────────┐
│              ABAC Policy Examples                            │
└─────────────────────────────────────────────────────────────┘

Policy 1: Department-Based Access
    IF subject.department == resource.owner_department
    AND action == "read"
    THEN ALLOW
    ELSE DENY

Policy 2: Time-Based Access
    IF subject.clearance_level >= "confidential"
    AND environment.current_time BETWEEN "09:00" AND "17:00"
    AND environment.location == "office"
    THEN ALLOW
    ELSE DENY

Policy 3: Data Classification
    IF subject.clearance_level >= resource.classification
    AND subject.department == "IT"
    AND action == "read"
    THEN ALLOW
    ELSE DENY

Policy 4: Location-Based
    IF subject.location == "US"
    AND resource.data_residency == "US"
    AND action == "read"
    THEN ALLOW
    ELSE DENY

Policy 5: Multi-Factor
    IF subject.has_mfa == true
    AND environment.device_type == "corporate"
    AND resource.sensitivity == "high"
    THEN ALLOW
    ELSE DENY
```

### ABAC Evaluation Flow
```
┌─────────────────────────────────────────────────────────────┐
│              ABAC Evaluation Flow                           │
└─────────────────────────────────────────────────────────────┘

Step 1: Request Received
    User ──► Application: GET /api/salary-data/123

Step 2: Collect Attributes
    Application collects:
    Subject:
        - user_id: "user123"
        - department: "HR"
        - clearance_level: "confidential"
        - location: "US"
    
    Resource:
        - type: "salary_data"
        - owner_department: "HR"
        - classification: "confidential"
        - data_residency: "US"
    
    Environment:
        - current_time: "14:30"
        - day_of_week: "Monday"
        - IP_address: "10.0.0.1"
        - device_type: "corporate_laptop"

Step 3: Policy Evaluation
    Policy Engine evaluates:
    
    Policy 1: Department Match
        subject.department == "HR"
        resource.owner_department == "HR"
        Result: ✓ MATCH
    
    Policy 2: Clearance Level
        subject.clearance_level == "confidential"
        resource.classification == "confidential"
        Result: ✓ MATCH
    
    Policy 3: Time Restriction
        environment.current_time == "14:30"
        Business hours: 09:00 - 17:00
        Result: ✓ MATCH
    
    Policy 4: Location
        subject.location == "US"
        resource.data_residency == "US"
        Result: ✓ MATCH

Step 4: Decision
    All policies: ALLOW
    Application: Access GRANTED
    Application ──► User: Salary data (200 OK)
```

### ABAC vs RBAC
```
┌─────────────────────────────────────────────────────────────┐
│              ABAC vs RBAC Comparison                        │
└─────────────────────────────────────────────────────────────┘

Feature              RBAC              ABAC
─────────────────────────────────────────────────────────────
Granularity          Coarse            Fine-grained
Flexibility          Limited           High
Complexity           Low               High
Performance           Fast              Slower
Use Case              Simple            Complex
Attributes           Roles only         Multiple attributes
Context Awareness    No                Yes
Dynamic              Static            Dynamic
Scalability          Good              Excellent
─────────────────────────────────────────────────────────────

RBAC Example:
    IF user.role == "Admin"
    THEN ALLOW

ABAC Example:
    IF user.department == "HR"
    AND user.clearance >= "confidential"
    AND resource.classification <= user.clearance
    AND time.hour BETWEEN 9 AND 17
    AND location == "office"
    THEN ALLOW
```

---

## 4. Policy-Based Access Control

### Policy Engine Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Policy-Based Access Control                    │
└─────────────────────────────────────────────────────────────┘

    Request
    │
    ▼
┌──────────────────┐
│ Policy Enforcement│
│ Point (PEP)       │
│                   │
│ Intercepts request│
└──────────────────┘
    │
    │ Request + Context
    │
    ▼
┌──────────────────┐
│ Policy Decision  │
│ Point (PDP)      │
│                   │
│ Evaluates policies│
└──────────────────┘
    │
    │ Query Attributes
    │
    ▼
┌──────────────────┐
│ Policy Information│
│ Point (PIP)      │
│                   │
│ Provides attributes│
└──────────────────┘
    │
    │ Attributes
    │
    ▼
┌──────────────────┐
│ Policy            │
│ Administration   │
│ Point (PAP)      │
│                   │
│ Manages policies │
└──────────────────┘
    │
    │ Decision (ALLOW/DENY)
    │
    ▼
    Response
```

### Policy Language Examples
```
┌─────────────────────────────────────────────────────────────┐
│              Policy Language Examples                       │
└─────────────────────────────────────────────────────────────┘

XACML (eXtensible Access Control Markup Language):
    <Policy>
        <Rule Effect="Permit">
            <Condition>
                <Apply FunctionId="and">
                    <Apply FunctionId="string-equal">
                        <SubjectAttributeDesignator 
                            AttributeId="department"/>
                        <ResourceAttributeDesignator 
                            AttributeId="owner_department"/>
                    </Apply>
                    <Apply FunctionId="time-in-range">
                        <EnvironmentAttributeDesignator 
                            AttributeId="current_time"/>
                        <AttributeValue>09:00</AttributeValue>
                        <AttributeValue>17:00</AttributeValue>
                    </Apply>
                </Apply>
            </Condition>
        </Rule>
    </Policy>

Rego (Open Policy Agent):
    package authz
    
    default allow = false
    
    allow {
        input.subject.department == input.resource.owner_department
        input.action == "read"
        hours_between(input.environment.current_time, "09:00", "17:00")
    }

JSON Policy:
    {
        "version": "1.0",
        "statements": [
            {
                "effect": "allow",
                "principal": {
                    "department": "HR"
                },
                "action": "read",
                "resource": {
                    "type": "salary_data"
                },
                "condition": {
                    "time": {
                        "between": ["09:00", "17:00"]
                    }
                }
            }
        ]
    }
```

### Policy Evaluation Process
```
┌─────────────────────────────────────────────────────────────┐
│              Policy Evaluation Process                      │
└─────────────────────────────────────────────────────────────┘

Step 1: Policy Parsing
    Policy Text ──► Parse ──► Policy Tree
    Example:
        IF (A AND B) OR (C AND D)
        THEN ALLOW

Step 2: Attribute Resolution
    Policy Engine ──► PIP ──► Attributes
    Resolve all attribute references:
    - subject.department → "HR"
    - resource.type → "salary_data"
    - environment.time → "14:30"

Step 3: Condition Evaluation
    Evaluate each condition:
    - Condition A: subject.department == "HR" → TRUE
    - Condition B: action == "read" → TRUE
    - Condition C: time > "17:00" → FALSE
    - Condition D: location == "office" → TRUE

Step 4: Boolean Logic
    (A AND B) OR (C AND D)
    (TRUE AND TRUE) OR (FALSE AND TRUE)
    TRUE OR FALSE
    = TRUE

Step 5: Effect Application
    IF result == TRUE
    THEN effect = ALLOW
    ELSE effect = DENY

Step 6: Decision Return
    Policy Engine ──► PEP: ALLOW
    PEP ──► Application: Grant access
```

---

## 5. Hybrid Authorization Models

### Combining RBAC and ABAC
```
┌─────────────────────────────────────────────────────────────┐
│              Hybrid RBAC + ABAC Model                       │
└─────────────────────────────────────────────────────────────┘

    User
    │
    ├──► Role Check (RBAC)
    │    │
    │    ├──► Has "Admin" role? ──► YES ──► Continue
    │    │
    │    └──► NO ──► DENY
    │
    ├──► Attribute Check (ABAC)
    │    │
    │    ├──► department == "IT"? ──► YES ──► Continue
    │    │
    │    └──► NO ──► DENY
    │
    ├──► Policy Check
    │    │
    │    ├──► All policies pass? ──► YES ──► ALLOW
    │    │
    │    └──► NO ──► DENY
    │
    └──► Decision: ALLOW/DENY

Benefits:
- RBAC for coarse-grained control
- ABAC for fine-grained control
- Policies for complex rules
```

### Multi-Level Authorization
```
┌─────────────────────────────────────────────────────────────┐
│              Multi-Level Authorization                      │
└─────────────────────────────────────────────────────────────┘

Level 1: Authentication
    User authenticated? ──► YES ──► Continue
    └──► NO ──► DENY (401 Unauthorized)

Level 2: Role-Based (RBAC)
    User has required role? ──► YES ──► Continue
    └──► NO ──► DENY (403 Forbidden)

Level 3: Attribute-Based (ABAC)
    Attributes match policy? ──► YES ──► Continue
    └──► NO ──► DENY (403 Forbidden)

Level 4: Resource-Level
    User owns resource? ──► YES ──► Continue
    └──► NO ──► Check other permissions

Level 5: Action-Level
    User can perform action? ──► YES ──► ALLOW
    └──► NO ──► DENY (403 Forbidden)

Final Decision: ALLOW ──► Access Granted
```

---

## 6. Authorization Best Practices

### Security Principles
```
┌─────────────────────────────────────────────────────────────┐
│              Authorization Best Practices                   │
└─────────────────────────────────────────────────────────────┘

1. Principle of Least Privilege:
   - Grant minimum permissions needed
   - Regular access reviews
   - Remove unused permissions

2. Defense in Depth:
   - Multiple authorization layers
   - Fail securely (default deny)
   - Validate at every layer

3. Separation of Duties:
   - No single user has all permissions
   - Critical actions require multiple approvals
   - Audit separation violations

4. Regular Audits:
   - Review access logs
   - Identify anomalies
   - Remove orphaned permissions

5. Centralized Management:
   - Single source of truth
   - Consistent policies
   - Easier to maintain

6. Attribute Validation:
   - Validate all attributes
   - Sanitize inputs
   - Prevent attribute injection

7. Performance Optimization:
   - Cache authorization decisions
   - Optimize policy evaluation
   - Use indexes for lookups

8. Logging and Monitoring:
   - Log all authorization decisions
   - Monitor for anomalies
   - Alert on policy violations
```

---

## Key Takeaways

### Authorization Model Selection
```
┌─────────────────────────────────────────────────────────────┐
│              Choosing Authorization Model                    │
└─────────────────────────────────────────────────────────────┘

Simple Application:
    └──► RBAC (Role-Based)

Enterprise Application:
    └──► ABAC (Attribute-Based)

Complex Business Rules:
    └──► Policy-Based

Multi-Tenant SaaS:
    └──► Hybrid (RBAC + ABAC)

High Security:
    └──► Multi-Level Authorization

Microservices:
    └──► Policy-Based (Centralized PDP)
```

---

**Next: Part 3 will cover API Security (API Keys, Rate Limiting, OAuth2 Flows).**


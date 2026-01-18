# Part 1: Org Id Fundamentals

## Overview

Organization ID (Org Id) is a fundamental concept in the multi-tenant IAM and OMS (Organization Management Service) system. It represents a unique identifier for organizational entities within a tenant, enabling hierarchical organization structures, access control, and data segregation.

## What is Org Id?

**Organization ID (Org Id)** is a unique identifier that:
- Identifies a specific organization within a tenant
- Represents a node in the organizational hierarchy
- Enables hierarchical relationships (parent-child)
- Provides context for access control and data filtering
- Supports multi-level organizational structures

```
┌─────────────────────────────────────────────────────────┐
│         Org Id Concept                                  │
└─────────────────────────────────────────────────────────┘

Org Id Characteristics:
├─ Unique within tenant
├─ Hierarchical identifier
├─ Access control context
├─ Data segregation key
└─ User assignment target
```

## Org Id Data Model

### Database Schema

```
┌─────────────────────────────────────────────────────────┐
│         org_node Table Structure                        │
└─────────────────────────────────────────────────────────┘

Fields:
├─ id (UUID) - Primary key, Org Id
├─ name (String) - Organization name
├─ display_name (String) - Display name
├─ external_id (String) - External system ID
├─ parent_id (UUID) - Parent organization reference
├─ tenant_id (UUID) - Tenant isolation
├─ org_type_id (UUID) - Organization type
└─ extra_fields (JSON) - Extensible attributes

Relationships:
├─ parent → org_node (self-reference)
├─ children → org_node[] (one-to-many)
├─ tenant → tenant (many-to-one)
└─ org_type → org_type (many-to-one)
```

### Entity Model

```java
@Entity
@Table(name = "org_node")
public class OrgNode {
    @Id
    private UUID id;  // This is the Org Id
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "display_name")
    private String displayName;
    
    @Column(name = "external_id")
    private String externalId;
    
    @ManyToOne
    @JoinColumn(name = "parent_id")
    private OrgNode parent;  // Parent organization
    
    @OneToMany(mappedBy = "parent")
    private List<OrgNode> children;  // Child organizations
    
    @ManyToOne
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
    
    @ManyToOne
    @JoinColumn(name = "org_type_id")
    private OrgType orgType;
    
    @Column(name = "extra_fields", columnDefinition = "jsonb")
    private Map<String, Object> extraFields;
}
```

## Hierarchical Organization Structure

### Tree Structure

```
┌─────────────────────────────────────────────────────────┐
│         Organization Hierarchy                          │
└─────────────────────────────────────────────────────────┘

Tenant: "Acme Corp"
│
├─ Root Org (orgId: "root-001")
│   │
│   ├─ Enterprise (orgId: "ent-001")
│   │   │
│   │   ├─ Branch (orgId: "branch-001")
│   │   │   │
│   │   │   ├─ Firm (orgId: "firm-001")
│   │   │   │   │
│   │   │   │   └─ Team (orgId: "team-001")
│   │   │   │
│   │   │   └─ Firm (orgId: "firm-002")
│   │   │
│   │   └─ Branch (orgId: "branch-002")
│   │
│   └─ Enterprise (orgId: "ent-002")
```

### Hierarchy Properties

```
┌─────────────────────────────────────────────────────────┐
│         Hierarchy Properties                            │
└─────────────────────────────────────────────────────────┘

Root Organization:
├─ Each tenant has one root organization
├─ Root org has no parent (parent_id = null)
├─ All other orgs are descendants of root
└─ Root org ID stored in tenant.root_org_id

Parent-Child Relationships:
├─ Parent can have multiple children
├─ Child has exactly one parent
├─ Self-referencing foreign key (parent_id → id)
└─ Cascade delete: deleting parent deletes children

Depth:
├─ Unlimited depth supported
├─ Common patterns: 3-5 levels
├─ Performance optimized for deep hierarchies
└─ Path queries for efficient traversal
```

## Org Id in Multi-Tenant Context

### Tenant Isolation

```
┌─────────────────────────────────────────────────────────┐
│         Tenant Isolation Model                          │
└─────────────────────────────────────────────────────────┘

Tenant A:
├─ Root Org: root-A
├─ Org IDs: [org-A1, org-A2, org-A3, ...]
└─ Isolated from Tenant B

Tenant B:
├─ Root Org: root-B
├─ Org IDs: [org-B1, org-B2, org-B3, ...]
└─ Isolated from Tenant A

Key Points:
├─ Org IDs are unique within tenant
├─ Same org ID can exist in different tenants
├─ All queries filtered by tenant_id
└─ Cross-tenant access prevented
```

### Tenant-Org Relationship

```java
@Entity
@Table(name = "tenant")
public class Tenant {
    @Id
    private UUID id;
    
    @Column(name = "tenant_name")
    private String tenantName;
    
    @OneToOne
    @JoinColumn(name = "root_org_id")
    private OrgNode rootOrg;  // Root organization for tenant
}
```

## Organization Types

### Org Type System

```
┌─────────────────────────────────────────────────────────┐
│         Organization Types                              │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Categorize organizations
├─ Define hierarchy rules
├─ Apply type-specific logic
└─ Support business rules

Common Types:
├─ Enterprise
├─ Branch
├─ Firm
├─ Department
├─ Team
└─ Division

Type Hierarchy:
├─ Parent types can have child types
├─ Example: Enterprise → Branch → Firm
└─ Enforced at application level
```

### Org Type Model

```java
@Entity
@Table(name = "org_type")
public class OrgType {
    @Id
    private UUID id;
    
    @Column(name = "name")
    private String name;  // e.g., "Enterprise", "Branch"
    
    @Column(name = "tenant_id")
    private UUID tenantId;
    
    @Column(name = "parent_org_type")
    private String parentOrgType;  // Hierarchy of types
}
```

## Org Id Usage Patterns

### Pattern 1: User Assignment

```
┌─────────────────────────────────────────────────────────┐
│         User-Organization Mapping                       │
└─────────────────────────────────────────────────────────┘

Many-to-Many Relationship:
├─ Users can belong to multiple organizations
├─ Organizations can have multiple users
└─ Stored in org_user_mapping table

Mapping Table:
├─ org_node_id (Org Id)
├─ user_id (User Id)
└─ Unique constraint: (org_node_id, user_id)

Example:
User "John" belongs to:
├─ orgId: "branch-001" (role: advisor)
└─ orgId: "firm-001" (role: manager)
```

### Pattern 2: Access Control Context

```
┌─────────────────────────────────────────────────────────┐
│         Access Control with Org Id                      │
└─────────────────────────────────────────────────────────┘

Org Id as Context:
├─ Determines what data user can access
├─ Filters queries by organization
├─ Enforces organizational boundaries
└─ Supports hierarchical permissions

Example:
User searches for users:
├─ With orgId: "branch-001"
├─ Returns: Users in branch-001 and child orgs
└─ Excludes: Users in other branches
```

### Pattern 3: Resource Access

```
┌─────────────────────────────────────────────────────────┐
│         Resource Access with Org Id                     │
└─────────────────────────────────────────────────────────┘

Resource-Org Relationship:
├─ Resources belong to organizations
├─ Org Id determines resource visibility
├─ Users access resources through org membership
└─ Hierarchical access inheritance

Example:
Resource "Account-123":
├─ Belongs to: orgId "firm-001"
├─ Accessible by: Users in firm-001 and parent orgs
└─ Not accessible by: Users in other firms
```

## Org Id Identifiers

### Internal ID (UUID)

```
┌─────────────────────────────────────────────────────────┐
│         Internal Org Id (UUID)                          │
└─────────────────────────────────────────────────────────┘

Format:
├─ UUID v4
├─ Example: "550e8400-e29b-41d4-a716-446655440000"
├─ Generated by database
└─ Primary key in org_node table

Usage:
├─ Internal system operations
├─ Database foreign keys
├─ API responses
└─ Service-to-service communication
```

### External ID

```
┌─────────────────────────────────────────────────────────┐
│         External Org Id                                 │
└─────────────────────────────────────────────────────────┘

Purpose:
├─ Integration with external systems
├─ Human-readable identifiers
├─ Legacy system compatibility
└─ Business identifier mapping

Format:
├─ String (custom format)
├─ Example: "ACME-BRANCH-001"
├─ Unique within tenant
└─ Optional field

Usage:
├─ External system integration
├─ CSV imports/exports
├─ Business reporting
└─ User-facing displays
```

## Org Id in API Requests

### Request Headers

```
┌─────────────────────────────────────────────────────────┐
│         Org Id in Request Headers                       │
└─────────────────────────────────────────────────────────┘

Required Headers:
├─ X-Jiffy-Tenant-ID: Tenant identifier
└─ X-Jiffy-App-ID: Application identifier

Optional Headers:
├─ X-Jiffy-User-ID: Current user identifier
└─ X-Jiffy-Org-ID: Organization context (some APIs)

Usage:
├─ Tenant ID: Required for all org operations
├─ App ID: Required for org hierarchy lookups
└─ User ID: For access validation
```

### Query Parameters

```
┌─────────────────────────────────────────────────────────┐
│         Org Id as Query Parameter                       │
└─────────────────────────────────────────────────────────┘

Common Patterns:
├─ ?orgId=550e8400-e29b-41d4-a716-446655440000
├─ ?filterOrgId=550e8400-e29b-41d4-a716-446655440000
└─ ?organizationId=550e8400-e29b-41d4-a716-446655440000

Usage:
├─ Filtering search results
├─ Scoping operations to specific org
├─ Access control validation
└─ Hierarchy expansion
```

### Path Parameters

```
┌─────────────────────────────────────────────────────────┐
│         Org Id in URL Path                              │
└─────────────────────────────────────────────────────────┘

Pattern:
├─ /oms/api/v2/org/{orgId}
├─ /oms/api/v2/user?orgId={orgId}
└─ /apexiam/v1/user/tenant?orgId={orgId}

Examples:
├─ GET /oms/api/v2/org/550e8400-e29b-41d4-a716-446655440000
├─ GET /oms/api/v2/user?orgId=550e8400-e29b-41d4-a716-446655440000
└─ DELETE /oms/api/v2/org/550e8400-e29b-41d4-a716-446655440000
```

## Org Id Validation

### Validation Rules

```
┌─────────────────────────────────────────────────────────┐
│         Org Id Validation                                │
└─────────────────────────────────────────────────────────┘

Format Validation:
├─ Must be valid UUID format
├─ Cannot be null or empty
└─ Must exist in database

Existence Validation:
├─ Org must exist in tenant
├─ Org must not be deleted
└─ Org must be accessible

Access Validation:
├─ User must have access to org
├─ Org must be in user's accessible orgs
└─ Or org must be child of accessible org
```

### Validation Flow

```
┌─────────────────────────────────────────────────────────┐
│         Validation Process                              │
└─────────────────────────────────────────────────────────┘

1. Format Check
   │
   ├─ Is valid UUID?
   └─ Is not null/empty?

2. Existence Check
   │
   ├─ Does org exist in tenant?
   └─ Is org not deleted?

3. Access Check
   │
   ├─ Is user authorized?
   └─ Is org accessible?

4. Return Result
   │
   ├─ Valid → Proceed
   └─ Invalid → Error
```

## Org Id Best Practices

### 1. Always Validate

```
┌─────────────────────────────────────────────────────────┐
│         Validation Best Practices                       │
└─────────────────────────────────────────────────────────┘

Before Using Org Id:
├─ Validate format (UUID)
├─ Check existence in tenant
├─ Verify user access
└─ Handle validation errors

Error Handling:
├─ Return clear error messages
├─ Log validation failures
├─ Use appropriate HTTP status codes
└─ Provide helpful error details
```

### 2. Use Tenant Context

```
┌─────────────────────────────────────────────────────────┐
│         Tenant Context Best Practices                   │
└─────────────────────────────────────────────────────────┘

Always Include Tenant:
├─ All org operations require tenant
├─ Filter queries by tenant_id
├─ Prevent cross-tenant access
└─ Validate tenant-org relationship

Query Pattern:
SELECT * FROM org_node 
WHERE id = :orgId 
AND tenant_id = :tenantId
```

### 3. Handle Hierarchy

```
┌─────────────────────────────────────────────────────────┐
│         Hierarchy Best Practices                        │
└─────────────────────────────────────────────────────────┘

Consider Children:
├─ Org operations often affect children
├─ Search includes child orgs
├─ Access inherits from parent
└─ Deletion cascades to children

Query Pattern:
WITH RECURSIVE org_tree AS (
    SELECT * FROM org_node WHERE id = :orgId
    UNION ALL
    SELECT o.* FROM org_node o
    INNER JOIN org_tree ot ON o.parent_id = ot.id
)
SELECT * FROM org_tree
```

## Summary

**Org Id Fundamentals:**
- **Definition**: Unique identifier for organizations within a tenant
- **Structure**: Hierarchical tree with parent-child relationships
- **Isolation**: Tenant-scoped, prevents cross-tenant access
- **Types**: Categorized by organization types (Enterprise, Branch, etc.)
- **Usage**: User assignment, access control, resource access

**Key Characteristics:**
- UUID format (internal identifier)
- Supports external IDs for integration
- Unlimited hierarchy depth
- Many-to-many user relationships
- Cascade operations for children

**Best Practices:**
- Always validate org ID format and existence
- Include tenant context in all operations
- Consider hierarchy in operations
- Handle access control validation
- Use appropriate error handling

**Remember**: Org Id is the foundation for organizational structure, access control, and data segregation in the multi-tenant system. Understanding its fundamentals is essential for working with organization-related APIs and features.

# Liquibase Migrations in Apex IAM - Detailed Explanation

## Table of Contents
1. [Overview](#overview)
2. [Configuration](#configuration)
3. [Master Changelog Structure](#master-changelog-structure)
4. [Migration Patterns](#migration-patterns)
5. [Migration Examples](#migration-examples)
6. [Migration Execution Flow](#migration-execution-flow)
7. [Best Practices Observed](#best-practices-observed)
8. [Migration Categories](#migration-categories)

---

## Overview

### What is Liquibase?

Liquibase is an open-source database-independent library for tracking, managing, and applying database schema changes. In the Apex IAM project, Liquibase is used to manage PostgreSQL database schema evolution in a version-controlled, repeatable manner.

### Key Benefits

```
┌─────────────────────────────────────────────────────────┐
│         Liquibase Benefits                            │
└─────────────────────────────────────────────────────────┘

1. Version Control
   ├─ Database changes tracked in Git
   ├─ Complete history of schema evolution
   └─ Rollback capabilities

2. Consistency
   ├─ Same migrations run across environments
   ├─ Reproducible database state
   └─ Team collaboration

3. Safety
   ├─ Preconditions prevent errors
   ├─ Idempotent changes
   └─ Transaction support

4. Automation
   ├─ Automatic execution on application startup
   ├─ CI/CD integration
   └─ No manual SQL scripts needed
```

---

## Configuration

### 1. Liquibase Properties File

**Location**: `src/main/resources/liquibase.properties`

```properties
url=jdbc:postgresql://localhost:5432/iam
username=iam
password=password
driver=org.postgresql.Driver
changeLogFile=src/main/resources/db/changelog/db.changelog-master.xml
referenceUrl=jdbc:postgresql://localhost:5434/iam_liquibase
referenceUsername=postgres
referencePassword=postgres
diffChangeLogFile=src/main/resources/db.changelog/db.changelog-master.xml
```

**Configuration Details**:
- **url**: Target database connection (PostgreSQL)
- **username/password**: Database credentials
- **changeLogFile**: Master changelog file path
- **referenceUrl**: Reference database for diff operations
- **diffChangeLogFile**: Output file for generated diffs

### 2. Spring Boot Configuration

**Location**: `src/main/resources/application.yaml`

```yaml
spring:
  liquibase:
    enabled: false  # Disabled by default, enabled per environment
    change-log: classpath:db/changelog/db.changelog-master.xml
```

**Configuration Notes**:
- Liquibase is **disabled by default** (`enabled: false`)
- Enabled per environment (dev, prod) via profile-specific configs
- Master changelog path specified relative to classpath

---

## Master Changelog Structure

### Master Changelog File

**Location**: `src/main/resources/db/changelog/db.changelog-master.xml`

The master changelog includes **55 individual migration files** in chronological order:

```xml
<databaseChangeLog>
    <include file="db/changelog/initial-change-log.xml" />
    <include file="db/changelog/user-group-unique-comb.xml" />
    <include file="db/changelog/remove-app-collab-group-mapping.xml" />
    <!-- ... 52 more includes ... -->
    <include file="db/changelog/update-app-permission-to-service-permission-mapping-add-enabled.xml"/>
</databaseChangeLog>
```

### Migration File Organization

```
┌─────────────────────────────────────────────────────────┐
│         Migration File Organization                   │
└─────────────────────────────────────────────────────────┘

db/changelog/
├── db.changelog-master.xml (Master file)
├── initial-change-log.xml (Initial schema)
├── user-group-unique-comb.xml (Constraint addition)
├── add-public-user-signup-tables.xml (New tables)
├── user-add-external-user-id.xml (Column addition)
├── update-user-env-name.xml (Data migration)
├── drop-deprecated-models.xml (Table removal)
└── ... (50+ more migration files)
```

---

## Migration Patterns

### Pattern 1: Table Creation with Preconditions

**Example**: `initial-change-log.xml`

```xml
<changeSet author="bhargav.m (generated)" id="1671613407727-1">
    <preConditions onFail="MARK_RAN">
        <not>
            <tableExists tableName="apex_user"/>
        </not>
    </preConditions>
    <createTable tableName="apex_user">
        <column name="user_id" type="VARCHAR(40)">
            <constraints nullable="false" primaryKey="true" 
                         primaryKeyName="apex_user_pkey"/>
        </column>
        <column name="disabled" type="BOOLEAN"/>
        <column name="email" type="VARCHAR(255)"/>
        <!-- More columns -->
    </createTable>
</changeSet>
```

**Key Features**:
- **Precondition**: Checks if table doesn't exist
- **onFail="MARK_RAN"**: Marks as executed if precondition fails
- **Idempotent**: Safe to run multiple times

### Pattern 2: Column Addition with SQL

**Example**: `user-add-external-user-id.xml`

```xml
<changeSet id="03102024-2" author="iam">
    <validCheckSum>ANY</validCheckSum>
    <sql splitStatements="false" stripComments="false">
        ALTER TABLE IF EXISTS apex_user 
        ADD COLUMN IF NOT EXISTS external_user_id character varying(255);
    </sql>
</changeSet>
```

**Key Features**:
- **Raw SQL**: Direct SQL execution
- **IF EXISTS/IF NOT EXISTS**: PostgreSQL-specific safety
- **validCheckSum="ANY"**: Allows checksum changes (for SQL changes)

### Pattern 3: Data Migration

**Example**: `update-user-env-name.xml`

```xml
<changeSet id="update_user_env_name" author="iam">
    <validCheckSum>ANY</validCheckSum>
    <sql splitStatements="false" stripComments="false">
        UPDATE apex_user 
        SET env_name = 'common' 
        WHERE env_name IS null OR env_name = '';
    </sql>
</changeSet>
```

**Key Features**:
- **Data updates**: Modifies existing data
- **Conditional updates**: Only updates specific rows
- **Idempotent**: Safe to re-run

### Pattern 4: Complex Table Creation

**Example**: `add-public-user-signup-tables.xml`

```xml
<changeSet id="create-registration-request-table" author="iam">
    <preConditions onFail="MARK_RAN">
        <not>
            <tableExists tableName="registration_request"/>
        </not>
    </preConditions>

    <createTable tableName="registration_request">
        <column name="reference_id" type="uuid">
            <constraints primaryKey="true" nullable="false"/>
        </column>
        <column name="first_name" type="varchar(255)"/>
        <column name="email" type="varchar(255)"/>
        <column name="status" type="varchar(20)">
            <constraints nullable="false"/>
        </column>
        <column name="created_at" type="timestamp" 
                defaultValueComputed="CURRENT_TIMESTAMP">
            <constraints nullable="false"/>
        </column>
    </createTable>
</changeSet>

<changeSet id="create-id-provider-public-signup-apps-table" author="iam">
    <!-- Table creation -->
    <addForeignKeyConstraint
        baseTableName="id_provider_public_signup_apps"
        baseColumnNames="id_provider_id"
        referencedTableName="id_provider"
        referencedColumnNames="provider_id"
        constraintName="fk_id_provider_public_signup_apps"/>
</changeSet>
```

**Key Features**:
- **Multiple changesets**: Separate changesets for related changes
- **Foreign keys**: Added after table creation
- **Default values**: Using `defaultValueComputed`

### Pattern 5: Constraint Addition with Data Cleanup

**Example**: `user-group-unique-comb.xml`

```xml
<changeSet author="apex-iam" id="1677489243000-1">
    <validCheckSum>ANY</validCheckSum>
    <sql splitStatements="false" stripComments="false">
        -- Remove duplicates before adding constraint
        WITH duplicates AS (
            SELECT ug_mapping_id, ROW_NUMBER() OVER(
                PARTITION BY apex_group, apex_user
            ) AS rownum
            FROM user_group_mapping
        )
        DELETE FROM user_group_mapping
        USING duplicates
        WHERE user_group_mapping.ug_mapping_id = duplicates.ug_mapping_id 
          AND duplicates.rownum > 1;

        -- Add unique constraint
        ALTER TABLE user_group_mapping 
        ADD CONSTRAINT uq_user_group_combination 
        UNIQUE(apex_user, apex_group);
    </sql>
</changeSet>
```

**Key Features**:
- **Data cleanup**: Removes duplicates before constraint
- **Complex SQL**: Uses CTEs and window functions
- **Two-step process**: Cleanup then constraint

### Pattern 6: Column Modifications

**Example**: `add-new-columns-to-service-permissions.xml`

```xml
<changeSet id="update-columns-in-service-permission" author="iam">
    <validCheckSum>ANY</validCheckSum>
    <sql splitStatements="false" stripComments="false">
        -- Drop and recreate column (type change)
        ALTER TABLE IF EXISTS service_permission 
        DROP COLUMN IF EXISTS http_verb;
        ALTER TABLE IF EXISTS service_permission 
        ADD COLUMN IF NOT EXISTS http_verb integer;
        
        -- Add new columns
        ALTER TABLE IF EXISTS service_permission 
        ADD COLUMN IF NOT EXISTS service_type integer;
        ALTER TABLE IF EXISTS service_permission 
        ADD COLUMN IF NOT EXISTS component_id character varying(255);
    </sql>
</changeSet>
```

**Key Features**:
- **Column type changes**: Drop and recreate
- **Multiple operations**: Multiple ALTER statements
- **Safe execution**: IF EXISTS/IF NOT EXISTS guards

### Pattern 7: Unique Constraint with Precondition Check

**Example**: `add-new-columns-to-service-permissions.xml`

```xml
<changeSet id="add-unique-constraint-app_permission_to_service_permission_mapping" 
          author="iam">
    <preConditions onFail="MARK_RAN">
        <sqlCheck expectedResult="0">
            SELECT COUNT(*)
            FROM information_schema.table_constraints
            WHERE table_name = 'app_permission_to_service_permission_mapping'
            AND constraint_name = 'uq_app_permission_to_service_permission_mapping_app_permission_id_service_permission_id'
            AND constraint_type = 'UNIQUE';
        </sqlCheck>
    </preConditions>
    <addUniqueConstraint
        columnNames="app_permission_id, service_permission_id"
        constraintName="uq_app_permission_to_service_permission_mapping_app_permission_id_service_permission_id"
        tableName="app_permission_to_service_permission_mapping" />
</changeSet>
```

**Key Features**:
- **SQL precondition**: Checks constraint existence
- **Information schema query**: Uses PostgreSQL system tables
- **Liquibase tag**: Uses `<addUniqueConstraint>` tag

### Pattern 8: Table/Column Removal with Foreign Key Handling

**Example**: `drop-deprecated-models.xml`

```xml
<changeSet id="drop-application-column-from-role" author="iam">
    <preConditions onFail="CONTINUE">
        <columnExists tableName="role" columnName="application"/>
    </preConditions>

    <sql splitStatements="false" stripComments="false">
        <![CDATA[
        DO $$
        DECLARE
            fk_name text;
        BEGIN
            -- Find foreign key constraint
            SELECT tc.constraint_name INTO fk_name
            FROM information_schema.table_constraints tc
            JOIN information_schema.key_column_usage kcu
                ON kcu.constraint_name = tc.constraint_name
            WHERE tc.table_name = 'role'
              AND tc.constraint_type = 'FOREIGN KEY'
              AND kcu.column_name = 'application';

            -- Drop FK if exists
            IF fk_name IS NOT NULL THEN
                EXECUTE 'ALTER TABLE role DROP CONSTRAINT ' || quote_ident(fk_name);
            END IF;
        END $$;
        ]]>
    </sql>

    <dropColumn tableName="role" columnName="application"/>
</changeSet>
```

**Key Features**:
- **PL/pgSQL block**: Uses PostgreSQL procedural language
- **Dynamic SQL**: Finds and drops FK constraints
- **Two-step**: Drop FK, then drop column
- **CDATA**: Preserves SQL formatting

---

## Migration Examples

### Example 1: Initial Schema Creation

**File**: `initial-change-log.xml`

**Purpose**: Creates the base database schema

**Key Tables Created**:
- `apex_user` - User entity
- `apex_group` - Group entity
- `role` - Role entity
- `application` - Application entity
- `tenant` - Tenant entity
- Mapping tables (user_group_mapping, user_role_mapping, etc.)

**Features**:
- 53 changesets in single file
- Creates tables, indexes, foreign keys
- Uses preconditions for idempotency
- Generated from JPA entities (indicated by "generated" author)

### Example 2: Adding New Feature Tables

**File**: `add-public-user-signup-tables.xml`

**Purpose**: Adds tables for public user signup feature

**Tables Created**:
- `registration_request` - User registration requests
- `id_provider_public_signup_apps` - Signup app configuration

**Features**:
- UUID primary keys
- Timestamp columns with defaults
- Foreign key relationships
- Well-documented with comments

### Example 3: Data Type Migration

**File**: `organisation_id_data_type_change.xml`

**Purpose**: Changes organization ID data type (likely VARCHAR to UUID)

**Pattern**: Drop column, add new column, migrate data, drop old column

### Example 4: Adding Columns to Existing Tables

**Files**:
- `user-add-external-user-id.xml` - Adds external_user_id
- `add_mobile_number_to_apex_user.xml` - Adds mobile_number
- `user-add-display-name.xml` - Adds display_name

**Pattern**: Simple ALTER TABLE ADD COLUMN statements

### Example 5: Provider Model Updates

**Files**:
- `update-provider-model.xml` - Updates provider table structure
- `update-provider-add-sync-to-keycloak.xml` - Adds Keycloak sync
- `update-provider-add-sso-token-user-attribute-map.xml` - Adds SSO mapping

**Pattern**: Incremental provider feature additions

---

## Migration Execution Flow

### Execution Process

```
┌─────────────────────────────────────────────────────────┐
│         Liquibase Execution Flow                      │
└─────────────────────────────────────────────────────────┘

1. Application Startup
   │
   ├─ Spring Boot detects Liquibase dependency
   ├─ Reads application.yaml configuration
   └─ Checks if liquibase.enabled = true
   │
   ▼
2. Connect to Database
   │
   ├─ Uses datasource configuration
   ├─ Connects to PostgreSQL
   └─ Checks for DATABASECHANGELOG table
   │
   ▼
3. Read Master Changelog
   │
   ├─ Loads db.changelog-master.xml
   ├─ Resolves all included files
   └─ Builds execution plan
   │
   ▼
4. Check Applied Changesets
   │
   ├─ Queries DATABASECHANGELOG table
   ├─ Compares with changelog files
   └─ Identifies pending changesets
   │
   ▼
5. Execute Pending Changesets
   │
   ├─ For each pending changeset:
   │   ├─ Check preconditions
   │   ├─ Execute changeset
   │   ├─ Record in DATABASECHANGELOG
   │   └─ Record checksum
   │
   └─ Continue until all executed
   │
   ▼
6. Application Continues
   │
   └─ Spring Boot continues startup
```

### Database Tracking Tables

Liquibase creates tracking tables:

```sql
-- Tracks executed changesets
DATABASECHANGELOG
├─ ID (changeset id)
├─ AUTHOR (changeset author)
├─ FILENAME (changelog file)
├─ DATEEXECUTED (execution timestamp)
├─ ORDEREXECUTED (execution order)
├─ EXECTYPE (EXECUTED, SKIPPED, etc.)
├─ MD5SUM (checksum)
└─ DESCRIPTION

-- Tracks lock (prevents concurrent execution)
DATABASECHANGELOGLOCK
├─ ID
├─ LOCKED (boolean)
├─ LOCKGRANTED (timestamp)
└─ LOCKEDBY (process identifier)
```

---

## Best Practices Observed

### 1. Idempotent Changesets

✅ **Good Practice**: All changesets use preconditions or IF EXISTS/IF NOT EXISTS

```xml
<preConditions onFail="MARK_RAN">
    <not>
        <tableExists tableName="apex_user"/>
    </not>
</preConditions>
```

**Benefit**: Safe to re-run migrations

### 2. Descriptive Changeset IDs

✅ **Good Practice**: Meaningful IDs

```xml
<changeSet id="create-registration-request-table" author="iam">
<changeSet id="add-unique-constraint-app_permission_to_service_permission_mapping" author="iam">
```

**Benefit**: Easy to identify purpose

### 3. Author Attribution

✅ **Good Practice**: All changesets have authors

```xml
author="iam"
author="bhargav.m (generated)"
```

**Benefit**: Accountability and traceability

### 4. Preconditions for Safety

✅ **Good Practice**: Extensive use of preconditions

```xml
<preConditions onFail="MARK_RAN">
<preConditions onFail="CONTINUE">
```

**Benefit**: Prevents errors on re-execution

### 5. Valid Checksum Handling

✅ **Good Practice**: Uses `validCheckSum="ANY"` for SQL changesets

```xml
<validCheckSum>ANY</validCheckSum>
```

**Benefit**: Allows SQL modifications without checksum errors

### 6. Separate Changesets for Related Changes

✅ **Good Practice**: One logical change per changeset

```xml
<changeSet id="create-table">...</changeSet>
<changeSet id="add-foreign-key">...</changeSet>
```

**Benefit**: Better granularity and rollback

### 7. SQL Comments

✅ **Good Practice**: Comments in SQL for clarity

```sql
-- Remove duplicates before adding constraint
WITH duplicates AS ...
```

**Benefit**: Self-documenting migrations

---

## Migration Categories

### Category 1: Schema Creation (Initial)

**Files**: `initial-change-log.xml`

**Purpose**: Creates base schema

**Tables**: Core entities (user, group, role, application, tenant)

### Category 2: Column Additions

**Files**:
- `user-add-external-user-id.xml`
- `add_mobile_number_to_apex_user.xml`
- `user-add-display-name.xml`
- `add_env_name_to_apex_user.xml`
- `add_service_client_id_to_apex_user.xml`

**Purpose**: Add new columns to existing tables

### Category 3: Table Additions

**Files**:
- `add-public-user-signup-tables.xml`
- `add-post-user-creation-hook-config-table.xml`
- `add-user-entitlements-model.xml`
- `add-field-access-permission-models.xml`

**Purpose**: Add new feature tables

### Category 4: Constraints

**Files**:
- `user-group-unique-comb.xml`
- `permission-unique-constraint.xml`
- `add-new-columns-to-service-permissions.xml` (unique constraints)

**Purpose**: Add unique constraints, foreign keys

### Category 5: Data Migrations

**Files**:
- `update-user-env-name.xml`
- `user-group-role-update-records.xml`
- `insert-local-provider-per-tenant.xml`
- `insert-azure-provider.xml`

**Purpose**: Update existing data

### Category 6: Schema Modifications

**Files**:
- `alter-user-group-role.xml`
- `organisation_id_data_type_change.xml`
- `update-destroy-status-field-to-uuid.xml`
- `add-new-columns-to-service-permissions.xml`

**Purpose**: Modify existing schema (column types, add columns)

### Category 7: Provider/IDP Updates

**Files**:
- `idp-providers-db.xml`
- `update-provider-model.xml`
- `update-provider-add-sync-to-keycloak.xml`
- `update-provider-add-sso-token-user-attribute-map.xml`
- `insert-local-provider-per-tenant.xml`
- `insert-azure-provider.xml`

**Purpose**: Identity provider related changes

### Category 8: Permission/Role Updates

**Files**:
- `app-permission.xml`
- `service-permission.xml`
- `permission-add-permission-columns.xml`
- `app-permission-role-mapping.xml`
- `add-role-accessible-application.xml`
- `update-role-add-is-permission-group.xml`

**Purpose**: Permission and role model changes

### Category 9: Cleanup/Removal

**Files**:
- `remove-app-collab-group-mapping.xml`
- `drop-deprecated-models.xml`
- `destroy-status-db.xml`

**Purpose**: Remove deprecated tables/columns

### Category 10: Feature Additions

**Files**:
- `add-public-user-signup-tables.xml`
- `add-post-user-creation-hook-config-table.xml`
- `add-default-org-public-signup-config-model.xml`
- `add-landing-page-to-user-signup-table.xml`

**Purpose**: New feature support

---

## Summary

### Key Statistics

- **Total Migration Files**: 55
- **Master Changelog**: 1 (`db.changelog-master.xml`)
- **Database**: PostgreSQL
- **Pattern**: XML-based changesets
- **Preconditions**: Extensive use for safety
- **SQL Usage**: Mix of Liquibase tags and raw SQL

### Migration Evolution

The migrations show a clear evolution:

1. **Initial Schema** (initial-change-log.xml) - Base tables
2. **Feature Additions** - New columns, tables for features
3. **Data Migrations** - Updates to existing data
4. **Refactoring** - Schema improvements, type changes
5. **Cleanup** - Removal of deprecated models

### Best Practices Summary

✅ Idempotent changesets  
✅ Preconditions for safety  
✅ Descriptive IDs and authors  
✅ Separate changesets for logical changes  
✅ SQL comments for clarity  
✅ Proper handling of foreign keys  
✅ Data cleanup before constraints  

### Common Patterns

1. **Table Creation**: Precondition check → Create table → Add constraints
2. **Column Addition**: IF NOT EXISTS guard → ALTER TABLE ADD COLUMN
3. **Data Migration**: UPDATE statements with conditions
4. **Constraint Addition**: Data cleanup → Add constraint
5. **Table Removal**: Drop FKs → Drop table

This migration structure demonstrates a mature, well-organized database evolution strategy using Liquibase best practices.

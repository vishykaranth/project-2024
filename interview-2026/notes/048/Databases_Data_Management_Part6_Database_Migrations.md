# Databases & Data Management - In Depth Diagrams (Part 6: Database Migrations)

## 🔄 Database Migrations: Flyway, Liquibase, Version Control

---

## 1. Database Migration Concept

### What are Migrations?
```
┌─────────────────────────────────────────────────────────────┐
│              Database Migration Concept                      │
└─────────────────────────────────────────────────────────────┘

Problem: Database Schema Evolution
    Version 1.0              Version 2.0              Version 3.0
    ┌──────────┐            ┌──────────┐            ┌──────────┐
    │ users    │            │ users    │            │ users    │
    │ - id     │            │ - id     │            │ - id     │
    │ - name   │    ────►   │ - name   │    ────►   │ - name   │
    │          │            │ - email  │            │ - email  │
    └──────────┘            │ - status │            │ - status │
                           └──────────┘            │ - role   │
                                                    └──────────┘
    
Solution: Versioned Migrations
    ┌──────────────────────────────────────┐
    │ Migration Scripts                    │
    │                                      │
    │ V1__Create_users_table.sql           │
    │ V2__Add_email_to_users.sql           │
    │ V3__Add_status_to_users.sql          │
    │ V4__Add_role_to_users.sql            │
    └──────────────────────────────────────┘
         │
         ▼
    ┌──────────────────────────────────────┐
    │ Migration Tool                       │
    │ (Flyway/Liquibase)                  │
    │                                      │
    │ - Tracks applied migrations         │
    │ - Applies new migrations             │
    │ - Rollback support                   │
    └──────────────────────────────────────┘
```

### Migration Lifecycle
```
┌─────────────────────────────────────────────────────────────┐
│              Migration Lifecycle                            │
└─────────────────────────────────────────────────────────────┘

Development:
    ┌──────────────┐
    │ Developer    │
    │ Creates      │
    │ Migration    │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Migration    │
    │ Script       │
    │ (SQL/Java)   │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Version      │
    │ Control      │
    │ (Git)        │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ CI/CD        │
    │ Pipeline     │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Test         │
    │ Environment  │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Production   │
    │ Environment  │
    └──────────────┘
```

---

## 2. Flyway

### Flyway Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Flyway Architecture                            │
└─────────────────────────────────────────────────────────────┘

Application:
    ┌──────────────┐
    │ Application │
    │   Code      │
    └──────┬───────┘
           │
           │ Flyway API
           ▼
    ┌──────────────┐
    │   Flyway     │
    │              │
    │ - Scans      │
    │   migrations │
    │ - Checks     │
    │   schema     │
    │ - Applies    │
    │   pending    │
    └──────┬───────┘
           │
    ┌──────┴──────────────────────┐
    │                              │
    ▼                              ▼
┌──────────┐              ┌──────────────┐
│Migration │              │   Database   │
│ Scripts  │              │              │
│          │              │ flyway_schema│
│V1__...sql│              │ _history     │
│V2__...sql│              │              │
│V3__...sql│              │ - version    │
└──────────┘              │ - checksum   │
                          │ - installed_on│
                          └──────────────┘
```

### Flyway Naming Convention
```
┌─────────────────────────────────────────────────────────────┐
│              Flyway Migration Naming                        │
└─────────────────────────────────────────────────────────────┘

Pattern:
<VERSION>__<DESCRIPTION>.<EXTENSION>

Examples:
V1__Create_users_table.sql
V2__Add_email_column.sql
V3__Create_orders_table.sql
V4.1__Fix_email_constraint.sql
V5__Add_indexes.sql

Version Formats:
- Versioned: V1, V2, V3, ...
- Undo: U1, U2, U3, ... (rollback)
- Repeatable: R__description.sql (always runs)

Directory Structure:
src/main/resources/
    └── db/
        └── migration/
            ├── V1__Create_users_table.sql
            ├── V2__Add_email_column.sql
            └── V3__Create_orders_table.sql
```

### Flyway Migration Table
```
┌─────────────────────────────────────────────────────────────┐
│              Flyway Schema History Table                     │
└─────────────────────────────────────────────────────────────┘

flyway_schema_history:
┌──────┬──────────────────────┬──────────┬─────────────┬──────────────┐
│installed_rank│ version │ description │ type │ script │ installed_on │
├──────┼──────────────────────┼──────────┼─────────────┼──────────────┤
│  1   │ 1      │ Create users │ SQL │ V1__...│ 2024-01-01 │
│  2   │ 2      │ Add email    │ SQL │ V2__...│ 2024-01-02 │
│  3   │ 3      │ Create orders│ SQL │ V3__...│ 2024-01-03 │
└──────┴──────────────────────┴──────────┴─────────────┴──────────────┘

Columns:
- installed_rank: Execution order
- version: Migration version
- description: Migration description
- type: SQL or JAVA
- script: Script filename
- installed_on: Timestamp
- execution_time: Duration
- success: Success flag
```

### Flyway Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Flyway Configuration                           │
└─────────────────────────────────────────────────────────────┘

application.yml (Spring Boot):
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    baseline-version: 0
    validate-on-migrate: true
    clean-disabled: true
    out-of-order: false
    placeholder-replacement: true
    placeholders:
      table_prefix: app_
      schema_name: public

Java Configuration:
Flyway flyway = Flyway.configure()
    .dataSource(url, user, password)
    .locations("classpath:db/migration")
    .baselineOnMigrate(true)
    .validateOnMigrate(true)
    .load();

flyway.migrate();
```

### Flyway Workflow
```
┌─────────────────────────────────────────────────────────────┐
│              Flyway Migration Workflow                      │
└─────────────────────────────────────────────────────────────┘

Application Startup:
    ┌──────────────┐
    │ Application │
    │   Starts    │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Flyway       │
    │ Initializes  │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Check Schema │
    │ History      │
    └──────┬───────┘
           │
    ┌──────┴───────┐
    │              │
    │ Exists       │ Not Exists
    │              │
    ▼              ▼
┌──────────┐  ┌──────────┐
│ Read     │  │ Baseline │
│ Applied  │  │ Schema   │
│ Versions │  └──────────┘
└────┬─────┘
     │
     ▼
┌──────────┐
│ Scan     │
│ Migration│
│ Scripts  │
└────┬─────┘
     │
     ▼
┌──────────┐
│ Compare  │
│ Versions │
└────┬─────┘
     │
     ▼
┌──────────┐
│ Apply    │
│ Pending  │
│ Migrations│
└────┬─────┘
     │
     ▼
┌──────────┐
│ Update   │
│ History  │
└──────────┘
```

---

## 3. Liquibase

### Liquibase Architecture
```
┌─────────────────────────────────────────────────────────────┐
│              Liquibase Architecture                         │
└─────────────────────────────────────────────────────────────┘

Application:
    ┌──────────────┐
    │ Application │
    │   Code      │
    └──────┬───────┘
           │
           │ Liquibase API
           ▼
    ┌──────────────┐
    │  Liquibase   │
    │              │
    │ - Reads      │
    │   changelog  │
    │ - Tracks     │
    │   changes    │
    │ - Applies    │
    │   changesets │
    └──────┬───────┘
           │
    ┌──────┴──────────────────────┐
    │                              │
    ▼                              ▼
┌──────────┐              ┌──────────────┐
│Changelog │              │   Database   │
│ Files    │              │              │
│          │              │DATABASECHANGE│
│db.changelog│            │LOG           │
│-master.xml│            │              │
│          │              │ - id         │
│-001.xml  │              │ - author     │
│-002.xml  │              │ - filename   │
└──────────┘              │ - dateexecuted│
                          └──────────────┘
```

### Liquibase Changelog Structure
```
┌─────────────────────────────────────────────────────────────┐
│              Liquibase Changelog                             │
└─────────────────────────────────────────────────────────────┘

db.changelog-master.xml:
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    
    <include file="db/changelog/changes/001-create-users.xml"/>
    <include file="db/changelog/changes/002-add-email.xml"/>
    <include file="db/changelog/changes/003-create-orders.xml"/>
    
</databaseChangeLog>

001-create-users.xml:
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog>
    <changeSet id="1" author="developer">
        <createTable tableName="users">
            <column name="id" type="BIGINT">
                <constraints primaryKey="true"/>
            </column>
            <column name="name" type="VARCHAR(100)">
                <constraints nullable="false"/>
            </column>
        </createTable>
    </changeSet>
</databaseChangeLog>
```

### Liquibase ChangeSet
```
┌─────────────────────────────────────────────────────────────┐
│              Liquibase ChangeSet                             │
└─────────────────────────────────────────────────────────────┘

ChangeSet Structure:
    ┌──────────────┐
    │  ChangeSet   │
    │              │
    │ - id         │  ← Unique identifier
    │ - author     │  ← Author name
    │ - changes    │  ← List of changes
    └──────────────┘

Example:
<changeSet id="2" author="developer">
    <addColumn tableName="users">
        <column name="email" type="VARCHAR(255)"/>
    </addColumn>
    <createIndex tableName="users" indexName="idx_email">
        <column name="email"/>
    </createIndex>
</changeSet>

ChangeSet Tracking:
- Tracked in DATABASECHANGELOG
- Each changeset executed once
- Identified by (id, author, filename)
- Checksum validation
```

### Liquibase Formats
```
┌─────────────────────────────────────────────────────────────┐
│              Liquibase Format Support                        │
└─────────────────────────────────────────────────────────────┘

Supported Formats:
1. XML (default):
   <createTable tableName="users">...</createTable>
   
2. YAML:
   databaseChangeLog:
     - changeSet:
         id: 1
         author: developer
         changes:
           - createTable:
               tableName: users
               
3. JSON:
   {
     "databaseChangeLog": [{
       "changeSet": {
         "id": "1",
         "author": "developer",
         "changes": [...]
       }
     }]
   }
   
4. SQL (raw SQL):
   --liquibase formatted sql
   --changeset developer:1
   CREATE TABLE users (...);
```

### Liquibase Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Liquibase Configuration                        │
└─────────────────────────────────────────────────────────────┘

application.yml (Spring Boot):
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/db.changelog-master.xml
    drop-first: false
    contexts: dev,prod
    default-schema: public
    liquibase-schema: public

Java Configuration:
Liquibase liquibase = new Liquibase(
    "db/changelog/db.changelog-master.xml",
    new ClassLoaderResourceAccessor(),
    database
);

liquibase.update("");
```

---

## 4. Flyway vs Liquibase

### Comparison Matrix
```
┌─────────────────────────────────────────────────────────────┐
│              Flyway vs Liquibase Comparison                  │
└─────────────────────────────────────────────────────────────┘

Feature              │  Flyway        │  Liquibase
─────────────────────┼────────────────┼──────────────────
Format               │  SQL/Java      │  XML/YAML/JSON/SQL
Naming                │  Strict        │  Flexible
Rollback              │  Limited       │  Built-in
Change Tracking       │  File-based    │  ChangeSet-based
Complexity            │  Simple        │  More complex
Learning Curve        │  Easy          │  Steeper
Community             │  Large         │  Large
Spring Boot Support   │  Excellent     │  Excellent
Multi-database        │  Yes           │  Yes
Preconditions         │  Limited       │  Yes
Contexts              │  No            │  Yes
Labels                │  Yes            │  Yes

When to Use Flyway:
✓ Simple migrations
✓ SQL-focused team
✓ Straightforward versioning
✓ Fast setup

When to Use Liquibase:
✓ Complex migrations
✓ Multi-format support
✓ Need rollback
✓ Preconditions needed
```

---

## 5. Migration Best Practices

### Version Control Integration
```
┌─────────────────────────────────────────────────────────────┐
│              Version Control Best Practices                 │
└─────────────────────────────────────────────────────────────┘

Git Workflow:
    Feature Branch
        │
        ├──► Create migration
        │    V10__Add_feature.sql
        │
        ├──► Commit to Git
        │
        ├──► Code Review
        │
        ├──► Merge to Main
        │
        └──► Deploy to Production

Rules:
✓ One migration per feature
✓ Never modify applied migrations
✓ Use descriptive names
✓ Test migrations
✓ Review before merge
```

### Migration Naming Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Migration Naming Guidelines                     │
└─────────────────────────────────────────────────────────────┘

Good Names:
V1__Create_users_table.sql
V2__Add_email_to_users.sql
V3__Create_orders_table.sql
V4__Add_index_on_email.sql

Bad Names:
V1__Migration.sql
V2__Update.sql
V3__Fix.sql

Guidelines:
✓ Descriptive
✓ Action-oriented (Create, Add, Drop)
✓ Table/object name included
✓ Sequential versioning
```

### Testing Migrations
```
┌─────────────────────────────────────────────────────────────┐
│              Migration Testing Strategy                     │
└─────────────────────────────────────────────────────────────┘

Test Strategy:
    ┌──────────────┐
    │ Development  │
    │   Test       │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Integration  │
    │   Test       │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Staging      │
    │   Test       │
    └──────┬───────┘
           │
           ▼
    ┌──────────────┐
    │ Production   │
    │   Deploy     │
    └──────────────┘

Test Cases:
✓ Migration applies successfully
✓ Rollback works (if supported)
✓ Data integrity maintained
✓ Performance acceptable
✓ No downtime (if possible)
```

### Rollback Strategies
```
┌─────────────────────────────────────────────────────────────┐
│              Rollback Strategies                            │
└─────────────────────────────────────────────────────────────┘

Flyway:
- Manual rollback scripts
- U1__Rollback_description.sql
- Or manual SQL scripts

Liquibase:
- Built-in rollback
- Automatic rollback generation
- rollback tag in changeset

Example (Liquibase):
<changeSet id="2" author="developer">
    <addColumn tableName="users">
        <column name="email" type="VARCHAR(255)"/>
    </addColumn>
    <rollback>
        <dropColumn tableName="users" columnName="email"/>
    </rollback>
</changeSet>

Best Practice:
✓ Always test rollback
✓ Keep rollback scripts
✓ Document rollback process
```

---

## 6. Migration Patterns

### Add Column Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Add Column Migration Pattern                   │
└─────────────────────────────────────────────────────────────┘

Flyway:
-- V10__Add_email_to_users.sql
ALTER TABLE users
ADD COLUMN email VARCHAR(255);

CREATE INDEX idx_users_email ON users(email);

Liquibase:
<changeSet id="10" author="developer">
    <addColumn tableName="users">
        <column name="email" type="VARCHAR(255)"/>
    </addColumn>
    <createIndex tableName="users" indexName="idx_users_email">
        <column name="email"/>
    </createIndex>
</changeSet>

Considerations:
- Default values for existing rows
- NULL constraints
- Index creation
- Data migration if needed
```

### Data Migration Pattern
```
┌─────────────────────────────────────────────────────────────┐
│              Data Migration Pattern                         │
└─────────────────────────────────────────────────────────────┘

Scenario: Migrate data during schema change

Flyway:
-- V10__Migrate_user_status.sql
-- Step 1: Add new column
ALTER TABLE users ADD COLUMN status_new VARCHAR(20);

-- Step 2: Migrate data
UPDATE users 
SET status_new = CASE 
    WHEN active = true THEN 'ACTIVE'
    ELSE 'INACTIVE'
END;

-- Step 3: Drop old column
ALTER TABLE users DROP COLUMN active;

-- Step 4: Rename new column
ALTER TABLE users RENAME COLUMN status_new TO status;

Best Practice:
✓ Multi-step approach
✓ Test with sample data
✓ Backup before migration
✓ Verify data integrity
```

---

## Key Concepts Summary

### Database Migrations
```
- Version control for database schema
- Tracked and repeatable
- Applied automatically
- Rollback support
```

### Flyway
```
- SQL/Java based
- Simple, file-based
- Strict naming
- Fast and lightweight
```

### Liquibase
```
- Multiple formats (XML/YAML/JSON/SQL)
- ChangeSet based
- Built-in rollback
- More features
```

### Best Practices
```
✓ Version control migrations
✓ Descriptive naming
✓ Test thoroughly
✓ One migration per feature
✓ Never modify applied migrations
✓ Document rollback procedures
```

---

**This completes all 6 parts of Databases & Data Management diagrams!**

**Summary:**
- Part 1: SQL Fundamentals (Joins, Subqueries, Window Functions, CTEs)
- Part 2: Database Design (Normalization, Denormalization, Indexing)
- Part 3: Transaction Management (ACID, Isolation Levels, Deadlocks)
- Part 4: Query Optimization (Execution Plans, Indexes, Tuning)
- Part 5: Connection Pooling (HikariCP, C3P0, Management)
- Part 6: Database Migrations (Flyway, Liquibase, Version Control)

All diagrams are in ASCII/text format for comprehensive understanding! 🚀


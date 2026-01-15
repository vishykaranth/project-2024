# Configuration Management: Automated Configuration, Drift Detection

## Overview

Configuration Management is the process of maintaining systems in a desired, consistent state. It involves automating configuration tasks, detecting configuration drift, and ensuring systems remain compliant with defined standards over time.

## Configuration Management Concepts

```
┌─────────────────────────────────────────────────────────┐
│         Configuration Management Benefits              │
└─────────────────────────────────────────────────────────┘

Manual Configuration:
  ├─► Time-consuming
  ├─► Error-prone
  ├─► Inconsistent
  └─► Hard to track

Automated Configuration:
  ├─► Fast and efficient
  ├─► Consistent
  ├─► Repeatable
  └─► Auditable
```

## Configuration Drift

### What is Configuration Drift?

Configuration drift occurs when systems deviate from their intended configuration over time due to manual changes, updates, or errors.

```
┌─────────────────────────────────────────────────────────┐
│         Configuration Drift Example                    │
└─────────────────────────────────────────────────────────┘

Initial State (Desired):
  Server 1: Apache 2.4, PHP 7.4, MySQL 8.0
  Server 2: Apache 2.4, PHP 7.4, MySQL 8.0
  Server 3: Apache 2.4, PHP 7.4, MySQL 8.0

After Drift:
  Server 1: Apache 2.4, PHP 7.4, MySQL 8.0 ✓
  Server 2: Apache 2.4, PHP 8.0, MySQL 8.0 ✗ (PHP updated manually)
  Server 3: Apache 2.6, PHP 7.4, MySQL 8.0 ✗ (Apache updated manually)

Result: Inconsistent configuration
```

### Drift Detection Process

```
┌─────────────────────────────────────────────────────────┐
│         Drift Detection Workflow                       │
└─────────────────────────────────────────────────────────┘

1. Define Desired State
   └─► Configuration files, templates

2. Capture Current State
   └─► Scan systems, collect configuration

3. Compare States
   └─► Desired vs Current

4. Identify Drift
   └─► Differences detected

5. Remediate
   └─► Apply desired configuration
   └─► Or alert for manual review
```

## Configuration Management Tools

### Tool Categories

```
┌─────────────────────────────────────────────────────────┐
│         Configuration Management Tools                 │
└─────────────────────────────────────────────────────────┘

Agent-Based:
  ├─► Puppet
  ├─► Chef
  └─► SaltStack

Agentless:
  ├─► Ansible
  └─► Terraform

Cloud-Native:
  ├─► AWS Systems Manager
  ├─► Azure Automation
  └─► Google Cloud Deployment Manager
```

## Automated Configuration

### Configuration Automation Process

```
┌─────────────────────────────────────────────────────────┐
│         Configuration Automation Flow                  │
└─────────────────────────────────────────────────────────┘

Configuration Definition
    │
    ├─► Code/Templates
    ├─► Version Control
    └─► Documentation
         │
         ▼
Configuration Tool
    │
    ├─► Parse configuration
    ├─► Generate execution plan
    └─► Apply changes
         │
         ▼
Target Systems
    │
    ├─► Apply configuration
    ├─► Verify compliance
    └─► Report status
```

### Ansible Configuration Example

```yaml
# configure-webserver.yml
- name: Configure Web Server
  hosts: webservers
  become: yes
  vars:
    apache_version: "2.4"
    php_version: "7.4"
  
  tasks:
    - name: Ensure Apache is installed
      yum:
        name: httpd
        state: present
        version: "{{ apache_version }}"
    
    - name: Ensure PHP is installed
      yum:
        name: "php{{ php_version }}"
        state: present
    
    - name: Configure Apache
      template:
        src: httpd.conf.j2
        dest: /etc/httpd/conf/httpd.conf
      notify: restart apache
    
    - name: Ensure Apache is running
      systemd:
        name: httpd
        state: started
        enabled: yes
  
  handlers:
    - name: restart apache
      systemd:
        name: httpd
        state: restarted
```

## Drift Detection

### Drift Detection Methods

```
┌─────────────────────────────────────────────────────────┐
│         Drift Detection Methods                        │
└─────────────────────────────────────────────────────────┘

1. Configuration Scanning
   └─► Periodically scan systems
   └─► Compare with baseline

2. Continuous Monitoring
   └─► Real-time monitoring
   └─► Alert on changes

3. Compliance Checks
   └─► Run compliance tests
   └─► Report violations

4. Audit Logs
   └─► Track all changes
   └─► Identify drift sources
```

### Puppet Drift Detection

```puppet
# drift-detection.pp
class webserver_config {
  # Desired state
  package { 'httpd':
    ensure => '2.4.41',
  }
  
  file { '/etc/httpd/conf/httpd.conf':
    ensure => present,
    source => 'puppet:///modules/webserver/httpd.conf',
    notify => Service['httpd'],
  }
  
  service { 'httpd':
    ensure => running,
    enable => true,
  }
}

# Puppet automatically detects and corrects drift
```

### Ansible Drift Detection

```yaml
# check-drift.yml
- name: Check Configuration Drift
  hosts: all
  tasks:
    - name: Get current Apache version
      command: httpd -v
      register: current_version
    
    - name: Check if version matches
      assert:
        that:
          - "'2.4.41' in current_version.stdout"
        fail_msg: "Apache version drift detected!"
    
    - name: Get current PHP version
      command: php -v
      register: current_php
    
    - name: Check PHP version
      assert:
        that:
          - "'7.4' in current_php.stdout"
        fail_msg: "PHP version drift detected!"
```

## Configuration Compliance

### Compliance Checking

```
┌─────────────────────────────────────────────────────────┐
│         Configuration Compliance                      │
└─────────────────────────────────────────────────────────┘

Compliance Standards:
  ├─► Security baselines
  ├─► Industry standards (PCI-DSS, HIPAA)
  ├─► Organizational policies
  └─► Best practices

Compliance Checks:
  ├─► Security configurations
  ├─► Software versions
  ├─► Access controls
  └─► Audit settings
```

### Compliance Example

```yaml
# compliance-check.yml
- name: Security Compliance Check
  hosts: all
  tasks:
    - name: Check SSH configuration
      lineinfile:
        path: /etc/ssh/sshd_config
        regexp: '^PermitRootLogin'
        line: 'PermitRootLogin no'
      notify: restart sshd
    
    - name: Check firewall rules
      command: firewall-cmd --list-all
      register: firewall_status
    
    - name: Verify required ports are open
      assert:
        that:
          - "'80/tcp' in firewall_status.stdout"
          - "'443/tcp' in firewall_status.stdout"
        fail_msg: "Required ports not open!"
    
    - name: Check password policy
      command: grep -i "PASS_MIN_LEN" /etc/login.defs
      register: pass_policy
      failed_when: "'14' not in pass_policy.stdout"
```

## Configuration Management Best Practices

### 1. Idempotency
```
✅ Same configuration applied multiple times = same result
✅ No side effects from re-running
✅ Safe to execute repeatedly
```

### 2. Version Control
```
✅ Store all configuration in Git
✅ Version configuration changes
✅ Review before applying
✅ Tag releases
```

### 3. Testing
```
✅ Test in staging first
✅ Validate configuration syntax
✅ Test rollback procedures
✅ Integration testing
```

### 4. Documentation
```
✅ Document configuration purpose
✅ Explain decisions
✅ Usage instructions
✅ Update regularly
```

### 5. Monitoring
```
✅ Monitor configuration changes
✅ Track drift
✅ Alert on violations
✅ Regular audits
```

## Configuration Management Patterns

### 1. Immutable Infrastructure
```
┌─────────────────────────────────────────────────────────┐
│         Immutable Infrastructure Pattern              │
└─────────────────────────────────────────────────────────┘

Approach:
  - Don't modify existing systems
  - Replace with new configured systems
  - No configuration drift possible

Benefits:
  - No drift
  - Consistent
  - Easy rollback
```

### 2. Configuration as Code
```
┌─────────────────────────────────────────────────────────┐
│         Configuration as Code Pattern                 │
└─────────────────────────────────────────────────────────┘

Approach:
  - All configuration in code
  - Version controlled
  - Automated application

Benefits:
  - Version control
  - Reviewable
  - Reproducible
```

### 3. Continuous Compliance
```
┌─────────────────────────────────────────────────────────┐
│         Continuous Compliance Pattern                 │
└─────────────────────────────────────────────────────────┘

Approach:
  - Continuous monitoring
  - Automated compliance checks
  - Immediate remediation

Benefits:
  - Always compliant
  - Early detection
  - Automated fixes
```

## Summary

Configuration Management:
- **Purpose**: Maintain systems in desired, consistent state
- **Process**: Define, apply, monitor, remediate
- **Tools**: Ansible, Puppet, Chef, Terraform
- **Benefits**: Consistency, automation, compliance, drift detection

**Key Components:**
- Configuration definition
- Automated application
- Drift detection
- Compliance checking
- Continuous monitoring

**Best Practices:**
- Idempotency
- Version control
- Testing
- Documentation
- Monitoring

**Remember**: Configuration management ensures systems remain consistent and compliant over time!

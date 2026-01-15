# Infrastructure as Code: Terraform, Ansible, Puppet, Chef

## Overview

Infrastructure as Code (IaC) is the practice of managing and provisioning infrastructure through machine-readable definition files rather than manual processes. It enables version control, automation, consistency, and reproducibility of infrastructure.

## Infrastructure as Code Concepts

```
┌─────────────────────────────────────────────────────────┐
│         Infrastructure as Code Benefits               │
└─────────────────────────────────────────────────────────┘

Traditional Approach:
  Manual Configuration
    ├─► Time-consuming
    ├─► Error-prone
    ├─► Inconsistent
    └─► Hard to reproduce

IaC Approach:
  Code-Based Configuration
    ├─► Automated
    ├─► Version controlled
    ├─► Consistent
    └─► Reproducible
```

## IaC Tools Comparison

### Tool Categories

```
┌─────────────────────────────────────────────────────────┐
│         IaC Tool Categories                            │
└─────────────────────────────────────────────────────────┘

Declarative (What):
  ├─► Terraform
  ├─► CloudFormation
  └─► Pulumi

Imperative (How):
  ├─► Ansible
  ├─► Puppet
  └─► Chef

Configuration Management:
  ├─► Ansible
  ├─► Puppet
  ├─► Chef
  └─► SaltStack

Provisioning:
  ├─► Terraform
  ├─► CloudFormation
  └─► Pulumi
```

## 1. Terraform

### Terraform Overview

Terraform is an open-source infrastructure provisioning tool that uses declarative configuration files to manage cloud infrastructure. It supports multiple cloud providers and maintains state to track infrastructure changes.

### Terraform Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Terraform Architecture                         │
└─────────────────────────────────────────────────────────┘

Terraform Configuration (.tf files)
    │
    ▼
Terraform Plan
    │
    ├─► Analyzes current state
    ├─► Compares with desired state
    └─► Generates execution plan
         │
         ▼
Terraform Apply
    │
    ├─► Creates/updates resources
    └─► Updates state file
```

### Terraform Example

```hcl
# main.tf
provider "aws" {
  region = "us-east-1"
}

resource "aws_instance" "web" {
  ami           = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
  
  tags = {
    Name = "WebServer"
  }
}

resource "aws_s3_bucket" "data" {
  bucket = "my-app-data-bucket"
  
  tags = {
    Environment = "production"
  }
}
```

### Terraform Workflow

```
┌─────────────────────────────────────────────────────────┐
│         Terraform Workflow                             │
└─────────────────────────────────────────────────────────┘

1. Write Configuration
   └─► Define infrastructure in .tf files

2. Initialize
   └─► terraform init
       - Download providers
       - Setup backend

3. Plan
   └─► terraform plan
       - Preview changes
       - Validate configuration

4. Apply
   └─► terraform apply
       - Create/update resources
       - Update state

5. Destroy (optional)
   └─► terraform destroy
       - Remove resources
```

### Terraform State Management

```
┌─────────────────────────────────────────────────────────┐
│         Terraform State                                │
└─────────────────────────────────────────────────────────┘

State File:
  - Tracks managed resources
  - Maps configuration to real resources
  - Stores resource metadata

State Backends:
  - Local (default)
  - Remote (S3, Azure Storage, GCS)
  - Terraform Cloud

State Locking:
  - Prevents concurrent modifications
  - Uses DynamoDB, Azure Blob, etc.
```

## 2. Ansible

### Ansible Overview

Ansible is an automation tool for configuration management, application deployment, and orchestration. It uses YAML playbooks and requires no agents on target systems, using SSH for communication.

### Ansible Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Ansible Architecture                           │
└─────────────────────────────────────────────────────────┘

Control Node (Ansible)
    │
    ├─► Playbooks (.yml)
    ├─► Inventory (hosts)
    └─► Modules
         │
         ▼
Target Nodes
    ├─► Managed via SSH
    ├─► No agent required
    └─► Idempotent operations
```

### Ansible Playbook Example

```yaml
# playbook.yml
- name: Configure Web Server
  hosts: webservers
  become: yes
  tasks:
    - name: Install Apache
      yum:
        name: httpd
        state: present
    
    - name: Start Apache
      systemd:
        name: httpd
        state: started
        enabled: yes
    
    - name: Copy index.html
      copy:
        src: index.html
        dest: /var/www/html/index.html
```

### Ansible Inventory

```ini
# inventory.ini
[webservers]
web1.example.com
web2.example.com

[dbservers]
db1.example.com

[webservers:vars]
ansible_user=admin
ansible_ssh_private_key_file=~/.ssh/id_rsa
```

## 3. Puppet

### Puppet Overview

Puppet is a configuration management tool that uses a declarative language to describe system configuration. It uses a client-server model with agents on managed nodes.

### Puppet Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Puppet Architecture                            │
└─────────────────────────────────────────────────────────┘

Puppet Master
    │
    ├─► Manifests (.pp)
    ├─► Modules
    └─► Certificates
         │
         ▼
Puppet Agents
    ├─► Pull configuration
    ├─► Apply changes
    └─► Report status
```

### Puppet Manifest Example

```puppet
# web.pp
class webserver {
  package { 'httpd':
    ensure => installed,
  }
  
  service { 'httpd':
    ensure => running,
    enable => true,
    require => Package['httpd'],
  }
  
  file { '/var/www/html/index.html':
    ensure => present,
    content => '<h1>Welcome</h1>',
    require => Package['httpd'],
  }
}

include webserver
```

## 4. Chef

### Chef Overview

Chef is a configuration management tool that uses Ruby-based recipes and cookbooks to automate infrastructure. It follows a client-server model with Chef Server managing configurations.

### Chef Architecture

```
┌─────────────────────────────────────────────────────────┐
│         Chef Architecture                             │
└─────────────────────────────────────────────────────────┘

Chef Server
    │
    ├─► Cookbooks
    ├─► Recipes
    └─► Policies
         │
         ▼
Chef Clients
    ├─► Pull recipes
    ├─► Apply configuration
    └─► Report to server
```

### Chef Recipe Example

```ruby
# default.rb
package 'httpd' do
  action :install
end

service 'httpd' do
  action [:enable, :start]
end

file '/var/www/html/index.html' do
  content '<h1>Welcome</h1>'
  action :create
end
```

## Tool Comparison

| Feature | Terraform | Ansible | Puppet | Chef |
|--------|-----------|---------|--------|------|
| **Type** | Declarative | Imperative | Declarative | Imperative |
| **Language** | HCL | YAML | Puppet DSL | Ruby |
| **Agent** | No | No | Yes | Yes |
| **State** | Yes | No | Yes | Yes |
| **Best For** | Provisioning | Config/Deploy | Config Mgmt | Config Mgmt |

## IaC Best Practices

### 1. Version Control
```
✅ Store all IaC code in Git
✅ Use meaningful commit messages
✅ Tag releases
✅ Review changes
```

### 2. Modularity
```
✅ Break into modules
✅ Reusable components
✅ Clear organization
✅ DRY principle
```

### 3. Testing
```
✅ Validate syntax
✅ Test in staging
✅ Use linters
✅ Integration tests
```

### 4. Documentation
```
✅ Document modules
✅ Explain decisions
✅ Usage examples
✅ Update regularly
```

### 5. Security
```
✅ No secrets in code
✅ Use secret management
✅ Least privilege
✅ Regular audits
```

## Summary

Infrastructure as Code:
- **Purpose**: Manage infrastructure through code
- **Tools**: Terraform (provisioning), Ansible (config), Puppet/Chef (config)
- **Benefits**: Version control, automation, consistency, reproducibility
- **Best Practices**: Version control, modularity, testing, security

**Key Tools:**
- **Terraform**: Infrastructure provisioning
- **Ansible**: Configuration management, agentless
- **Puppet**: Configuration management, declarative
- **Chef**: Configuration management, Ruby-based

**Remember**: IaC is essential for modern DevOps, enabling infrastructure to be managed like application code!

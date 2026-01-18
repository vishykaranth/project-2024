# AWS 101: Complete Guide - Core Concepts, Commands & Best Practices

## Table of Contents
1. [Introduction to AWS](#introduction-to-aws)
2. [Core Concepts](#core-concepts)
3. [AWS CLI Setup & Configuration](#aws-cli-setup--configuration)
4. [Identity and Access Management (IAM)](#identity-and-access-management-iam)
5. [EC2 (Elastic Compute Cloud)](#ec2-elastic-compute-cloud)
6. [S3 (Simple Storage Service)](#s3-simple-storage-service)
7. [VPC (Virtual Private Cloud)](#vpc-virtual-private-cloud)
8. [RDS (Relational Database Service)](#rds-relational-database-service)
9. [Lambda (Serverless Functions)](#lambda-serverless-functions)
10. [CloudWatch (Monitoring & Logging)](#cloudwatch-monitoring--logging)
11. [Auto Scaling & Load Balancing](#auto-scaling--load-balancing)
12. [Security Best Practices](#security-best-practices)
13. [Cost Management](#cost-management)
14. [Common Use Cases & Patterns](#common-use-cases--patterns)

---

## Introduction to AWS

### What is AWS?
**Amazon Web Services (AWS)** is a comprehensive cloud computing platform offering over 200 services including computing, storage, databases, networking, analytics, machine learning, and more.

### Key Benefits
- **Scalability**: Scale up or down based on demand
- **Reliability**: 99.99% uptime SLA for most services
- **Cost-Effective**: Pay only for what you use
- **Global Infrastructure**: 33 regions, 105+ availability zones
- **Security**: Enterprise-grade security and compliance

### AWS Global Infrastructure
- **Regions**: Geographic areas (e.g., us-east-1, ap-south-1)
- **Availability Zones (AZs)**: Isolated data centers within a region
- **Edge Locations**: Content delivery network endpoints

---

## Core Concepts

### 1. Regions and Availability Zones

**Region**: A geographic area containing multiple Availability Zones
- Example: `us-east-1` (N. Virginia), `ap-south-1` (Mumbai)

**Availability Zone (AZ)**: Isolated data center within a region
- Example: `us-east-1a`, `us-east-1b`, `us-east-1c`

**Best Practice**: Deploy resources across multiple AZs for high availability

### 2. AWS Service Categories

#### Compute
- **EC2**: Virtual servers
- **Lambda**: Serverless functions
- **ECS/EKS**: Container services
- **Elastic Beanstalk**: Platform as a Service

#### Storage
- **S3**: Object storage
- **EBS**: Block storage for EC2
- **EFS**: Managed file storage
- **Glacier**: Archive storage

#### Database
- **RDS**: Managed relational databases
- **DynamoDB**: NoSQL database
- **ElastiCache**: In-memory caching
- **Redshift**: Data warehousing

#### Networking
- **VPC**: Virtual private cloud
- **CloudFront**: Content delivery network
- **Route 53**: DNS service
- **API Gateway**: API management

#### Security
- **IAM**: Identity and access management
- **KMS**: Key management
- **Secrets Manager**: Secrets storage
- **WAF**: Web application firewall

---

## AWS CLI Setup & Configuration

### Installation

#### macOS
```bash
# Using Homebrew
brew install awscli

# Or download from AWS
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg AWSCLIV2.pkg -target /
```

#### Linux
```bash
# Download and install
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install
```

#### Windows
```powershell
# Using MSI installer
# Download from: https://awscli.amazonaws.com/AWSCLIV2.msi
# Run the installer
```

### Verify Installation
```bash
aws --version
# Output: aws-cli/2.x.x Python/3.x.x ...
```

### Configuration

#### Initial Setup
```bash
# Configure AWS CLI with credentials
aws configure

# You'll be prompted for:
# AWS Access Key ID: [Your Access Key]
# AWS Secret Access Key: [Your Secret Key]
# Default region name: [e.g., us-east-1]
# Default output format: [json, yaml, text, table]
```

#### Manual Configuration
```bash
# Edit credentials file
nano ~/.aws/credentials

# Format:
[default]
aws_access_key_id = YOUR_ACCESS_KEY
aws_secret_access_key = YOUR_SECRET_KEY

[profile-name]
aws_access_key_id = ANOTHER_ACCESS_KEY
aws_secret_access_key = ANOTHER_SECRET_KEY
```

```bash
# Edit config file
nano ~/.aws/config

# Format:
[default]
region = us-east-1
output = json

[profile profile-name]
region = ap-south-1
output = table
```

#### Using Profiles
```bash
# List profiles
aws configure list-profiles

# Use specific profile
aws s3 ls --profile profile-name

# Set default profile
export AWS_PROFILE=profile-name
```

### Environment Variables
```bash
# Set credentials via environment
export AWS_ACCESS_KEY_ID=your_access_key
export AWS_SECRET_ACCESS_KEY=your_secret_key
export AWS_DEFAULT_REGION=us-east-1

# Verify configuration
aws sts get-caller-identity
```

### Common CLI Options
```bash
# Specify region
aws s3 ls --region ap-south-1

# Specify profile
aws ec2 describe-instances --profile dev

# Output format
aws ec2 describe-instances --output json
aws ec2 describe-instances --output table
aws ec2 describe-instances --output yaml
aws ec2 describe-instances --output text

# Query with JMESPath
aws ec2 describe-instances --query 'Reservations[*].Instances[*].[InstanceId,State.Name]' --output table

# Pagination
aws s3api list-objects --bucket my-bucket --max-items 10
```

---

## Identity and Access Management (IAM)

### Core Concepts

**IAM** manages access to AWS services and resources.

#### Key Components
- **Users**: Individual accounts
- **Groups**: Collections of users
- **Roles**: Temporary credentials for services/resources
- **Policies**: JSON documents defining permissions

### IAM Commands

#### Users
```bash
# Create user
aws iam create-user --user-name john-doe

# List users
aws iam list-users

# Get user details
aws iam get-user --user-name john-doe

# Create access key for user
aws iam create-access-key --user-name john-doe

# List access keys
aws iam list-access-keys --user-name john-doe

# Delete access key
aws iam delete-access-key --user-name john-doe --access-key-id AKIAIOSFODNN7EXAMPLE

# Delete user
aws iam delete-user --user-name john-doe
```

#### Groups
```bash
# Create group
aws iam create-group --group-name developers

# List groups
aws iam list-groups

# Add user to group
aws iam add-user-to-group --user-name john-doe --group-name developers

# List users in group
aws iam get-group --group-name developers

# Remove user from group
aws iam remove-user-from-group --user-name john-doe --group-name developers

# Delete group
aws iam delete-group --group-name developers
```

#### Policies
```bash
# Create policy from file
aws iam create-policy \
    --policy-name S3ReadOnlyPolicy \
    --policy-document file://policy.json

# List policies
aws iam list-policies --scope Local

# Get policy
aws iam get-policy --policy-arn arn:aws:iam::123456789012:policy/S3ReadOnlyPolicy

# Attach policy to user
aws iam attach-user-policy \
    --user-name john-doe \
    --policy-arn arn:aws:iam::123456789012:policy/S3ReadOnlyPolicy

# Attach policy to group
aws iam attach-group-policy \
    --group-name developers \
    --policy-arn arn:aws:iam::123456789012:policy/S3ReadOnlyPolicy

# Detach policy
aws iam detach-user-policy \
    --user-name john-doe \
    --policy-arn arn:aws:iam::123456789012:policy/S3ReadOnlyPolicy

# Delete policy
aws iam delete-policy --policy-arn arn:aws:iam::123456789012:policy/S3ReadOnlyPolicy
```

**Example Policy (policy.json)**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "s3:GetObject",
        "s3:ListBucket"
      ],
      "Resource": [
        "arn:aws:s3:::my-bucket",
        "arn:aws:s3:::my-bucket/*"
      ]
    }
  ]
}
```

#### Roles
```bash
# Create role
aws iam create-role \
    --role-name EC2S3AccessRole \
    --assume-role-policy-document file://trust-policy.json

# List roles
aws iam list-roles

# Attach policy to role
aws iam attach-role-policy \
    --role-name EC2S3AccessRole \
    --policy-arn arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess

# List policies attached to role
aws iam list-attached-role-policies --role-name EC2S3AccessRole

# Delete role
aws iam delete-role --role-name EC2S3AccessRole
```

**Example Trust Policy (trust-policy.json)**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "ec2.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
```

### IAM Best Practices
1. **Least Privilege**: Grant minimum required permissions
2. **Use Groups**: Manage permissions via groups, not individual users
3. **Enable MFA**: Require multi-factor authentication
4. **Rotate Credentials**: Regularly rotate access keys
5. **Use Roles**: Prefer roles over access keys for EC2 instances
6. **Audit Regularly**: Review IAM access reports

---

## EC2 (Elastic Compute Cloud)

### Core Concepts

**EC2** provides resizable compute capacity in the cloud.

#### Instance Types
- **General Purpose**: t3, t4g, m5, m6i
- **Compute Optimized**: c5, c6i
- **Memory Optimized**: r5, r6i, x1e
- **Storage Optimized**: i3, d2
- **GPU Instances**: p3, p4, g4dn

#### Instance Purchasing Options
- **On-Demand**: Pay per hour, no commitment
- **Reserved Instances**: 1-3 year commitment, 30-75% discount
- **Spot Instances**: Bid on unused capacity, up to 90% discount
- **Savings Plans**: Flexible pricing model

### EC2 Commands

#### Instances
```bash
# List all instances
aws ec2 describe-instances

# List running instances only
aws ec2 describe-instances \
    --filters "Name=instance-state-name,Values=running" \
    --query 'Reservations[*].Instances[*].[InstanceId,InstanceType,State.Name,PublicIpAddress]' \
    --output table

# Get specific instance details
aws ec2 describe-instances --instance-ids i-1234567890abcdef0

# Launch instance
aws ec2 run-instances \
    --image-id ami-0c55b159cbfafe1f0 \
    --instance-type t3.micro \
    --key-name my-key-pair \
    --security-group-ids sg-12345678 \
    --subnet-id subnet-12345678 \
    --count 1 \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=WebServer}]'

# Start instance
aws ec2 start-instances --instance-ids i-1234567890abcdef0

# Stop instance
aws ec2 stop-instances --instance-ids i-1234567890abcdef0

# Reboot instance
aws ec2 reboot-instances --instance-ids i-1234567890abcdef0

# Terminate instance
aws ec2 terminate-instances --instance-ids i-1234567890abcdef0
```

#### AMIs (Amazon Machine Images)
```bash
# List available AMIs
aws ec2 describe-images --owners amazon

# List your AMIs
aws ec2 describe-images --owners self

# Search for specific AMI
aws ec2 describe-images \
    --filters "Name=name,Values=amzn2-ami-hvm-*" \
              "Name=architecture,Values=x86_64" \
              "Name=virtualization-type,Values=hvm" \
    --query 'Images[*].[ImageId,Name,CreationDate]' \
    --output table \
    --region us-east-1

# Create AMI from instance
aws ec2 create-image \
    --instance-id i-1234567890abcdef0 \
    --name "my-server-image" \
    --description "Backup of production server"

# Copy AMI to another region
aws ec2 copy-image \
    --source-region us-east-1 \
    --source-image-id ami-12345678 \
    --region ap-south-1 \
    --name "copied-ami"
```

#### Key Pairs
```bash
# Create key pair
aws ec2 create-key-pair \
    --key-name my-key-pair \
    --query 'KeyMaterial' \
    --output text > my-key-pair.pem

# Set permissions (Linux/Mac)
chmod 400 my-key-pair.pem

# List key pairs
aws ec2 describe-key-pairs

# Delete key pair
aws ec2 delete-key-pair --key-name my-key-pair
```

#### Security Groups
```bash
# List security groups
aws ec2 describe-security-groups

# Create security group
aws ec2 create-security-group \
    --group-name web-sg \
    --description "Security group for web servers" \
    --vpc-id vpc-12345678

# Add inbound rule
aws ec2 authorize-security-group-ingress \
    --group-id sg-12345678 \
    --protocol tcp \
    --port 80 \
    --cidr 0.0.0.0/0

# Add SSH access
aws ec2 authorize-security-group-ingress \
    --group-id sg-12345678 \
    --protocol tcp \
    --port 22 \
    --cidr 0.0.0.0/0

# Remove rule
aws ec2 revoke-security-group-ingress \
    --group-id sg-12345678 \
    --protocol tcp \
    --port 80 \
    --cidr 0.0.0.0.0/0

# Delete security group
aws ec2 delete-security-group --group-id sg-12345678
```

#### Elastic IPs
```bash
# Allocate Elastic IP
aws ec2 allocate-address --domain vpc

# Associate Elastic IP with instance
aws ec2 associate-address \
    --instance-id i-1234567890abcdef0 \
    --allocation-id eipalloc-12345678

# Disassociate Elastic IP
aws ec2 disassociate-address --association-id eipassoc-12345678

# Release Elastic IP
aws ec2 release-address --allocation-id eipalloc-12345678
```

#### Snapshots
```bash
# Create snapshot
aws ec2 create-snapshot \
    --volume-id vol-1234567890abcdef0 \
    --description "Backup snapshot"

# List snapshots
aws ec2 describe-snapshots --owner-ids self

# Copy snapshot to another region
aws ec2 copy-snapshot \
    --source-region us-east-1 \
    --source-snapshot-id snap-12345678 \
    --region ap-south-1 \
    --description "Copied snapshot"

# Delete snapshot
aws ec2 delete-snapshot --snapshot-id snap-12345678
```

#### Volumes (EBS)
```bash
# List volumes
aws ec2 describe-volumes

# Create volume
aws ec2 create-volume \
    --size 100 \
    --volume-type gp3 \
    --availability-zone us-east-1a

# Attach volume to instance
aws ec2 attach-volume \
    --volume-id vol-1234567890abcdef0 \
    --instance-id i-1234567890abcdef0 \
    --device /dev/sdf

# Detach volume
aws ec2 detach-volume --volume-id vol-1234567890abcdef0

# Delete volume
aws ec2 delete-volume --volume-id vol-1234567890abcdef0
```

#### Tags
```bash
# Create tags
aws ec2 create-tags \
    --resources i-1234567890abcdef0 \
    --tags Key=Environment,Value=Production Key=Project,Value=WebApp

# List tags
aws ec2 describe-tags --filters "Name=resource-id,Values=i-1234567890abcdef0"

# Remove tag
aws ec2 delete-tags \
    --resources i-1234567890abcdef0 \
    --tags Key=Project
```

### EC2 Best Practices
1. **Right-Sizing**: Choose appropriate instance types
2. **Use Spot Instances**: For fault-tolerant workloads
3. **Enable Termination Protection**: Prevent accidental deletion
4. **Use IAM Roles**: Instead of access keys on instances
5. **Enable CloudWatch Monitoring**: Monitor instance health
6. **Regular Backups**: Create AMIs and snapshots

---

## S3 (Simple Storage Service)

### Core Concepts

**S3** is object storage for any amount of data.

#### Storage Classes
- **Standard**: General purpose, frequently accessed
- **Standard-IA**: Infrequently accessed, lower cost
- **One Zone-IA**: Single AZ, lower cost
- **Glacier Instant Retrieval**: Archive with instant access
- **Glacier Flexible Retrieval**: Archive (expedited/standard/bulk)
- **Glacier Deep Archive**: Lowest cost, 12-hour retrieval
- **Intelligent-Tiering**: Automatic cost optimization

#### Features
- **Versioning**: Keep multiple versions of objects
- **Lifecycle Policies**: Automate transitions
- **Encryption**: Server-side encryption (SSE)
- **Access Control**: Bucket policies, ACLs

### S3 Commands

#### Buckets
```bash
# List buckets
aws s3 ls

# Create bucket
aws s3 mb s3://my-bucket-name --region us-east-1

# Delete empty bucket
aws s3 rb s3://my-bucket-name

# Delete bucket and contents
aws s3 rb s3://my-bucket-name --force

# List bucket contents
aws s3 ls s3://my-bucket-name

# List with details
aws s3 ls s3://my-bucket-name --recursive --human-readable --summarize
```

#### Objects
```bash
# Upload file
aws s3 cp file.txt s3://my-bucket-name/

# Upload directory
aws s3 cp /local/directory s3://my-bucket-name/ --recursive

# Download file
aws s3 cp s3://my-bucket-name/file.txt .

# Download directory
aws s3 cp s3://my-bucket-name/ /local/directory --recursive

# Sync directory (like rsync)
aws s3 sync /local/directory s3://my-bucket-name/

# Remove object
aws s3 rm s3://my-bucket-name/file.txt

# Remove directory
aws s3 rm s3://my-bucket-name/prefix/ --recursive

# Move object
aws s3 mv s3://my-bucket-name/old-name.txt s3://my-bucket-name/new-name.txt
```

#### Presigned URLs
```bash
# Generate presigned URL (valid for 1 hour)
aws s3 presign s3://my-bucket-name/file.txt

# Custom expiration (7 days)
aws s3 presign s3://my-bucket-name/file.txt --expires-in 604800
```

#### Versioning
```bash
# Enable versioning
aws s3api put-bucket-versioning \
    --bucket my-bucket-name \
    --versioning-configuration Status=Enabled

# List object versions
aws s3api list-object-versions \
    --bucket my-bucket-name \
    --prefix file.txt

# Restore previous version
aws s3api copy-object \
    --bucket my-bucket-name \
    --copy-source my-bucket-name/file.txt?versionId=version-id \
    --key file.txt
```

#### Lifecycle Policies
```bash
# Create lifecycle policy
aws s3api put-bucket-lifecycle-configuration \
    --bucket my-bucket-name \
    --lifecycle-configuration file://lifecycle.json
```

**Example lifecycle.json**:
```json
{
  "Rules": [
    {
      "Id": "Move to Glacier",
      "Status": "Enabled",
      "Prefix": "archive/",
      "Transitions": [
        {
          "Days": 30,
          "StorageClass": "GLACIER"
        }
      ]
    },
    {
      "Id": "Delete old versions",
      "Status": "Enabled",
      "NoncurrentVersionExpiration": {
        "NoncurrentDays": 90
      }
    }
  ]
}
```

#### Encryption
```bash
# Enable default encryption (SSE-S3)
aws s3api put-bucket-encryption \
    --bucket my-bucket-name \
    --server-side-encryption-configuration '{
      "Rules": [{
        "ApplyServerSideEncryptionByDefault": {
          "SSEAlgorithm": "AES256"
        }
      }]
    }'

# Enable encryption with KMS
aws s3api put-bucket-encryption \
    --bucket my-bucket-name \
    --server-side-encryption-configuration '{
      "Rules": [{
        "ApplyServerSideEncryptionByDefault": {
          "SSEAlgorithm": "aws:kms",
          "KMSMasterKeyID": "arn:aws:kms:us-east-1:123456789012:key/12345678-1234-1234-1234-123456789012"
        }
      }]
    }'
```

#### Bucket Policies
```bash
# Get bucket policy
aws s3api get-bucket-policy --bucket my-bucket-name

# Set bucket policy
aws s3api put-bucket-policy \
    --bucket my-bucket-name \
    --policy file://bucket-policy.json

# Delete bucket policy
aws s3api delete-bucket-policy --bucket my-bucket-name
```

**Example bucket-policy.json**:
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::my-bucket-name/*"
    }
  ]
}
```

#### Website Hosting
```bash
# Enable static website hosting
aws s3 website s3://my-bucket-name/ --index-document index.html --error-document error.html

# Get website configuration
aws s3api get-bucket-website --bucket my-bucket-name
```

### S3 Best Practices
1. **Use Appropriate Storage Classes**: Match access patterns
2. **Enable Versioning**: For critical data
3. **Enable Encryption**: Always encrypt sensitive data
4. **Use Lifecycle Policies**: Automate cost optimization
5. **Enable MFA Delete**: For production buckets
6. **Use Bucket Policies**: Fine-grained access control
7. **Enable Access Logging**: Monitor bucket access

---

## VPC (Virtual Private Cloud)

### Core Concepts

**VPC** is a logically isolated network in AWS.

#### Components
- **Subnets**: Network segments within VPC
- **Route Tables**: Control traffic routing
- **Internet Gateway**: Internet access for VPC
- **NAT Gateway**: Outbound internet for private subnets
- **Security Groups**: Instance-level firewall
- **NACLs**: Subnet-level firewall
- **VPC Peering**: Connect VPCs

### VPC Commands

#### VPCs
```bash
# List VPCs
aws ec2 describe-vpcs

# Create VPC
aws ec2 create-vpc \
    --cidr-block 10.0.0.0/16 \
    --tag-specifications 'ResourceType=vpc,Tags=[{Key=Name,Value=MyVPC}]'

# Delete VPC
aws ec2 delete-vpc --vpc-id vpc-12345678
```

#### Subnets
```bash
# List subnets
aws ec2 describe-subnets

# Create subnet
aws ec2 create-subnet \
    --vpc-id vpc-12345678 \
    --cidr-block 10.0.1.0/24 \
    --availability-zone us-east-1a

# Delete subnet
aws ec2 delete-subnet --subnet-id subnet-12345678
```

#### Internet Gateway
```bash
# Create internet gateway
aws ec2 create-internet-gateway

# Attach to VPC
aws ec2 attach-internet-gateway \
    --internet-gateway-id igw-12345678 \
    --vpc-id vpc-12345678

# Detach from VPC
aws ec2 detach-internet-gateway \
    --internet-gateway-id igw-12345678 \
    --vpc-id vpc-12345678

# Delete internet gateway
aws ec2 delete-internet-gateway --internet-gateway-id igw-12345678
```

#### Route Tables
```bash
# List route tables
aws ec2 describe-route-tables

# Create route table
aws ec2 create-route-table --vpc-id vpc-12345678

# Add route
aws ec2 create-route \
    --route-table-id rtb-12345678 \
    --destination-cidr-block 0.0.0.0/0 \
    --gateway-id igw-12345678

# Associate with subnet
aws ec2 associate-route-table \
    --subnet-id subnet-12345678 \
    --route-table-id rtb-12345678
```

#### NAT Gateway
```bash
# Allocate Elastic IP
aws ec2 allocate-address --domain vpc

# Create NAT gateway
aws ec2 create-nat-gateway \
    --subnet-id subnet-12345678 \
    --allocation-id eipalloc-12345678

# Delete NAT gateway
aws ec2 delete-nat-gateway --nat-gateway-id nat-12345678
```

### VPC Best Practices
1. **Use Private Subnets**: For resources without internet access
2. **Multiple AZs**: Deploy across availability zones
3. **NAT Gateway**: For outbound internet from private subnets
4. **Security Groups**: Primary security mechanism
5. **VPC Flow Logs**: Monitor network traffic

---

## RDS (Relational Database Service)

### Core Concepts

**RDS** is a managed relational database service.

#### Supported Engines
- **MySQL**: Popular open-source database
- **PostgreSQL**: Advanced open-source database
- **MariaDB**: MySQL fork
- **Oracle**: Enterprise database
- **SQL Server**: Microsoft database
- **Aurora**: AWS-optimized MySQL/PostgreSQL

#### Features
- **Automated Backups**: Point-in-time recovery
- **Multi-AZ**: High availability
- **Read Replicas**: Scale read performance
- **Encryption**: At rest and in transit

### RDS Commands

#### Databases
```bash
# List databases
aws rds describe-db-instances

# Create database
aws rds create-db-instance \
    --db-instance-identifier mydb \
    --db-instance-class db.t3.micro \
    --engine mysql \
    --master-username admin \
    --master-user-password MyPassword123 \
    --allocated-storage 20 \
    --vpc-security-group-ids sg-12345678 \
    --db-subnet-group-name default

# Get database details
aws rds describe-db-instances --db-instance-identifier mydb

# Modify database
aws rds modify-db-instance \
    --db-instance-identifier mydb \
    --allocated-storage 100 \
    --apply-immediately

# Delete database
aws rds delete-db-instance \
    --db-instance-identifier mydb \
    --skip-final-snapshot
```

#### Snapshots
```bash
# Create snapshot
aws rds create-db-snapshot \
    --db-instance-identifier mydb \
    --db-snapshot-identifier mydb-snapshot

# List snapshots
aws rds describe-db-snapshot-attributes --db-snapshot-identifier mydb-snapshot

# Restore from snapshot
aws rds restore-db-instance-from-db-snapshot \
    --db-instance-identifier mydb-restored \
    --db-snapshot-identifier mydb-snapshot
```

#### Read Replicas
```bash
# Create read replica
aws rds create-db-instance-read-replica \
    --db-instance-identifier mydb-replica \
    --source-db-instance-identifier mydb

# Promote read replica to standalone
aws rds promote-read-replica \
    --db-instance-identifier mydb-replica
```

### RDS Best Practices
1. **Multi-AZ**: For production workloads
2. **Automated Backups**: Enable point-in-time recovery
3. **Encryption**: Enable at rest and in transit
4. **Parameter Groups**: Optimize database settings
5. **Monitoring**: Use CloudWatch for metrics
6. **Read Replicas**: Scale read-heavy workloads

---

## Lambda (Serverless Functions)

### Core Concepts

**Lambda** runs code without provisioning servers.

#### Features
- **Event-Driven**: Triggered by events
- **Auto-Scaling**: Handles traffic automatically
- **Pay per Use**: Charged per request and compute time
- **Multiple Runtimes**: Python, Node.js, Java, Go, .NET, Ruby

### Lambda Commands

#### Functions
```bash
# List functions
aws lambda list-functions

# Create function from zip
aws lambda create-function \
    --function-name my-function \
    --runtime python3.9 \
    --role arn:aws:iam::123456789012:role/lambda-execution-role \
    --handler index.handler \
    --zip-file fileb://function.zip

# Update function code
aws lambda update-function-code \
    --function-name my-function \
    --zip-file fileb://function.zip

# Get function
aws lambda get-function --function-name my-function

# Invoke function
aws lambda invoke \
    --function-name my-function \
    --payload '{"key":"value"}' \
    response.json

# Delete function
aws lambda delete-function --function-name my-function
```

#### Versions & Aliases
```bash
# Publish version
aws lambda publish-version --function-name my-function

# Create alias
aws lambda create-alias \
    --function-name my-function \
    --name production \
    --function-version 1

# Update alias
aws lambda update-alias \
    --function-name my-function \
    --name production \
    --function-version 2
```

#### Environment Variables
```bash
# Update environment variables
aws lambda update-function-configuration \
    --function-name my-function \
    --environment Variables={KEY1=value1,KEY2=value2}
```

### Lambda Best Practices
1. **Stateless Functions**: Don't store state
2. **Optimize Cold Starts**: Minimize dependencies
3. **Error Handling**: Implement retry logic
4. **Monitoring**: Use CloudWatch Logs
5. **Timeouts**: Set appropriate timeout values
6. **Memory**: Right-size memory allocation

---

## CloudWatch (Monitoring & Logging)

### Core Concepts

**CloudWatch** provides monitoring and observability.

#### Features
- **Metrics**: Collect and track metrics
- **Logs**: Centralized logging
- **Alarms**: Automated actions
- **Dashboards**: Visualize metrics
- **Events**: Event-driven automation

### CloudWatch Commands

#### Metrics
```bash
# List metrics
aws cloudwatch list-metrics

# Get metric statistics
aws cloudwatch get-metric-statistics \
    --namespace AWS/EC2 \
    --metric-name CPUUtilization \
    --dimensions Name=InstanceId,Value=i-1234567890abcdef0 \
    --start-time 2024-01-01T00:00:00Z \
    --end-time 2024-01-01T23:59:59Z \
    --period 3600 \
    --statistics Average

# Put custom metric
aws cloudwatch put-metric-data \
    --namespace MyApp \
    --metric-name RequestCount \
    --value 10 \
    --unit Count
```

#### Alarms
```bash
# Create alarm
aws cloudwatch put-metric-alarm \
    --alarm-name high-cpu \
    --alarm-description "Alarm when CPU exceeds 80%" \
    --metric-name CPUUtilization \
    --namespace AWS/EC2 \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold \
    --evaluation-periods 2

# List alarms
aws cloudwatch describe-alarms

# Delete alarm
aws cloudwatch delete-alarms --alarm-names high-cpu
```

#### Logs
```bash
# List log groups
aws logs describe-log-groups

# Create log group
aws logs create-log-group --log-group-name /aws/lambda/my-function

# List log streams
aws logs describe-log-streams --log-group-name /aws/lambda/my-function

# Get log events
aws logs get-log-events \
    --log-group-name /aws/lambda/my-function \
    --log-stream-name 2024/01/01/[$LATEST]abc123

# Filter log events
aws logs filter-log-events \
    --log-group-name /aws/lambda/my-function \
    --filter-pattern "ERROR"
```

### CloudWatch Best Practices
1. **Enable Detailed Monitoring**: For critical resources
2. **Set Up Alarms**: Proactive monitoring
3. **Centralized Logging**: Aggregate logs
4. **Custom Metrics**: Track application metrics
5. **Dashboards**: Visualize key metrics
6. **Log Retention**: Set appropriate retention periods

---

## Auto Scaling & Load Balancing

### Auto Scaling

#### Commands
```bash
# Create launch configuration
aws autoscaling create-launch-configuration \
    --launch-configuration-name my-launch-config \
    --image-id ami-12345678 \
    --instance-type t3.micro \
    --key-name my-key-pair \
    --security-groups sg-12345678

# Create auto scaling group
aws autoscaling create-auto-scaling-group \
    --auto-scaling-group-name my-asg \
    --launch-configuration-name my-launch-config \
    --min-size 2 \
    --max-size 10 \
    --desired-capacity 4 \
    --vpc-zone-identifier subnet-12345678,subnet-87654321

# Update auto scaling group
aws autoscaling update-auto-scaling-group \
    --auto-scaling-group-name my-asg \
    --min-size 3 \
    --max-size 15 \
    --desired-capacity 6

# Create scaling policy
aws autoscaling put-scaling-policy \
    --auto-scaling-group-name my-asg \
    --policy-name scale-up \
    --policy-type TargetTrackingScaling \
    --target-tracking-configuration '{
      "PredefinedMetricSpecification": {
        "PredefinedMetricType": "ASGAverageCPUUtilization"
      },
      "TargetValue": 70.0
    }'
```

### Load Balancing

#### Application Load Balancer (ALB)
```bash
# Create target group
aws elbv2 create-target-group \
    --name my-targets \
    --protocol HTTP \
    --port 80 \
    --vpc-id vpc-12345678 \
    --health-check-path /health

# Create load balancer
aws elbv2 create-load-balancer \
    --name my-alb \
    --subnets subnet-12345678 subnet-87654321 \
    --security-groups sg-12345678

# Register targets
aws elbv2 register-targets \
    --target-group-arn arn:aws:elasticloadbalancing:... \
    --targets Id=i-1234567890abcdef0

# Create listener
aws elbv2 create-listener \
    --load-balancer-arn arn:aws:elasticloadbalancing:... \
    --protocol HTTP \
    --port 80 \
    --default-actions Type=forward,TargetGroupArn=arn:aws:elasticloadbalancing:...
```

---

## Security Best Practices

### 1. IAM Security
- Use least privilege principle
- Enable MFA for root account
- Rotate access keys regularly
- Use IAM roles instead of access keys
- Enable CloudTrail for audit logging

### 2. Network Security
- Use security groups (stateful firewall)
- Implement NACLs for subnet-level control
- Use private subnets for databases
- Enable VPC Flow Logs
- Use VPN or Direct Connect for on-premises connectivity

### 3. Data Security
- Enable encryption at rest (EBS, S3, RDS)
- Enable encryption in transit (SSL/TLS)
- Use AWS KMS for key management
- Enable S3 bucket versioning
- Use Secrets Manager for credentials

### 4. Monitoring & Compliance
- Enable CloudTrail for API logging
- Enable Config for compliance monitoring
- Set up CloudWatch alarms
- Regular security audits
- Use AWS Security Hub for centralized security

---

## Cost Management

### Cost Optimization Strategies

1. **Right-Sizing**: Choose appropriate instance types
2. **Reserved Instances**: Commit to 1-3 year terms
3. **Spot Instances**: Use for fault-tolerant workloads
4. **Savings Plans**: Flexible pricing model
5. **S3 Lifecycle Policies**: Move to cheaper storage classes
6. **Delete Unused Resources**: Regular cleanup
7. **Use Cost Explorer**: Analyze spending patterns

### Cost Monitoring Commands
```bash
# Get cost and usage
aws ce get-cost-and-usage \
    --time-period Start=2024-01-01,End=2024-01-31 \
    --granularity MONTHLY \
    --metrics BlendedCost

# Get cost forecast
aws ce get-cost-forecast \
    --time-period Start=2024-02-01,End=2024-02-28 \
    --metric BLENDED_COST \
    --granularity MONTHLY
```

---

## Common Use Cases & Patterns

### 1. Web Application Architecture
- **Frontend**: S3 + CloudFront
- **Application**: EC2 or ECS
- **Database**: RDS
- **Load Balancer**: ALB
- **Caching**: ElastiCache

### 2. Serverless Application
- **API**: API Gateway
- **Compute**: Lambda
- **Database**: DynamoDB
- **Storage**: S3
- **CDN**: CloudFront

### 3. Data Processing Pipeline
- **Ingestion**: Kinesis
- **Processing**: Lambda or EMR
- **Storage**: S3
- **Analytics**: Athena or Redshift
- **Visualization**: QuickSight

### 4. CI/CD Pipeline
- **Source**: CodeCommit
- **Build**: CodeBuild
- **Deploy**: CodeDeploy or CodePipeline
- **Infrastructure**: CloudFormation or Terraform

---

## Quick Reference: Essential Commands

### General
```bash
# Check identity
aws sts get-caller-identity

# List all regions
aws ec2 describe-regions

# List all availability zones
aws ec2 describe-availability-zones

# Get account summary
aws iam get-account-summary
```

### Resource Tagging
```bash
# Tag resource
aws ec2 create-tags \
    --resources i-1234567890abcdef0 \
    --tags Key=Environment,Value=Production

# List resources by tag
aws resourcegroupstaggingapi get-resources \
    --tag-filters Key=Environment,Values=Production
```

### Help & Documentation
```bash
# Get help for any command
aws s3 help
aws ec2 run-instances help

# List available services
aws help
```

---

## Conclusion

This guide covers the fundamental AWS concepts, services, and commands. Key takeaways:

1. **Start with IAM**: Proper access control is critical
2. **Understand Regions & AZs**: For high availability
3. **Use Managed Services**: Reduce operational overhead
4. **Monitor Everything**: CloudWatch is your friend
5. **Optimize Costs**: Right-size and use appropriate pricing models
6. **Security First**: Enable encryption, use security groups, audit regularly

### Next Steps
- Practice with AWS Free Tier
- Get AWS Certified Cloud Practitioner
- Build a simple project (web app, API, etc.)
- Explore AWS Well-Architected Framework
- Join AWS community forums

### Resources
- **AWS Documentation**: https://docs.aws.amazon.com
- **AWS CLI Reference**: https://awscli.amazonaws.com/v2/documentation
- **AWS Well-Architected**: https://aws.amazon.com/architecture/well-architected
- **AWS Training**: https://aws.amazon.com/training

---

**Last Updated**: 2025-01-28  
**Version**: 1.0


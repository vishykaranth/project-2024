# Object Storage in System Design Interviews w/ Ex-Meta Staff Engineer

## Overview

Object storage is essential for storing large files, media, and unstructured data. This guide covers object storage concepts, S3 architecture, and design considerations for system design interviews.

## Object Storage vs Block Storage

```
┌─────────────────────────────────────────────────────────┐
│         Storage Types Comparison                       │
└─────────────────────────────────────────────────────────┘

Object Storage (S3):
├─ Flat namespace
├─ REST API
├─ Metadata + data
├─ Good for: Files, media, backups
└─ Examples: S3, GCS, Azure Blob

Block Storage:
├─ Block-level access
├─ Mount as disk
├─ Good for: Databases, VMs
└─ Examples: EBS, Persistent Disks

File Storage:
├─ Hierarchical structure
├─ NFS, SMB protocols
├─ Good for: Shared files
└─ Examples: EFS, Filestore
```

## S3 Architecture

```
┌─────────────────────────────────────────────────────────┐
│         S3 Architecture                                │
└─────────────────────────────────────────────────────────┘

Client
    │
    ▼
S3 API Gateway
    │
    ▼
Metadata Service
    │
    ├─► Object Metadata DB
    └─► Location mapping
    │
    ▼
Storage Nodes
    ├─► Replication
    └─► Durability
```

## Key Features

```
┌─────────────────────────────────────────────────────────┐
│         Object Storage Features                         │
└─────────────────────────────────────────────────────────┘

Durability:
├─ 99.999999999% (11 9's)
├─ Replication across zones
└─ Versioning

Availability:
├─ 99.99% SLA
├─ Multiple availability zones
└─ Redundancy

Scalability:
├─ Unlimited storage
├─ Auto-scaling
└─ No file system limits
```

## Use Cases

```
┌─────────────────────────────────────────────────────────┐
│         Common Use Cases                               │
└─────────────────────────────────────────────────────────┘

1. Media Storage
   ├─ Images, videos
   └─ CDN integration

2. Backup & Archive
   ├─ Long-term storage
   └─ Cost-effective

3. Static Website Hosting
   ├─ HTML, CSS, JS
   └─ S3 + CloudFront

4. Data Lakes
   ├─ Big data storage
   └─ Analytics
```

## Summary

Object Storage:
- **S3**: AWS object storage service
- **Durability**: 11 9's durability
- **Scalability**: Unlimited storage
- **API**: REST-based access
- **Use Cases**: Media, backups, static hosting

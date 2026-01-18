# Part 46: Docker & Kubernetes - Quick Revision

## Docker Basics

- **Container**: Lightweight, isolated environment, runs application
- **Image**: Read-only template for creating containers
- **Dockerfile**: Instructions to build image
- **Docker Compose**: Multi-container applications, define services

## Docker Concepts

- **Layers**: Images built in layers, cached for efficiency
- **Volumes**: Persistent data storage, survive container deletion
- **Networking**: Bridge, host, overlay networks
- **Registry**: Docker Hub, private registries for images

## Kubernetes Basics

- **Pod**: Smallest deployable unit, one or more containers
- **Deployment**: Manages pod replicas, rolling updates
- **Service**: Exposes pods, load balancing, service discovery
- **Namespace**: Virtual cluster, resource isolation

## Kubernetes Objects

- **ConfigMap**: Configuration data, decouple from containers
- **Secret**: Sensitive data (passwords, tokens)
- **Ingress**: External access to services, routing rules
- **StatefulSet**: Stateful applications, persistent storage

## Key Concepts

- **ReplicaSet**: Maintains desired number of pod replicas
- **Horizontal Pod Autoscaler**: Auto-scale based on metrics
- **Service Discovery**: DNS-based, automatic service registration
- **Rolling Updates**: Zero-downtime deployments

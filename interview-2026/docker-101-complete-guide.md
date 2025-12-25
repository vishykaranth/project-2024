# Docker 101: Complete Guide

## Table of Contents
1. [Introduction](#introduction)
2. [Core Concepts](#core-concepts)
3. [Installation](#installation)
4. [Essential Commands](#essential-commands)
5. [Dockerfile Deep Dive](#dockerfile-deep-dive)
6. [Docker Compose](#docker-compose)
7. [Volumes & Data Management](#volumes--data-management)
8. [Networking](#networking)
9. [Image Management](#image-management)
10. [Best Practices](#best-practices)
11. [Troubleshooting](#troubleshooting)

---

## Introduction

**Docker** is a platform for developing, shipping, and running applications using containerization. Containers package an application with all its dependencies, ensuring it runs consistently across different environments.

### Why Docker?
- **Consistency**: Same environment from development to production
- **Isolation**: Applications don't interfere with each other
- **Portability**: Run anywhere Docker is installed
- **Resource Efficiency**: Lightweight compared to VMs
- **Scalability**: Easy to scale applications horizontally

---

## Core Concepts

### 1. **Image**
A read-only template used to create containers. Images are built from a `Dockerfile` and stored in registries (Docker Hub, ECR, GCR, etc.).

**Key Points:**
- Immutable (layered filesystem)
- Can be versioned with tags
- Shared layers reduce storage

### 2. **Container**
A running instance of an image. Containers are isolated processes with their own filesystem, network, and process space.

**Key Points:**
- Ephemeral by default (data lost when stopped)
- Can be started, stopped, paused, removed
- Shares host OS kernel

### 3. **Dockerfile**
A text file with instructions to build an image. Each instruction creates a layer in the image.

### 4. **Volume**
Persistent storage that survives container lifecycle. Used for data that needs to persist.

### 5. **Network**
Isolated network for containers to communicate. Default networks: bridge, host, none.

### 6. **Registry**
Repository for storing and distributing images (Docker Hub, AWS ECR, Google GCR, Azure ACR).

---

## Installation

### macOS
```bash
# Using Homebrew
brew install --cask docker

# Or download Docker Desktop from docker.com
```

### Linux (Ubuntu/Debian)
```bash
# Update package index
sudo apt-get update

# Install prerequisites
sudo apt-get install ca-certificates curl gnupg lsb-release

# Add Docker's official GPG key
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Set up repository
echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# Install Docker Engine
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-compose-plugin

# Start Docker
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group (to run without sudo)
sudo usermod -aG docker $USER
```

### Verify Installation
```bash
docker --version
docker info
docker run hello-world
```

---

## Essential Commands

### Container Lifecycle

#### `docker run`
Creates and starts a container from an image.

```bash
# Basic run
docker run <image-name>

# Run with name
docker run --name my-container <image-name>

# Run in detached mode (background)
docker run -d <image-name>

# Run with port mapping
docker run -p 8080:80 <image-name>
# Format: -p <host-port>:<container-port>

# Run with environment variables
docker run -e VAR_NAME=value <image-name>

# Run with volume mount
docker run -v /host/path:/container/path <image-name>

# Run with interactive terminal
docker run -it <image-name>

# Run and remove container when it stops
docker run --rm <image-name>

# Run with resource limits
docker run --memory="512m" --cpus="1.0" <image-name>

# Run with custom command
docker run <image-name> <command>

# Complete example
docker run -d \
  --name web-server \
  -p 8080:80 \
  -e ENV=production \
  -v /data:/app/data \
  --memory="1g" \
  nginx:latest
```

**Explanation:**
- `-d`: Detached mode (runs in background)
- `-p`: Port mapping (host:container)
- `-e`: Environment variable
- `-v`: Volume mount
- `-it`: Interactive terminal
- `--rm`: Auto-remove on exit
- `--name`: Assign a name

#### `docker start`
Starts a stopped container.

```bash
docker start <container-id-or-name>
docker start -a <container-id-or-name>  # Attach to output
docker start -i <container-id-or-name> # Interactive mode
```

#### `docker stop`
Gracefully stops a running container (SIGTERM, then SIGKILL after timeout).

```bash
docker stop <container-id-or-name>
docker stop -t 30 <container-id-or-name>  # Timeout in seconds
```

#### `docker restart`
Restarts a container.

```bash
docker restart <container-id-or-name>
```

#### `docker pause` / `docker unpause`
Pauses/unpauses all processes in a container (uses cgroups freezer).

```bash
docker pause <container-id-or-name>
docker unpause <container-id-or-name>
```

#### `docker kill`
Forcefully stops a container (SIGKILL immediately).

```bash
docker kill <container-id-or-name>
docker kill -s SIGTERM <container-id-or-name>  # Send specific signal
```

#### `docker rm`
Removes one or more containers.

```bash
docker rm <container-id-or-name>
docker rm -f <container-id-or-name>  # Force remove running container
docker rm $(docker ps -aq)  # Remove all stopped containers
docker container prune  # Remove all stopped containers (newer syntax)
```

### Container Inspection

#### `docker ps`
Lists running containers.

```bash
docker ps                    # Running containers
docker ps -a                 # All containers (including stopped)
docker ps -l                 # Last created container
docker ps -q                 # Only container IDs
docker ps --format "table {{.ID}}\t{{.Names}}\t{{.Status}}"
docker ps --filter "status=exited"
docker ps --filter "name=web"
```

#### `docker logs`
Shows container logs.

```bash
docker logs <container-id-or-name>
docker logs -f <container-id-or-name>        # Follow (like tail -f)
docker logs --tail 100 <container-id-or-name> # Last 100 lines
docker logs --since 10m <container-id-or-name> # Since 10 minutes ago
docker logs --until 2023-01-01 <container-id-or-name>
docker logs -t <container-id-or-name>         # Show timestamps
```

#### `docker inspect`
Shows detailed information about a container or image.

```bash
docker inspect <container-id-or-name>
docker inspect --format='{{.NetworkSettings.IPAddress}}' <container-id-or-name>
docker inspect --format='{{json .Config}}' <container-id-or-name> | jq
```

#### `docker exec`
Executes a command in a running container.

```bash
docker exec <container-id-or-name> <command>
docker exec -it <container-id-or-name> /bin/bash  # Interactive shell
docker exec -u root <container-id-or-name> <command>  # As root
docker exec -w /app <container-id-or-name> <command>  # Working directory
```

#### `docker top`
Shows running processes in a container.

```bash
docker top <container-id-or-name>
```

#### `docker stats`
Shows real-time resource usage statistics.

```bash
docker stats
docker stats <container-id-or-name>
docker stats --no-stream  # One-time snapshot
docker stats --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"
```

### Image Management

#### `docker images` / `docker image ls`
Lists images.

```bash
docker images
docker images -a              # All images (including intermediate)
docker images --filter "dangling=true"  # Dangling images
docker images --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
```

#### `docker pull`
Downloads an image from a registry.

```bash
docker pull <image-name>
docker pull <image-name>:<tag>
docker pull nginx:1.23
docker pull myregistry.com/myimage:latest
docker pull --platform linux/amd64 <image-name>  # Specific platform
```

#### `docker build`
Builds an image from a Dockerfile.

```bash
docker build .
docker build -t myimage:tag .
docker build -t myimage:tag -f Dockerfile.prod .
docker build --no-cache .  # Build without cache
docker build --build-arg VAR=value .
docker build --target stage-name .  # Multi-stage build target
docker build --progress=plain .  # Plain output
```

#### `docker push`
Uploads an image to a registry.

```bash
docker push <image-name>
docker push myregistry.com/myimage:tag
```

#### `docker rmi` / `docker image rm`
Removes one or more images.

```bash
docker rmi <image-id-or-name>
docker rmi -f <image-id-or-name>  # Force remove
docker rmi $(docker images -q)  # Remove all images
docker image prune  # Remove dangling images
docker image prune -a  # Remove all unused images
```

#### `docker tag`
Creates a tag for an image.

```bash
docker tag <source-image> <target-image>
docker tag nginx:latest mynginx:v1.0
docker tag myimage:latest myregistry.com/myimage:1.0
```

#### `docker save` / `docker load`
Saves/loads images to/from a tar archive.

```bash
docker save -o image.tar <image-name>
docker save <image-name> | gzip > image.tar.gz
docker load -i image.tar
docker load < image.tar
```

#### `docker import` / `docker export`
Imports/exports container filesystem.

```bash
docker export <container-id> > container.tar
docker import container.tar myimage:tag
```

### System Commands

#### `docker info`
Shows Docker system information.

```bash
docker info
```

#### `docker version`
Shows Docker version information.

```bash
docker version
```

#### `docker system df`
Shows Docker disk usage.

```bash
docker system df
docker system df -v  # Verbose
```

#### `docker system prune`
Removes unused data.

```bash
docker system prune              # Remove stopped containers, unused networks, dangling images
docker system prune -a           # Also remove unused images
docker system prune --volumes    # Also remove unused volumes
docker system prune -a --volumes --force  # Everything, no confirmation
```

#### `docker events`
Shows real-time events from Docker daemon.

```bash
docker events
docker events --filter "container=mycontainer"
docker events --since "2023-01-01" --until "2023-01-02"
```

---

## Dockerfile Deep Dive

### Basic Structure

```dockerfile
# Base image
FROM ubuntu:22.04

# Metadata
LABEL maintainer="your.email@example.com"
LABEL version="1.0"

# Working directory
WORKDIR /app

# Copy files
COPY requirements.txt .
COPY app.py .

# Install dependencies
RUN apt-get update && \
    apt-get install -y python3 python3-pip && \
    pip3 install -r requirements.txt && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Environment variables
ENV PYTHONUNBUFFERED=1
ENV APP_ENV=production

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Default command
CMD ["python3", "app.py"]

# Entry point (alternative to CMD)
# ENTRYPOINT ["python3", "app.py"]
```

### Dockerfile Instructions

#### `FROM`
Sets the base image.

```dockerfile
FROM ubuntu:22.04
FROM python:3.11-slim
FROM node:18-alpine
FROM scratch  # Empty base image
FROM myimage:tag AS builder  # Named stage for multi-stage builds
```

#### `RUN`
Executes commands in the image during build.

```dockerfile
RUN apt-get update && apt-get install -y nginx
RUN pip install -r requirements.txt
RUN ["/bin/bash", "-c", "echo $HOME"]
```

**Best Practice:** Chain commands with `&&` to reduce layers.

#### `COPY` / `ADD`
Copies files from host to image.

```dockerfile
COPY src/ /app/src/
COPY requirements.txt /app/
COPY --chown=user:group file.txt /app/
COPY --from=builder /app/dist /app/dist  # From another stage

# ADD can also fetch from URLs and extract archives
ADD https://example.com/file.tar.gz /tmp/
ADD file.tar.gz /tmp/  # Auto-extracts
```

**Difference:** `COPY` is preferred (simpler, more predictable). `ADD` has extra features (URLs, auto-extract) but less explicit.

#### `WORKDIR`
Sets working directory for subsequent instructions.

```dockerfile
WORKDIR /app
WORKDIR /app/src
```

#### `ENV`
Sets environment variables.

```dockerfile
ENV NODE_ENV=production
ENV PATH=/usr/local/bin:$PATH
ENV VAR1=value1 VAR2=value2
```

#### `ARG`
Defines build-time variables.

```dockerfile
ARG VERSION=latest
ARG BUILD_DATE
FROM ubuntu:${VERSION}
```

Use with: `docker build --build-arg VERSION=22.04 .`

#### `EXPOSE`
Documents which ports the container listens on (doesn't actually publish).

```dockerfile
EXPOSE 8080
EXPOSE 8080/tcp
EXPOSE 8080/udp
```

#### `CMD`
Default command when container starts (can be overridden).

```dockerfile
CMD ["executable", "param1", "param2"]  # Exec form (preferred)
CMD executable param1 param2            # Shell form
CMD ["python3", "app.py"]
```

#### `ENTRYPOINT`
Command that always runs (CMD becomes arguments).

```dockerfile
ENTRYPOINT ["executable", "param1"]
ENTRYPOINT ["/entrypoint.sh"]
```

**With CMD:**
```dockerfile
ENTRYPOINT ["docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
# Runs: docker-entrypoint.sh nginx -g daemon off;
```

#### `VOLUME`
Creates a mount point for persistent data.

```dockerfile
VOLUME ["/data"]
VOLUME /var/log
```

#### `USER`
Sets the user for subsequent instructions.

```dockerfile
USER nginx
USER 1000:1000
USER nobody
```

#### `HEALTHCHECK`
Defines how to check container health.

```dockerfile
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

HEALTHCHECK NONE  # Disable inherited healthcheck
```

### Multi-Stage Builds

```dockerfile
# Stage 1: Build
FROM node:18 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Production
FROM nginx:alpine
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**Benefits:**
- Smaller final image (no build tools)
- Better security (fewer dependencies)
- Faster builds (can cache stages)

---

## Docker Compose

Docker Compose manages multi-container applications defined in `docker-compose.yml`.

### Basic Example

```yaml
version: '3.8'

services:
  web:
    image: nginx:alpine
    ports:
      - "8080:80"
    volumes:
      - ./html:/usr/share/nginx/html
    environment:
      - ENV=production
    depends_on:
      - db

  db:
    image: postgres:15
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: user
      POSTGRES_PASSWORD: password
    volumes:
      - db_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

volumes:
  db_data:
```

### Compose Commands

```bash
docker-compose up              # Start services
docker-compose up -d           # Detached mode
docker-compose up --build      # Build images before starting
docker-compose down            # Stop and remove containers
docker-compose down -v         # Also remove volumes
docker-compose ps              # List services
docker-compose logs            # View logs
docker-compose logs -f web     # Follow logs for service
docker-compose exec web bash   # Execute command in service
docker-compose build           # Build images
docker-compose pull            # Pull images
docker-compose restart         # Restart services
docker-compose stop            # Stop services
docker-compose start           # Start stopped services
docker-compose pause           # Pause services
docker-compose unpause         # Unpause services
docker-compose top             # Show running processes
docker-compose config           # Validate and view config
docker-compose scale web=3      # Scale service (deprecated, use deploy.replicas)
```

### Compose File Structure

```yaml
version: '3.8'

services:
  service-name:
    image: image:tag
    build:
      context: .
      dockerfile: Dockerfile
      args:
        VAR: value
    ports:
      - "8080:80"
    volumes:
      - ./data:/app/data
      - named_volume:/app/storage
    environment:
      - VAR=value
    env_file:
      - .env
    networks:
      - mynetwork
    depends_on:
      - other-service
    restart: always  # no, always, on-failure, unless-stopped
    command: ["custom", "command"]
    entrypoint: ["/entrypoint.sh"]
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost/health"]
      interval: 30s
      timeout: 3s
      retries: 3
    deploy:
      replicas: 3
      resources:
        limits:
          cpus: '0.5'
          memory: 512M
        reservations:
          cpus: '0.25'
          memory: 256M

volumes:
  named_volume:

networks:
  mynetwork:
    driver: bridge
```

---

## Volumes & Data Management

### Volume Types

1. **Named Volumes** (Managed by Docker)
2. **Bind Mounts** (Host filesystem)
3. **tmpfs Mounts** (In-memory)

### Volume Commands

```bash
docker volume create myvolume
docker volume ls
docker volume inspect myvolume
docker volume rm myvolume
docker volume prune  # Remove unused volumes
```

### Using Volumes

```bash
# Named volume
docker run -v myvolume:/data <image>

# Bind mount
docker run -v /host/path:/container/path <image>

# Read-only mount
docker run -v /host/path:/container/path:ro <image>

# tmpfs mount
docker run --tmpfs /tmp <image>
```

### In Dockerfile

```dockerfile
VOLUME ["/data"]
```

### In Compose

```yaml
volumes:
  - myvolume:/data
  - ./host/path:/container/path
  - /host/path:/container/path:ro
```

---

## Networking

### Network Types

1. **bridge**: Default network for containers
2. **host**: Use host's network directly
3. **none**: No networking
4. **overlay**: For Swarm mode
5. **macvlan**: Assign MAC addresses

### Network Commands

```bash
docker network ls
docker network create mynetwork
docker network inspect mynetwork
docker network rm mynetwork
docker network prune  # Remove unused networks
```

### Connect Container to Network

```bash
docker run --network mynetwork <image>
docker network connect mynetwork <container>
docker network disconnect mynetwork <container>
```

### Port Mapping

```bash
# Map single port
docker run -p 8080:80 <image>

# Map multiple ports
docker run -p 8080:80 -p 443:443 <image>

# Map to specific interface
docker run -p 127.0.0.1:8080:80 <image>

# Map random host port
docker run -p 80 <image>

# Map UDP port
docker run -p 8080:80/udp <image>
```

### In Compose

```yaml
networks:
  frontend:
    driver: bridge
  backend:
    driver: bridge

services:
  web:
    networks:
      - frontend
  api:
    networks:
      - frontend
      - backend
```

---

## Image Management

### Building Best Practices

1. **Use .dockerignore**
```
node_modules
.git
*.log
.env
```

2. **Layer Ordering**
- Copy dependency files first
- Install dependencies
- Copy application code last

3. **Minimize Layers**
```dockerfile
# Bad
RUN apt-get update
RUN apt-get install -y nginx
RUN apt-get clean

# Good
RUN apt-get update && \
    apt-get install -y nginx && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*
```

4. **Use Multi-Stage Builds**
5. **Use Specific Tags**
6. **Run as Non-Root User**

### Image Optimization

```bash
# Analyze image layers
docker history <image>

# Check image size
docker images

# Use distroless or alpine images
FROM alpine:latest
FROM gcr.io/distroless/java:11
```

---

## Best Practices

### Security

1. **Use official/base images**
2. **Keep images updated**
3. **Run as non-root user**
4. **Scan images for vulnerabilities**
5. **Don't store secrets in images**
6. **Use minimal base images**

### Performance

1. **Use .dockerignore**
2. **Order Dockerfile instructions by change frequency**
3. **Use build cache effectively**
4. **Multi-stage builds**
5. **Remove unnecessary files**

### Development

1. **Use volumes for code during development**
2. **Use docker-compose for local development**
3. **Keep Dockerfile and docker-compose.yml in version control**
4. **Document with comments in Dockerfile**

### Production

1. **Use specific image tags (not `latest`)**
2. **Set resource limits**
3. **Use health checks**
4. **Implement proper logging**
5. **Use orchestration (Kubernetes, Swarm)**

---

## Troubleshooting

### Common Issues

#### Container won't start
```bash
docker logs <container-id>
docker inspect <container-id>
docker run -it <image> /bin/bash  # Debug interactively
```

#### Out of space
```bash
docker system df
docker system prune -a --volumes
```

#### Port already in use
```bash
# Find process using port
lsof -i :8080
# Or use different port
docker run -p 8081:80 <image>
```

#### Permission denied
```bash
# Check user in container
docker exec <container> whoami
# Run with specific user
docker run -u 1000:1000 <image>
```

#### Network issues
```bash
docker network inspect bridge
docker network ls
# Test connectivity
docker exec <container> ping <other-container>
```

### Debug Commands

```bash
# Container logs
docker logs <container>

# Container processes
docker top <container>

# Container stats
docker stats <container>

# Inspect container
docker inspect <container>

# Execute shell in container
docker exec -it <container> /bin/bash

# Check Docker daemon logs
# Linux: journalctl -u docker
# macOS: View Docker Desktop logs
```

---

## Quick Reference

### Most Used Commands

```bash
# Run container
docker run -d -p 8080:80 --name web nginx

# View logs
docker logs -f web

# Execute command
docker exec -it web bash

# Stop/Start
docker stop web
docker start web

# Remove
docker rm web

# Build image
docker build -t myapp:1.0 .

# List containers
docker ps -a

# List images
docker images

# Clean up
docker system prune -a
```

### Useful Aliases

Add to `~/.bashrc` or `~/.zshrc`:

```bash
alias dps='docker ps'
alias dpsa='docker ps -a'
alias di='docker images'
alias dex='docker exec -it'
alias dlog='docker logs -f'
alias dstop='docker stop'
alias drm='docker rm'
alias drmi='docker rmi'
alias dclean='docker system prune -a'
```

---

## Next Steps

1. **Docker Swarm**: Native orchestration
2. **Kubernetes**: Production orchestration
3. **Docker Security**: Image scanning, secrets management
4. **CI/CD Integration**: Build and deploy pipelines
5. **Monitoring**: Container metrics and logging

---

## Resources

- [Docker Official Documentation](https://docs.docker.com/)
- [Docker Hub](https://hub.docker.com/)
- [Best Practices Guide](https://docs.docker.com/develop/dev-best-practices/)
- [Dockerfile Reference](https://docs.docker.com/reference/dockerfile/)

---

*Last Updated: 2024*


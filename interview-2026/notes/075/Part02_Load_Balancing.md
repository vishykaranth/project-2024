# Part 2: Load Balancing - Quick Revision

## Load Balancing Algorithms

- **Round Robin**: Distribute requests sequentially; simple, fair distribution
- **Least Connections**: Route to server with fewest active connections; good for long-lived connections
- **IP Hash**: Hash client IP to determine server; ensures sticky sessions
- **Weighted Round Robin**: Assign weights to servers; handles heterogeneous capacities
- **Geographic Routing**: Route based on client location; reduces latency

## Load Balancer Types

- **Application Load Balancer (ALB)**: Layer 7, HTTP/HTTPS, content-based routing
- **Network Load Balancer (NLB)**: Layer 4, TCP/UDP, high performance
- **Classic Load Balancer (CLB)**: Legacy, basic load balancing

## Health Checks

- **Liveness Check**: Is server running? (TCP ping, basic connectivity)
- **Readiness Check**: Can server handle traffic? (database connectivity, dependencies)
- **Startup Check**: Is server starting correctly? (prevents premature routing)
- **Response Codes**: 200 OK = healthy, 503 = unhealthy, timeout = remove from pool

## Session Persistence

- **Sticky Sessions**: Same client → same server; use IP hash or cookie-based
- **Session Storage**: External storage (Redis, database) for stateless services
- **Trade-off**: Sticky sessions vs stateless architecture

## Key Features

- **SSL Termination**: Offload SSL/TLS processing from application servers
- **Connection Pooling**: Reuse connections, reduce overhead
- **Auto-scaling Integration**: Add/remove servers based on traffic and health

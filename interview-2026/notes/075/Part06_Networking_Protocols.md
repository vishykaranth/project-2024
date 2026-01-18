# Part 6: Networking & Protocols - Quick Revision

## Network Stack (OSI Model)

```
Application (HTTP, HTTPS, WebSocket) 
    ↓
Transport (TCP, UDP)
    ↓
Network (IP - IPv4, IPv6)
    ↓
Data Link (Ethernet, WiFi)
    ↓
Physical (Cables, Radio waves)
```

## TCP vs UDP

- **TCP**: Connection-oriented, reliable, ordered, flow control, congestion control, slower
- **UDP**: Connectionless, unreliable, unordered, no flow control, faster, lower overhead
- **Use TCP**: Web browsing, file transfer, email, when reliability matters
- **Use UDP**: Video streaming, gaming, DNS, when speed matters

## Application Protocols

- **HTTP/HTTPS**: Request-response, stateless, methods (GET, POST, PUT, DELETE)
- **WebSocket**: Full-duplex, persistent connection, real-time updates
- **gRPC**: HTTP/2 based, protocol buffers, efficient, microservices
- **MQTT**: Lightweight messaging, IoT devices, pub/sub
- **WebRTC**: Peer-to-peer, video/audio streaming

## DNS Resolution

```
Client → Local DNS → Root DNS → TLD DNS → Authoritative DNS → IP Address
```

- **Caching**: DNS responses cached for TTL (Time To Live)
- **Record Types**: A (IPv4), AAAA (IPv6), CNAME (alias), MX (mail)

## Key Concepts

- **IP Addressing**: IPv4 (32-bit, 4.3B addresses), IPv6 (128-bit, 3.4×10³⁸ addresses)
- **Ports**: 0-65535; well-known ports (HTTP: 80, HTTPS: 443, SSH: 22)
- **NAT**: Network Address Translation, maps private to public IPs

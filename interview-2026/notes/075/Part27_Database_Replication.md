# Part 27: Database Replication - Quick Revision

## Replication Types

- **Master-Slave**: One master (writes), multiple slaves (reads); read scalability
- **Master-Master**: Multiple masters (writes); write scalability, conflict resolution
- **Synchronous**: Wait for all replicas to confirm; strong consistency, slower
- **Asynchronous**: Don't wait for replicas; faster, eventual consistency

## Replication Lag

- **Causes**: Network latency, write volume, replica processing time
- **Impact**: Stale reads, eventual consistency
- **Mitigation**: Read from master for critical reads, monitor lag

## Use Cases

- **Read Scalability**: Distribute read traffic across replicas
- **High Availability**: Failover to replica if master fails
- **Geographic Distribution**: Replicas closer to users, lower latency
- **Backup**: Replicas serve as backups

## Failover Strategies

- **Automatic Failover**: Promote replica to master automatically
- **Manual Failover**: Admin promotes replica manually
- **Split-Brain**: Multiple masters think they're primary; prevent with quorum

## Monitoring

- **Replication Lag**: Monitor delay between master and replicas
- **Replica Health**: Check replica status, remove unhealthy replicas
- **Write Conflicts**: Monitor conflicts in master-master setup

# AWS Reference Architecture: Netflix‑Like Streaming Service

In-depth blueprint for a multi-region streaming platform on AWS. Split into planes/workloads with concrete AWS components, scaling, security, and ops practices.

## 1) Networking, Regions, Failover
- Multi-region (active-active preferred) across 3+ AZs each; Route 53 latency/geolocation routing with health checks on ALB/NLB + custom health endpoints.
- VPC per region: public subnets (edge/ingress), private subnets (services/data), per-AZ. CIDR /16 typical.
- Egress: PrivateLink to AWS APIs; NAT per AZ; VPC Endpoints for S3, DynamoDB, STS, KMS, ECR, CloudWatch.
- Placement: Spread/partition for resilience; cluster placement only for low-latency compute (e.g., encoding jobs).

## 2) Edge, CDN, Delivery
- CloudFront with Origin Shield, signed URLs/cookies, cache policies keyed on device/DRM headers; gzip/brotli.
- Origins: S3 (masters + renditions), MediaStore/MediaPackage for low-latency origins.
- WAF on CloudFront: OWASP baseline, rate limits, geo controls, bot control (Shield Advanced).
- DRM: MediaPackage integrated DRM (Widevine/FairPlay/PlayReady); keys via KMS; short-lived playback tokens.
- ABR: HLS/DASH/CMAF, segment 2–6s, ladder per device/network.

## 3) Ingest, Transcode, Package
- Ingest: Upload to S3 “incoming”; S3 Event → Lambda/Step Functions to orchestrate.
- Transcode: MediaConvert (VOD); Elemental Live/MediaLive (live). Job templates for ladder/codecs/captions/audio. Use Spot for batch transcodes; tag jobs.
- Packaging: MediaPackage to emit HLS/DASH/CMAF; protect origin via CloudFront.
- Storage: Masters in S3 Standard; renditions in Standard-IA/One Zone-IA; CRR to secondary region; lifecycle to Glacier for masters.

## 4) Control Plane (APIs/Auth/Catalog)
- Ingress: API Gateway or ALB → services on EKS or ECS/Fargate.
- Service-to-service: mTLS (mesh optional: App Mesh/istio) or SG-scoped TLS + ACM Private CA.
- AuthN/Z: Cognito for users/devices; OIDC/SAML for partners; JWT access tokens; signed playback tokens.
- Catalog/metadata: DynamoDB for hot lookups (PK contentId or tenant+contentId, GSIs for titles/tags); OpenSearch for search; Aurora/Aurora Serverless v2 for transactional/ops/entitlements.
- Sessions/devices: DynamoDB with TTL; Redis for short-lived session tokens/rate limits.
- API hygiene: Rate limits/throttles at API GW + Redis; request validation; idempotency keys; pagination.

## 5) Data Plane (Playback/User State)
- Watch state: DynamoDB (Global Tables) PK userId, SK contentId; GSIs for “recently watched”.
- Caching: CloudFront edges; Redis (ElastiCache) for hot metadata/entitlements/rate limits.
- HA/DR: DynamoDB Global Tables (RPO≈0, RTO minutes); Aurora Global Database for relational needs.

## 6) Recommendations/ML
- Data Lake: S3 + Glue catalog; Lake Formation for permissions; partitions by dt/user/region.
- Streaming events: Kinesis Data Streams or MSK for QoE/views/search; Firehose → S3; Lambda for inline transforms.
- Processing: EMR/Spark or Glue ETL; Athena ad-hoc; Redshift/OpenSearch for dashboards/BI; SageMaker for training + batch/real-time inference; Redis/Feature Store for low-latency features.
- Model ops: Artifacts in S3; SageMaker endpoints; A/B via weighted routing or feature flags.

## 7) Observability & SLOs
- Metrics: CloudWatch (RED + platform); Container Insights/ServiceLens; SLOs on p95/p99 latency, error rates, rebuffer %, startup delay, throughput per region.
- Logs: CloudWatch Logs → Firehose → S3/OpenSearch; structured JSON with trace IDs.
- Tracing: OpenTelemetry collector on EKS/ECS; X-Ray or vendor backend; propagate trace IDs through gateways/services.
- Alarms: Health on ALB/NLB targets; Route 53 health for failover; autoscaling alarms; WAF/Shield events.

## 8) Resilience & Chaos
- Patterns: Timeouts, retries with jitter, circuit breakers (Resilience4j/Envoy), bulkheads, request hedging for tail latency (where safe).
- Chaos: AWS FIS for fault injection; game days for regional failover/origin or cache degradation.
- Backpressure: Scale on queue depth; return 429/503 with Retry-After when shedding load.

## 9) Security & Compliance
- Identity: IAM roles per service; no long-lived keys; Secrets via Secrets Manager/SSM.
- Network: SGs least-privilege; NACLs; private subnets for services/data; SSM Session Manager for bastionless ops.
- Data Protection: KMS for S3/EBS/EFS/Aurora/Redis snapshots; TLS everywhere; signed URLs; DRM keys via KMS/MediaPackage.
- Edge: WAF (OWASP, geo, rate); Shield Advanced on CloudFront/ALB.
- PII: Segregate PII stores; minimize fields; audit via CloudTrail (org), Access Analyzer, Config rules.

## 10) Deployment, CI/CD, Scalability
- Compute: EKS or ECS/Fargate; ALB ingress; HPA/KEDA or Service Auto Scaling on CPU/mem/requests/queue depth/custom SLI.
- CI/CD: CodePipeline/CodeBuild or GitHub Actions + ArgoCD; blue/green or canary via ALB target groups or CloudFront weighted behaviors; immutable infra via Launch Templates/ASGs when using EC2.
- Images: ECR; signing/scanning.
- Scaling:
  - Control plane: autoscale APIs/services; Redis clustering for throughput.
  - Data: DynamoDB autoscaling/On-Demand; Aurora Serverless v2 or provisioned + autoscaling replicas; Redis cluster mode.
  - Media: CloudFront + S3/MediaPackage scale elastically.
- Cost: Spot for batch/transcode; RIs/Savings Plans for steady control-plane; S3 lifecycle; right-size DynamoDB capacity.

## 11) Backup, DR, Compliance
- Backups: EBS snapshots; EFS backup; Aurora backups + Global DB; DynamoDB PITR; S3 versioning + Object Lock for critical buckets.
- DR: S3 CRR; DynamoDB Global Tables; Aurora Global; tested failovers/runbooks.

## 12) Key Data Flows
- **VOD Playback**: Client auth (Cognito) → signed playback token → CloudFront → MediaPackage/S3 → DRM license check → player ABR.
- **Ingest/Encode**: S3 upload → event → Step Functions → MediaConvert → S3 renditions → MediaPackage → CloudFront cache/invalidate/prefetch as needed.
- **Analytics**: Player QoE → Kinesis/MSK → (Lambda/Firehose) → S3/OpenSearch → dashboards/alerts; offline ETL to Redshift/EMR/SageMaker for recs.
- **Control Actions**: API GW → ALB → service (EKS/ECS) → DynamoDB/Aurora → Redis cache → event to Kinesis for analytics.

## 13) Starter vs. Mature
- **Starter (single region)**: CloudFront + S3 + MediaPackage; API GW + Lambda/ECS Fargate; DynamoDB; Cognito; WAF; CloudWatch; on-demand capacity.
- **Mature (multi-region active-active)**: Duplicate per region; DynamoDB Global Tables; Aurora Global; Route 53 latency routing; per-region ALB/ingress; CRR; chaos/failover drills.

Use this as a reference and adapt for specific constraints (cost, compliance, live vs. VOD emphasis, device targets).




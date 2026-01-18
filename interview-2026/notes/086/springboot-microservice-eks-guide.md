# Spring Boot Microservice → Docker → Kubernetes (EKS) on AWS: Step-by-Step

## 1) Build the Spring Boot service
1) Scaffold: `spring init --dependencies=web,actuator,lombok dev-demo` (or Spring Initializr UI).  
2) Add endpoints:  
   - `GET /health` returns “ok”.  
   - `GET /api/demo` returns payload.  
3) Config: `application.yaml` for server port, management endpoints.  
4) Tests: basic `@SpringBootTest` / slice tests.  
5) Build: `./mvnw clean package -DskipTests=false`.

## 2) Add Docker
Dockerfile (multi-stage):
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```
Build/test locally:  
`docker build -t demo:local .`  
`docker run -p 8080:8080 demo:local` → hit `/health`.

## 3) Push image to AWS ECR
1) Create repo: `aws ecr create-repository --repository-name demo`  
2) Login: `aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <acct>.dkr.ecr.<region>.amazonaws.com`  
3) Tag/push:  
`docker tag demo:local <acct>.dkr.ecr.<region>.amazonaws.com/demo:1.0.0`  
`docker push <acct>.dkr.ecr.<region>.amazonaws.com/demo:1.0.0`

## 4) Provision EKS
- Quick start: `eksctl create cluster --name demo-eks --region <region> --nodes 2 --node-type t3.medium`
- Add-ons: VPC CNI, CoreDNS, kube-proxy (managed), AWS Load Balancer Controller, Cluster Autoscaler (recommended).

## 5) Deploy to EKS
1) Kubeconfig: `aws eks update-kubeconfig --region <region> --name demo-eks`  
2) Namespace: `kubectl create namespace demo`  
3) Apply manifests: `deployment.yaml`, `service.yaml`, `ingress.yaml` (optional)  
4) `kubectl apply -n demo -f deployment.yaml -f service.yaml`  
5) Verify: `kubectl get pods -n demo`; `kubectl port-forward svc/demo 8080:80` → test `/health`.  
6) Expose via ALB: install AWS Load Balancer Controller (Helm), then create Ingress with ALB annotations; test ALB DNS.

## 6) External config & secrets
- Use SSM Parameter Store or Secrets Manager.  
- Mount via env or files using Secrets Store CSI Driver (AWS provider) or External Secrets Operator.  
- Never bake secrets into images/manifests.

## 7) Observability & health
- Liveness/readiness probes on `/health` or `/actuator/health/readiness`.  
- Logs: ship to CloudWatch (Fluent Bit add-on).  
- Metrics: Actuator + Prometheus/Grafana or CloudWatch Container Insights.  
- Tracing: OpenTelemetry agent/collector → X-Ray or vendor backend.

## 8) Scaling & resilience
- HPA on CPU/mem/custom metrics.  
- Cluster Autoscaler on node group.  
- PodDisruptionBudget; resource requests/limits.  
- Rollouts: `kubectl rollout status` or Helm/Argo Rollouts for canary/blue-green.

## 9) CI/CD outline
- Stages: build → test → docker build → push to ECR → deploy to EKS.  
- Tools: GitHub Actions, CodeBuild/CodePipeline, GitLab CI, Jenkins.  
- Use Helm or Kustomize; set image tag from pipeline.

## 10) Security basics
- IAM Roles for Service Accounts (IRSA) for AWS access per workload.  
- SGs for nodes; NetworkPolicies for pod-to-pod (Calico/Cilium).  
- TLS everywhere (Ingress termination + optional mTLS internally).  
- Scan images (ECR scan/Trivy); keep base images slim.

## Minimal manifest hints
- Deployment: image `...ecr.../demo:1.0.0`, replicas 2–3, probes on `/actuator/health`.  
- Service: ClusterIP port 80 → targetPort 8080.  
- Ingress: ALB Ingress with ACM cert for HTTPS (via AWS Load Balancer Controller).

## Common pitfalls to avoid
- Missing probes; no resource requests/limits.  
- Hardcoded secrets; not using IRSA.  
- No autoscaling; no logging/metrics/tracing.  
- Skipping image scans and pinned tags.




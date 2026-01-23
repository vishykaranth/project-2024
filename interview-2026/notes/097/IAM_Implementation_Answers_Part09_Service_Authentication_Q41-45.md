# IAM Implementation Answers - Part 9: Service-to-Service Authentication (Questions 41-45)

## Question 41: You "enabled seamless service-to-service authentication and authorization across microservices architecture." How did you achieve this?

### Answer

### Service-to-Service Authentication

#### 1. **Architecture Overview**

```
┌─────────────────────────────────────────────────────────┐
│         Service-to-Service Authentication            │
└─────────────────────────────────────────────────────────┘

Service A
    │
    ├─► Request with Service Token
    │
    ▼
IAM Service
    │
    ├─► Validate Service Token
    ├─► Check Service Permissions
    │
    └─► Authorize Request
        │
        ▼
Service B
```

#### 2. **Service Identity Management**

```java
@Service
public class ServiceIdentityService {
    
    /**
     * Register service identity
     */
    public ServiceIdentity registerService(String serviceName, String serviceId) {
        ServiceIdentity identity = new ServiceIdentity(
            serviceId,
            serviceName,
            generateServiceCertificate(serviceName)
        );
        
        serviceIdentityRepository.save(identity);
        return identity;
    }
    
    /**
     * Generate service token
     */
    public ServiceToken generateServiceToken(String serviceId) {
        ServiceIdentity identity = serviceIdentityRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException(serviceId));
        
        // Generate JWT token for service
        String token = jwtService.generateToken(identity);
        
        return new ServiceToken(token, Duration.ofHours(24));
    }
}
```

#### 3. **Service Authentication Filter**

```java
@Component
public class ServiceAuthenticationFilter implements Filter {
    
    private final ServiceTokenValidator tokenValidator;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Extract service token
        String token = extractServiceToken(httpRequest);
        
        if (token != null) {
            // Validate service token
            ServiceIdentity service = tokenValidator.validate(token);
            
            if (service != null) {
                // Set service context
                ServiceContext.setCurrentService(service);
                chain.doFilter(request, response);
                return;
            }
        }
        
        // Unauthorized
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
    }
    
    private String extractServiceToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
```

---

## Question 42: What authentication mechanism did you use for service-to-service communication?

### Answer

### Service Authentication Mechanisms

#### 1. **mTLS (Mutual TLS)**

```java
/**
 * mTLS for service-to-service authentication
 */
@Configuration
public class MTLSConfiguration {
    
    @Bean
    public SSLContext sslContext() throws Exception {
        // Load service certificate
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(
            new FileInputStream("service-cert.p12"),
            "password".toCharArray()
        );
        
        // Load CA certificate
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(
            new FileInputStream("ca-cert.jks"),
            "password".toCharArray()
        );
        
        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, "password".toCharArray());
        
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        
        sslContext.init(
            keyManagerFactory.getKeyManagers(),
            trustManagerFactory.getTrustManagers(),
            new SecureRandom()
        );
        
        return sslContext;
    }
}
```

#### 2. **JWT Service Tokens**

```java
/**
 * JWT-based service authentication
 */
@Service
public class JWTServiceAuthentication {
    
    private final JwtService jwtService;
    
    /**
     * Generate service JWT token
     */
    public String generateServiceToken(ServiceIdentity service) {
        return jwtService.generateToken(
            JwtClaims.builder()
                .subject(service.getServiceId())
                .claim("service_name", service.getServiceName())
                .claim("service_roles", service.getRoles())
                .expiration(Instant.now().plus(Duration.ofHours(24)))
                .build()
        );
    }
    
    /**
     * Validate service token
     */
    public ServiceIdentity validateServiceToken(String token) {
        JwtClaims claims = jwtService.validateToken(token);
        
        return ServiceIdentity.builder()
            .serviceId(claims.getSubject())
            .serviceName(claims.getClaim("service_name", String.class))
            .roles(claims.getClaim("service_roles", List.class))
            .build();
    }
}
```

#### 3. **API Key Authentication**

```java
/**
 * API key for service authentication
 */
@Service
public class APIKeyServiceAuthentication {
    
    private final ServiceAPIKeyRepository apiKeyRepository;
    
    /**
     * Validate API key
     */
    public ServiceIdentity validateAPIKey(String apiKey) {
        ServiceAPIKey key = apiKeyRepository.findByKey(apiKey)
            .orElseThrow(() -> new InvalidAPIKeyException());
        
        if (key.isExpired()) {
            throw new ExpiredAPIKeyException();
        }
        
        return key.getServiceIdentity();
    }
}
```

---

## Question 43: How did you handle service identity and certificates?

### Answer

### Service Identity & Certificate Management

#### 1. **Service Identity Management**

```java
@Entity
public class ServiceIdentity {
    @Id
    private String serviceId;
    
    private String serviceName;
    private String certificatePath;
    private String privateKeyPath;
    private Instant certificateExpiry;
    private List<String> roles;
    
    // Getters and setters
}

@Service
public class ServiceIdentityManager {
    
    /**
     * Create service identity
     */
    public ServiceIdentity createServiceIdentity(String serviceName) {
        // Generate certificate
        CertificatePair certificate = certificateService.generateCertificate(serviceName);
        
        ServiceIdentity identity = new ServiceIdentity(
            UUID.randomUUID().toString(),
            serviceName,
            certificate.getCertificatePath(),
            certificate.getPrivateKeyPath(),
            certificate.getExpiryDate(),
            getDefaultRoles(serviceName)
        );
        
        return serviceIdentityRepository.save(identity);
    }
    
    /**
     * Rotate service certificate
     */
    public ServiceIdentity rotateCertificate(String serviceId) {
        ServiceIdentity identity = serviceIdentityRepository.findById(serviceId)
            .orElseThrow(() -> new ServiceNotFoundException(serviceId));
        
        // Generate new certificate
        CertificatePair newCertificate = certificateService.generateCertificate(
            identity.getServiceName()
        );
        
        // Update identity
        identity.setCertificatePath(newCertificate.getCertificatePath());
        identity.setPrivateKeyPath(newCertificate.getPrivateKeyPath());
        identity.setCertificateExpiry(newCertificate.getExpiryDate());
        
        return serviceIdentityRepository.save(identity);
    }
}
```

#### 2. **Certificate Generation**

```java
@Service
public class CertificateService {
    
    /**
     * Generate service certificate
     */
    public CertificatePair generateCertificate(String serviceName) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            // Create certificate
            X500Name subject = new X500Name("CN=" + serviceName);
            X509Certificate certificate = createCertificate(
                subject,
                keyPair,
                365 // 1 year validity
            );
            
            // Save certificate and private key
            String certPath = saveCertificate(serviceName, certificate);
            String keyPath = savePrivateKey(serviceName, keyPair.getPrivate());
            
            return new CertificatePair(certPath, keyPath, certificate.getNotAfter().toInstant());
            
        } catch (Exception e) {
            throw new CertificateGenerationException("Failed to generate certificate", e);
        }
    }
}
```

---

## Question 44: What's your approach to mTLS (mutual TLS) for service-to-service auth?

### Answer

### mTLS Implementation

#### 1. **mTLS Architecture**

```
┌─────────────────────────────────────────────────────────┐
│         mTLS Architecture                              │
└─────────────────────────────────────────────────────────┘

Service A                    Service B
    │                            │
    ├─► Client Certificate       │
    │   (Service A identity)      │
    │                            │
    ├─► Server Certificate       │
    │   (Service B identity)      │
    │                            │
    └─► Encrypted Connection     │
        (TLS 1.3)                │
```

#### 2. **mTLS Configuration**

```java
@Configuration
public class MTLSConfiguration {
    
    @Bean
    public SSLContext serviceSSLContext() throws Exception {
        // Load service certificate (client cert)
        KeyStore keyStore = loadServiceKeyStore();
        
        // Load CA certificate (trust store)
        KeyStore trustStore = loadCAKeyStore();
        
        // Create SSL context
        SSLContext sslContext = SSLContext.getInstance("TLS");
        
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(keyStore, "password".toCharArray());
        
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
        trustManagerFactory.init(trustStore);
        
        sslContext.init(
            keyManagerFactory.getKeyManagers(),
            trustManagerFactory.getTrustManagers(),
            new SecureRandom()
        );
        
        return sslContext;
    }
    
    @Bean
    public RestTemplate restTemplate() throws Exception {
        SSLContext sslContext = serviceSSLContext();
        
        SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(
            sslContext,
            new String[]{"TLSv1.3"},
            null,
            new DefaultHostnameVerifier()
        );
        
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(socketFactory)
            .build();
        
        HttpComponentsClientHttpRequestFactory factory = 
            new HttpComponentsClientHttpRequestFactory(httpClient);
        
        return new RestTemplate(factory);
    }
}
```

#### 3. **mTLS Server Configuration**

```java
@Configuration
public class MTLSServerConfiguration {
    
    @Bean
    public TomcatServletWebServerFactory servletContainer() throws Exception {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        
        factory.addConnectorCustomizers(connector -> {
            connector.setScheme("https");
            connector.setSecure(true);
            connector.setPort(8443);
            
            // Enable client certificate authentication
            connector.setProperty("SSLEnabled", "true");
            connector.setProperty("clientAuth", "true"); // Require client cert
            connector.setProperty("sslProtocol", "TLS");
            connector.setProperty("keystoreFile", "service-cert.p12");
            connector.setProperty("keystorePass", "password");
            connector.setProperty("truststoreFile", "ca-cert.jks");
            connector.setProperty("truststorePass", "password");
        });
        
        return factory;
    }
}
```

---

## Question 45: How did you manage service credentials and rotation?

### Answer

### Service Credential Management

#### 1. **Credential Management Strategy**

```
┌─────────────────────────────────────────────────────────┐
│         Credential Management Strategy                 │
└─────────────────────────────────────────────────────────┘

Management:
├─ Credential storage (secure vault)
├─ Automatic rotation
├─ Version management
├─ Expiry monitoring
└─ Revocation support
```

#### 2. **Credential Storage**

```java
@Service
public class ServiceCredentialManager {
    
    private final VaultService vaultService;
    
    /**
     * Store service credentials securely
     */
    public void storeCredentials(String serviceId, ServiceCredentials credentials) {
        String vaultPath = "services/" + serviceId + "/credentials";
        vaultService.write(vaultPath, credentials);
    }
    
    /**
     * Retrieve service credentials
     */
    public ServiceCredentials getCredentials(String serviceId) {
        String vaultPath = "services/" + serviceId + "/credentials";
        return vaultService.read(vaultPath, ServiceCredentials.class);
    }
}
```

#### 3. **Automatic Rotation**

```java
@Service
public class CredentialRotationService {
    
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void rotateCredentials() {
        List<ServiceIdentity> services = serviceIdentityRepository.findAll();
        
        for (ServiceIdentity service : services) {
            // Check if rotation needed
            if (shouldRotate(service)) {
                rotateServiceCredentials(service);
            }
        }
    }
    
    private boolean shouldRotate(ServiceIdentity service) {
        // Rotate if:
        // - Certificate expires in 30 days
        // - Credential is older than 90 days
        Instant expiry = service.getCertificateExpiry();
        return expiry.isBefore(Instant.now().plus(Duration.ofDays(30)));
    }
    
    private void rotateServiceCredentials(ServiceIdentity service) {
        // Generate new credentials
        ServiceCredentials newCredentials = generateCredentials(service);
        
        // Store new credentials (versioned)
        credentialManager.storeCredentials(service.getServiceId(), newCredentials);
        
        // Notify service to reload credentials
        notifyService(service, newCredentials);
    }
}
```

---

## Summary

Part 9 covers questions 41-45 on Service-to-Service Authentication:

41. **Service-to-Service Auth**: Architecture, service identity, authentication filter
42. **Authentication Mechanisms**: mTLS, JWT, API keys
43. **Service Identity & Certificates**: Identity management, certificate generation
44. **mTLS Approach**: mTLS configuration, client/server setup
45. **Credential Management**: Storage, rotation, versioning

Key techniques:
- Multiple authentication mechanisms
- Service identity management
- mTLS implementation
- Secure credential management
- Automatic credential rotation

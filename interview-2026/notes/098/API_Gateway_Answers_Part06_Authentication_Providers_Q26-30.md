# API Gateway Answers - Part 6: Authentication Providers (Questions 26-30)

## Question 26: You "supported OAuth 2.0, API key, and custom authentication providers." How did you implement multiple authentication mechanisms?

### Answer

### Multiple Authentication Mechanisms

#### 1. **Authentication Strategy Pattern**

```java
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private final List<AuthenticationProvider> providers;
    
    public AuthenticationFilter(List<AuthenticationProvider> providers) {
        this.providers = providers;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, 
                            GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Try each provider in order
        return findProvider(request)
            .flatMap(provider -> provider.authenticate(exchange))
            .flatMap(authentication -> {
                exchange.getAttributes().put("authentication", authentication);
                return chain.filter(exchange);
            })
            .onErrorResume(AuthenticationException.class, error -> {
                return handleUnauthorized(exchange);
            });
    }
    
    private Mono<AuthenticationProvider> findProvider(ServerHttpRequest request) {
        for (AuthenticationProvider provider : providers) {
            if (provider.supports(request)) {
                return Mono.just(provider);
            }
        }
        return Mono.error(new AuthenticationException("No suitable provider"));
    }
    
    @Override
    public int getOrder() {
        return -100;
    }
}
```

#### 2. **Authentication Provider Interface**

```java
public interface AuthenticationProvider {
    boolean supports(ServerHttpRequest request);
    Mono<Authentication> authenticate(ServerWebExchange exchange);
}
```

---

## Question 27: Walk me through the OAuth 2.0 implementation in your gateway.

### Answer

### OAuth 2.0 Implementation

```java
@Component
public class OAuth2AuthenticationProvider implements AuthenticationProvider {
    private final ReactiveClientRegistrationRepository clientRegistrations;
    private final ServerOAuth2AuthorizedClientRepository authorizedClients;
    
    @Override
    public boolean supports(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        return authHeader != null && authHeader.startsWith("Bearer ");
    }
    
    @Override
    public Mono<Authentication> authenticate(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String token = extractToken(request);
        
        // Validate token
        return validateToken(token)
            .map(claims -> {
                return new OAuth2Authentication(claims);
            });
    }
    
    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        return authHeader.substring(7); // Remove "Bearer "
    }
    
    private Mono<Jwt> validateToken(String token) {
        // Validate with OAuth 2.0 server
        return jwtDecoder.decode(token);
    }
}
```

---

## Question 28: How did you handle API key authentication?

### Answer

### API Key Authentication

```java
@Component
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {
    private final ApiKeyRepository apiKeyRepository;
    
    @Override
    public boolean supports(ServerHttpRequest request) {
        return request.getHeaders().containsKey("X-API-Key") ||
               request.getQueryParams().containsKey("api_key");
    }
    
    @Override
    public Mono<Authentication> authenticate(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String apiKey = extractApiKey(request);
        
        return apiKeyRepository.findByKey(apiKey)
            .switchIfEmpty(Mono.error(new AuthenticationException("Invalid API key")))
            .flatMap(keyEntity -> {
                if (!keyEntity.isActive()) {
                    return Mono.error(new AuthenticationException("API key inactive"));
                }
                
                // Update last used
                keyEntity.setLastUsed(LocalDateTime.now());
                apiKeyRepository.save(keyEntity);
                
                return Mono.just(new ApiKeyAuthentication(keyEntity));
            });
    }
    
    private String extractApiKey(ServerHttpRequest request) {
        String headerKey = request.getHeaders().getFirst("X-API-Key");
        if (headerKey != null) {
            return headerKey;
        }
        return request.getQueryParams().getFirst("api_key");
    }
}
```

---

## Question 29: What custom authentication providers did you implement?

### Answer

### Custom Authentication Providers

```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Override
    public boolean supports(ServerHttpRequest request) {
        return request.getHeaders().containsKey("X-Custom-Auth");
    }
    
    @Override
    public Mono<Authentication> authenticate(ServerWebExchange exchange) {
        // Custom authentication logic
        String customToken = extractCustomToken(exchange);
        return validateCustomToken(customToken);
    }
}
```

---

## Question 30: How did you determine which authentication method to use for each request?

### Answer

### Authentication Method Selection

```java
@Component
public class AuthenticationSelector {
    private final List<AuthenticationProvider> providers;
    
    public Mono<AuthenticationProvider> selectProvider(ServerHttpRequest request) {
        // Priority order: OAuth 2.0 > API Key > Custom
        for (AuthenticationProvider provider : providers) {
            if (provider.supports(request)) {
                return Mono.just(provider);
            }
        }
        return Mono.error(new AuthenticationException("No authentication method"));
    }
}
```

---

## Summary

Part 6 covers questions 26-30 on Authentication Providers:
- Multiple authentication mechanisms
- OAuth 2.0 implementation
- API key authentication
- Custom authentication providers
- Authentication method selection

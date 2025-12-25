# Spring Security In-Depth Interview Guide: Authentication, Authorization, OAuth2 & JWT

## Table of Contents
1. [Spring Security Overview](#spring-security-overview)
2. [Authentication](#authentication)
3. [Authorization](#authorization)
4. [OAuth2](#oauth2)
5. [JWT (JSON Web Tokens)](#jwt-json-web-tokens)
6. [Best Practices](#best-practices)
7. [Interview Questions & Answers](#interview-questions--answers)

---

## Spring Security Overview

### What is Spring Security?

**Spring Security** is a framework that provides:
- **Authentication**: Who you are (login)
- **Authorization**: What you can do (permissions)
- **Protection**: CSRF, XSS, session fixation
- **Integration**: Works with Spring Framework, Spring Boot

### Key Concepts

1. **Principal**: Represents the user/entity
2. **Authentication**: Process of verifying identity
3. **Authorization**: Process of granting/denying access
4. **Security Context**: Holds authentication information
5. **Filter Chain**: Series of security filters

### Spring Security Architecture

```
Request
    ↓
Security Filter Chain
    ↓
Authentication Manager
    ↓
Authentication Provider
    ↓
User Details Service
    ↓
Security Context
    ↓
Authorization Decision
    ↓
Response
```

### Adding Spring Security

**Maven Dependency:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

**Default Behavior:**
- All endpoints require authentication
- Default user: `user`
- Default password: Generated (shown in logs)
- Form-based login enabled
- CSRF protection enabled

---

## Authentication

### What is Authentication?

**Authentication** is the process of verifying:
- **Who you are**: Identity verification
- **Credentials**: Username/password, tokens, certificates

### Authentication Flow

```
1. User submits credentials
    ↓
2. Authentication Filter extracts credentials
    ↓
3. Authentication Manager processes authentication
    ↓
4. Authentication Provider validates credentials
    ↓
5. UserDetailsService loads user details
    ↓
6. Authentication object created
    ↓
7. Security Context updated
    ↓
8. User is authenticated
```

### In-Memory Authentication

**Basic Configuration:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic();
        return http.build();
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withDefaultPasswordEncoder()
                .username("user")
                .password("password")
                .roles("USER")
                .build();
        
        UserDetails admin = User.withDefaultPasswordEncoder()
                .username("admin")
                .password("admin")
                .roles("ADMIN")
                .build();
        
        return new InMemoryUserDetailsManager(user, admin);
    }
}
```

### Database Authentication

**UserDetailsService Implementation:**

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
    
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());
    }
}
```

**Security Configuration:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    
    public SecurityConfig(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            )
            .userDetailsService(userDetailsService);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Password Encoding

**BCryptPasswordEncoder (Recommended):**

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // Strength 4-31, default 10
    }
}

// Usage in service
@Service
public class UserService {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }
    
    public User createUser(String username, String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(username, encodedPassword);
        return userRepository.save(user);
    }
    
    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }
}
```

**Other Password Encoders:**

```java
// BCrypt (Recommended)
PasswordEncoder bcrypt = new BCryptPasswordEncoder();

// Argon2 (Modern, secure)
PasswordEncoder argon2 = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

// PBKDF2
PasswordEncoder pbkdf2 = new Pbkdf2PasswordEncoder();

// SCrypt
PasswordEncoder scrypt = new SCryptPasswordEncoder();

// NoOp (Not recommended, for testing only)
PasswordEncoder noOp = NoOpPasswordEncoder.getInstance();
```

### Form-Based Authentication

**Custom Login Page:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .usernameParameter("username")
                .passwordParameter("password")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .failureHandler(customAuthenticationFailureHandler())
                .successHandler(customAuthenticationSuccessHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .logoutSuccessHandler(customLogoutSuccessHandler())
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
            );
        
        return http.build();
    }
    
    @Bean
    public AuthenticationFailureHandler customAuthenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
    
    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }
    
    @Bean
    public LogoutSuccessHandler customLogoutSuccessHandler() {
        return new CustomLogoutSuccessHandler();
    }
}
```

**Login Controller:**

```java
@Controller
public class LoginController {
    
    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
```

**Login HTML:**

```html
<!DOCTYPE html>
<html>
<head>
    <title>Login</title>
</head>
<body>
    <form method="post" action="/perform_login">
        <input type="text" name="username" placeholder="Username" required/>
        <input type="password" name="password" placeholder="Password" required/>
        <button type="submit">Login</button>
    </form>
    <div th:if="${param.error}">Invalid username or password</div>
    <div th:if="${param.logout}">You have been logged out</div>
</body>
</html>
```

### HTTP Basic Authentication

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .httpBasic();
        
        return http.build();
    }
}
```

### Custom Authentication Provider

```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    
    public CustomAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) 
            throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        UserDetails user = userDetailsService.loadUserByUsername(username);
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        
        if (!user.isEnabled()) {
            throw new DisabledException("User account is disabled");
        }
        
        return new UsernamePasswordAuthenticationToken(
                user, password, user.getAuthorities());
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
                .isAssignableFrom(authentication);
    }
}

// Register in SecurityConfig
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final CustomAuthenticationProvider authenticationProvider;
    
    public SecurityConfig(CustomAuthenticationProvider authenticationProvider) {
        this.authenticationProvider = authenticationProvider;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .formLogin();
        
        return http.build();
    }
}
```

### Security Context

**Accessing Authentication:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Or inject Authentication
        return ResponseEntity.ok(userService.findByUsername(username));
    }
    
    @GetMapping("/me-injected")
    public ResponseEntity<User> getCurrentUserInjected(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
    
    @GetMapping("/me-principal")
    public ResponseEntity<User> getCurrentUserPrincipal(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        return ResponseEntity.ok(userService.findByUsername(username));
    }
}
```

---

## Authorization

### What is Authorization?

**Authorization** is the process of:
- **Granting/denying access** to resources
- **Based on roles** (ROLE_ADMIN, ROLE_USER)
- **Based on permissions** (READ, WRITE, DELETE)
- **Method-level security**
- **URL-level security**

### Role-Based Access Control (RBAC)

**Configuring Roles:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/**").hasAuthority("API_ACCESS")
                .anyRequest().authenticated()
            )
            .formLogin();
        
        return http.build();
    }
}
```

**Authorization Methods:**

```java
// Has role
.hasRole("ADMIN")           // ROLE_ADMIN
.hasAnyRole("USER", "ADMIN") // ROLE_USER or ROLE_ADMIN

// Has authority
.hasAuthority("READ")        // Exact authority
.hasAnyAuthority("READ", "WRITE")

// Expression-based
.access("hasRole('ADMIN')")
.access("hasAuthority('READ') and hasAuthority('WRITE')")
.access("@securityService.canAccess(authentication, #id)")

// Permit/Deny
.permitAll()
.denyAll()
.authenticated()
```

### Method-Level Security

**Enable Method Security:**

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class MethodSecurityConfig {
    // Configuration
}
```

**@PreAuthorize:**

```java
@Service
public class UserService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @PreAuthorize("hasAuthority('USER_READ') or hasRole('ADMIN')")
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }
    
    @PreAuthorize("@securityService.canAccessUser(authentication, #id)")
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

**@PostAuthorize:**

```java
@Service
public class UserService {
    
    @PostAuthorize("returnObject.owner == authentication.name or hasRole('ADMIN')")
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

**@Secured:**

```java
@Service
public class UserService {
    
    @Secured("ROLE_ADMIN")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
}
```

**@RolesAllowed (JSR-250):**

```java
@Service
public class UserService {
    
    @RolesAllowed("ADMIN")
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
```

### Permission-Based Access Control

**Custom Permission Evaluator:**

```java
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {
    
    @Override
    public boolean hasPermission(
            Authentication auth,
            Object targetDomainObject,
            Object permission) {
        
        if (auth == null || targetDomainObject == null || !(permission instanceof String)) {
            return false;
        }
        
        String targetType = targetDomainObject.getClass().getSimpleName().toUpperCase();
        return hasPrivilege(auth, targetType, permission.toString().toUpperCase());
    }
    
    @Override
    public boolean hasPermission(
            Authentication auth,
            Serializable targetId,
            String targetType,
            Object permission) {
        
        if (auth == null || targetType == null || !(permission instanceof String)) {
            return false;
        }
        
        return hasPrivilege(auth, targetType.toUpperCase(), permission.toString().toUpperCase());
    }
    
    private boolean hasPrivilege(Authentication auth, String targetType, String permission) {
        for (GrantedAuthority grantedAuth : auth.getAuthorities()) {
            if (grantedAuth.getAuthority().startsWith(targetType) &&
                grantedAuth.getAuthority().contains(permission)) {
                return true;
            }
        }
        return false;
    }
}

// Register in SecurityConfig
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public PermissionEvaluator permissionEvaluator() {
        return new CustomPermissionEvaluator();
    }
}

// Usage
@Service
public class UserService {
    
    @PreAuthorize("hasPermission(#id, 'User', 'READ')")
    public User getUser(Long id) {
        return userRepository.findById(id).orElseThrow();
    }
    
    @PreAuthorize("hasPermission(#user, 'WRITE')")
    public User updateUser(User user) {
        return userRepository.save(user);
    }
}
```

---

## OAuth2

### What is OAuth2?

**OAuth2** is an authorization framework that:
- **Delegates authorization** to third-party services
- **Access tokens** instead of credentials
- **Scopes** for fine-grained permissions
- **Multiple grant types** (Authorization Code, Client Credentials, etc.)

### OAuth2 Roles

1. **Resource Owner**: User who owns the resource
2. **Client**: Application requesting access
3. **Authorization Server**: Issues access tokens
4. **Resource Server**: Hosts protected resources

### OAuth2 Grant Types

#### 1. **Authorization Code** (Most Common)

**Flow:**
```
1. Client redirects user to authorization server
2. User authenticates and authorizes
3. Authorization server redirects back with code
4. Client exchanges code for access token
5. Client uses access token to access resources
```

**Spring Security OAuth2 Client:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

**Configuration:**

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid,profile,email
            redirect-uri: http://localhost:8080/login/oauth2/code/google
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub
```

**Security Configuration:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard")
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> customOAuth2UserService() {
        return new CustomOAuth2UserService();
    }
}
```

#### 2. **Client Credentials** (Server-to-Server)

**Configuration:**

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          my-client:
            client-id: ${CLIENT_ID}
            client-secret: ${CLIENT_SECRET}
            authorization-grant-type: client_credentials
            scope: read,write
        provider:
          my-client:
            token-uri: https://auth.example.com/oauth/token
```

**Usage:**

```java
@Service
public class ApiService {
    
    private final OAuth2AuthorizedClientService clientService;
    
    public ApiService(OAuth2AuthorizedClientService clientService) {
        this.clientService = clientService;
    }
    
    public String callApi() {
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                "my-client", "principal");
        
        String accessToken = client.getAccessToken().getTokenValue();
        
        // Use access token to call API
        return restTemplate.exchange(
                "https://api.example.com/data",
                HttpMethod.GET,
                new HttpEntity<>(createHeaders(accessToken)),
                String.class
        ).getBody();
    }
}
```

#### 3. **Resource Server** (Protecting APIs)

**Dependencies:**

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**Configuration:**

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://auth.example.com
          jwk-set-uri: https://auth.example.com/.well-known/jwks.json
```

**Security Configuration:**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri("https://auth.example.com/.well-known/jwks.json")
                .build();
    }
}
```

**Accessing JWT Claims:**

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("sub", jwt.getSubject());
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("roles", jwt.getClaim("roles"));
        return ResponseEntity.ok(userInfo);
    }
}
```

---

## JWT (JSON Web Tokens)

### What is JWT?

**JWT (JSON Web Token)** is a compact, URL-safe token format:
- **Stateless**: No server-side session
- **Self-contained**: Contains claims (user info, permissions)
- **Signed**: Prevents tampering
- **Three parts**: Header.Payload.Signature

### JWT Structure

```
Header.Payload.Signature

Header: {"alg": "HS256", "typ": "JWT"}
Payload: {"sub": "user123", "name": "John", "exp": 1234567890}
Signature: HMACSHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

### JWT Dependencies

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

### JWT Service

**Creating JWT Service:**

```java
@Service
public class JwtService {
    
    private final String secretKey;
    private final long jwtExpiration;
    
    public JwtService(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expiration}") long jwtExpiration) {
        this.secretKey = secretKey;
        this.jwtExpiration = jwtExpiration;
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userDetails.getUsername());
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return createToken(claims, userDetails.getUsername());
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
```

### JWT Authentication Filter

**Creating JWT Filter:**

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            final String jwt = authHeader.substring(7);
            final String username = jwtService.extractUsername(jwt);
            
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                if (jwtService.validateToken(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e);
        }
        
        filterChain.doFilter(request, response);
    }
}
```

**Security Configuration with JWT:**

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    
    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthFilter,
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### JWT Authentication Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);
        
        return ResponseEntity.ok(new AuthResponse(token));
    }
    
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String newToken = jwtService.generateToken(userDetails);
        
        return ResponseEntity.ok(new AuthResponse(newToken));
    }
}
```

### JWT Configuration Properties

```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-base64-encoded}
  expiration: 86400000  # 24 hours in milliseconds
```

**Generate Secret Key:**

```java
SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
String base64Key = Encoders.BASE64.encode(key.getEncoded());
System.out.println("JWT Secret: " + base64Key);
```

---

## Best Practices

### Security Best Practices

1. **Password Encoding**: Always use BCryptPasswordEncoder
2. **HTTPS**: Use HTTPS in production
3. **CSRF Protection**: Enable for state-changing operations
4. **Session Management**: Use secure session configuration
5. **JWT Expiration**: Set reasonable expiration times
6. **Secret Keys**: Store securely, never commit to version control
7. **Input Validation**: Validate all user inputs
8. **Principle of Least Privilege**: Grant minimum necessary permissions

### Authentication Best Practices

1. **Strong Passwords**: Enforce password policies
2. **Account Lockout**: Lock accounts after failed attempts
3. **Password Reset**: Secure password reset flow
4. **Multi-Factor Authentication**: Enable MFA for sensitive operations
5. **Session Timeout**: Configure appropriate session timeout

### Authorization Best Practices

1. **Role-Based**: Use roles for coarse-grained access
2. **Permission-Based**: Use permissions for fine-grained access
3. **Method Security**: Protect methods with @PreAuthorize
4. **URL Security**: Protect endpoints with request matchers
5. **Resource-Level**: Implement resource-level authorization

### JWT Best Practices

1. **Short Expiration**: Use short-lived access tokens
2. **Refresh Tokens**: Use refresh tokens for long-lived sessions
3. **Token Storage**: Store tokens securely (httpOnly cookies, memory)
4. **Token Rotation**: Rotate refresh tokens
5. **Blacklisting**: Implement token blacklisting for logout

---

## Interview Questions & Answers

### Q1: What is the difference between Authentication and Authorization?

**Answer:**
- **Authentication**: Verifies who you are (identity verification)
- **Authorization**: Determines what you can do (permission check)
- Authentication comes first, then authorization

### Q2: How does Spring Security filter chain work?

**Answer:**
- Series of filters that process requests
- Each filter has specific responsibility
- Filters execute in order
- SecurityContext is shared across filters
- Last filter is DispatcherServlet

### Q3: What is the difference between hasRole() and hasAuthority()?

**Answer:**
- **hasRole()**: Automatically prefixes with "ROLE_", checks for ROLE_ADMIN
- **hasAuthority()**: Checks exact authority, no prefix
- hasRole("ADMIN") is equivalent to hasAuthority("ROLE_ADMIN")

### Q4: How do you implement JWT authentication in Spring Security?

**Answer:**
1. Create JwtService for token generation/validation
2. Create JwtAuthenticationFilter to extract and validate tokens
3. Configure SecurityFilterChain with stateless session
4. Add filter before UsernamePasswordAuthenticationFilter
5. Extract token from Authorization header
6. Validate token and set SecurityContext

### Q5: What is OAuth2 and its grant types?

**Answer:**
- **OAuth2**: Authorization framework for delegated access
- **Grant Types**:
  - Authorization Code: Web applications
  - Client Credentials: Server-to-server
  - Implicit: Legacy, not recommended
  - Resource Owner Password: Not recommended
  - Refresh Token: Token renewal

### Q6: How do you handle password encoding in Spring Security?

**Answer:**
- Use PasswordEncoder interface
- BCryptPasswordEncoder is recommended
- Encode passwords when storing
- Use matches() to verify passwords
- Never store plain text passwords

### Q7: What is @PreAuthorize and @PostAuthorize?

**Answer:**
- **@PreAuthorize**: Checks authorization before method execution
- **@PostAuthorize**: Checks authorization after method execution, can use return value
- Both use SpEL expressions
- Enable with @EnableMethodSecurity

### Q8: How do you implement custom authentication in Spring Security?

**Answer:**
1. Implement AuthenticationProvider interface
2. Override authenticate() method
3. Validate credentials
4. Return Authentication object
5. Register provider in SecurityConfig
6. Optionally implement UserDetailsService

### Q9: What is the Security Context?

**Answer:**
- Holds Authentication object for current user
- Thread-local storage
- Accessible via SecurityContextHolder
- Set after successful authentication
- Used for authorization decisions

### Q10: How do you secure REST APIs with JWT?

**Answer:**
1. Stateless session management
2. JWT filter extracts token from Authorization header
3. Validate token and extract user info
4. Set SecurityContext with authentication
5. Use method security or URL security for authorization
6. Return 401 for invalid/missing tokens

---

## Summary

**Key Takeaways:**
1. **Authentication**: Verify identity (who you are)
2. **Authorization**: Grant/deny access (what you can do)
3. **OAuth2**: Delegated authorization framework
4. **JWT**: Stateless token-based authentication
5. **Password Encoding**: Always use BCryptPasswordEncoder
6. **Method Security**: Use @PreAuthorize for method-level authorization
7. **Best Practices**: HTTPS, CSRF protection, secure token storage

**Complete Coverage:**
- Authentication (in-memory, database, custom providers)
- Authorization (RBAC, method-level, permission-based)
- OAuth2 (all grant types, resource server)
- JWT (token generation, validation, filters)
- Security configuration
- Best practices and interview Q&A

---

**Guide Complete** - Ready for interview preparation!


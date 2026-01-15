# Spring Framework Ecosystem - Complete Guide (Part 5: Spring Security)

## 🔒 Spring Security: Authentication, Authorization, OAuth2, JWT

---

## 1. Spring Security Architecture

### Security Filter Chain
```
┌─────────────────────────────────────────────────────────────┐
│              Security Filter Chain                          │
└─────────────────────────────────────────────────────────────┘

    HTTP Request
    │
    ▼
┌──────────────────────┐
│  Security Filter Chain│
│                      │
│  ┌────────────────┐  │
│  │ SecurityContext│  │  ← Stores authentication
│  │ Persistence    │  │
│  └────────────────┘  │
│                      │
│  ┌────────────────┐  │
│  │ UsernamePassword│  │  ← Form login
│  │ Authentication │  │
│  └────────────────┘  │
│                      │
│  ┌────────────────┐  │
│  │ Basic Auth     │  │  ← HTTP Basic
│  └────────────────┘  │
│                      │
│  ┌────────────────┐  │
│  │ JWT Filter     │  │  ← JWT tokens
│  └────────────────┘  │
│                      │
│  ┌────────────────┐  │
│  │ Authorization │  │  ← Access control
│  │ Filter        │  │
│  └────────────────┘  │
│                      │
│  ┌────────────────┐  │
│  │ Exception      │  │  ← Error handling
│  │ Translation    │  │
│  └────────────────┘  │
└──────────────────────┘
    │
    │ (if authenticated & authorized)
    ▼
    Your Controller
```

### Security Configuration
```
┌─────────────────────────────────────────────────────────────┐
│              Security Configuration                         │
└─────────────────────────────────────────────────────────────┘

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
            )
            .httpBasic(Customizer.withDefaults());
        
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
            .roles("USER", "ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(user, admin);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## 2. Authentication

### Authentication Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Authentication Process                          │
└─────────────────────────────────────────────────────────────┘

1. User submits credentials
    │
    │ POST /login
    │ username=user&password=pass
    ▼
2. UsernamePasswordAuthenticationFilter
    ┌──────────────────────┐
    │ Extracts credentials │
    └──────────┬───────────┘
                │
                ▼
3. AuthenticationManager
    ┌──────────────────────┐
    │ Delegates to         │
    │ Provider             │
    └──────────┬───────────┘
                │
                ▼
4. AuthenticationProvider
    ┌──────────────────────┐
    │ - Loads UserDetails   │
    │ - Validates password  │
    │ - Creates Authentication│
    └──────────┬───────────┘
                │
                ▼
5. SecurityContext
    ┌──────────────────────┐
    │ Stores Authentication│
    │ (for session)         │
    └──────────┬───────────┘
                │
                ▼
6. Success Handler
    ┌──────────────────────┐
    │ Redirects to         │
    │ success URL          │
    └──────────────────────┘
```

### Custom Authentication Provider
```
┌─────────────────────────────────────────────────────────────┐
│              Custom Authentication                          │
└─────────────────────────────────────────────────────────────┘

@Component
public class CustomAuthenticationProvider 
    implements AuthenticationProvider {
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public Authentication authenticate(
            Authentication authentication) 
            throws AuthenticationException {
        
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        UserDetails user = userDetailsService
            .loadUserByUsername(username);
        
        if (passwordEncoder.matches(password, user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(
                user,
                password,
                user.getAuthorities()
            );
        } else {
            throw new BadCredentialsException("Invalid password");
        }
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
            .isAssignableFrom(authentication);
    }
}

Configuration:
@Configuration
public class SecurityConfig {
    
    @Autowired
    private CustomAuthenticationProvider authProvider;
    
    @Bean
    public AuthenticationManager authManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {
        http.authenticationProvider(authProvider);
        return http.build();
    }
}
```

### UserDetailsService
```
┌─────────────────────────────────────────────────────────────┐
│              UserDetailsService                             │
└─────────────────────────────────────────────────────────────┘

@Service
public class CustomUserDetailsService 
    implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found"));
        
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(getAuthorities(user))
            .accountExpired(false)
            .accountLocked(false)
            .credentialsExpired(false)
            .disabled(!user.isEnabled())
            .build();
    }
    
    private Collection<? extends GrantedAuthority> 
            getAuthorities(User user) {
        return user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(
                "ROLE_" + role.getName()))
            .collect(Collectors.toList());
    }
}
```

---

## 3. Authorization

### Method Security
```
┌─────────────────────────────────────────────────────────────┐
│              Method-Level Security                          │
└─────────────────────────────────────────────────────────────┘

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
    // Enables @PreAuthorize, @PostAuthorize, etc.
}

Service:
@Service
public class UserService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        // Only ADMIN can delete
    }
    
    @PreAuthorize("hasRole('USER') and #userId == authentication.principal.id")
    public User getUser(Long userId) {
        // Users can only view their own data
    }
    
    @PostAuthorize("returnObject.owner == authentication.name")
    public User getSecureUser(Long id) {
        // Check after method execution
    }
    
    @Secured("ROLE_ADMIN")
    public void adminOnly() {
        // Simple role check
    }
    
    @RolesAllowed("ADMIN")
    public void rolesAllowed() {
        // JSR-250 annotation
    }
}
```

### URL-Based Authorization
```
┌─────────────────────────────────────────────────────────────┐
│              URL Authorization                             │
└─────────────────────────────────────────────────────────────┘

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/public/**").permitAll()
                
                // Role-based
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("USER", "ADMIN")
                
                // Authority-based
                .requestMatchers("/read/**").hasAuthority("READ")
                .requestMatchers("/write/**").hasAuthority("WRITE")
                
                // IP-based
                .requestMatchers("/internal/**")
                    .hasIpAddress("192.168.1.0/24")
                
                // Custom expression
                .requestMatchers("/api/users/{id}")
                    .access(new WebExpressionAuthorizationManager(
                        "@securityService.canAccess(authentication, #id)"))
                
                // All other requests
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
```

---

## 4. JWT (JSON Web Token)

### JWT Structure
```
┌─────────────────────────────────────────────────────────────┐
│              JWT Token Structure                            │
└─────────────────────────────────────────────────────────────┘

JWT Token:
    header.payload.signature

Header:
{
  "alg": "HS256",
  "typ": "JWT"
}
    │
    │ Base64 encoded
    ▼
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9

Payload:
{
  "sub": "user123",
  "name": "John Doe",
  "roles": ["USER", "ADMIN"],
  "iat": 1516239022,
  "exp": 1516242622
}
    │
    │ Base64 encoded
    ▼
eyJzdWIiOiJ1c2VyMTIzIiwibmFtZSI6IkpvaG4gRG9lIn0

Signature:
HMACSHA256(
  base64UrlEncode(header) + "." +
  base64UrlEncode(payload),
  secret
)
    │
    ▼
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c

Complete Token:
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.
eyJzdWIiOiJ1c2VyMTIzIiwibmFtZSI6IkpvaG4gRG9lIn0.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### JWT Authentication Flow
```
┌─────────────────────────────────────────────────────────────┐
│              JWT Authentication Flow                         │
└─────────────────────────────────────────────────────────────┘

1. Login Request
    POST /api/login
    {
      "username": "user",
      "password": "pass"
    }
    │
    ▼
2. Validate Credentials
    ┌──────────────────────┐
    │ AuthenticationManager│
    │ validates user       │
    └──────────┬───────────┘
                │
                ▼
3. Generate JWT
    ┌──────────────────────┐
    │ JwtTokenProvider     │
    │ creates JWT          │
    └──────────┬───────────┘
                │
                ▼
4. Return JWT
    {
      "token": "eyJhbGciOiJIUzI1NiIs..."
    }
    │
    │ Client stores token
    │
    ▼
5. Subsequent Requests
    GET /api/users
    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
    │
    ▼
6. JWT Filter
    ┌──────────────────────┐
    │ Validates token      │
    │ Extracts user info   │
    │ Sets SecurityContext │
    └──────────┬───────────┘
                │
                ▼
7. Process Request
    Controller receives authenticated request
```

### JWT Implementation
```
┌─────────────────────────────────────────────────────────────┐
│              JWT Implementation                              │
└─────────────────────────────────────────────────────────────┘

JWT Filter:
@Component
public class JwtAuthenticationFilter 
    extends OncePerRequestFilter {
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) 
            throws ServletException, IOException {
        
        String token = getTokenFromRequest(request);
        
        if (token != null && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = 
                userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
            
            SecurityContextHolder.getContext()
                .setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

JWT Token Provider:
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = 
            (UserDetails) authentication.getPrincipal();
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, secret)
            .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(secret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
}
```

---

## 5. OAuth2

### OAuth2 Flow
```
┌─────────────────────────────────────────────────────────────┐
│              OAuth2 Authorization Code Flow                 │
└─────────────────────────────────────────────────────────────┘

1. User clicks "Login with Google"
    │
    │ Redirect to:
    ▼
2. Authorization Server (Google)
    ┌──────────────────────┐
    │ User authenticates   │
    │ Grants permissions   │
    └──────────┬───────────┘
                │
                │ Returns authorization code
                ▼
3. Client Application
    ┌──────────────────────┐
    │ Exchanges code for   │
    │ access token         │
    └──────────┬───────────┘
                │
                │ Access token
                ▼
4. Resource Server
    ┌──────────────────────┐
    │ Validates token      │
    │ Returns user data    │
    └──────────────────────┘
```

### Spring Security OAuth2
```
┌─────────────────────────────────────────────────────────────┐
│              OAuth2 Client Configuration                    │
└─────────────────────────────────────────────────────────────┘

application.yml:
┌─────────────────────────────────────┐
│ spring:                             │
│   security:                         │
│     oauth2:                         │
│       client:                       │
│         registration:               │
│           google:                    │
│             client-id: ${GOOGLE_CLIENT_ID}│
│             client-secret: ${GOOGLE_CLIENT_SECRET}│
│             scope:                  │
│               - email               │
│               - profile             │
│         provider:                   │
│           google:                   │
│             authorization-uri: https://accounts.google.com/o/oauth2/auth│
│             token-uri: https://oauth2.googleapis.com/token│
│             user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo│
└─────────────────────────────────────┘

Security Configuration:
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(customOAuth2UserService)
                )
                .successHandler(oauth2SuccessHandler)
            )
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}

OAuth2UserService:
@Service
public class CustomOAuth2UserService 
    implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {
    
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) 
            throws OAuth2AuthenticationException {
        OAuth2User oauth2User = 
            defaultOAuth2UserService.loadUser(userRequest);
        
        // Map OAuth2 user to your User entity
        // Save/update user in database
        
        return oauth2User;
    }
}
```

---

## Key Concepts Summary

### Security Best Practices
```
┌─────────────────────────────────────────────────────────────┐
│              Security Best Practices                       │
└─────────────────────────────────────────────────────────────┘

✅ Use strong password encoding
   - BCrypt, Argon2
   - Never store plain passwords

✅ Implement proper authentication
   - Multi-factor when possible
   - Account lockout policies

✅ Use HTTPS in production
   - Encrypt all communications
   - Protect tokens in transit

✅ Validate and sanitize input
   - Prevent injection attacks
   - XSS protection

✅ Implement proper authorization
   - Principle of least privilege
   - Role-based access control

✅ Secure JWT tokens
   - Use strong secrets
   - Set appropriate expiration
   - Use HTTPS only

✅ Regular security updates
   - Keep dependencies updated
   - Monitor security advisories
```

---

**Next: Part 6 will cover Spring Cloud - Microservices Patterns, Service Discovery, Config Server, Gateway.**


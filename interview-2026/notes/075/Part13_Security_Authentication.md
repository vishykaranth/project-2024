# Part 13: Security & Authentication - Quick Revision

## Authentication Methods

- **OAuth 2.0**: Authorization framework, access tokens, refresh tokens
- **JWT (JSON Web Tokens)**: Stateless tokens, self-contained, signed
- **API Keys**: Simple authentication, identify clients
- **SAML**: XML-based authentication, enterprise SSO

## Authorization

- **RBAC (Role-Based Access Control)**: Users have roles, roles have permissions
- **ABAC (Attribute-Based Access Control)**: Fine-grained, attribute-based
- **ACL (Access Control Lists)**: Per-resource permissions

## Security Best Practices

- **Encryption**: At rest (database) and in transit (TLS/SSL)
- **Input Validation**: Validate all inputs, prevent injection attacks
- **Secrets Management**: Store secrets securely (Vault, AWS Secrets Manager)
- **Rate Limiting**: Prevent abuse, DDoS protection
- **HTTPS**: Always use for sensitive data

## Common Vulnerabilities

- **SQL Injection**: Use parameterized queries
- **XSS (Cross-Site Scripting)**: Sanitize user input
- **CSRF (Cross-Site Request Forgery)**: Use CSRF tokens
- **Insecure Direct Object References**: Validate access permissions

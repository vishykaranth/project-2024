# Part 22: Spring Boot - Quick Revision

## Auto-Configuration

- **@ConditionalOnClass**: Configure if class is present
- **@ConditionalOnProperty**: Configure based on properties
- **@ConditionalOnBean**: Configure if bean exists
- **Purpose**: Automatically configure beans based on classpath and properties

## Starter Dependencies

- **spring-boot-starter-web**: Web applications (Tomcat, Spring MVC)
- **spring-boot-starter-data-jpa**: JPA, Hibernate
- **spring-boot-starter-security**: Spring Security
- **spring-boot-starter-test**: Testing dependencies

## Spring Boot Actuator

- **Endpoints**: /health, /metrics, /info, /env, /beans, /mappings
- **Health Checks**: Custom health indicators
- **Metrics**: Application metrics, custom metrics
- **Production Ready**: Monitoring and management endpoints

## Application Properties

- **application.properties**: Key-value pairs
- **application.yml**: YAML format, hierarchical
- **Profile-Specific**: application-dev.properties, application-prod.properties
- **@ConfigurationProperties**: Bind properties to POJOs

## DevTools

- **Automatic Restart**: Restart on classpath changes
- **Live Reload**: Browser refresh on changes
- **Property Defaults**: Sensible defaults for development

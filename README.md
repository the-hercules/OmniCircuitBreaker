# OmniResilience

A lightweight, thread-safe circuit breaker and retry library for Java applications.

## Modules

- **omniresilience-core**: Core circuit breaker and retry implementation (plain Java)
- **omniresilience-spring-boot-starter**: Spring Boot auto-configuration and annotations
- **omniresilience-examples**: Demo applications

## Quick Start

### Plain Java
```java
CircuitBreakerConfig config = CircuitBreakerConfig.builder()
    .failureThreshold(5)
    .timeout(Duration.ofSeconds(30))
    .maxRetries(3)
    .build();

CircuitBreaker cb = new CircuitBreaker("myService", config);

String result = cb.execute(() -> externalService.call());
```

### Spring Boot
```java
@Service
public class UserService {
    @CircuitBreaker(name = "getUser", failureThreshold = 5, maxRetries = 3)
    public User getUser(String id) {
        return externalApi.fetchUser(id);
    }
}
```

## Building
```bash
mvn clean install
```

## Running Examples
```bash
# Plain Java
cd omniresilience-examples/plain-java-example
mvn exec:java

# Spring Boot
cd omniresilience-examples/spring-boot-example
mvn spring-boot:run
```
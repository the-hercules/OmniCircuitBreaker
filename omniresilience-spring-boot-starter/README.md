# OmniResilience Spring Boot Starter

Spring Boot integration for OmniResilience.

## Usage
```java
@Retry(maxAttempts = 3)
@CircuitBreaker(name = "upstream")
public String call() {
    return client.fetch();
}
```

## Configuration
```yaml
omniresilience:
  circuitbreaker:
    failure-rate-threshold: 0.5
    sliding-window-size: 10
    minimum-number-of-calls: 10
    open-state-duration-millis: 30000
    half-open-max-calls: 3
```

# OmniResilience Core

Core resilience utilities for plain Java usage.

## Features
- Circuit breaker with CLOSED/OPEN/HALF_OPEN states
- Retry policy with pluggable backoff strategies
- Metrics snapshotting

## Quick Example
```java
CircuitBreaker breaker = new CircuitBreaker("example", CircuitBreakerConfig.builder().build());
String result = breaker.execute(() -> "ok");
```

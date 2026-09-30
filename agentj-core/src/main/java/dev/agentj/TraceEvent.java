package dev.agentj;

import java.time.Instant;
import java.util.Map;

public record TraceEvent(Instant at, String type, String name, long durationMs, Map<String,Object> attributes) {
    public static TraceEvent of(String type, String name, long durationMs, Map<String,Object> attributes) { return new TraceEvent(Instant.now(), type, name, durationMs, attributes == null ? Map.of() : Map.copyOf(attributes)); }
}

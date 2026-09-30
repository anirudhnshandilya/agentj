package dev.agentj;

public interface Tracer {
    void emit(TraceEvent event);
    Tracer NOOP = event -> {};
}

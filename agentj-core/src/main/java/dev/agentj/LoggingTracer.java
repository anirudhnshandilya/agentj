package dev.agentj;

import java.util.logging.Logger;

public final class LoggingTracer implements Tracer {
    private final Logger log;
    public LoggingTracer(Class<?> owner) { this.log = Logger.getLogger(owner.getName()); }
    public void emit(TraceEvent e) { log.info(() -> e.type() + " " + e.name() + " " + e.durationMs() + "ms " + e.attributes()); }
}

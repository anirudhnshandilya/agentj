package dev.agentj;

import java.util.*;

public final class CompositeToolProvider implements ToolProvider {
    private final List<ToolProvider> providers;
    public CompositeToolProvider(ToolProvider... providers) { this.providers = List.of(providers); }
    public Collection<ToolSpec> specs() { return providers.stream().flatMap(p -> p.specs().stream()).toList(); }
    public Object invoke(String name, Map<String,Object> args) throws Exception {
        for (ToolProvider p : providers) if (p.specs().stream().anyMatch(s -> s.name().equals(name))) return p.invoke(name, args);
        throw new IllegalArgumentException("Unknown tool: " + name);
    }
}

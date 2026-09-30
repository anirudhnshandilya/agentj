package dev.agentj;

import java.util.Map;

public record ToolCall(String id, String name, Map<String,Object> arguments) {
    public ToolCall { arguments = Map.copyOf(arguments == null ? Map.of() : arguments); }
}

package dev.agentj;

import java.util.List;

public record ModelResponse(String text, List<ToolCall> toolCalls, String raw) {
    public ModelResponse { toolCalls = List.copyOf(toolCalls == null ? List.of() : toolCalls); }
    public boolean hasToolCalls() { return !toolCalls.isEmpty(); }
}

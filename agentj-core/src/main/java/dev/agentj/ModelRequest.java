package dev.agentj;

import java.util.List;

public record ModelRequest(String system, List<Message> messages, List<ToolSpec> tools) {
    public ModelRequest {
        messages = List.copyOf(messages == null ? List.of() : messages);
        tools = List.copyOf(tools == null ? List.of() : tools);
    }
}

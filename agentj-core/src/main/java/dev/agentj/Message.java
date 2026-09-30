package dev.agentj;

import java.util.Map;

public record Message(Role role, String content, String toolCallId, String toolName, Map<String,Object> toolArguments) {
    public enum Role { SYSTEM, USER, ASSISTANT, TOOL }
    public static Message user(String content) { return new Message(Role.USER, content, null, null, Map.of()); }
    public static Message assistant(String content) { return new Message(Role.ASSISTANT, content, null, null, Map.of()); }
    public static Message tool(String id, String name, String content) { return new Message(Role.TOOL, content, id, name, Map.of()); }
    public static Message toolCall(String id, String name, Map<String,Object> args) { return new Message(Role.ASSISTANT, null, id, name, args); }
}

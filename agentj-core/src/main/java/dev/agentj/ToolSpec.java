package dev.agentj;

import com.fasterxml.jackson.databind.JsonNode;

public record ToolSpec(String name, String description, JsonNode inputSchema) {}

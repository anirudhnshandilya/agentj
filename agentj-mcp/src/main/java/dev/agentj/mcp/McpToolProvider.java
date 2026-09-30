package dev.agentj.mcp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.agentj.ToolProvider;
import dev.agentj.ToolSpec;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;

import java.net.URI;
import java.util.*;

/** Exposes an MCP server's tools as native AgentJ tools. */
public final class McpToolProvider implements ToolProvider, AutoCloseable {
    private final McpSyncClient client;
    private final ObjectMapper mapper = new ObjectMapper();
    private volatile List<ToolSpec> specs = List.of();

    McpToolProvider(McpSyncClient client) { this.client = client; refresh(); }

    public static McpToolProvider streamableHttp(String baseUrl) {
        var transport = HttpClientStreamableHttpTransport.builder(baseUrl).build();
        var client = McpClient.sync(transport).build();
        client.initialize();
        return new McpToolProvider(client);
    }

    public void refresh() {
        List<ToolSpec> next = new ArrayList<>();
        String cursor = null;
        do {
            McpSchema.ListToolsResult page = client.listTools(cursor);
            for (McpSchema.Tool tool : page.tools()) {
                JsonNode schema = mapper.valueToTree(tool.inputSchema());
                next.add(new ToolSpec(tool.name(), Optional.ofNullable(tool.description()).orElse("MCP tool"), schema));
            }
            cursor = page.nextCursor();
        } while (cursor != null);
        specs = List.copyOf(next);
    }

    @Override public Collection<ToolSpec> specs() { return specs; }

    @Override public Object invoke(String name, Map<String,Object> arguments) {
        var result = client.callTool(McpSchema.CallToolRequest.builder(name).arguments(arguments).build());
        return result.content().stream().map(Object::toString).toList();
    }

    @Override public void close() { client.closeGracefully(); }
}

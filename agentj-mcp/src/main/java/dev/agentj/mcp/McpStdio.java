package dev.agentj.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.json.McpJsonDefaults;

import java.util.List;

/** Small factory for connecting AgentJ to a local MCP process over STDIO. */
public final class McpStdio {
    private McpStdio() {}
    public static McpToolProvider connect(String command, List<String> args) {
        ServerParameters params = ServerParameters.builder(command).args(args).build();
        StdioClientTransport transport = new StdioClientTransport(params, McpJsonDefaults.getMapper());
        McpSyncClient client = McpClient.sync(transport).build();
        client.initialize();
        return new McpToolProvider(client);
    }
}

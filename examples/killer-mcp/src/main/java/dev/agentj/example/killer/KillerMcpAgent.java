package dev.agentj.example.killer;

import dev.agentj.Agent;
import dev.agentj.CompositeToolProvider;
import dev.agentj.LoggingTracer;
import dev.agentj.Tool;
import dev.agentj.ToolProvider;
import dev.agentj.mcp.McpStdio;
import dev.agentj.openai.OpenAIResponsesModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * The AgentJ launch demo: one local Java tool registry + one MCP server + one model loop.
 * The client automatically launches this same JAR in "server" mode over STDIO.
 */
public final class KillerMcpAgent {
    private KillerMcpAgent() {}

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "server".equals(args[0])) {
            runMcpServer();
            return;
        }

        String prompt = args.length == 0
                ? "Plan a customer order for 3 Java Starter Kits. Check the catalog, verify stock, "
                  + "calculate the 20% VAT-inclusive total with the local Java quote tool, and give me a concise order summary."
                : String.join(" ", args);

        Path jar = Path.of(KillerMcpAgent.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI());

        System.out.println("\nAgentJ MCP launch demo");
        System.out.println("======================");
        System.out.println("Prompt: " + prompt);
        System.out.println();

        try (var mcp = McpStdio.connect("java", List.of("-jar", jar.toString(), "server"))) {
            ToolProvider local = new dev.agentj.ToolRegistry(new ObjectMapper())
                    .register(new LocalOrderTools());
            ToolProvider allTools = new CompositeToolProvider(local, mcp);

            System.out.println("Discovered tools:");
            allTools.specs().forEach(spec -> System.out.println("  • " + spec.name() + " — " + spec.description()));
            System.out.println();

            var agent = Agent.builder()
                    .model(OpenAIResponsesModel.fromEnv())
                    .system("You are a precise order-planning agent. Use the MCP catalog and inventory tools "
                            + "for product facts. Use the local Java calculateQuote tool for arithmetic. "
                            + "Never invent catalog, inventory, or pricing data.")
                    .toolRegistry(allTools)
                    .tracer(new LoggingTracer(KillerMcpAgent.class))
                    .build();

            System.out.println("Agent execution:\n");
            System.out.println(agent.run(prompt));
        }
    }

    private static void runMcpServer() {
        var transport = new StdioServerTransportProvider(io.modelcontextprotocol.json.McpJsonDefaults.getMapper());
        var server = McpServer.sync(transport)
                .serverInfo("agentj-demo-catalog", "0.1.0")
                .capabilities(ServerCapabilities.builder().tools(true).build())
                .toolCall(
                        McpSchema.Tool.builder("catalog_search", schema("query", "string"))
                                .description("Search the demo product catalog. Returns product name, SKU, unit price, and short description.")
                                .build(),
                        (exchange, request) -> {
                            String query = String.valueOf(request.arguments().get("query")).toLowerCase();
                            String result = query.contains("starter") || query.contains("java")
                                    ? "Java Starter Kit | SKU AGJ-STARTER | £49.00 | A compact Java developer kit for building AI agent prototypes."
                                    : "No matching demo products. Try searching for 'Java Starter Kit'.";
                            return McpSchema.CallToolResult.builder()
                                    .content(List.of(new McpSchema.TextContent(result)))
                                    .build();
                        })
                .toolCall(
                        McpSchema.Tool.builder("inventory_check", schema("sku", "string"))
                                .description("Check current demo inventory for a product SKU. Returns available quantity.")
                                .build(),
                        (exchange, request) -> {
                            String sku = String.valueOf(request.arguments().get("sku"));
                            String result = "AGJ-STARTER".equalsIgnoreCase(sku)
                                    ? "SKU AGJ-STARTER | 17 units available | warehouse demo-east"
                                    : "SKU " + sku + " | 0 units available";
                            return McpSchema.CallToolResult.builder()
                                    .content(List.of(new McpSchema.TextContent(result)))
                                    .build();
                        })
                .build();

        // Keep the server process alive; STDIO transport owns the protocol lifecycle.
        System.err.println("AgentJ demo MCP server ready");
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            server.close();
        }
    }

    private static Map<String, Object> schema(String name, String type) {
        return Map.of(
                "type", "object",
                "properties", Map.of(name, Map.of("type", type)),
                "required", List.of(name),
                "additionalProperties", false);
    }

    static final class LocalOrderTools {
        @dev.agentj.Tool(description = "Calculate an order total including VAT. Use this for arithmetic rather than estimating.")
        public double calculateQuote(double unitPrice, int quantity, double vatRate) {
            return Math.round(unitPrice * quantity * (1.0 + vatRate) * 100.0) / 100.0;
        }
    }
}

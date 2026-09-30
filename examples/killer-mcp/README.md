# Killer MCP Agent Demo

This is the AgentJ launch demo: a real model-driven agent that combines **ordinary Java tools** with tools discovered from a **real MCP server over STDIO**.

The demo is intentionally self-contained. The shaded JAR contains both sides of the MCP connection. When the agent starts, it launches itself again in `server` mode and connects to it through the MCP Java SDK.

## What the agent can do

```text
User task
   │
   ▼
AgentJ Agent
   │
   ├── local Java tool: calculateQuote
   │
   └── MCP tools
       ├── catalog_search
       └── inventory_check
```

The default prompt asks the agent to:

1. Find the Java Starter Kit in the MCP catalog.
2. Check its live demo inventory.
3. Calculate a VAT-inclusive total with an ordinary Java method.
4. Produce a concise order summary.

## Run it

From the repository root:

```bash
export OPENAI_API_KEY="your-key"
export OPENAI_MODEL="your-model"
./mvnw -pl examples/killer-mcp -am package
java -jar examples/killer-mcp/target/agentj-example-killer-mcp-0.1.0.jar
```

Windows PowerShell:

```powershell
$env:OPENAI_API_KEY = "your-key"
$env:OPENAI_MODEL = "your-model"
.\mvnw.cmd -pl examples/killer-mcp -am package
java -jar examples/killer-mcp/target/agentj-example-killer-mcp-0.1.0.jar
```

You can also pass your own task:

```bash
java -jar examples/killer-mcp/target/agentj-example-killer-mcp-0.1.0.jar \
  "Find the Java Starter Kit, check whether 5 are available, and calculate the VAT-inclusive total."
```

## Why this example matters

There is no special AgentJ-specific MCP abstraction in the application code. MCP is plugged into the same `ToolProvider` seam as local Java tools, then `CompositeToolProvider` presents one tool surface to the agent.

That is the core AgentJ idea: **your existing Java methods and external MCP capabilities can participate in one small agent loop.**

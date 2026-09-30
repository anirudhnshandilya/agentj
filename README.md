# AgentJ

<p align="center">
  <strong>Java-native AI agents for LLMs, Jev decisions, and MCP.</strong><br/>
  Plain Java. Small core. Provider-neutral. Decision-aware.
</p>

<p align="center">
  <a href="https://github.com/anirudhnshandilya/agentj/actions/workflows/ci.yml"><img src="https://github.com/anirudhnshandilya/agentj/actions/workflows/ci.yml/badge.svg" alt="CI"></a>
  <img src="https://img.shields.io/badge/Java-21%2B-007396" alt="Java 21+">
  <img src="https://img.shields.io/badge/MCP-2.0.1-7c3aed" alt="MCP 2.0.1">
  <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="MIT License">
  <img src="https://img.shields.io/badge/status-0.1.0--early--access-orange" alt="0.1.0 early access">
</p>

<p align="center">
  <img src="docs/assets/agentj-mcp-demo.svg" alt="AgentJ combining local Java tools with MCP tools" width="900">
</p>

AgentJ is a small Java-native runtime for **AI-powered software**. It combines generative model tool calling, typed Jev decisions, ordinary Java methods, and MCP tools without forcing your application into a new programming model.

The point is not to hide Java behind another programming model. Your application keeps its Java objects, methods, tests, and boundaries; AgentJ supplies the agent loop around them.

## The idea: generation when you need it, decisions when you do not

Most agent frameworks treat every AI interaction as text generation. AgentJ is designed to let your application choose the right primitive.

- **LLMs** generate text, reason through tasks, and decide which tools to call.
- **Jev / System One** returns typed decisions, probabilities, and confidence for bounded choices such as routing, classification, scoring, and yes/no gates.
- **Java** remains the deterministic execution layer.
- **MCP** connects the agent to external tools.

```text
                         AgentJ
                            │
              ┌─────────────┴─────────────┐
              │                           │
         Generative AI              Decision AI
              │                         Jev
        OpenAI / future                 │
              │                   typed probabilities
              │                         │
              └─────────────┬───────────┘
                            ▼
                    Java application
                            │
                   ┌────────┴────────┐
                   ▼                 ▼
               Java @Tool           MCP
```

This is the direction that makes AgentJ different: **AI does the fuzzy work; Java owns the workflow.**

### Jev in 30 seconds

Jev is TypeSafe AI's System One model. Instead of generating prose, it evaluates application state against typed `choice`, `score`, and `noul` questions and returns structured answers with probabilities/confidence.

```java
try (var jev = JevClient.fromEnv()) {
    var questions = new LinkedHashMap<String, JevQuestion>();
    questions.put("team", JevQuestion.Choice.of(
            "Which team should handle this?",
            Map.of("billing", "Payments and refunds",
                   "technical", "Bugs and integrations")));
    questions.put("urgent", new JevQuestion.Noul(
            "Does this require same-day human attention?"));

    var result = jev.ask(ticket, questions);

    var team = (JevAnswer.Choice) result.answer("team");
    var urgent = (JevAnswer.Noul) result.answer("urgent");

    if (urgent.isTrue(0.80)) {
        escalate(team.choice());
    }
}
```

The important distinction is that Jev is **not** forced through AgentJ's text-generation `Model` interface. It has a dedicated decision API because decisions and generation are different primitives.

See [`agentj-jev`](agentj-jev) and [`examples/hybrid-agent`](examples/hybrid-agent).

**No API key is required to try the hybrid example.** It defaults to a deterministic local decision provider so the architecture can be explored offline. Set `AGENTJ_JEV_MODE=remote` and `TYPESAFE_API_KEY` only when you want real hosted Jev inference.

## The 60-second demo

The repository includes a self-contained end-to-end MCP demo. One shaded JAR contains the AgentJ client **and** a tiny MCP server. The client launches the server over STDIO, discovers its tools, combines them with a normal Java `@Tool`, and lets the model decide what to call.

```text
                         ┌─────────────────────────┐
                         │       AgentJ Agent       │
                         │   model → tools → loop   │
                         └───────────┬─────────────┘
                                     │
                    ┌────────────────┴────────────────┐
                    │                                 │
             local Java @Tool                    MCP / STDIO
                    │                                 │
             calculateQuote              ┌────────────┴────────────┐
                                         │                         │
                                  catalog_search            inventory_check
```

### Run it

Requirements: **JDK 21+** and an API key for the model provider configured by the OpenAI adapter.

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

The default task asks the agent to find a product, check inventory, calculate a VAT-inclusive total, and produce an order summary. You should see three tools discovered: one local Java method and two MCP tools.

Pass your own task as arguments:

```bash
java -jar examples/killer-mcp/target/agentj-example-killer-mcp-0.1.0.jar \
  "Find the Java Starter Kit, check whether 5 are available, and calculate the VAT-inclusive total."
```

**Demo source:** [`examples/killer-mcp`](examples/killer-mcp) · **Recording guide:** [`docs/DEMO.md`](docs/DEMO.md)

## Why AgentJ?

If your application already has useful Java methods, those methods are your tools. AgentJ makes them visible to the model with a small annotation and keeps the runtime provider-neutral.

```java
final class Tools {
    @Tool(description = "Add two integers.")
    public int add(int a, int b) {
        return a + b;
    }
}

var agent = Agent.builder()
        .model(OpenAIResponsesModel.fromEnv())
        .tools(new Tools())
        .build();

System.out.println(agent.run("Use the add tool to calculate 19 + 23."));
```

MCP then plugs into the same `ToolProvider` seam:

```java
try (var mcp = McpStdio.connect("my-mcp-server", List.of("--stdio"))) {
    var local = new ToolRegistry(new ObjectMapper()).register(new Tools());
    var tools = new CompositeToolProvider(local, mcp);

    var agent = Agent.builder()
            .model(OpenAIResponsesModel.fromEnv())
            .toolRegistry(tools)
            .build();

    System.out.println(agent.run("Use the available tools to complete the task."));
}
```

The result is deliberately boring at the API level—and that is the feature: **Java methods and MCP capabilities become one tool surface.**

## What is included in 0.1.0

- **Java 21+** — records, interfaces, and ordinary Java types.
- **`@Tool` reflection** — register methods on your own objects as model-callable tools.
- **Generated tool schemas** — describe Java method parameters to the model.
- **Provider-neutral `Model` interface** — keep the core independent of a model vendor.
- **OpenAI Responses adapter** — small HTTP-based integration in `agentj-openai`.
- **Jev decision adapter** — typed `choice`, `score`, and `noul` decisions in `agentj-jev`.
- **MCP bridge** — consume tools from MCP servers over STDIO and Streamable HTTP.
- **Memory abstraction** — in-memory and JSON-file implementations.
- **Tracing abstraction** — no-op and logging implementations.
- **Composite tool providers** — combine local and external tool sources.
- **Starter CLI** — environment diagnostics and project initialization.
- **End-to-end MCP example** — local Java tools + MCP tools + model in one runnable JAR.

AgentJ's MCP integration is built on the [official Java MCP SDK](https://github.com/modelcontextprotocol/java-sdk). The SDK provides Java MCP clients/servers and standard MCP transports; AgentJ keeps that dependency behind its `ToolProvider` boundary.

## Quick start

### Build and test

Clone the repository:

```bash
git clone https://github.com/anirudhnshandilya/agentj.git
cd agentj
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw.cmd test
```

Package all modules:

```bash
./mvnw package
```

### Basic agent

The basic example uses local Java tools and the OpenAI adapter:

```bash
export OPENAI_API_KEY="your-key"
export OPENAI_MODEL="your-model"
./mvnw -pl examples/basic -am package
```

Then inspect [`examples/basic`](examples/basic) for the complete minimal application.

### MCP agent

For the full launch demo:

```bash
./mvnw -pl examples/killer-mcp -am package
java -jar examples/killer-mcp/target/agentj-example-killer-mcp-0.1.0.jar
```

See [`examples/killer-mcp/README.md`](examples/killer-mcp/README.md).

## Architecture

AgentJ has a deliberately small set of seams:

| Component | Responsibility |
|---|---|
| `Agent` | Bounded synchronous model/tool execution loop |
| `Model` | Provider-neutral model generation interface |
| `ToolProvider` | Common discovery/invocation seam for every tool source |
| `ToolRegistry` | Local Java `@Tool` registration and invocation |
| `CompositeToolProvider` | Combine local and MCP/external tools |
| `Memory` | Conversation state |
| `Tracer` | Execution events |
| `agentj-openai` | OpenAI Responses API adapter |
| `agentj-mcp` | MCP tool bridge |
| `agentj-cli` | Starter command-line utilities |

```text
agentj-core
   │
   ├── Agent ──────── Model
   │      │
   │      ├────────── ToolProvider
   │      │              ├── ToolRegistry (@Tool Java methods)
   │      │              └── McpToolProvider (MCP)
   │      │
   │      ├────────── Memory
   │      └────────── Tracer
   │
   ├── agentj-openai
   └── agentj-mcp
```

More detail: [`ARCHITECTURE.md`](ARCHITECTURE.md).

## Modules

```text
agentj-core       → runtime interfaces, agent loop, tools, memory, tracing
agentj-openai     → OpenAI Responses API adapter
agentj-mcp        → MCP tool provider
agentj-cli        → command-line utilities
examples/basic    → minimal local-tool example
examples/killer-mcp → launch demo: Java tools + MCP + model
```

## Security model

AgentJ does **not** sandbox Java tools. An `@Tool` method runs with the same privileges as the hosting process. MCP tools are also capabilities supplied by the connected server.

Treat tools as privileged capabilities. For production systems, add the authentication, authorization, network/filesystem restrictions, validation, and human-approval controls appropriate to your environment. MCP tool metadata should also be treated as untrusted unless the server is trusted; the MCP specification explicitly cautions clients not to make tool-use decisions based solely on annotations from untrusted servers.

See [`SECURITY.md`](SECURITY.md).

## Project status

**0.1.0 — early access.**

The public API is intentionally small and may evolve. The project is useful today for experimentation, internal applications, prototypes, and contributors interested in shaping a lightweight Java agent runtime.

The current execution loop is synchronous and bounded. Streaming, asynchronous execution, richer schema generation, approval policies, additional model adapters, and broader integration coverage are natural follow-ons rather than hidden promises of the 0.1.0 release.

## Roadmap

Near-term areas are intentionally focused:

- More model adapters behind the same `Model` interface.
- Stronger schema generation for Java types.
- Streaming and asynchronous execution paths.
- Tool approval/policy hooks.
- More MCP transports and integration tests.
- Better observability integrations.
- More real-world examples.

## Contributing

Issues, examples, tests, documentation improvements, and integrations are welcome.

```bash
./mvnw test
```

Please keep the core small, add tests for behavioral changes, and update user-facing documentation when APIs change.

See [`CONTRIBUTING.md`](CONTRIBUTING.md).

## License

AgentJ is released under the [MIT License](LICENSE).

## Links

- [Killer MCP demo](examples/killer-mcp)
- [Terminal demo guide](docs/DEMO.md)
- [Architecture](ARCHITECTURE.md)
- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)
- [Security](SECURITY.md)
- [Basic example](examples/basic)
- [GitHub Issues](https://github.com/anirudhnshandilya/agentj/issues)
- [Official Java MCP SDK](https://github.com/modelcontextprotocol/java-sdk)

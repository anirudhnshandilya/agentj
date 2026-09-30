# AgentJ 0.1.0

AgentJ 0.1.0 is the first public release of the project.

## Highlights

- Small Java-native agent execution loop.
- `@Tool` annotation for exposing ordinary Java methods as tools.
- Provider-neutral `Model` and `ToolProvider` interfaces.
- OpenAI Responses API adapter.
- MCP 2.0.1 bridge for STDIO and Streamable HTTP.
- In-memory and JSON-file memory implementations.
- Trace events and logging tracer.
- Composite tool providers for combining local and external tools.
- Starter CLI and end-to-end example.
- Maven Wrapper and GitHub Actions CI.

## Compatibility

- Java 21+
- Maven 3.9+ or the included Maven Wrapper

## Build from source

```bash
./mvnw clean test
./mvnw package
```

## Early-access notes

The 0.1.0 execution loop is synchronous and intentionally small. The public API may evolve as the project gains real-world usage. Streaming, asynchronous execution, richer schemas, additional model adapters, and stronger integration coverage are natural areas for future contributions.

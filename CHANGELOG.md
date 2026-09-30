# Changelog

## 0.1.0 - 2026-09-30

First public release.

### Added

- Java-native synchronous agent execution loop.
- `@Tool` reflection and JSON-schema-like input descriptions.
- Provider-neutral `Model` and `ToolProvider` interfaces.
- OpenAI Responses API adapter.
- MCP 2.0.1 tool bridge for STDIO and Streamable HTTP.
- In-memory and JSON-file memory implementations.
- Trace events and logging tracer.
- Composite tool providers.
- Starter CLI and basic end-to-end example.
- Maven Wrapper and GitHub Actions CI.

### Notes

The 0.1.0 API is early access. Streaming and asynchronous execution are intentionally outside the current synchronous core.

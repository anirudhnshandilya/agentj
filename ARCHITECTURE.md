# Architecture

AgentJ is intentionally layered. Generative models, decision models, tools, memory, and tracing have separate seams so an application can combine them without turning the core into a provider-specific framework.

```text
                         +----------------------+
                         |       Agent          |
                         | bounded execution    |
                         +----------+-----------+
                                    |
                    +---------------+----------------+
                    |               |                |
                    v               v                v
                 Model         ToolProvider        Memory
                    |               |                |
             OpenAI adapter   +-----+-----+     in-memory / file
                              |           |
                              v           v
                         local @Tool     MCP

                    Decision layer (optional)
                              |
                              v
                         agentj-jev
                    typed choice/score/noul
                              |
                              v
                         Java workflow
```

## Agent loop

1. Add the user message to memory.
2. Send the current conversation plus tool schemas to `Model`.
3. If the model returns tool calls, record each call and invoke the matching `ToolProvider`.
4. Add tool outputs to memory.
5. Repeat until the model returns text or `maxSteps` is reached.
6. Emit model/tool `TraceEvent`s throughout the run.

The loop is deliberately synchronous in the current 0.1.x-era API. Streaming and asynchronous execution belong in later adapters so the core remains easy to understand.

## Tool model

`ToolRegistry` reflects ordinary Java methods annotated with `@Tool`. It creates a JSON Schema-like description and invokes methods after converting arguments through Jackson.

`ToolProvider` is the seam for external capabilities. The MCP module implements that seam by discovering an MCP server's tools and forwarding calls through the official Java MCP SDK.

`CompositeToolProvider` lets local Java methods and MCP capabilities appear as one tool surface to the agent.

## Decision model

`agentj-jev` intentionally does **not** implement Jev as a `Model`. A generative model produces messages/tool calls; Jev produces typed decisions such as `choice`, `score`, and `noul`. Keeping these APIs separate makes the distinction explicit and lets deterministic Java code own the final workflow decision.

The module contains two providers:

- `JevClient` for remote TypeSafe System One / Jev inference.
- `LocalJevProvider` for deterministic, zero-key demos and tests. It is a development substitute, not Jev inference.

The hybrid example demonstrates the intended pattern:

```text
state
  |
  +--> decision provider --> typed result
                              |
                              v
                         ordinary Java
                         control flow
```

## Safety boundary

AgentJ does not sandbox arbitrary Java methods. An `@Tool` method has the same privileges as its hosting process. Applications should treat tools as privileged capabilities and add approval/authentication/authorization policies before exposing destructive operations.

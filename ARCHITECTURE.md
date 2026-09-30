# Architecture

AgentJ is intentionally layered.

```text
                    +----------------------+
                    |       Agent          |
                    | bounded execution    |
                    +----------+-----------+
                               |
             +-----------------+------------------+
             |                 |                  |
             v                 v                  v
          Model           ToolProvider         Memory
             |                 |                  |
       OpenAI adapter    local @Tool / MCP     in-memory / file
             |
             v
        external model
```

## Agent loop

1. Add the user message to memory.
2. Send the current conversation plus tool schemas to `Model`.
3. If the model returns tool calls, record each call and invoke the matching `ToolProvider`.
4. Add tool outputs to memory.
5. Repeat until the model returns text or `maxSteps` is reached.
6. Emit model/tool `TraceEvent`s throughout the run.

The loop is deliberately synchronous in 0.1.0. Streaming and asynchronous execution belong in later adapters so the core remains easy to understand.

## Tool model

`ToolRegistry` reflects ordinary Java methods annotated with `@Tool`. It creates a JSON Schema-like description and invokes methods after converting arguments through Jackson.

`ToolProvider` is the seam for external capabilities. The MCP module implements that seam by discovering an MCP server's tools and forwarding calls through the official Java MCP SDK.

## Safety boundary

AgentJ does not sandbox arbitrary Java methods. An `@Tool` method has the same privileges as its hosting process. Applications should treat tools as privileged capabilities and add approval/authentication/authorization policies before exposing destructive operations.

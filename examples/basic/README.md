# Basic AgentJ Example

This example demonstrates the smallest AgentJ application that combines:

1. an OpenAI Responses model,
2. ordinary Java methods annotated with `@Tool`, and
3. the AgentJ execution loop.

Set your credentials first:

```bash
export OPENAI_API_KEY="your-key"
export OPENAI_MODEL="your-model"
```

Windows PowerShell:

```powershell
$env:OPENAI_API_KEY = "your-key"
$env:OPENAI_MODEL = "your-model"
```

Build the example and its dependencies:

```bash
./mvnw -pl examples/basic -am package
```

The implementation is in [`BasicAgent.java`](src/main/java/dev/agentj/example/BasicAgent.java).

# AgentJ + Jev: AI Decisions Inside Java

This example demonstrates the idea behind AgentJ's Jev integration:

**Jev decides. Java executes.**

It is designed to run **without an API key by default**. The default local provider is a deterministic development/demo implementation of the same typed decision contract. It is **not Jev inference**.

If you have access to the hosted Jev service, switch to remote mode with `AGENTJ_JEV_MODE=remote` and `TYPESAFE_API_KEY`.

## Run with zero keys

Build:

```bash
./mvnw -pl examples/hybrid-agent -am package
```

Run:

```bash
java -jar examples/hybrid-agent/target/agentj-example-hybrid-0.1.0.jar \
  "The payment integration has failed for three days and the customer needs help today."
```

The default flow is completely local and offline:

```text
application state
      │
      ▼
 LocalJevProvider
      │
      ├── choice → routing
      ├── noul   → confidence gate
      └── score  → severity
      │
      ▼
ordinary Java code
      │
      ├── act automatically
      └── escalate to a human
```

## Use real Jev

Set your TypeSafe API key and opt into remote mode:

### macOS / Linux

```bash
export AGENTJ_JEV_MODE=remote
export TYPESAFE_API_KEY="your-key"
java -jar examples/hybrid-agent/target/agentj-example-hybrid-0.1.0.jar
```

### Windows PowerShell

```powershell
$env:AGENTJ_JEV_MODE = "remote"
$env:TYPESAFE_API_KEY = "your-key"
java -jar .\examples\hybrid-agent\target\agentj-example-hybrid-0.1.0.jar
```

The same Java application then uses `JevClient` against the hosted service.

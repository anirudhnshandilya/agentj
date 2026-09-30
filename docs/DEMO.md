# AgentJ terminal demo

The launch demo is designed to fit into a 30–60 second terminal recording.

## One-command setup

```bash
export OPENAI_API_KEY="your-key"
export OPENAI_MODEL="your-model"
./mvnw -pl examples/killer-mcp -am package
java -jar examples/killer-mcp/target/agentj-example-killer-mcp-0.1.0.jar
```

## Recording script

Use a clean terminal, a large font, and no editor windows.

1. Show the repository prompt:

   ```text
   $ java -jar examples/killer-mcp/target/agentj-example-killer-mcp-0.1.0.jar
   ```

2. Let the tool discovery appear:

   ```text
   AgentJ MCP launch demo
   ======================
   Prompt: Plan a customer order for 3 Java Starter Kits...

   Discovered tools:
     • calculateQuote — Calculate an order total including VAT...
     • catalog_search — Search the demo product catalog...
     • inventory_check — Check current demo inventory...
   ```

3. Let the trace lines show that the model calls tools. The key visual is that `catalog_search` and `inventory_check` come from MCP while `calculateQuote` is an ordinary Java method.

4. End on the natural-language result:

   ```text
   Java Starter Kit (AGJ-STARTER)
   • Unit price: £49.00
   • Quantity: 3
   • Inventory: 17 available
   • VAT-inclusive total: £176.40
   ```

5. Cut immediately to the README section explaining `CompositeToolProvider`.

## Visual asset

`docs/assets/agentj-mcp-demo.svg` is a static companion graphic for README/social posts. It deliberately describes the real execution path rather than pretending to be a captured terminal session.

## Important

The exact model wording and trace timing can vary between runs. The catalog, inventory, and arithmetic values in the demo are deterministic; the model is responsible for deciding which tools to call and in what order.

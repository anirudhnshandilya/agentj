package dev.agentj;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;

public final class Agent {
    private final Model model;
    private final String system;
    private final ToolProvider tools;
    private final Memory memory;
    private final Tracer tracer;
    private final int maxSteps;

    private Agent(Builder b) {
        this.model = Objects.requireNonNull(b.model, "model");
        this.system = b.system == null ? "You are a helpful, precise AI agent. Use tools when they are useful." : b.system;
        this.tools = b.tools;
        this.memory = b.memory;
        this.tracer = b.tracer;
        this.maxSteps = b.maxSteps;
    }

    public static Builder builder() { return new Builder(); }
    public String run(String prompt) throws Exception {
        memory.add(Message.user(prompt));
        for (int step = 1; step <= maxSteps; step++) {
            long start = System.nanoTime();
            ModelResponse response = model.generate(new ModelRequest(system, memory.messages(), List.copyOf(tools.specs())));
            tracer.emit(TraceEvent.of("model", "generate", elapsed(start), Map.of("step", step, "toolCalls", response.toolCalls().size())));
            if (!response.hasToolCalls()) {
                String text = response.text() == null ? "" : response.text();
                memory.add(Message.assistant(text));
                return text;
            }
            if (response.text() != null && !response.text().isBlank()) memory.add(Message.assistant(response.text()));
            for (ToolCall call : response.toolCalls()) {
                memory.add(Message.toolCall(call.id(), call.name(), call.arguments()));
                long toolStart = System.nanoTime();
                try {
                    Object result = tools.invoke(call.name(), call.arguments());
                    String serialized = result instanceof String s ? s : new ObjectMapper().writeValueAsString(result);
                    memory.add(Message.tool(call.id(), call.name(), serialized));
                    tracer.emit(TraceEvent.of("tool", call.name(), elapsed(toolStart), Map.of("ok", true)));
                } catch (Exception e) {
                    memory.add(Message.tool(call.id(), call.name(), "ERROR: " + e.getMessage()));
                    tracer.emit(TraceEvent.of("tool", call.name(), elapsed(toolStart), Map.of("ok", false, "error", String.valueOf(e.getMessage()))));
                }
            }
        }
        throw new IllegalStateException("Agent exceeded maxSteps=" + maxSteps);
    }
    public Memory memory() { return memory; }
    public ToolProvider tools() { return tools; }

    private static long elapsed(long start) { return (System.nanoTime() - start) / 1_000_000; }

    public static final class Builder {
        private Model model;
        private String system;
        private ToolRegistry localTools = new ToolRegistry(new ObjectMapper());
        private ToolProvider tools = localTools;
        private Memory memory = new InMemoryMemory();
        private Tracer tracer = Tracer.NOOP;
        private int maxSteps = 12;
        public Builder model(Model model) { this.model = model; return this; }
        public Builder system(String system) { this.system = system; return this; }
        public Builder tools(Object... instances) { for (Object i : instances) localTools.register(i); tools = localTools; return this; }
        public Builder toolRegistry(ToolProvider provider) { this.tools = provider; return this; }
        public Builder memory(Memory memory) { this.memory = memory; return this; }
        public Builder tracer(Tracer tracer) { this.tracer = tracer; return this; }
        public Builder maxSteps(int maxSteps) { if (maxSteps < 1) throw new IllegalArgumentException("maxSteps"); this.maxSteps = maxSteps; return this; }
        public Agent build() { return new Agent(this); }
    }
}

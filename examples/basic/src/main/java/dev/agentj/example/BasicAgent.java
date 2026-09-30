package dev.agentj.example;

import dev.agentj.Agent;
import dev.agentj.Tool;
import dev.agentj.openai.OpenAIResponsesModel;

public final class BasicAgent {
    static final class Tools {
        @Tool(description="Add two integers.")
        public int add(int a, int b) { return a + b; }
        @Tool(description="Return a greeting for a name.")
        public String hello(String name) { return "Hello, " + name + "!"; }
    }
    public static void main(String[] args) throws Exception {
        var agent = Agent.builder().model(OpenAIResponsesModel.fromEnv()).tools(new Tools()).build();
        System.out.println(agent.run(args.length == 0 ? "Use the tools to add 19 and 23, then explain the result." : String.join(" ", args)));
    }
}

package dev.agentj;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AgentTest {
    static class Tools {
        @Tool(description="Add two integers")
        public int add(int a, int b) { return a + b; }
        @Tool(description="Echo text")
        public String echo(String input) { return input; }
    }

    @Test void registersAndInvokesTools() throws Exception {
        ToolRegistry registry = new ToolRegistry(new com.fasterxml.jackson.databind.ObjectMapper()).register(new Tools());
        assertTrue(registry.contains("add"));
        assertEquals(5, registry.invoke("add", Map.of("a",2,"b",3)));
    }

    @Test void convertsGenericToolArguments() throws Exception {
        class GenericTools {
            @Tool(description="Echo a list of strings")
            public String join(List<String> values) { return String.join(",", values); }
        }
        ToolRegistry registry = new ToolRegistry(new com.fasterxml.jackson.databind.ObjectMapper()).register(new GenericTools());
        assertEquals("a,b", registry.invoke("join", Map.of("values", List.of("a", "b"))));
    }

    @Test void agentLoopsThroughToolCall() throws Exception {
        List<ModelRequest> requests = new ArrayList<>();
        Model model = request -> {
            requests.add(request);
            if (requests.size() == 1) return new ModelResponse("", List.of(new ToolCall("1", "add", Map.of("a", 2, "b", 4))), "");
            return new ModelResponse("6", List.of(), "");
        };
        String answer = Agent.builder().model(model).tools(new Tools()).build().run("What is 2+4?");
        assertEquals("6", answer);
        assertEquals(2, requests.size());
    }
}

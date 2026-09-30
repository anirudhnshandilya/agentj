package dev.agentj.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dev.agentj.*;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/** Minimal, dependency-free OpenAI Responses API client for AgentJ. */
public final class OpenAIResponsesModel implements Model {
    private final HttpClient http;
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;
    private final URI endpoint;

    public OpenAIResponsesModel(String apiKey, String model) {
        this(apiKey, model, URI.create("https://api.openai.com/v1/responses"));
    }
    public OpenAIResponsesModel(String apiKey, String model, URI endpoint) {
        this.apiKey = Objects.requireNonNull(apiKey);
        this.model = Objects.requireNonNull(model);
        this.endpoint = Objects.requireNonNull(endpoint);
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build();
        this.mapper = new ObjectMapper();
    }

    public static OpenAIResponsesModel fromEnv() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("OPENAI_API_KEY is not set");
        return new OpenAIResponsesModel(key, System.getenv().getOrDefault("OPENAI_MODEL", "gpt-5.6-luna"));
    }

    @Override public ModelResponse generate(ModelRequest request) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("instructions", request.system());
        ArrayNode input = body.putArray("input");
        for (Message m : request.messages()) append(input, m);
        ArrayNode tools = body.putArray("tools");
        for (ToolSpec spec : request.tools()) {
            ObjectNode t = tools.addObject();
            t.put("type", "function"); t.put("name", spec.name()); t.put("description", spec.description()); t.set("parameters", spec.inputSchema()); t.put("strict", false);
        }
        body.put("parallel_tool_calls", true);

        HttpRequest req = HttpRequest.newBuilder(endpoint)
            .timeout(Duration.ofMinutes(3))
            .header("Authorization", "Bearer " + apiKey)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body))).build();
        HttpResponse<String> response = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) throw new IllegalStateException("OpenAI API " + response.statusCode() + ": " + response.body());
        JsonNode root = mapper.readTree(response.body());
        List<ToolCall> calls = new ArrayList<>();
        String text = root.path("output_text").asText("");
        for (JsonNode item : root.path("output")) {
            if ("function_call".equals(item.path("type").asText())) {
                String args = item.path("arguments").asText("{}");
                @SuppressWarnings("unchecked") Map<String,Object> parsed = mapper.readValue(args, Map.class);
                calls.add(new ToolCall(item.path("call_id").asText(item.path("id").asText()), item.path("name").asText(), parsed));
            }
        }
        return new ModelResponse(text, calls, response.body());
    }

    private void append(ArrayNode input, Message m) {
        switch (m.role()) {
            case USER -> {
                ObjectNode item = input.addObject(); item.put("type", "message"); item.put("role", "user");
                ArrayNode content = item.putArray("content"); ObjectNode text = content.addObject(); text.put("type", "input_text"); text.put("text", Objects.toString(m.content(), ""));
            }
            case ASSISTANT -> {
                if (m.toolCallId() == null) {
                    ObjectNode item = input.addObject(); item.put("type", "message"); item.put("role", "assistant");
                    ArrayNode content = item.putArray("content"); ObjectNode text = content.addObject(); text.put("type", "output_text"); text.put("text", Objects.toString(m.content(), ""));
                } else {
                    ObjectNode call = input.addObject(); call.put("type", "function_call"); call.put("call_id", m.toolCallId()); call.put("name", m.toolName());
                    try { call.put("arguments", mapper.writeValueAsString(m.toolArguments())); } catch (Exception e) { throw new RuntimeException(e); }
                }
            }
            case TOOL -> { ObjectNode item = input.addObject(); item.put("type", "function_call_output"); item.put("call_id", m.toolCallId()); item.put("output", Objects.toString(m.content(), "")); }
            case SYSTEM -> { /* system is sent as instructions */ }
        }
    }
}

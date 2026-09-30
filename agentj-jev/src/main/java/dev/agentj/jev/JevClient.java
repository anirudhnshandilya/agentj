package dev.agentj.jev;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.*;

/**
 * Small Java client for TypeSafe AI System One / Jev.
 *
 * Jev is a decision model, not a chat model: state goes in and typed,
 * probabilistic decisions come back. This client intentionally exposes that
 * distinction instead of forcing Jev through a text-generation abstraction.
 */
public final class JevClient implements JevProvider {
    public static final URI DEFAULT_BASE_URL = URI.create("https://api.typesafe.ai");
    private final String apiKey;
    private final URI baseUrl;
    private final String model;
    private final HttpClient http;
    private final ObjectMapper mapper;

    private JevClient(Builder b) {
        this.apiKey = Objects.requireNonNull(b.apiKey, "apiKey");
        this.baseUrl = b.baseUrl;
        this.model = b.model;
        this.mapper = b.mapper;
        this.http = b.http != null ? b.http : HttpClient.newBuilder().connectTimeout(b.timeout).build();
    }

    public static Builder builder() { return new Builder(); }

    public static JevClient fromEnv() {
        String key = System.getenv("TYPESAFE_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("TYPESAFE_API_KEY is not set");
        return builder().apiKey(key).build();
    }

    public JevResponse ask(Object state, Map<String, JevQuestion> questions) throws IOException, InterruptedException {
        if (questions == null || questions.isEmpty()) throw new IllegalArgumentException("questions must not be empty");
        ObjectNode root = mapper.createObjectNode();
        root.put("model", model);
        root.set("state", mapper.valueToTree(state));
        ObjectNode qs = root.putObject("questions");
        for (var entry : questions.entrySet()) writeQuestion(qs.putObject(entry.getKey()), entry.getValue());

        HttpRequest request = HttpRequest.newBuilder(baseUrl.resolve("/v1/systemone"))
                .timeout(Duration.ofSeconds(60))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(root)))
                .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() / 100 != 2) {
            throw new IOException("Jev API " + response.statusCode() + ": " + response.body());
        }
        return parse(response.body());
    }

    private void writeQuestion(ObjectNode node, JevQuestion question) {
        if (question instanceof JevQuestion.Noul q) {
            node.put("type", "noul");
            node.put("instructions", q.instructions());
            if (!q.criteria().isEmpty()) node.set("criteria", mapper.valueToTree(q.criteria()));
        } else if (question instanceof JevQuestion.Choice q) {
            node.put("type", "choice");
            node.put("instructions", q.instructions());
            node.set("criteria", mapper.valueToTree(q.criteria()));
        } else if (question instanceof JevQuestion.Score q) {
            node.put("type", "score");
            node.put("instructions", q.instructions());
            node.set("criteria", mapper.valueToTree(q.criteria()));
        }
    }

    private JevResponse parse(String body) throws IOException {
        JsonNode root = mapper.readTree(body);
        Map<String, JevAnswer> answers = new LinkedHashMap<>();
        JsonNode answerNode = root.path("answers");
        answerNode.fields().forEachRemaining(e -> {
            JsonNode a = e.getValue();
            String type = a.path("type").asText();
            Double confidence = a.has("confidence") && !a.get("confidence").isNull() ? a.get("confidence").asDouble() : null;
            if ("noul".equals(type)) {
                answers.put(e.getKey(), new JevAnswer.Noul(a.path("noul").asDouble(), confidence));
            } else if ("choice".equals(type)) {
                Map<String, Double> probabilities = doubles(a.path("probabilities"));
                answers.put(e.getKey(), new JevAnswer.Choice(a.path("choice").asText(), probabilities, confidence));
            } else if ("score".equals(type)) {
                Map<String, Double> probabilities = doubles(a.path("probabilities"));
                List<String> legend = new ArrayList<>();
                JsonNode legendNode = a.path("legend");
                if (legendNode.isArray()) legendNode.forEach(n -> legend.add(n.asText()));
                answers.put(e.getKey(), new JevAnswer.Score(a.path("score").asDouble(), probabilities, legend, confidence));
            }
        });
        JsonNode usage = root.path("usage");
        JevResponse.Usage u = new JevResponse.Usage(usage.path("input_tokens").asLong(0), usage.path("output_tokens").asLong(0));
        return new JevResponse(root.path("model").asText(model), answers, u, body);
    }

    private static Map<String, Double> doubles(JsonNode node) {
        Map<String, Double> out = new LinkedHashMap<>();
        node.fields().forEachRemaining(e -> out.put(e.getKey(), e.getValue().asDouble()));
        return out;
    }

    @Override public void close() { /* HttpClient has no close operation on Java 21. */ }

    public static final class Builder {
        private String apiKey;
        private URI baseUrl = DEFAULT_BASE_URL;
        private String model = "jev-latest";
        private HttpClient http;
        private ObjectMapper mapper = new ObjectMapper();
        private Duration timeout = Duration.ofSeconds(10);
        public Builder apiKey(String apiKey) { this.apiKey = apiKey; return this; }
        public Builder baseUrl(String baseUrl) { this.baseUrl = URI.create(baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl); return this; }
        public Builder model(String model) { this.model = Objects.requireNonNull(model); return this; }
        public Builder httpClient(HttpClient http) { this.http = http; return this; }
        public Builder objectMapper(ObjectMapper mapper) { this.mapper = mapper; return this; }
        public Builder timeout(Duration timeout) { this.timeout = timeout; return this; }
        public JevClient build() { return new JevClient(this); }
    }
}

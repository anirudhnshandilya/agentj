package dev.agentj.jev;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class JevClientTest {
    @Test void sendsTypedQuestionsAndParsesAnswers() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v1/systemone", exchange -> {
            assertEquals("Bearer test-key", exchange.getRequestHeaders().getFirst("Authorization"));
            String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(body.contains("\"type\":\"choice\""));
            assertTrue(body.contains("\"type\":\"noul\""));
            String response = """
                    {"model":"jev-test","answers":{
                      "team":{"type":"choice","choice":"billing","probabilities":{"billing":0.9,"support":0.1},"confidence":0.9},
                      "urgent":{"type":"noul","noul":0.82,"confidence":0.82}},
                      "usage":{"input_tokens":12,"output_tokens":0}}
                    """;
            exchange.sendResponseHeaders(200, response.getBytes(StandardCharsets.UTF_8).length);
            exchange.getResponseBody().write(response.getBytes(StandardCharsets.UTF_8));
            exchange.close();
        });
        server.start();
        try (var client = JevClient.builder().apiKey("test-key").baseUrl("http://localhost:" + server.getAddress().getPort()).build()) {
            var response = client.ask(Map.of("message", "charged twice"), new LinkedHashMap<>(Map.of(
                    "team", JevQuestion.Choice.of("Which team?", Map.of("billing", "payments", "support", "technical")),
                    "urgent", new JevQuestion.Noul("Is this urgent?")
            )));
            assertEquals("billing", ((JevAnswer.Choice) response.answer("team")).choice());
            assertTrue(((JevAnswer.Noul) response.answer("urgent")).isTrue(0.8));
            assertEquals(12, response.usage().inputTokens());
        } finally { server.stop(0); }
    }
}

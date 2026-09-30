package dev.agentj.jev;

import java.util.*;

/** Complete System One response. */
public record JevResponse(String model, Map<String, JevAnswer> answers, Usage usage, String raw) {
    public JevResponse {
        answers = Map.copyOf(answers == null ? Map.of() : answers);
    }
    public JevAnswer answer(String name) {
        var answer = answers.get(name);
        if (answer == null) throw new IllegalArgumentException("No Jev answer named: " + name);
        return answer;
    }
    public record Usage(long inputTokens, long outputTokens) {}
}

package dev.agentj.jev;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Zero-key local decision provider for demos, development and tests.
 *
 * This is intentionally deterministic and keyword-based. It is NOT Jev inference;
 * it exists so an AgentJ application can demonstrate the same typed decision
 * contract without network access or a paid API account.
 */
public final class LocalJevProvider implements JevProvider {
    @Override
    public JevResponse ask(Object state, Map<String, JevQuestion> questions) {
        Objects.requireNonNull(questions, "questions");
        String text = String.valueOf(state).toLowerCase();
        Map<String, JevAnswer> answers = new LinkedHashMap<>();

        for (var entry : questions.entrySet()) {
            JevQuestion question = entry.getValue();
            if (question instanceof JevQuestion.Choice choice) {
                answers.put(entry.getKey(), choose(text, choice));
            } else if (question instanceof JevQuestion.Noul) {
                boolean urgent = containsAny(text, "today", "urgent", "immediately", "outage", "failed", "blocked", "critical");
                answers.put(entry.getKey(), new JevAnswer.Noul(urgent ? 0.92 : 0.08, 0.92));
            } else if (question instanceof JevQuestion.Score score) {
                int index = severityIndex(text, score.criteria().size());
                double normalized = score.criteria().size() == 1 ? 0 : (double) index / (score.criteria().size() - 1);
                Map<String, Double> probabilities = new LinkedHashMap<>();
                for (int i = 0; i < score.criteria().size(); i++) {
                    probabilities.put(String.valueOf(score.criteria().get(i)), i == index ? 0.90 : 0.10 / Math.max(1, score.criteria().size() - 1));
                }
                answers.put(entry.getKey(), new JevAnswer.Score(normalized, probabilities,
                        score.criteria().stream().map(String::valueOf).toList(), 0.90));
            }
        }

        return new JevResponse("local-mock", answers, new JevResponse.Usage(0, 0), "local deterministic provider");
    }

    private JevAnswer.Choice choose(String text, JevQuestion.Choice question) {
        String selected = question.criteria().keySet().iterator().next();
        String description = "";
        for (var option : question.criteria().entrySet()) {
            String candidate = String.valueOf(option.getValue()).toLowerCase();
            if (matches(text, candidate) || matches(text, option.getKey().toLowerCase()) || (option.getKey().equalsIgnoreCase("billing") && containsAny(text, "charged", "invoice", "refund", "payment"))) {
                selected = option.getKey();
                description = candidate;
                break;
            }
        }
        Map<String, Double> probabilities = new LinkedHashMap<>();
        for (String key : question.criteria().keySet()) probabilities.put(key, key.equals(selected) ? 0.90 : 0.10 / Math.max(1, question.criteria().size() - 1));
        return new JevAnswer.Choice(selected, probabilities, 0.90);
    }

    private static int severityIndex(String text, int levels) {
        int score = 0;
        if (containsAny(text, "annoying", "minor")) score = 1;
        if (containsAny(text, "failed", "blocked", "duplicate","charged twice", "three days", "broken")) score = Math.max(score, 2);
        if (containsAny(text, "outage", "critical", "security", "all customers")) score = Math.max(score, levels - 1);
        return Math.min(score, levels - 1);
    }

    private static boolean matches(String text, String phrase) {
        String[] words = phrase.replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        for (String word : words) if (word.length() >= 4 && text.contains(word)) return true;
        return false;
    }

    private static boolean containsAny(String text, String... values) {
        for (String value : values) if (text.contains(value)) return true;
        return false;
    }
}

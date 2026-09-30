package dev.agentj.example.hybrid;

import dev.agentj.jev.*;

import java.util.LinkedHashMap;
import java.util.Map;

/** Demonstrates the AgentJ + Jev pattern: AI decides, Java executes. */
public final class HybridDecisionDemo {
    public static void main(String[] args) throws Exception {
        String ticket = args.length == 0
                ? "I was charged twice for order 8129 and need this fixed today."
                : String.join(" ", args);

        var questions = new LinkedHashMap<String, JevQuestion>();
        questions.put("team", JevQuestion.Choice.of(
                "Which team should handle this customer issue?",
                Map.of(
                        "billing", "Payments, invoices, refunds and duplicate charges",
                        "technical", "Bugs, outages and integration failures",
                        "support", "General customer support and account questions")));
        questions.put("urgent", new JevQuestion.Noul("Does this issue require same-day human attention?"));
        questions.put("severity", JevQuestion.Score.of(
                "How severe is the customer impact?",
                "Minor impact", "Moderate impact", "Major impact", "Critical impact"));

        try (JevProvider jev = provider()) {
            var result = jev.ask(Map.of("customer_message", ticket), questions);
            var team = (JevAnswer.Choice) result.answer("team");
            var urgent = (JevAnswer.Noul) result.answer("urgent");
            var severity = (JevAnswer.Score) result.answer("severity");

            System.out.println("AgentJ + Jev");
            System.out.println("============");
            System.out.println("Provider:   " + result.model());
            System.out.println("Team:       " + team.choice() + " (confidence " + pct(team.confidence()) + ")");
            System.out.println("Urgent:     " + urgent.isTrue(0.80) + " (p=" + pct(urgent.noul()) + ")");
            System.out.println("Severity:   " + severity.score() + " / " + severity.legend());
            System.out.println();
            System.out.println("Java decision: " + (urgent.isTrue(0.80) ? "ESCALATE" : "NORMAL_QUEUE"));
        }
    }

    private static JevProvider provider() {
        String mode = System.getenv().getOrDefault("AGENTJ_JEV_MODE", "local").trim().toLowerCase();
        return switch (mode) {
            case "local", "mock", "offline" -> {
                System.out.println("Running in local zero-key mode (deterministic demo; not Jev inference).\n");
                yield new LocalJevProvider();
            }
            case "remote", "jev" -> JevClient.fromEnv();
            default -> throw new IllegalArgumentException("Unknown AGENTJ_JEV_MODE: " + mode + ". Use local or remote.");
        };
    }

    private static String pct(Double value) {
        return value == null ? "n/a" : String.format("%.0f%%", value * 100);
    }
}

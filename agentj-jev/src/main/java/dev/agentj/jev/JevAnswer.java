package dev.agentj.jev;

import java.util.*;

/** Structured result returned by a Jev decision. */
public sealed interface JevAnswer permits JevAnswer.Noul, JevAnswer.Choice, JevAnswer.Score {
    String type();
    Double confidence();

    record Noul(double noul, Double confidence) implements JevAnswer {
        @Override public String type() { return "noul"; }
        public boolean isTrue(double threshold) { return noul >= threshold; }
    }

    record Choice(String choice, Map<String, Double> probabilities, Double confidence) implements JevAnswer {
        public Choice {
            probabilities = probabilities == null ? Map.of() : Map.copyOf(probabilities);
        }
        @Override public String type() { return "choice"; }
    }

    record Score(double score, Map<String, Double> probabilities, List<String> legend, Double confidence) implements JevAnswer {
        public Score {
            probabilities = probabilities == null ? Map.of() : Map.copyOf(probabilities);
            legend = legend == null ? List.of() : List.copyOf(legend);
        }
        @Override public String type() { return "score"; }
    }
}

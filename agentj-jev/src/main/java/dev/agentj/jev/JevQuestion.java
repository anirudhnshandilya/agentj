package dev.agentj.jev;

import java.util.*;

/** Typed question primitives supported by TypeSafe System One / Jev. */
public sealed interface JevQuestion permits JevQuestion.Noul, JevQuestion.Choice, JevQuestion.Score {
    String instructions();

    record Noul(String instructions, Map<String, Object> criteria) implements JevQuestion {
        public Noul {
            Objects.requireNonNull(instructions, "instructions");
            criteria = criteria == null ? Map.of() : Map.copyOf(criteria);
        }
        public Noul(String instructions) { this(instructions, Map.of()); }
    }

    record Choice(String instructions, Map<String, Object> criteria) implements JevQuestion {
        public Choice {
            Objects.requireNonNull(instructions, "instructions");
            if (criteria == null || criteria.isEmpty()) throw new IllegalArgumentException("choice criteria must not be empty");
            if (criteria.size() > 255) throw new IllegalArgumentException("choice criteria cannot exceed 255 options");
            criteria = Map.copyOf(criteria);
        }
        public static Choice of(String instructions, Map<String, String> criteria) {
            return new Choice(instructions, new LinkedHashMap<>(criteria));
        }
    }

    record Score(String instructions, List<Object> criteria) implements JevQuestion {
        public Score {
            Objects.requireNonNull(instructions, "instructions");
            if (criteria == null || criteria.size() < 2 || criteria.size() > 10) {
                throw new IllegalArgumentException("score criteria must contain 2 to 10 levels");
            }
            criteria = List.copyOf(criteria);
        }
        public static Score of(String instructions, String... levels) {
            return new Score(instructions, new ArrayList<Object>(Arrays.asList(levels)));
        }
    }
}

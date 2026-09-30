package dev.agentj.jev;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class JevQuestionTest {
    @Test
    void choiceRequiresAtLeastOneOption() {
        assertThrows(IllegalArgumentException.class,
                () -> new JevQuestion.Choice("Pick one", Map.of()));
    }

    @Test
    void scoreRequiresBetweenTwoAndTenLevels() {
        assertThrows(IllegalArgumentException.class,
                () -> JevQuestion.Score.of("Score it", "only one"));
        assertDoesNotThrow(() -> JevQuestion.Score.of("Score it", "low", "high"));
    }
}

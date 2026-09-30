package dev.agentj.jev;

import java.io.IOException;
import java.util.Map;

/** Decision provider abstraction used by AgentJ examples and applications. */
@FunctionalInterface
public interface JevProvider extends AutoCloseable {
    JevResponse ask(Object state, Map<String, JevQuestion> questions) throws IOException, InterruptedException;

    @Override
    default void close() { }
}

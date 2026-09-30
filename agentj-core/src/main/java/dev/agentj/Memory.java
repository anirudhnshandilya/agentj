package dev.agentj;

import java.util.List;

public interface Memory {
    List<Message> messages();
    void add(Message message);
    default void clear() {}
}

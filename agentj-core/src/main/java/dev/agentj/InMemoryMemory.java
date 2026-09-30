package dev.agentj;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryMemory implements Memory {
    private final List<Message> messages = new CopyOnWriteArrayList<>();
    public List<Message> messages() { return List.copyOf(messages); }
    public void add(Message message) { messages.add(Objects.requireNonNull(message)); }
    public void clear() { messages.clear(); }
}

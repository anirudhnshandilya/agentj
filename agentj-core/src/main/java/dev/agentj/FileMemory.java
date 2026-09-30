package dev.agentj;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public final class FileMemory implements Memory {
    private final Path path;
    private final ObjectMapper mapper;
    private final List<Message> messages = new ArrayList<>();
    public FileMemory(Path path, ObjectMapper mapper) throws IOException {
        this.path = path; this.mapper = mapper;
        if (Files.exists(path)) messages.addAll(mapper.readValue(Files.readString(path), new TypeReference<>() {}));
    }
    public synchronized List<Message> messages() { return List.copyOf(messages); }
    public synchronized void add(Message message) {
        messages.add(message);
        try { Files.createDirectories(path.toAbsolutePath().getParent()); Files.writeString(path, mapper.writeValueAsString(messages)); }
        catch (IOException e) { throw new RuntimeException("Could not persist memory", e); }
    }
    public synchronized void clear() { messages.clear(); try { Files.deleteIfExists(path); } catch (IOException e) { throw new RuntimeException(e); } }
}

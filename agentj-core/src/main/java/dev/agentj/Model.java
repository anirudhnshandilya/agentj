package dev.agentj;

public interface Model {
    ModelResponse generate(ModelRequest request) throws Exception;
}

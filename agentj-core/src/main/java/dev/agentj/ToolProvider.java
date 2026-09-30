package dev.agentj;

import java.util.Collection;
import java.util.Map;

public interface ToolProvider {
    Collection<ToolSpec> specs();
    Object invoke(String name, Map<String,Object> arguments) throws Exception;
}

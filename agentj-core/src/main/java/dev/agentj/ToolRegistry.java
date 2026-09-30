package dev.agentj;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ToolRegistry implements ToolProvider {
    private final ObjectMapper mapper;
    private final Map<String, RegisteredTool> tools = new ConcurrentHashMap<>();

    public ToolRegistry(ObjectMapper mapper) { this.mapper = Objects.requireNonNull(mapper); }

    public ToolRegistry register(Object instance) {
        for (Method method : instance.getClass().getMethods()) {
            Tool annotation = method.getAnnotation(Tool.class);
            if (annotation == null) continue;
            String name = annotation.name().isBlank() ? method.getName() : annotation.name();
            String description = annotation.description().isBlank() ? method.getName() : annotation.description();
            tools.put(name, new RegisteredTool(instance, method, new ToolSpec(name, description, schema(method))));
        }
        return this;
    }

    @Override public Collection<ToolSpec> specs() { return List.copyOf(tools.values().stream().map(RegisteredTool::spec).toList()); }

    @Override public Object invoke(String name, Map<String,Object> arguments) throws Exception {
        RegisteredTool t = tools.get(name);
        if (t == null) throw new IllegalArgumentException("Unknown tool: " + name);
        Parameter[] parameters = t.method().getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            String n = p.isNamePresent() ? p.getName() : "arg" + i;
            Object raw = arguments.containsKey(n) ? arguments.get(n) : arguments.get("input");
            args[i] = mapper.convertValue(raw, mapper.constructType(p.getParameterizedType()));
        }
        try {
            Method method = t.method();
            if (!method.canAccess(t.instance())) method.setAccessible(true);
            return method.invoke(t.instance(), args);
        }
        catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) throw ex;
            if (cause instanceof Error err) throw err;
            throw new RuntimeException(cause);
        }
    }

    public boolean contains(String name) { return tools.containsKey(name); }

    private JsonNode schema(Method method) {
        ObjectNode root = mapper.createObjectNode();
        root.put("type", "object");
        ObjectNode properties = root.putObject("properties");
        var required = root.putArray("required");
        for (int i = 0; i < method.getParameterCount(); i++) {
            Parameter p = method.getParameters()[i];
            String n = p.isNamePresent() ? p.getName() : "arg" + i;
            properties.set(n, mapper.valueToTree(typeSchema(p.getParameterizedType())));
            if (p.getType().isPrimitive()) required.add(n);
        }
        return root;
    }

    private Map<String,Object> typeSchema(Type type) {
        Class<?> c = type instanceof Class<?> cl ? cl : Object.class;
        String json = switch (c.getSimpleName()) {
            case "String", "CharSequence" -> "string";
            case "int", "Integer", "long", "Long", "short", "Short" -> "integer";
            case "double", "Double", "float", "Float", "BigDecimal" -> "number";
            case "boolean", "Boolean" -> "boolean";
            case "List", "Collection", "Set" -> "array";
            default -> "object";
        };
        return Map.of("type", json);
    }

    private record RegisteredTool(Object instance, Method method, ToolSpec spec) {}
}

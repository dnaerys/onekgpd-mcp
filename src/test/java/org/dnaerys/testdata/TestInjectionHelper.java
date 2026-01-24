package org.dnaerys.testdata;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dnaerys.mcp.OneKGPdMCPServer;
import org.dnaerys.mcp.util.JsonUtil;
import org.dnaerys.mcp.util.McpResponse;
import java.lang.reflect.Field;

/**
 * Helper class to inject McpResponse dependencies into OneKGPdMCPServer for unit tests.
 * This enables testing without the Quarkus CDI container.
 */
public final class TestInjectionHelper {

    private TestInjectionHelper() {}

    /**
     * Injects the McpResponse dependency chain into a OneKGPdMCPServer instance.
     * Creates: ObjectMapper -> JsonUtil -> McpResponse -> server.mcpResponse
     *
     * @param server the server instance to inject dependencies into
     */
    public static void injectMcpResponse(OneKGPdMCPServer server) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonUtil jsonUtil = new JsonUtil();
            injectField(jsonUtil, "mapper", objectMapper);

            McpResponse mcpResponse = new McpResponse();
            injectField(mcpResponse, "jsonUtil", jsonUtil);

            injectField(server, "mcpResponse", mcpResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject McpResponse dependencies", e);
        }
    }

    private static void injectField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}

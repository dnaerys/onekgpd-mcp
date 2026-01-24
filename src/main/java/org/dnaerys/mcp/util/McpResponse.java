package org.dnaerys.mcp.util;

import io.quarkiverse.mcp.server.TextContent;
import io.quarkiverse.mcp.server.ToolResponse;
import io.quarkiverse.mcp.server.ToolCallException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;

@ApplicationScoped
public class McpResponse {

    @Inject
    JsonUtil jsonUtil;

    /**
     * Success for collections: Provides text for the LLM and a Map for the UI.
     */
    public ToolResponse success(Object structured, Collection<?> rawData) {
        String jsonText = jsonUtil.toJsonArray(rawData);
        return new ToolResponse(
            false,
            List.of(new TextContent(jsonText)),
            structured,
            null
        );
    }

    /**
     * Success for simple objects: Serializes the object for both fields (content & structuredContent).
     */
    public ToolResponse success(Object structured) {
        String jsonText = jsonUtil.stringify(structured);
        return new ToolResponse(
            false,
            List.of(new TextContent(jsonText)),
            structured,
            null
        );
    }

    /**
     * Returning ToolCallException so Quarkus sets isError=true and uses the message as content
     */
    public static ToolCallException handle(Throwable t) {
        if (t instanceof ToolCallException te) return te;

        if (t instanceof io.grpc.StatusRuntimeException grpcEx) {
            String details = grpcEx.getStatus().getDescription() != null
                ? ": " + grpcEx.getStatus().getDescription()
                : "";

            String message = switch (grpcEx.getStatus().getCode()) {
                case INVALID_ARGUMENT -> "Invalid request parameters" + details;
                case NOT_FOUND        -> "Requested genomic data not found" + details;
                case PERMISSION_DENIED -> "Access denied to Dnaerys resource";
                case UNAUTHENTICATED  -> "Authentication failed";
                case RESOURCE_EXHAUSTED -> "Server is overloaded or quota exceeded";
                case FAILED_PRECONDITION -> "Request failed precondition: " + details;
                case ABORTED          -> "The operation was aborted";
                case OUT_OF_RANGE     -> "Coordinate or value out of range";
                case UNIMPLEMENTED    -> "This feature is not yet supported by the server";
                case INTERNAL         -> "Internal Dnaerys server error";
                case UNAVAILABLE      -> "Dnaerys server is unreachable (down or network issue)";
                case DEADLINE_EXCEEDED -> "The genomic query timed out (10s+ limit reached)";
                case DATA_LOSS        -> "Unrecoverable data corruption detected";
                default               -> "Database error (" + grpcEx.getStatus().getCode() + ")" + details;
            };
            return new ToolCallException(message);
        }

        // Fallback for non-gRPC exceptions (NPEs, etc.)
        String fallback = t.getMessage() != null ? t.getMessage() : "An unexpected processing error occurred.";
        return new ToolCallException(fallback);
    }
}
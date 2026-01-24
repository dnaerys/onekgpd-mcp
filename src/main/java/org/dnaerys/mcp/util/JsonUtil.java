package org.dnaerys.mcp.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.Collection;

@ApplicationScoped
public class JsonUtil {

    @Inject
    ObjectMapper mapper;

    /**
     * Converts a single object (like a Summary Map) to a JSON string.
     */
    public String stringify(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }

    /**
     * Serializes a collection directly via Jackson.
     */
    public <T> String toJsonArray(Collection<T> collection) {
        if (collection == null || collection.isEmpty()) return "[]";
        try {
            return mapper.writeValueAsString(collection);
        } catch (Exception e) {
            return "[]";
        }
    }
}
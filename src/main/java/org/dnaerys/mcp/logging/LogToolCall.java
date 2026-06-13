package org.dnaerys.mcp.logging;

import jakarta.interceptor.InterceptorBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Interceptor binding that triggers {@link ToolCallLoggingInterceptor} so every
 * intercepted method (the MCP {@code @Tool} entry points) is logged at DEBUG.
 */
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface LogToolCall {
}

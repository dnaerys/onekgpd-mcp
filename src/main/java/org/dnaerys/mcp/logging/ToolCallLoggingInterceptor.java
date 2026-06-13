package org.dnaerys.mcp.logging;

import io.quarkiverse.mcp.server.Tool;
import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import org.jboss.logging.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.StringJoiner;

/**
 * Logs each incoming MCP tool call (method name + arguments) at DEBUG.
 *
 * <p>The quarkus-mcp-server extension invokes {@code @Tool} methods through the
 * CDI {@code jakarta.enterprise.invoke.Invoker} API (with instance lookup and no
 * interceptor opt-out), so a standard {@code @AroundInvoke} interceptor fires on
 * every tool call. Bound at class level via {@link LogToolCall} on the tool bean.
 */
@LogToolCall
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class ToolCallLoggingInterceptor {

    private static final Logger LOG = Logger.getLogger("org.dnaerys.mcp.toolcalls");

    @AroundInvoke
    Object logToolCall(InvocationContext ctx) throws Exception {
        Method method = ctx.getMethod();
        // Class-level binding intercepts every business method; Arc uses subclass-based
        // interception, so even internal helper calls (e.g. getGenomicRegions) are seen.
        // Restrict logging to actual MCP tool entry points.
        if (LOG.isDebugEnabled() && method.isAnnotationPresent(Tool.class)) {
            Parameter[] params = method.getParameters();
            Object[] args = ctx.getParameters();
            StringJoiner sj = new StringJoiner(", ", "{", "}");
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null) {
                    sj.add(params[i].getName() + "=" + args[i]);
                }
            }
            LOG.debugf("MCP tool call: %s %s", method.getName(), sj);
        }
        return ctx.proceed();
    }
}

package org.dnaerys.test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.wiremock.grpc.GrpcExtensionFactory;
import org.wiremock.grpc.dsl.WireMockGrpcService;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * Quarkus test resource that starts WireMock with gRPC extension for mocking
 * the DnaerysService gRPC backend.
 *
 * Usage:
 * <pre>{@code
 * @QuarkusTest
 * @QuarkusTestResource(WireMockGrpcResource.class)
 * class MyTest {
 *     @InjectWireMockGrpc
 *     WireMockGrpcService dnaerysService;
 *
 *     @InjectWireMockServer
 *     WireMockServer wireMockServer;
 *
 *     @BeforeEach
 *     void resetStubs() {
 *         wireMockServer.resetAll();
 *     }
 *
 *     @Test
 *     void testSomething() {
 *         dnaerysService.stubFor(
 *             method("DatasetInfo")
 *                 .willReturn(message(DatasetInfoResponse.newBuilder().build()))
 *         );
 *     }
 * }
 * }</pre>
 */
public class WireMockGrpcResource implements QuarkusTestResourceLifecycleManager {

    /** Fully-qualified service name from dnaerys.proto */
    private static final String DNAERYS_SERVICE_NAME = "org.dnaerys.cluster.grpc.DnaerysService";

    /** Directory containing proto descriptors relative to project root */
    private static final String WIREMOCK_ROOT = "src/test/resources/wiremock";

    /** Fixed port for WireMock gRPC server (must match wiremock profile in application.properties) */
    private static final int WIREMOCK_GRPC_PORT = 8089;

    private WireMockServer wireMockServer;
    private WireMockGrpcService dnaerysGrpcService;

    /**
     * Annotation for injecting the WireMockGrpcService into test classes.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InjectWireMockGrpc {
    }

    /**
     * Annotation for injecting the WireMockServer into test classes.
     */
    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface InjectWireMockServer {
    }

    @Override
    public Map<String, String> start() {
        // Start WireMock server with gRPC extension on fixed port
        wireMockServer = new WireMockServer(
            WireMockConfiguration.wireMockConfig()
                .port(WIREMOCK_GRPC_PORT)
                .withRootDirectory(WIREMOCK_ROOT)
                .extensions(new GrpcExtensionFactory())
        );
        wireMockServer.start();

        // Create gRPC service stub manager
        WireMock wireMock = new WireMock(wireMockServer.port());
        dnaerysGrpcService = new WireMockGrpcService(wireMock, DNAERYS_SERVICE_NAME);

        // Return configuration overrides to point Quarkus gRPC client at WireMock
        Map<String, String> config = new HashMap<>();
        config.put("quarkus.grpc.clients.dnaerys.host", "localhost");
        config.put("quarkus.grpc.clients.dnaerys.port", String.valueOf(WIREMOCK_GRPC_PORT));
        config.put("quarkus.grpc.clients.dnaerys.plain-text", "true");
        config.put("quarkus.grpc.clients.dnaerys.ssl.trust-certificate-path", "");

        return config;
    }

    @Override
    public void stop() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            wireMockServer = null;
        }
        dnaerysGrpcService = null;
    }

    @Override
    public void inject(TestInjector testInjector) {
        // Inject WireMockGrpcService for stubbing
        testInjector.injectIntoFields(
            dnaerysGrpcService,
            new TestInjector.AnnotatedAndMatchesType(InjectWireMockGrpc.class, WireMockGrpcService.class)
        );

        // Inject WireMockServer for direct access (reset, etc.)
        testInjector.injectIntoFields(
            wireMockServer,
            new TestInjector.AnnotatedAndMatchesType(InjectWireMockServer.class, WireMockServer.class)
        );
    }
}

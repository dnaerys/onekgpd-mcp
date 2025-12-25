package org.dnaerys.client;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.TlsChannelCredentials;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

import org.dnaerys.cluster.grpc.*;


public class GrpcChannel {
    private final ManagedChannel channel;
    private final DnaerysServiceGrpc.DnaerysServiceBlockingStub blockingStub;

    // Private constructor prevents instantiation
    private GrpcChannel() {
        String hostname = ReadConfig.getProp().getProperty("dnaerysHost");
        String grpcPort = ReadConfig.getProp().getProperty("dnaerysGRPCPort");
        String ssl = ReadConfig.getProp().getProperty("ssl");

        int port = 7443; // default
        try {
            port = Integer.parseInt(grpcPort);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port value in config: " + grpcPort + ". Falling back to default values");
        }

        // Creates a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of application and reuse them
        // until the application shuts down.

        if (ssl.equalsIgnoreCase("true")) {
            System.err.println("Database host: " + hostname + ":" + grpcPort + " via TLS gRPC");
            // TrustManager that trusts all certificates
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };

            TlsChannelCredentials.Builder tlsBuilder = TlsChannelCredentials.newBuilder();
            tlsBuilder.trustManager(trustAllCerts[0]); // Use relaxed trust manager

            channel = Grpc.newChannelBuilderForAddress(hostname, port, tlsBuilder.build()).build();
            this.blockingStub = DnaerysServiceGrpc.newBlockingStub(this.channel);
        } else {
            System.err.println("Database host: " + hostname + ":" + grpcPort + " via plain gRPC");
            this.channel = Grpc.newChannelBuilderForAddress(hostname, port, InsecureChannelCredentials.create()).build();
            this.blockingStub = DnaerysServiceGrpc.newBlockingStub(this.channel);
        }
    }

    // Singleton
    private static class ResourceHolder {
        private static final GrpcChannel INSTANCE = new GrpcChannel();
    }

    public static GrpcChannel getInstance() {
        return ResourceHolder.INSTANCE;
    }

    public DnaerysServiceGrpc.DnaerysServiceBlockingStub getBlockingStub() {
        return blockingStub;
    }
}
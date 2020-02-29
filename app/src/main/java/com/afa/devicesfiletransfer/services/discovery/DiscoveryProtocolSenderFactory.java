package com.afa.devicesfiletransfer.services.discovery;

public class DiscoveryProtocolSenderFactory {
    public static DiscoveryProtocolSender getDefault(int port) {
        return new DiscoveryProtocolSender(new NetworkDataProvider(), port);
    }
}

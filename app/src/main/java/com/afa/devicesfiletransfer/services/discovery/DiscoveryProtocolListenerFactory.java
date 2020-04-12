package com.afa.devicesfiletransfer.services.discovery;

public class DiscoveryProtocolListenerFactory {
    public static DiscoveryProtocolListener getDefault(int port) {
        return new DiscoveryProtocolListener(new NetworkDataProvider(), port);
    }

    public static DiscoveryProtocolListener getDefault(int port, DiscoveryProtocolListener.Callback callback) {
        return new DiscoveryProtocolListener(new NetworkDataProvider(), port, callback);
    }
}

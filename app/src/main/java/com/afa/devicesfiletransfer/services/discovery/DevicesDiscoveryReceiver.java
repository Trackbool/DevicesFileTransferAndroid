package com.afa.devicesfiletransfer.services.discovery;

public interface DevicesDiscoveryReceiver {
    void setCallback(DiscoveryProtocolListener.Callback callback);

    void receive();

    void stop();
}

package com.afa.devicesfiletransfer.services.discovery;

import java.net.SocketException;

public interface DevicesDiscoveryExecutor {
    void setCallback(DiscoveryProtocolListener.Callback callback);

    void start();

    void stop();

    void discover() throws SocketException;
}

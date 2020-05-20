package com.afa.devicesfiletransfer.services.discovery;

import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;

import java.net.SocketException;

public interface DiscoveryServiceInteractor {
    void setServiceConnectionCallback(ServiceConnectionCallback callback);

    void setCallback(DiscoveryProtocolListener.Callback callback);

    void receive();

    void discover() throws SocketException;

    void stop();
}

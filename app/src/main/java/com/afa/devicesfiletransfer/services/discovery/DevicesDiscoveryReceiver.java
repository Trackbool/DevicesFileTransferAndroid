package com.afa.devicesfiletransfer.services.discovery;

import com.afa.devicesfiletransfer.services.ServiceConnectionCallback;

public interface DevicesDiscoveryReceiver {
    void setServiceConnectionCallback(ServiceConnectionCallback callback);

    void setCallback(DiscoveryProtocolListener.Callback callback);

    void receive();

    void stop();
}
